package mg.itu.prom16.controller;

import mg.itu.prom16.annotation.*;
import mg.itu.prom16.models.ModelView;
import mg.itu.prom16.utils.*;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import java.lang.reflect.Method;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;

public class FrontController extends HttpServlet {
    private List<String> controller = new ArrayList<>();
    private String controllerPackage;
    HashMap <String,Mapping> urlMapping = new HashMap<>() ;
    Exception exception = new Exception("");
    

    @Override
    public void init() throws ServletException {
        super.init();
        controllerPackage = getInitParameter("controller-package");
        if (controllerPackage == null || controllerPackage.isEmpty()) {
            exception = new Exception("Le paramètre 'controller-package' doit être défini dans les paramètres d'initialisation.");
        } else {
            try {
                scan();
            } catch (Exception e) {
                exception = e;
            }
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        if (exception.getMessage()!=""){ 
            out.println(exception.getMessage());
        } else {
            try {
                String reponse = this.process(request, response);
                out.println(reponse);
            } catch (Exception e) {
                out.println(e.getMessage());
            }
        }
        out.close();
    }



    
    public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer requestURL = request.getRequestURL();
        String[] requestUrlSplitted = requestURL.toString().split("/");
        String map = requestUrlSplitted[requestUrlSplitted.length-1];
        String retour = "";
        if (requestUrlSplitted.length <=4 ) {map = "/" ;}

        if (urlMapping.containsKey(map)) {
            Mapping mapping = urlMapping.get(map);
            retour+="<p><strong>chemin URL :</strong> "+requestURL.toString()+"</p>";
            retour+="<p><strong>Mapping :</strong> "+mapping.getClassName()+"</p>";
            retour+="<p><strong>MethodName :</strong> "+mapping.getMethodName()+"</p>";

            try {
                Class<?> classe = Class.forName(mapping.getClassName());
                Object classInstance = classe.getDeclaredConstructor().newInstance();
    
                Method method = classe.getMethod(mapping.getMethodName());
                Object result = method.invoke(classInstance);
                if (result instanceof ModelView){
                    ModelView mv = (ModelView) result;
                    RequestDispatcher dispatch = request.getRequestDispatcher(mv.getUrl());
                    Set<String> keys = mv.getData().keySet();
                    for (String key : keys) {
                        request.setAttribute(key, mv.getData().get(key));
                        break;
                    }
                    dispatch.forward(request, response);
                } else if (result instanceof String){
                    String val = (String)result;
                    retour+="<p><strong>Valeur de retour :</strong> "+val;
                } else {
                    throw new Exception("<p>Le type de retour n'est ni un ModelView ni un String</p>");
                }
            } catch (Exception e){
                throw e;
            }
        } else {
            throw new Exception("<p>Il n'y a pas de méthode associée à ce chemin \""+requestURL+"\"</p>");
        }    
        return retour;
    }



    public void scan() throws Exception{
        try {
            String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
            String decodedPath = URLDecoder.decode(classesPath, "UTF-8");
            String packagePath = decodedPath +"\\"+ controllerPackage.replace('.', '\\');
            File packageDirectory = new File(packagePath);
            if (packageDirectory.exists() && packageDirectory.isDirectory()) {
                File[] classFiles = packageDirectory.listFiles((dir, name) -> name.endsWith(".class"));
                if (classFiles != null) {
                    for (File classFile : classFiles) {
                        String className = controllerPackage + '.' + classFile.getName().substring(0, classFile.getName().length() - 6);
                        try {
                            Class<?> classe = Class.forName(className);
                            if (classe.isAnnotationPresent(Controller.class)) {
                                controller.add(classe.getSimpleName());

                                Method[] methods = classe.getMethods();
                                for (Method item : methods) {
                                    if (item.isAnnotationPresent(Get.class)) {
                                        // Mapping(controller.name, method.name)
                                        Mapping mapping = new Mapping(className, item.getName());

                                        Get get = item.getAnnotation(Get.class);
                                        String getValue = get.value();

                                        // HashMap.associer(annotation.value, mapping)
                                        if (!urlMapping.containsKey(getValue)){
                                            urlMapping.put(getValue, mapping);
                                        } else {
                                            throw new Exception("L'url \""+getValue+"\" apparaît plusieurs fois dans les controlleurs.");
                                        }
                                    }
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            throw e;
                        }
                    }
                    if (controller.size()==0) {
                        throw new Exception("Il n'y aucun controller dans ce package");
                    }
                }
            } else {
                throw new Exception("Le package "+ controllerPackage +" n'existe pas");
            }
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

}


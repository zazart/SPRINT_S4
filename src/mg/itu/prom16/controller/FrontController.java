package mg.itu.prom16.controller;

import mg.itu.prom16.annotation.*;
import mg.itu.prom16.utils.*;

import java.util.List;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import java.lang.reflect.Method;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    private List<String> controller = new ArrayList<>();
    private String controllerPackage;
    HashMap <String,Mapping> urlMapping = new HashMap<>() ;
    

    @Override
    public void init() throws ServletException {
        super.init();
        controllerPackage = getInitParameter("controller-package");
        scan();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        StringBuffer requestURL = request.getRequestURL();
        response.setContentType("text/html");
        
        String[] requestUrlSplitted = requestURL.toString().split("/");
        String map = requestUrlSplitted[requestUrlSplitted.length-1];


        if (urlMapping.containsKey(map)) {
            Mapping mapping = urlMapping.get(map);
            out.println("<p><strong>chemin URL :</strong> "+requestURL.toString()+"</p>");
            out.println("<p><strong>Mapping :</strong> "+mapping.getClassName()+"</p>");
            out.println("<p><strong>MethodName :</strong> "+mapping.getMethodName()+"</p>");
        }
        else {
            out.println("<p>Il n'y a pas de méthode associée à ce chemin</p>");
        }    
        out.close();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    public void scan() {
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
                                        urlMapping.put(getValue, mapping);
                                    }
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


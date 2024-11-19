package mg.itu.prom16.controller;

import mg.itu.prom16.annotation.*;
import mg.itu.prom16.annotation.verb.Post;
import mg.itu.prom16.models.ModelView;
import mg.itu.prom16.models.VerbMethod;
import mg.itu.prom16.utils.*;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.servlet.RequestDispatcher;

import jakarta.servlet.annotation.MultipartConfig;


@MultipartConfig
public class FrontController extends HttpServlet {
    private List<String> controller = new ArrayList<>();
    private String controllerPackage;
    private HashMap <String,Mapping> urlMapping = new HashMap<>() ;
    private Exception exception = new Exception("");
    private int statusCode = 200;
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<String> getController() {
        return controller;
    }

    public void setController(List<String> controller) {
        this.controller = controller;
    }

    public String getControllerPackage() {
        return controllerPackage;
    }

    public void setControllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
    }

    public HashMap<String, Mapping> getUrlMapping() {
        return urlMapping;
    }

    public void setUrlMapping(HashMap<String, Mapping> urlMapping) {
        this.urlMapping = urlMapping;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }



    @Override
    public void init() throws ServletException {
        super.init();
        this.setControllerPackage(getInitParameter("controller-package"));
        if (this.getControllerPackage() == null || this.getControllerPackage().isEmpty()) {
            this.setException(new Exception("Le paramètre 'controller-package' doit être défini dans les paramètres d'initialisation."));
        } else {
            try {
                scan();
            } catch (Exception e) {
                this.setException(e);
                e.printStackTrace();
            }
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        if (this.getException().getMessage()!=""){ 
            response.sendError(this.getStatusCode(), this.getException().getMessage());
        } else {
            try {
                String reponse = this.subProcess(request, response);
                out.println(reponse);
            } catch (Exception e) {
                response.sendError(this.getStatusCode(), e.getMessage());
            }
        }
        out.close();
    }



    
    public String subProcess(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer requestURL = request.getRequestURL();
        String[] requestUrlSplitted = requestURL.toString().split("/");
        String map = requestUrlSplitted[requestUrlSplitted.length-1];
        String retour = "";
        if (requestUrlSplitted.length <=4 ) {map = "/" ;}

        if (this.getUrlMapping().containsKey(map)) {
            Mapping mapping = this.getUrlMapping().get(map);
            String verb = request.getMethod();
            int idverb = 0;
            for (int i=0; i < mapping.getListVerbMethod().size(); i++) {
                if (mapping.getListVerbMethod().get(i).getVerb().equals(verb)) {
                    idverb = i;
                }
            } 

        
            try {
                String methodVerb = mapping.getListVerbMethod().get(idverb).getVerb();
                String methodName = mapping.getListVerbMethod().get(idverb).getMethodName();
                if (!verb.equals(methodVerb)) {
                    this.setStatusCode(500);
                    throw new Exception("Le verb "+methodVerb+" au niveau de la methode ne correspond pas au method de la requete : "+verb );
                }

                Class<?> classe = Class.forName(mapping.getClassName());
                Object classInstance = classe.getDeclaredConstructor().newInstance();

                Field[] attributs = classInstance.getClass().getDeclaredFields();
                for (Field item : attributs) {
                    if (item.getType().equals(CustomSession.class)) {
                        item.setAccessible(true);
                        CustomSession session = new CustomSession();
                        session.setMySession(request.getSession());
                        item.set(classInstance, session);
                    }
                }

                Boolean paramExist = false;
                Method[] methods = classInstance.getClass().getDeclaredMethods();
                for (Method item : methods) {
                    if (item.getName().equals(methodName)) {
                        paramExist = item.getParameterCount() > 0;
                    }
                }
                if (paramExist) {
                    Parameter[] listeParam = null;                    
                    for (Method item : methods) {
                        if (item.getName().equals(methodName)) {
                            listeParam = item.getParameters();
                            break;
                        }
                    }
                    Object[] values = new Object[listeParam.length];
                    // formParameterNames as parameters of the request 
                    Enumeration<String> formParameterNames = request.getParameterNames();
                    for (int i = 0; i < values.length; i++) {
                        
                        String paramName = listeParam[i].getName();
                        if (listeParam[i].isAnnotationPresent(Param.class)) {
                            paramName = listeParam[i].getAnnotation(Param.class).value();
                        } else if (!listeParam[i].getType().equals(CustomSession.class)){
                            String errorMess = "vous n'avez pas annoté le paramètre '"+paramName+"' par @Param(\"...\")"  ;
                            this.setStatusCode(500);
                            throw new Exception("<p>ETU-002415 : "+errorMess+"</p>");
                        }
                        
                        if (listeParam[i].getType() == Part.class) {
                            try {
                                String name = listeParam[i].getAnnotation(Param.class).value();
                                values[i] = request.getPart(name);
                            } catch (Exception e) {
                                throw new Exception("Erreur lors de la récupération du fichier :"+e.getMessage());
                            }
                        } else {
                            if (!listeParam[i].getClass().isPrimitive() && listeParam[i].getType().isAnnotationPresent(Objet.class)) {
                                Class<?> clazz = Class.forName(listeParam[i].getParameterizedType().getTypeName());
                                Object obj = clazz.getDeclaredConstructor().newInstance();

                                Field[] fields = obj.getClass().getDeclaredFields();
                                Object[] valuesObject = new Object[fields.length];
                                while (formParameterNames.hasMoreElements()) {
                                    String name = formParameterNames.nextElement();
                                    for (int j = 0; j < fields.length; j++) {
                                        if (name.startsWith(paramName+".")) {
                                            int indexSuite = (paramName + ".").length();
                                            String paramSimpleName = name.substring(indexSuite);

                                            if (fields[j].isAnnotationPresent(AttribObjet.class)){
                                                if (paramSimpleName.equals(fields[j].getAnnotation(AttribObjet.class).value())){
                                                    if (Validation.validation(fields[j], request.getParameter(name)) != "") {
                                                        this.setStatusCode(401);
                                                        throw new Exception("Erreur :"+Validation.validation(fields[j], request.getParameter(name)));
                                                    } else {
                                                        valuesObject[j] = TypeHandler.castParameter(request.getParameter(name), fields[j].getType().getName());
                                                        break;
                                                    }
                                                } 
                                            } else {
                                                if (paramSimpleName.equals(fields[j].getName())){
                                                    if (Validation.validation(fields[j], request.getParameter(name)) != "") {
                                                        this.setStatusCode(401);
                                                        throw new Exception("Erreur :"+Validation.validation(fields[j], request.getParameter(name)));
                                                    } else {
                                                        valuesObject[j] = TypeHandler.castParameter(request.getParameter(name), fields[j].getType().getName());
                                                        break;
                                                    }
                                                }  
                                            }
                                        }
                                    }
                                }

                                obj = process(obj, valuesObject);
                                values[i] = obj;
                            } else if (listeParam[i].getType().equals(CustomSession.class)) {
                                CustomSession session = new CustomSession(); 
                                session.setMySession(request.getSession());
                                values[i] = session;
                            }
                            else {
                                boolean isNull = true;
                                while (formParameterNames.hasMoreElements()) {
                                    String name = formParameterNames.nextElement();
                                    if (name.equals(paramName)) {
                                        values[i] =TypeHandler.castParameter(request.getParameter(name), listeParam[i].getParameterizedType().getTypeName());
                                        isNull = false;
                                        break;
                                    }
                                }
                                if (isNull) {
                                    values[i] = null;
                                }
                            }
                        }

                    
                    }
                    
                    Class<?>[] parameterTypes = TypeHandler.checkParameterTypes(values);

                    Method method = classe.getDeclaredMethod(methodName, parameterTypes);
                    Object result = method.invoke(classInstance,values);
                    retour += resultHandler(result, request, response, method);
                } else {
                    Method method = classe.getMethod(methodName);
                    Object result = method.invoke(classInstance);
                    retour += resultHandler(result, request, response, method);
                }
            
            } catch (Exception e){
                throw new Exception(e.getMessage());
            }
        } else {
            this.setStatusCode(404);
            throw new Exception("<p>Il n'y a pas de méthode associée à ce chemin \""+requestURL+"\"</p>");
        }    
        return retour;
    }



    public <T> T  process(T obj, Object[] valueObjects) throws Exception {
        Class<?> classe = obj.getClass();
        Field[] fields = classe.getDeclaredFields();
        
        try {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Object valeur = valueObjects[i];
                if(valeur != null){
                    field.set(obj, valeur);
                } else {
                    field.set(obj, null);
                }
            }
        } catch (Exception e) {
            this.setStatusCode(4321);
            throw e;
        }
        return obj;
    }




    public static String resultHandler(Object result, HttpServletRequest request, HttpServletResponse response, Method method) throws Exception {
        String retour = "";
        Gson gson = new Gson();
        if (result instanceof ModelView){
            ModelView mv = (ModelView) result;
            RequestDispatcher dispatch = request.getRequestDispatcher(mv.getUrl());
            Set<String> keys = mv.getData().keySet();
            for (String key : keys) {
                request.setAttribute(key, mv.getData().get(key));
            }
            if (method.isAnnotationPresent(Restapi.class)) {
                response.setContentType("text/json");
                retour += gson.toJson(mv.getData());
            } else {
                dispatch.forward(request, response);
            }
        } else if (result instanceof String){
            String val = (String)result;
            if (method.isAnnotationPresent(Restapi.class)) {
                response.setContentType("text/json");
                retour += gson.toJson(val);
            } else {
                retour+="<p><strong>Valeur de retour :</strong> "+val;
            }
        } else {
            throw new Exception("<p>Le type de retour n'est ni un ModelView ni un String</p>");
        }
        return retour;
    }











    public void scan() throws Exception{
        try {
            String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
            String decodedPath = URLDecoder.decode(classesPath, "UTF-8");
            String packagePath = decodedPath +"\\"+ this.getControllerPackage().replace('.', '\\');
            File packageDirectory = new File(packagePath);
            if (packageDirectory.exists() && packageDirectory.isDirectory()) {
                File[] classFiles = packageDirectory.listFiles((dir, name) -> name.endsWith(".class"));
                if (classFiles != null) {
                    for (File classFile : classFiles) {
                        String className = this.getControllerPackage() + '.' + classFile.getName().substring(0, classFile.getName().length() - 6);
                        try {
                            Class<?> classe = Class.forName(className);
                            if (classe.isAnnotationPresent(Controller.class)) {
                                this.getController().add(classe.getSimpleName());

                                Method[] methods = classe.getMethods();
                                for (Method item : methods) {
                                    if (item.isAnnotationPresent(Url.class)) {
                                        // Mapping(controller.name, method.name)
                                        Mapping mapping = new Mapping(className);
                                        String verb = "GET";

                                        if (item.isAnnotationPresent(Post.class)){
                                            verb = "POST";
                                        }

                                        Url url = item.getAnnotation(Url.class);
                                        String urlValue = url.value();

                                        VerbMethod vm = new VerbMethod(item.getName(),verb);

                                        // HashMap.associer(annotation.value, mapping)
                                        if (!this.getUrlMapping().containsKey(urlValue)){
                                            mapping.addVerbMethod(vm);
                                            this.getUrlMapping().put(urlValue, mapping);
                                        } else {
                                            Mapping map = this.getUrlMapping().get(urlValue);
                                            if (map.contains(vm)) {
                                                this.setStatusCode(500);
                                                throw new Exception("L'url \""+urlValue+"\" apparaît plusieurs fois dans vos controlleur, avec le meme verb "+ verb );
                                            }
                                            map.addVerbMethod(vm);
                                        }
                                    }
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            throw e;
                        }
                    }
                    if (this.getController().size()==0) {
                        this.setStatusCode(500);
                        throw new Exception("Il n'y aucun controller dans ce package");
                    }
                }
            } else {
                this.setStatusCode(500);
                throw new Exception("Le package "+ this.getControllerPackage() +" n'existe pas");
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




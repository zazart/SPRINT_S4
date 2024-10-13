package mg.itu.prom16.controller;

import mg.itu.prom16.annotation.*;
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
                e.printStackTrace();
            }
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        if (exception.getMessage()!=""){ 
            out.println(exception.getMessage());
        } else {
            try {
                String reponse = this.subProcess(request, response);
                out.println(reponse);
            } catch (Exception e) {
                out.println(e.getMessage());
                e.printStackTrace();
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

        if (urlMapping.containsKey(map)) {
            Mapping mapping = urlMapping.get(map);
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
                // listeParam as parameters of the method
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
                        // Begin change
                        String paramName = listeParam[i].getName();
                        if (listeParam[i].isAnnotationPresent(Param.class)) {
                            paramName = listeParam[i].getAnnotation(Param.class).value();
                        } else if (!listeParam[i].getType().equals(CustomSession.class)){
                            String errorMess = "vous n'avez pas annoté le paramètre '"+paramName+"' par @Param(\"...\")"  ;
                            throw new Exception("<p>ETU-002415 : "+errorMess+"</p>");
                        }
                        
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
                                                valuesObject[j] = CastTo.castParameter(request.getParameter(name), fields[j].getType().getName());
                                            }
                                        } else {
                                            if (paramSimpleName.equals(fields[j].getName())){
                                                valuesObject[j] = CastTo.castParameter(request.getParameter(name), fields[j].getType().getName());
                                            } 
                                        }
                                    }
                                }
                            }
                            obj = process(obj, valuesObject);
                            values[i] = obj;
                        }
                        else if (listeParam[i].getType().equals(CustomSession.class)) {
                            CustomSession session = new CustomSession();
                            session.setMySession(request.getSession());
                            values[i] = session;
                        }
                        else {
                            boolean isNull = true;
                            while (formParameterNames.hasMoreElements()) {
                                String name = formParameterNames.nextElement();
                                if (name.equals(paramName)) {
                                    values[i] =CastTo.castParameter(request.getParameter(name), listeParam[i].getParameterizedType().getTypeName());
                                    isNull = false;
                                    break;
                                }
                            }
                            if (isNull) {
                                values[i] = null;
                            }
                        }
                        // End change
                    }
                    Class<?>[] parameterTypes;
                    parameterTypes = new Class<?>[values.length];
                    for (int i = 0; i < values.length; i++) {
                        if (values[i] instanceof Integer) {
                            parameterTypes[i] = int.class;
                        } else if (values[i] instanceof Double) {
                            parameterTypes[i] = double.class;
                        } else if (values[i] instanceof Boolean) {
                            parameterTypes[i] = boolean.class;
                        } else if (values[i] instanceof Long) {
                            parameterTypes[i] = long.class;
                        } else if (values[i] instanceof Float) {
                            parameterTypes[i] = float.class;
                        } else if (values[i] instanceof Short) {
                            parameterTypes[i] = short.class;
                        } else if (values[i] instanceof Byte) {
                            parameterTypes[i] = byte.class;
                        } else {
                            parameterTypes[i] = values[i].getClass();
                        }
                    }
                    Method method = classe.getDeclaredMethod(methodName, parameterTypes);
                    Object result = method.invoke(classInstance,values);
                    retour += resultHandler(result, request, response, method);
                } else {
                    Method method = classe.getMethod(methodName);
                    Object result = method.invoke(classInstance);
                    retour += resultHandler(result, request, response, method);
                }
            
            } catch (Exception e){
                throw e;
            }
        } else {
            throw new Exception("<p>Il n'y a pas de méthode associée à ce chemin \""+requestURL+"\"</p>");
        }    
        return retour;
    }



    public static <T> T  process(T obj, Object[] valueObjects) throws Exception {
        Class<?> classe = obj.getClass();
        Field[] fields = classe.getDeclaredFields();

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
                                        if (!urlMapping.containsKey(urlValue)){
                                            mapping.addVerbMethod(vm);
                                            urlMapping.put(urlValue, mapping);
                                        } else {
                                            Mapping map = urlMapping.get(urlValue);
                                            if (map.contains(vm)) {
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


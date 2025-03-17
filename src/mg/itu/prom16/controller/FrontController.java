package mg.itu.prom16.controller;

import jakarta.servlet.ServletConfig;
import mg.itu.prom16.annotation.*;
import mg.itu.prom16.annotation.verb.Post;
import mg.itu.prom16.models.ModelView;
import mg.itu.prom16.models.Role;
import mg.itu.prom16.models.VerbMethod;
import mg.itu.prom16.utils.*;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private int statusCode = 500;
    private final List<Role> roles = new ArrayList<>();
    
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
                ServletConfig config = getServletConfig();
                Enumeration<String> initParams = config.getInitParameterNames();
                while (initParams.hasMoreElements()) {
                    String paramName = initParams.nextElement();
                    if (paramName.startsWith("role-")) {
                        String paramValue = getInitParameter(paramName);
                        try {
                            int level = Integer.parseInt(paramValue);
                            String roleName = paramName.substring(5);
                            this.roles.add(new Role(roleName, level));
                        } catch (NumberFormatException e) {
                            this.setStatusCode(500);
                            throw new Exception("La valeur du paramètre " + paramName + " n'est pas un entier valide.");
                        }
                    }
                }
                scan();
            } catch (Exception e) {
                this.setException(e);
            }
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        if (!Objects.equals(this.getException().getMessage(), "")) {
            response.sendError(this.getStatusCode(), this.getException().getMessage());
        } else {
            try {
                String reponse = this.subProcessRequest(request, response);
                out.println(reponse);
            } catch (Exception e) {
                response.sendError(this.getStatusCode(), e.getMessage());
                e.printStackTrace();
            }
        }
        out.close();
    }

    public String subProcessRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer requestURL = request.getRequestURL();
        String[] requestUrlSplitted = requestURL.toString().split("/");
        String map = requestUrlSplitted[requestUrlSplitted.length-1];
        String retour = "";
        AtomicBoolean isErrorForm = new AtomicBoolean(false);
        if (requestUrlSplitted.length <=4 ) {map = "/" ;}

        if (this.getUrlMapping().containsKey(map)) {
            Mapping mapping = this.getUrlMapping().get(map);
            try {
                VerbMethod verbMethod = checkVerbMethod(request, mapping);
                String methodName = verbMethod.getMethodName();
                Class<?> classe = Class.forName(mapping.getClassName());
                Object classInstance = classe.getDeclaredConstructor().newInstance();
                checkCustomSession(request, classInstance);  // Check CustomSession
                checkRoleAuthentification(request,verbMethod); // Auth (role)
                boolean paramExist = false;
                Method[] methods = classInstance.getClass().getDeclaredMethods();
                for (Method item : methods) {
                    if (item.getName().equals(methodName)) {
                        paramExist = item.getParameterCount() > 0;
                    }
                }
                if (paramExist) {
                    Object[] values = getValuesIfParamExist(request, methods, methodName, isErrorForm);
                    Class<?>[] parameterTypes = TypeHandler.checkParameterTypes(values);
                    Method method = classe.getDeclaredMethod(methodName, parameterTypes);
                    Object result = method.invoke(classInstance,values);
                    retour += resultHandler(result, request, response, method, isErrorForm);
                } else {
                    Method method = classe.getMethod(methodName);
                    Object result = method.invoke(classInstance);
                    retour += resultHandler(result, request, response, method, isErrorForm);
                }
            
            } catch (Exception e){
                throw new Exception(e.getMessage());
            }
        } else {
            this.setStatusCode(404);
            throw new Exception("Il n'y a pas de méthode associée à ce chemin \""+requestURL+"\"");
        }    
        return retour;
    }

    public void checkCustomSession(HttpServletRequest request, Object classInstance) throws Exception{
        Field[] attributs = classInstance.getClass().getDeclaredFields();
        for (Field item : attributs) {
            if (item.getType().equals(CustomSession.class)) {
                item.setAccessible(true);
                CustomSession session = new CustomSession();
                session.setMySession(request.getSession());
                item.set(classInstance, session);
            }
        }

    }

    public void checkRoleAuthentification(HttpServletRequest request,VerbMethod verbMethod) throws Exception{
        if (verbMethod.getRole() == null) {
            return;
        }
        Exception e = new Exception("Page indisponible ! erreur d'authentification");
        String sessionAttributeName = getServletContext().getInitParameter("auth");
        if (sessionAttributeName == null || sessionAttributeName.trim().isEmpty()) {
            sessionAttributeName = "auth"; // default value
        }

        Object role = request.getSession().getAttribute(sessionAttributeName);
        if (role == null) {
            this.setStatusCode(404);
            throw e;
        }
        Role requiredRole = verbMethod.getRole();
        Role admin = new Role(this.roles, (String)role );
        if (!admin.hasAccessLevel(requiredRole)) {
            this.setStatusCode(404);
            throw e;
        }
    }


    public VerbMethod checkVerbMethod(HttpServletRequest request, Mapping mapping) throws Exception{
        String verb = request.getMethod();
        int idverb = 0;
        for (int i=0; i < mapping.getListVerbMethod().size(); i++) {
            if (mapping.getListVerbMethod().get(i).getVerb().equals(verb)) {
                idverb = i;
            }
        } 
        String methodVerb = mapping.getListVerbMethod().get(idverb).getVerb();
        String methodName = mapping.getListVerbMethod().get(idverb).getMethodName();
        if (request.getAttribute("error")!=null) {
            verb = "GET";
        }
        if (!verb.equals(methodVerb)) {
            this.setStatusCode(500);
            throw new Exception("Le verb "+methodVerb+" au niveau de la methode ne correspond pas au method de la requete : "+verb );
        }
        return mapping.getListVerbMethod().get(idverb);
    }

    public Object[] getValuesIfParamExist(HttpServletRequest request, Method[] methods, String methodName, AtomicBoolean isErrorForm) throws Exception {
        Parameter[] listeParam = null;                    
        for (Method item : methods) {
            if (item.getName().equals(methodName)) {
                listeParam = item.getParameters(); break;
            }
        }
        Object[] values = new Object[listeParam.length];
        Enumeration<String> formParamNames = request.getParameterNames(); // formParamNames as parameters of the request 
        for (int i = 0; i < values.length; i++) {
            String paramName = checkAnnotationParam(listeParam[i]); // Check Annotation @Param
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
                    Object[] fieldsObjectValues = getFieldsObjectValues(request, fields, formParamNames, isErrorForm , listeParam[i]);
                    obj = process(obj, fieldsObjectValues);
                    values[i] = obj;
                } else if (listeParam[i].getType().equals(CustomSession.class)) {
                    CustomSession session = new CustomSession(); 
                    session.setMySession(request.getSession());
                    values[i] = session;
                }
                else {
                    boolean isNull = true;
                    while (formParamNames.hasMoreElements()) {
                        String name = formParamNames.nextElement();
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
        return values;
    }

    public Object[] getFieldsObjectValues(HttpServletRequest request, Field[] fields, Enumeration<String> formParamNames,
                                            AtomicBoolean isErrorForm, Parameter parameter) throws Exception{
        String paramName = checkAnnotationParam(parameter); 
        Object[] fieldsObjectValues = new Object[fields.length]; 
        while (formParamNames.hasMoreElements()) {
            String name = formParamNames.nextElement();
            for (int j = 0; j < fields.length; j++) {
                if (name.startsWith(paramName+".")) {
                    int indexSuite = (paramName + ".").length();
                    String paramSimpleName = name.substring(indexSuite);
                    request.setAttribute(name, request.getParameter(name));

                    if (fields[j].isAnnotationPresent(AttribObjet.class)){
                        if (paramSimpleName.equals(fields[j].getAnnotation(AttribObjet.class).value())){
                            if (Validation.validation(fields[j], request.getParameter(name)) != "") {
                                this.setStatusCode(401);
                                String error = Validation.validation(fields[j], request.getParameter(name));
                                request.setAttribute("error_"+name, error);
                                isErrorForm.set(true);
                            } else {
                                fieldsObjectValues[j] = TypeHandler.castParameter(request.getParameter(name), fields[j].getType().getName());
                                break;
                            }
                        } 
                    } else {
                        if (paramSimpleName.equals(fields[j].getName())){
                            if (Validation.validation(fields[j], request.getParameter(name)) != "") {
                                String error = Validation.validation(fields[j], request.getParameter(name));
                                request.setAttribute("error_"+name, error);
                                isErrorForm.set(true);
                            } else {
                                fieldsObjectValues[j] = TypeHandler.castParameter(request.getParameter(name), fields[j].getType().getName());
                                break;
                            }
                        }  
                    }
                }
            }
        }
        return fieldsObjectValues;
    }

    public String checkAnnotationParam(Parameter param) throws Exception {
        String paramName = param.getName();
        if (param.isAnnotationPresent(Param.class)) {
            paramName = param.getAnnotation(Param.class).value();
        } else if (!param.getType().equals(CustomSession.class)){
            String errorMess = "vous n'avez pas annoté le paramètre '"+paramName+"' par @Param(\"...\")"  ;
            this.setStatusCode(500);
            throw new Exception("<p>ETU-002415 : "+errorMess+"</p>");
        }
        return paramName;
    }

    public <T> T  process(T obj, Object[] valueObjects) throws Exception {
        Class<?> classe = obj.getClass();
        Field[] fields = classe.getDeclaredFields();
        try {
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                Object valeur = valueObjects[i];
                field.set(obj, valeur);
            }
        } catch (Exception e) {
            this.setStatusCode(4321);
            throw e;
        }
        return obj;
    }

    public String resultHandler(Object result, HttpServletRequest request, HttpServletResponse response, Method method, AtomicBoolean isErrorForm) throws Exception {
        String retour = "";
        Gson gson = new Gson();
        if (isErrorForm.get()) {
            request.setAttribute("error", "value");
            ModelView mv = (ModelView) result;
            String referer = mv.getError();
            RequestDispatcher dispatcher = request.getRequestDispatcher(referer);
            dispatcher.forward(request, response);
            return "";
        }
        if (result instanceof ModelView){
            ModelView mv = (ModelView) result;
            if (mv.getUrl().startsWith("redirect:")) {
                mv.setUrl(mv.getUrl().substring("redirect:".length()));
                response.sendRedirect(mv.getUrl());
                return "";
            }
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
        String classesPath = getServletContext().getRealPath("/WEB-INF/classes");
        String decodedPath = URLDecoder.decode(classesPath, "UTF-8");
        String packagePath = decodedPath +"/"+ this.getControllerPackage().replace('.', '/');
        File packageDirectory = new File(packagePath);
        if (packageDirectory.exists() && packageDirectory.isDirectory()) {
            File[] classFiles = packageDirectory.listFiles((dir, name) -> name.endsWith(".class"));
            if (classFiles != null) {
                for (File classFile : classFiles) {
                    String className = this.getControllerPackage() + '.' + classFile.getName().substring(0, classFile.getName().length() - 6);
                    Class<?> classe = Class.forName(className);
                    if (classe.isAnnotationPresent(Controller.class)) {
                        this.getController().add(classe.getSimpleName());
                        Method[] methods = classe.getMethods();
                        subScan(methods, className);
                    }
                }
                if (this.getController().isEmpty()) {
                    this.setStatusCode(500);
                    throw new Exception("Il n'y aucun controller dans ce package");
                }
            }
        } else {
            this.setStatusCode(500);
            throw new Exception("Le package "+ this.getControllerPackage() +" n'existe pas");
        }
    }
    
    public void subScan(Method[] methods, String className) throws Exception {
        Class<?> classe = Class.forName(className);
        Role superRole = null;
        if (classe.isAnnotationPresent(Auth.class)) {
            Auth auth = classe.getAnnotation(Auth.class);
            String nameRole = auth.role();
            superRole = new Role(this.roles, nameRole);
        }

        for (Method item : methods) {
            if (item.isAnnotationPresent(Url.class)) {
                Mapping mapping = new Mapping(className);
                mapping.setRole(superRole);
                String verb = "GET";

                if (item.isAnnotationPresent(Post.class)){
                    verb = "POST";
                }
                Url url = item.getAnnotation(Url.class);
                String urlValue = url.value();
                Role role = null;
                if (item.isAnnotationPresent(Auth.class)) {
                    Auth auth = item.getAnnotation(Auth.class);
                    String nameRole = auth.role();
                    role = new Role(this.roles, nameRole);
                }
                if (role != null) {
                    role.applyParentRoleIfWeaker(superRole);
                } else {
                    role = superRole;
                }
                VerbMethod vm = new VerbMethod(item.getName(),verb,role);

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



}




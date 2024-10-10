package mg.itu.prom16.utils;

public class Mapping {
    String className;
    String methodName;
    String verb; 

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }
    public String getMethodName() {
        return methodName;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

}


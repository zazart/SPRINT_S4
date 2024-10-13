package mg.itu.prom16.models;

public class VerbMethod {
    String methodName;
    String verb;

    public VerbMethod() {
        
    }

    public VerbMethod(String methodName, String verb) {
        this.methodName = methodName;
        this.verb = verb;
    }

    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    } 
}

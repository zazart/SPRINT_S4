package mg.itu.prom16.models;

public class VerbMethod {
    Role role;
    String methodName;
    String verb;

    public VerbMethod() {
        
    }

    public VerbMethod(String methodName, String verb, Role role) {
        this.methodName = methodName;
        this.verb = verb;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

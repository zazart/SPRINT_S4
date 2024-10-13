package mg.itu.prom16.utils;

import mg.itu.prom16.models.VerbMethod;
import java.util.*;

public class Mapping {
    String className;
    List<VerbMethod> listVerbMethod = new ArrayList<>() ;

    
    public Mapping(String className) {
        this.className = className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void addVerbMethod(VerbMethod verbMethod) {
        this.listVerbMethod.add(verbMethod);
    }

    public boolean contains(VerbMethod verbMethod) {
        boolean retour = false;
        for (VerbMethod vm : this.listVerbMethod) {
            if (vm.getMethodName().equals(verbMethod.getMethodName()) && vm.getVerb().equals(verbMethod.getVerb())) {
                retour = true;
            }
        }
        return retour;
    }

    public List<VerbMethod> getListVerbMethod() {
        return listVerbMethod;
    }

    public void setListVerbMethod(List<VerbMethod> listVerbMethod) {
        this.listVerbMethod = listVerbMethod;
    }

}


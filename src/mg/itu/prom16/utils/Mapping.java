package mg.itu.prom16.utils;

import mg.itu.prom16.models.Role;
import mg.itu.prom16.models.VerbMethod;
import java.util.*;

public class Mapping {
    Role role;
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
            if (vm.getVerb().equals(verbMethod.getVerb())) {
                retour = true;
                break;
            }
        }
        return retour;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<VerbMethod> getListVerbMethod() {
        return listVerbMethod;
    }

    public void setListVerbMethod(List<VerbMethod> listVerbMethod) {
        this.listVerbMethod = listVerbMethod;
    }

}


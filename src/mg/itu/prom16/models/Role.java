package mg.itu.prom16.models;

import java.util.List;

public class Role {

    private String name;
    private int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public Role() {}


    public Role(String name, int level) {
        this.name = name.toUpperCase();
        this.level = level;
    }



    public Role(List<Role> roles, String name) throws Exception {
        this.name = name.toUpperCase();
        this.level = -1;
        for (Role role : roles) {
            if(role.getName().equals(this.name)){
                this.level = role.getLevel();
            }
        }
        if (level == -1) {
            throw new Exception("Le role : "+ this.name +" n'existe pas!");
        }
    }


    public boolean hasAccessLevel(Role requiredRole) {
        return this.getLevel() <= requiredRole.getLevel() & this.getLevel() != -1;
    }


}

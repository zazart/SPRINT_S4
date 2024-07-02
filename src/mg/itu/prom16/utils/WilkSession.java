package mg.itu.prom16.utils;

import java.util.HashMap;

public class WilkSession {
    private HashMap<String, Object> values = new HashMap<String, Object>();

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

    public void add(String key, Object value) {
        this.getValues().put(key,value);
    }

    public Object get(String key) {
        return this.getValues().get(key);
    }

    public void update(String key, Object value) {
        this.getValues().put(key,value);
    }

    public void remove(String key) {
        this.getValues().remove(key);
    }

}

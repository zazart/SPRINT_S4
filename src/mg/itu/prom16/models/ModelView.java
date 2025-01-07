package mg.itu.prom16.models;

import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String,Object> data = new HashMap<>();
    private String error ;

    public ModelView(String url) {
        this.url = url;
    }

    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public HashMap<String, Object> getData() {
        return data;
    }
    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void addObject(String key, Object objet) {
        data.put(key, objet);
    }


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

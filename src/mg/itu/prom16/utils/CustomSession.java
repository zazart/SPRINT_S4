package mg.itu.prom16.utils;

import jakarta.servlet.http.HttpSession;

public class CustomSession {
    private HttpSession mySession;

    public HttpSession getMySession() {
        return mySession;
    }

    public void setMySession(HttpSession mySession) {
        this.mySession = mySession;
    }


    public void add(String key, Object value) {
        this.mySession.setAttribute(key, value);
    }

    public Object get(String key) {
        return this.mySession.getAttribute(key);
    }

    public void remove(String key) {
        this.mySession.removeAttribute(key);
    }

    public void update(String key, Object value) {
        this.mySession.setAttribute(key, value);
    }
}

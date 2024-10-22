package mg.itu.prom16.utils;

public class TypeHandler {

    public static Class<?> checkClassForName(String typeName) throws Exception {
        switch (typeName) {
            case "int":
                return int.class;
            case "boolean":
                return boolean.class;  
            case "byte":
                return byte.class;
            case "char":
                return char.class;  
            case "short":
                return short.class;
            case "long":
                return long.class;    
            case "float":
                return float.class;
            case "double":
                return double.class;          
            default:
                return Class.forName(typeName);
        }
    } 

    public static Object castParameter(String value, String type) throws Exception {
        Class<?> classe = checkClassForName(type);
        if (value == null) {
            if (classe == String.class) {
                return "";
            } else if (classe == Integer.class || classe == int.class) {
                return 0;
            } else if (classe == Double.class || classe == double.class) {
                return 0.0;
            }
            return null;
        }

        if (classe == String.class) {
            return value;
        } else if (classe == Integer.class || classe == int.class) {
            return Integer.parseInt(value);
        } else if (classe == Double.class || classe == double.class) {
            return Double.parseDouble(value);
        } else if (classe == Boolean.class || classe == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (classe == Long.class || classe == long.class) {
            return Long.parseLong(value);
        } else if (classe == Float.class || classe == float.class) {
            return Float.parseFloat(value);
        } else if (classe == Short.class || classe == short.class) {
            return Short.parseShort(value);
        } else if (classe == Byte.class || classe == byte.class) {
            return Byte.parseByte(value);
        } else {
            throw new Exception("Erreur de cast sur "+type);
        }
    }

    public static Class<?>[] checkParameterTypes(Object[] values) {
        Class<?>[] parameterTypes = new Class<?>[values.length];

       for (int i = 0; i < values.length; i++) {
           if (values[i] instanceof Integer) {
               parameterTypes[i] = int.class;
           } else if (values[i] instanceof Double) {
               parameterTypes[i] = double.class;
           } else if (values[i] instanceof Boolean) {
               parameterTypes[i] = boolean.class;
           } else if (values[i] instanceof Long) {
               parameterTypes[i] = long.class;
           } else if (values[i] instanceof Float) {
               parameterTypes[i] = float.class;
           } else if (values[i] instanceof Short) {
               parameterTypes[i] = short.class;
           } else if (values[i].getClass().getName().equals("org.apache.catalina.core.ApplicationPart")) {
               parameterTypes[i] = jakarta.servlet.http.Part.class;
           } else if (values[i] instanceof Byte) {
               parameterTypes[i] = byte.class;
           } else {
               parameterTypes[i] = values[i].getClass();
           }
       }
       return parameterTypes;
    }
}

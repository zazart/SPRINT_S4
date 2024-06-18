package mg.itu.prom16.utils;

public class CastTo {

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
        Class<?> clazz = checkClassForName(type);
        if (value == null) {
            if (clazz == String.class) {
                return "";
            } else if (clazz == Integer.class || clazz == int.class) {
                return 0;
            } else if (clazz == Double.class || clazz == double.class) {
                return 0.0;
            }
            return null;
        }

        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class || clazz == int.class) {
            return Integer.parseInt(value);
        } else if (clazz == Double.class || clazz == double.class) {
            return Double.parseDouble(value);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (clazz == Long.class || clazz == long.class) {
            return Long.parseLong(value);
        } else if (clazz == Float.class || clazz == float.class) {
            return Float.parseFloat(value);
        } else if (clazz == Short.class || clazz == short.class) {
            return Short.parseShort(value);
        } else if (clazz == Byte.class || clazz == byte.class) {
            return Byte.parseByte(value);
        } else {
            throw new Exception("Erreur de cast sur "+type);
        }
    }
}

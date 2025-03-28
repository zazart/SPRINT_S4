package mg.itu.prom16.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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


    public static Class<?>[] checkParameterTypes(Method[] methods, String methodName) {
        Parameter[] listeParam = null;
        for (Method item : methods) {
            if (item.getName().equals(methodName)) {
                listeParam = item.getParameters(); break;
            }
        }
        Class<?>[] parameterTypes = new Class<?>[listeParam.length];
        for (int i = 0; i < listeParam.length; i++) {
            parameterTypes[i] = listeParam[i].getType();
        }
        return parameterTypes;
    }
}

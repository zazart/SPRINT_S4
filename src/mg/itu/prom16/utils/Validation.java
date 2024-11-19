package mg.itu.prom16.utils;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import mg.itu.prom16.annotation.validation.Email;
import mg.itu.prom16.annotation.validation.Range;
import mg.itu.prom16.annotation.validation.Required;

public class Validation {
    
    public static String validation(Field attributs, String valeur) {
        String validation = "";

        if (attributs.isAnnotationPresent(Required.class)) {
            if (valeur == null || valeur.trim().isEmpty()) {
                validation = "La valeur ne doit pas être null ou vide sur "+attributs.getName();
                return validation;
            }
        }

        if (attributs.isAnnotationPresent(Email.class)) {
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            Pattern pattern = Pattern.compile(emailRegex);
            if (!pattern.matcher(valeur).matches()) {
                return "La valeur doit correspondre au format email sur "+attributs.getName();
            }
        }

        if (attributs.isAnnotationPresent(Range.class)) {
            try {
                double valeurNumerique = Double.parseDouble(valeur);
                Range range = attributs.getAnnotation(Range.class);
                if (valeurNumerique < range.min() || valeurNumerique > range.max()) {
                    validation = "La valeur doit être entre "+range.min()+" et "+range.max()+" sur "+attributs.getName();
                    return validation;
                }
            } catch (NumberFormatException e) {
                return "La valeur doit être un nombre si @Range est présent sur "+attributs.getName();
            }
        } 
        return validation;
    }


}

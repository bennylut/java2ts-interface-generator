/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bennylut.github.tsigen.util;

import com.google.auto.common.MoreTypes;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 *
 * @author bennyl
 */
public class MirrorUtil {
    
    private static final Class[] NUMMERIC_TYPES = {
        byte.class,
        Byte.class,
        short.class,
        Short.class,
        int.class,
        Integer.class,
        long.class,
        Long.class,
        float.class,
        Float.class,
        double.class,
        Double.class,
        Number.class
    };
    
    public static boolean isNummericType (TypeMirror type) {
        for (Class n : NUMMERIC_TYPES) {
            if (MoreTypes.isTypeOf(n, type)) return true;
        }
        
        return false;
    }
    
    public static TypeMirror getTypeMirror(Class c, Elements elementUtils) {
        return elementUtils.getTypeElement(c.getCanonicalName()).asType();
    }
}

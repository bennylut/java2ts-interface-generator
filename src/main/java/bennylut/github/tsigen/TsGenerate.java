/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bennylut.github.tsigen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author bennyl
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TsGenerate {

    String file();

    String name();
    
    /**
     * e.g., imports="{A,B} from 'aaa/bbb'"
     * @return 
     */
    String[] imports() default {""};
}

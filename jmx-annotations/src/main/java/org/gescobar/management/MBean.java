package org.gescobar.management;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(value=RUNTIME)
@Target(value=TYPE)
@Inherited
public @interface MBean {
    String value() default "";
    boolean autoRegister() default true;
}

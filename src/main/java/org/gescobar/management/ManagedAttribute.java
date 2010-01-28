package org.gescobar.management;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(value=RUNTIME)
@Target(value={FIELD,METHOD})
public @interface ManagedAttribute {
    boolean readable() default true;
    boolean writable() default true;
}

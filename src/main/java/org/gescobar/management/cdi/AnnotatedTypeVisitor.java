package org.gescobar.management.cdi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.management.MBeanInfo;

/**
 * @author German Escobar
 *
 * Implementation of the Visitor Pattern to create an MBean from an AnnotatedType 
 * (object that is going to be exposed as an MBean). While the AnnotatedType is
 * transversed, visitors obtain the exposed fields, methods, and create the
 * MBeanInfo. This information is then used to create the MBean that is 
 * registered on the MBeanServer.
 */
public interface AnnotatedTypeVisitor {

    <T> void visitAnnotatedType(AnnotatedType<T> at);
    
    <T> void visitAnnotatedConstructor(AnnotatedConstructor<T> ac);
    
    <T> void visitAnnotatedField(AnnotatedField<T> af);
    
    <T> void visitAnnotatedMethod(AnnotatedMethod<T> am);
    
    MBeanInfo getMBeanInfo();
    
    Field[] getFields();
    
    Method[] getMethods();
    
}

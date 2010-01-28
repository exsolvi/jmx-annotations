package org.gescobar.management.cdi;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;

import org.gescobar.management.Impact;
import org.gescobar.management.ManagedAttribute;
import org.gescobar.management.ManagedOperation;

/**
 * @author German Escobar
 *
 * A DynamicMBeanInfoBuilder is a {@link StandardMBeanInfoBuilder} that only 
 * includes those fields and methods annotated with
 * {@link org.gescobar.management.ManagedAttribute} and
 * {@link org.gescobar.management.ManagedOperation} in the MBeanInfo. The 
 * AnnotatedType must be annotated with {@link org.gescobar.management.MBean}.
 */
public class DynamicMBeanInfoBuilder extends StandardMBeanInfoBuilder {

    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#visitAnnotatedField(javax.enterprise.inject.spi.AnnotatedField)
     */
    @Override
    public <T> void visitAnnotatedField(AnnotatedField<T> af) {
	// if the annotation is not present, ignore the field
	if (!af.isAnnotationPresent(ManagedAttribute.class)) {
	    return;
	}
	
	ManagedAttribute annAttribute = af.getAnnotation(ManagedAttribute.class);
	boolean readable = annAttribute.readable();
	boolean writable = annAttribute.writable();
	
	super.visitAnnotatedField(af, readable, writable);
    }

    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#visitAnnotatedMethod(javax.enterprise.inject.spi.AnnotatedMethod)
     */
    @Override
    public <T> void visitAnnotatedMethod(AnnotatedMethod<T> am) {
	// if the annotation is not present, ignore the method
	if (!am.isAnnotationPresent(ManagedOperation.class)) {
	    return;
	}
	
	ManagedOperation annOperation = am.getAnnotation(ManagedOperation.class);
	Impact impact = annOperation.impact();
	
	super.visitAnnotatedMethod(am, impact);

    }

}

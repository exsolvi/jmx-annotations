package org.gescobar.management.cdi;

import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.gescobar.management.MBean;
import org.gescobar.management.MBeanFactory;
import org.gescobar.management.ManagementException;
import org.gescobar.management.util.MBeanImpl;

/**
 * @author German Escobar
 *
 * An abstract class used a as a base class for {@link org.gescobar.management.MBeanFactory}
 * implementations that create MBeans from AnnotatedTypes.
 */
public abstract class AbstractMBeanFactory implements MBeanFactory {
    
    /**
     * This method has to be implemented by concrete classes to retrieve the annotated
     * type from which the MBean is going to be created.
     * @param instance the object that is going to be exposed.
     * @return an AnnotatedType representing the object received by parameter.
     * @throws Exception
     */
    protected abstract AnnotatedType<? extends Object> getAnnotatedType(Object instance) throws Exception;

    /* (non-Javadoc)
     * @see org.gescobar.management.MBeanFactory#createMBean(java.lang.Object)
     */
    @Override
    public <T> MBeanImpl<T> createMBean(T instance) throws ManagementException {
	try {
	    AnnotatedType<? extends Object> at = getAnnotatedType(instance);
	    
	    // select the type of visitor
	    AnnotatedTypeVisitor visitor = null;
	    if (at.isAnnotationPresent(MBean.class)) {
		visitor = new DynamicMBeanInfoBuilder();
	    } else {
		visitor = new StandardMBeanInfoBuilder();
	    }
	    
	    processAnnotatedType(at, visitor);
    	    	
    	    // create the MBean
    	    MBeanImpl<T> mBean = new MBeanImpl<T>(instance, visitor.getFields(), visitor.getMethods(), 
    		    visitor.getMBeanInfo());
    	
    	    return mBean;
	} catch (Exception e) {
	    throw new ManagementException(e);
	}
    }
    
    /**
     * This method is used to create the {@link ExposedMembers} and the {@link javax.management.MBeanInfo}
     * objects using the AnnotatedType to retrieve everything that should be exposed 
     * as JMX.
     * @param <T>
     * @param at
     * @param visitor
     */
    private <T> void processAnnotatedType(AnnotatedType<T> at, AnnotatedTypeVisitor visitor) {
	// visit annotated type
	visitor.visitAnnotatedType(at);
	
	// visit constructors
	Set<AnnotatedConstructor<T>> annConstructors = at.getConstructors();
	for (AnnotatedConstructor<T> ac : annConstructors) {
	    visitor.visitAnnotatedConstructor(ac);
	}
	
	// visit fields
	Set<AnnotatedField<? super T>> annFields = at.getFields();
	for (AnnotatedField<? super T> af : annFields) {
	    visitor.visitAnnotatedField(af);
	}
	
	// visit methods
	Set<AnnotatedMethod<? super T>> annMethods = at.getMethods();
	for (AnnotatedMethod<? super T> am : annMethods) {
		visitor.visitAnnotatedMethod(am);
	}
    }

}

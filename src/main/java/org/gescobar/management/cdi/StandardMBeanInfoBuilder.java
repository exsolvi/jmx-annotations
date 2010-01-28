package org.gescobar.management.cdi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.gescobar.management.Description;
import org.gescobar.management.Impact;

/**
 * @author German Escobar
 *
 */
public class StandardMBeanInfoBuilder implements AnnotatedTypeVisitor {
    
    /**
     * The name of the class. Used to create the MBeanInfo.
     */
    protected String className = "";
    
    /**
     * The description of the MBean. Used to create the MBeanInfo.
     */
    protected String description = "";
    
    /**
     * The constructors of the MBean. Used to create the MBeanInfo.
     */
    protected Set<MBeanConstructorInfo> mBeanConstructors = new HashSet<MBeanConstructorInfo>();
    
    /**
     * The attributes of the MBean. Used to create the MBeanInfo.
     */
    protected Set<MBeanAttributeInfo> mBeanAttributes = new HashSet<MBeanAttributeInfo>();
    
    /**
     * The operations of the MBean. Used to create the MBeanInfo.
     */
    protected Set<MBeanOperationInfo> mBeanOperations = new HashSet<MBeanOperationInfo>();
    
    /**
     * The attributes that will be exposed in the MBean.
     */
    protected Set<Field> exposedFields = new HashSet<Field>();
    
    /**
     * The methods that will be exposed in the MBean. 
     */
    protected Set<Method> exposedMethods = new HashSet<Method>();
    
    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#visitAnnotatedType(javax.enterprise.inject.spi.AnnotatedType)
     */
    @Override
    public <T> void visitAnnotatedType(AnnotatedType<T> at) {
	this.className = at.getJavaClass().getName();
	
	// retrieve the description
	if (at.isAnnotationPresent(Description.class)) {
	    Description annDescription = at.getAnnotation(Description.class);
	    this.description = annDescription.value();
	}
    }

    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#visitAnnotatedConstructor(javax.enterprise.inject.spi.AnnotatedConstructor)
     */
    @Override
    public <T> void visitAnnotatedConstructor(AnnotatedConstructor<T> ac) {
	String consDescription = "";
	if (ac.isAnnotationPresent(Description.class)) {
	    Description annDescription = ac.getAnnotation(Description.class);
	    consDescription = annDescription.value();
	}
	
	MBeanConstructorInfo constructorInfo = new MBeanConstructorInfo(consDescription, ac.getJavaMember());
	this.mBeanConstructors.add(constructorInfo);
    }

    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#visitAnnotatedField(javax.enterprise.inject.spi.AnnotatedField)
     */
    @Override
    public <T> void visitAnnotatedField(AnnotatedField<T> af) {
	/* FIXME should check if set and get methods exists */
	boolean readable = true;
	boolean writable = true;
	
	this.visitAnnotatedField(af, readable, writable);
    }
    
    protected <T> void visitAnnotatedField(AnnotatedField<T> af, boolean readable, boolean writable) {
	// add the field to the collection of exposed fields
	exposedFields.add(af.getJavaMember());
	
	// create the MBeanAttributeInfo
	String fieldDescription = "";
	if (af.isAnnotationPresent(Description.class)) {
	    Description annDescription = af.getAnnotation(Description.class);
	    fieldDescription = annDescription.value();
	}
	
	Field field = af.getJavaMember();
	MBeanAttributeInfo attributeInfo = new MBeanAttributeInfo(field.getName(), field.getType().getName(), 
		fieldDescription, readable, writable, false);
	
	this.mBeanAttributes.add(attributeInfo);
    }

    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#visitAnnotatedMethod(javax.enterprise.inject.spi.AnnotatedMethod)
     */
    @Override
    public <T> void visitAnnotatedMethod(AnnotatedMethod<T> am) {
	visitAnnotatedMethod(am, Impact.UNKNOWN);
    }
    
    protected <T> void visitAnnotatedMethod(AnnotatedMethod<T> am, Impact impact) {
	// add the method to the collection of exposed methods
	exposedMethods.add(am.getJavaMember());
	
	// create the MBeanOperationInfo
	String methodDescription = "";
	if (am.isAnnotationPresent(Description.class)) {
	    Description annDescription = am.getAnnotation(Description.class);
	    methodDescription = annDescription.value();
	}
	
	Method method = am.getJavaMember();
	
	List<AnnotatedParameter<T>> annotatedParams = am.getParameters();
	Class<?>[] paramsTypes = method.getParameterTypes();
	MBeanParameterInfo[] params = new MBeanParameterInfo[annotatedParams.size()];

	for (AnnotatedParameter<T> ap : annotatedParams) {
	    String paramDescription = "";
	    if (ap.isAnnotationPresent(Description.class)) {
		Description annDescription = ap.getAnnotation(Description.class);
		paramDescription = annDescription.value();
	    }
	    
	    int position = ap.getPosition();
	    
	    MBeanParameterInfo parameterInfo = new MBeanParameterInfo("param" + position, paramDescription, paramsTypes[position].getName());
	    params[position] = parameterInfo;

	}
	
	MBeanOperationInfo operationInfo = new MBeanOperationInfo(method.getName(), 
		methodDescription, params, method.getReturnType().getName(), impact.getCode());
	
	mBeanOperations.add(operationInfo);
    }
    
    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#getFields()
     */
    @Override
    public Field[] getFields() {
	return exposedFields.toArray(new Field[exposedFields.size()]);
    }

    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#getMethods()
     */
    @Override
    public Method[] getMethods() {
	return exposedMethods.toArray(new Method[exposedMethods.size()]);
    }
    
    /* (non-Javadoc)
     * @see org.gescobar.management.cdi.AnnotatedTypeVisitor#getMBeanInfo()
     */
    @Override
    public MBeanInfo getMBeanInfo() {
	return new MBeanInfo(this.className, this.description, getMBeanAttributes(), 
		getMBeanConstructors(), getMBeanOperations(), new MBeanNotificationInfo[0]);
    }
    
    private MBeanConstructorInfo[] getMBeanConstructors() {
        return this.mBeanConstructors.toArray(new MBeanConstructorInfo[mBeanConstructors.size()]);
    }

    private MBeanAttributeInfo[] getMBeanAttributes() {
        return this.mBeanAttributes.toArray(new MBeanAttributeInfo[mBeanAttributes.size()]);
    }

    private MBeanOperationInfo[] getMBeanOperations() {
        return this.mBeanOperations.toArray(new MBeanOperationInfo[mBeanOperations.size()]);
    }

}

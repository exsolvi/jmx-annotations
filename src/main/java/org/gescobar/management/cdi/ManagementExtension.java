package org.gescobar.management.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.util.AnnotationLiteral;

import org.gescobar.management.MBean;
import org.gescobar.management.MBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author German Escobar
 * 
 * This class is the portable extension for CDI. It observes for the
 * ProcessInjectionTarget event and decorates the InjectionTarget for
 * automatic MBean registration.
 */
public class ManagementExtension implements Extension {

    private static Logger log = LoggerFactory.getLogger(ManagementExtension.class);
    
    /**
     * This method gets called when an injection target is processed on bootstrap. It
     * wraps the InjectionTarget to add the JMX registration magic.
     * @param <T>
     * @param pit
     */
    public <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
	
	// check if the MBean annotation is present
	AnnotatedType<T> at = pit.getAnnotatedType();
	if (at.isAnnotationPresent(MBean.class)) {
	    
	    // check if automatic registration is on
	    MBean mBeanAnnotation = at.getAnnotation(MBean.class);
	    if (!mBeanAnnotation.autoRegister()) {
		log.info(at.getJavaClass().getName() + "has the MBean annotation with autoRegister=false");
		return;
	    }
	    
	    log.info("adding automatic JMX registration for: " + at.getJavaClass().getName());
		
	    // decorate the InjectionTarget
	    final InjectionTarget<T> delegate = pit.getInjectionTarget();
	    InjectionTarget<T> wrapper = new ManagementInjectionTarget<T>(at, delegate);

	    // change the InjectionTarget with the decorated one
	    pit.setInjectionTarget(wrapper);
	}
    }
    
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
	// add the MBeanFactory service
	final Class<CDIMBeanFactory> c = CDIMBeanFactory.class;
	
	//use this to read annotations of the class
        AnnotatedType at = bm.createAnnotatedType(c);

        //use this to create the class and inject dependencies
        final InjectionTarget it = bm.createInjectionTarget(at);

        abd.addBean( new Bean() {
 
            @Override
            public Class<?> getBeanClass() {
                return c;
            }
 
            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }
 
            @Override
            public String getName() {
                return null;
            }
 
            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<Annotation>();
                qualifiers.add( new AnnotationLiteral<Default>() {} );
                qualifiers.add( new AnnotationLiteral<Any>() {} );
                return qualifiers;
            }
 
            @Override
            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }
 
            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }
 
            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<Type>();
                types.add(c);
                types.add(MBeanFactory.class);
                types.add(Object.class);
                return types;
            }
 
            @Override
            public boolean isAlternative() {
                return false;
            }
 
            @Override
            public boolean isNullable() {
                return false;
            }
 
            @Override
            public Object create(CreationalContext ctx) {
                Object instance = it.produce(ctx);
                it.inject(instance, ctx);
                it.postConstruct(instance);
                return instance;
            }
 
            @Override
            public void destroy(Object instance, CreationalContext ctx) {
                it.preDestroy(instance);
                it.dispose(instance);
                ctx.release();
            }
             
        } );

    }
	
}
   

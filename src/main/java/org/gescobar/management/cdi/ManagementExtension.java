package org.gescobar.management.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.gescobar.management.MBean;
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
	
}
   

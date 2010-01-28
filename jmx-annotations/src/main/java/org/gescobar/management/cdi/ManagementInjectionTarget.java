package org.gescobar.management.cdi;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.gescobar.management.MBean;
import org.gescobar.management.MBeanFactory;
import org.gescobar.management.util.MBeanServerLocator;

public class ManagementInjectionTarget<T> implements InjectionTarget<T> {
    
    private AnnotatedType<T> at;
    private InjectionTarget<T> delegate;
    
    private ObjectName objectName;
    
    public ManagementInjectionTarget(AnnotatedType<T> at, InjectionTarget<T> delegate) {
	this.at = at;
	this.delegate = delegate;
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
	delegate.inject(instance, ctx);
    }

    @Override
    public void postConstruct(T instance) {
	delegate.postConstruct(instance);
	
	try {
	    MBeanFactory mBeanFactory = new BootstrapMBeanFactory(at);
	    DynamicMBean mBeanImpl = mBeanFactory.createMBean(instance);
	    
	    objectName = this.getObjectName(at);
	
	    MBeanServer mBeanServer = MBeanServerLocator.instance().getmBeanServer();
	    mBeanServer.registerMBean(mBeanImpl, objectName);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
    }

    @Override
    public void preDestroy(T instance) {
	delegate.preDestroy(instance);
	
	try {
	    MBeanServer mBeanServer = MBeanServerLocator.instance().getmBeanServer();
	    mBeanServer.unregisterMBean(objectName);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
    }

    @Override
    public void dispose(T instance) {
	delegate.dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
	return delegate.getInjectionPoints();
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
	return delegate.produce(ctx);
    }
    
    private ObjectName getObjectName(AnnotatedType<T> at) throws Exception {
	
	String name = "";
	
	if (at.isAnnotationPresent(MBean.class)) {
	    MBean mBeanAnnotation = at.getAnnotation(MBean.class);
	    name = mBeanAnnotation.value();
	    if (name != null && name.equals("")) {
		name = null;
	    }
	}
	
	if (name == null) {
	    name = at.getJavaClass().getPackage().getName() + ":type=" + at.getJavaClass().getSimpleName();
	}
	
	return new ObjectName(name);
    }
    
    private class BootstrapMBeanFactory extends AbstractMBeanFactory {
	
	private AnnotatedType<T> at;
	
	public BootstrapMBeanFactory(AnnotatedType<T> at) {
	    this.at = at;
	}

	@Override
	protected AnnotatedType<? extends Object> getAnnotatedType(Object instance) throws Exception {
	    return at;
	}
	
    }

}

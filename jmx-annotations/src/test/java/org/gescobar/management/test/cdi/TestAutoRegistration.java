package org.gescobar.management.test.cdi;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.gescobar.management.Description;
import org.gescobar.management.Impact;
import org.gescobar.management.MBean;
import org.gescobar.management.MBeanFactory;
import org.gescobar.management.ManagedAttribute;
import org.gescobar.management.ManagedOperation;
import org.gescobar.management.ManagementException;
import org.gescobar.management.cdi.AbstractMBeanFactory;
import org.gescobar.management.cdi.AnnotatedTypeVisitor;
import org.gescobar.management.cdi.CDIMBeanFactory;
import org.gescobar.management.cdi.DynamicMBeanInfoBuilder;
import org.gescobar.management.cdi.ManagementExtension;
import org.gescobar.management.cdi.ManagementInjectionTarget;
import org.gescobar.management.cdi.StandardMBeanInfoBuilder;
import org.gescobar.management.test.CounterAutoRegisterNoName;
import org.gescobar.management.test.CounterAutoRegisterWithName;
import org.gescobar.management.util.MBeanImpl;
import org.gescobar.management.util.MBeanServerLocator;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.Paths;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.ByteArrayAsset;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author German Escobar
 *
 */
public class TestAutoRegistration extends Arquillian {
    
    private static Logger log = Logger.getLogger("TestAutoRegistration"); 
    
    @Deployment
    public static JavaArchive createDeployment() {
	
	JavaArchive archive = Archives.create("test.jar", JavaArchive.class)
		.addClasses(ManagementException.class, MBeanFactory.class, MBeanImpl.class, 
			Description.class, Impact.class, ManagedAttribute.class, 
			ManagedOperation.class, MBean.class, AbstractMBeanFactory.class, 
			AnnotatedTypeVisitor.class, CDIMBeanFactory.class, DynamicMBeanInfoBuilder.class,
			ManagementExtension.class, ManagementInjectionTarget.class,
			StandardMBeanInfoBuilder.class,MBeanServerLocator.class)
		.addClasses(CounterAutoRegisterWithName.class, CounterAutoRegisterNoName.class)
		.addManifestResource("META-INF/services/javax.enterprise.inject.spi.Extension", "services/javax.enterprise.inject.spi.Extension")
		.addManifestResource(
			new ByteArrayAsset("<beans/>".getBytes()), 
	                     Paths.create("beans.xml"));
	
	log.info("Archive: " + archive.toString(true));
	
	return archive;
    }
    
    @Inject
    private CounterAutoRegisterWithName counterWithName;
    
    @Inject
    private CounterAutoRegisterNoName counterNoName;

    @Test
    public void shouldRegisterAnnotatedWithNameMBean() throws Exception {
	Assert.assertNotNull(counterWithName);
	
	// the bean is not created until the first call - maybe a bug in weld?
	Assert.assertEquals(counterWithName.getCounter(), 0);
	
	MBeanServer mBeanServer = MBeanServerLocator.instance().getmBeanServer();
	ObjectName name = new ObjectName("org.gescobar:type=CounterAutoRegisterWithName");
	
	// check we can add the counter
	mBeanServer.setAttribute(name, new Attribute("counter", 1));
	Assert.assertEquals(counterWithName.getCounter(), 1);
	
	// check we can retrieve the counter
	Integer result = (Integer) mBeanServer.getAttribute(name, "counter");
	Assert.assertNotNull(result);
	
	// check we can call method without arguments
	mBeanServer.invoke(name, "resetCounter", null, null);
	Assert.assertEquals(counterWithName.getCounter(), 0);
	
	// check we can call method with arguments
	mBeanServer.invoke(name, "resetCounter2", new Object[] { 5 }, new String[] { "java.lang.Integer" });
	Assert.assertEquals(counterWithName.getCounter(), 5);
    }
    
    @Test
    public void shouldRegisterAnnotatedWithNoNameMBean() throws Exception {
	Assert.assertNotNull(counterNoName);
	
	Assert.assertEquals(counterNoName.getCounter(), 0);
	
	MBeanServer mBeanServer = MBeanServerLocator.instance().getmBeanServer();
	Object result = mBeanServer.getAttribute(new ObjectName("org.gescobar.management.test:type=CounterAutoRegisterNoName"), "counter");
	
	Assert.assertNotNull(result);
    }
}

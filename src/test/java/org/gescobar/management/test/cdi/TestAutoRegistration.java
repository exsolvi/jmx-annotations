package org.gescobar.management.test.cdi;

import javax.inject.Inject;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.gescobar.management.MBeanFactory;
import org.gescobar.management.cdi.CDIMBeanFactory;
import org.gescobar.management.test.CounterAutoRegisterNoName;
import org.gescobar.management.test.CounterAutoRegisterWithName;
import org.gescobar.management.util.MBeanServerLocator;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author German Escobar
 *
 */
public class TestAutoRegistration extends Arquillian { 
    
    @Deployment
    public static JavaArchive createDeployment() {
	
	JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
		.addPackage(MBeanFactory.class.getPackage()) 
		.addPackage(CDIMBeanFactory.class.getPackage())
		.addPackage(MBeanServerLocator.class.getPackage())
		.addClasses(CounterAutoRegisterWithName.class, CounterAutoRegisterNoName.class)
		.addManifestResource("META-INF/services/javax.enterprise.inject.spi.Extension", 
			"services/javax.enterprise.inject.spi.Extension")
		.addManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
	
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

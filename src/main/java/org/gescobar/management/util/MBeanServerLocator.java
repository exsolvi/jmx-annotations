package org.gescobar.management.util;

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

public class MBeanServerLocator {
    
    private MBeanServer mBeanServer;
    
    private static MBeanServerLocator instance;
    
    private MBeanServerLocator() {
	this.mBeanServer = locateMBeanServer();
    }
    
    public static MBeanServerLocator instance() {
	if (instance == null) {
	    instance = new MBeanServerLocator();
	}
	
	return instance;
    }

    public MBeanServer getmBeanServer() {
        return mBeanServer;
    }

    public void setmBeanServer(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    private MBeanServer locateMBeanServer() {
	MBeanServer mBeanServer = null;
	
	try {
	    mBeanServer = org.jboss.mx.util.MBeanServerLocator.locateJBoss();
	} catch (IllegalStateException e) {
	    ArrayList<MBeanServer> al = MBeanServerFactory.findMBeanServer(null);
	    if (al.isEmpty())
		mBeanServer = MBeanServerFactory.createMBeanServer();
	    else
		mBeanServer = (MBeanServer) al.get(0);
	}
	
	return mBeanServer;
    }
    
}

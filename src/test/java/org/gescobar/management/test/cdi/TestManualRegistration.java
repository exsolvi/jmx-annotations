package org.gescobar.management.test.cdi;

import javax.inject.Inject;

import org.gescobar.management.test.CounterAutoRegisterWithName;
import org.jboss.arquillian.testng.Arquillian;

public class TestManualRegistration extends Arquillian {

    @Inject
    private CounterAutoRegisterWithName counter;
    
    
}

package org.gescobar.management.test;

import javax.enterprise.context.ApplicationScoped;

import org.gescobar.management.Impact;
import org.gescobar.management.MBean;
import org.gescobar.management.ManagedAttribute;
import org.gescobar.management.ManagedOperation;

@ApplicationScoped
@MBean
public class CounterAutoRegisterNoName {

    @ManagedAttribute(readable=true,writable=true)
    private int counter;
    
    @ManagedOperation(impact=Impact.ACTION)
    public void resetCounter() {
	counter = 0;
    }
    
    @ManagedOperation(impact=Impact.ACTION)
    public void resetCounter2(int value) {
	counter = value;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}

public class VMDeathRequestTest extends TestScaffold {
    boolean requestedVMDeathOccurred = false;
    boolean defaultVMDeathOccurred = false;
    Object syncer = new Object();
    boolean disconnected = false;
    VMDeathRequest deathRequest;
    EventSet currentEventSet;
    VMDeathRequestTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new VMDeathRequestTest(args).startTests();
    }
    public void eventSetReceived(EventSet set) {
        currentEventSet = set;
    }
    public void vmDied(VMDeathEvent event) {
        if (event.request() == deathRequest) {
            requestedVMDeathOccurred = true;
            println("Got requested VMDeathEvent");
            if (currentEventSet.suspendPolicy() !=
                                   EventRequest.SUSPEND_ALL) {
                failure("failure: wrong suspend policy");
            }
        } else if (event.request() == null) {
            defaultVMDeathOccurred = true;
            println("Got default VMDeathEvent");
        } else {
            failure("failure: Unexpected type of VMDeathEvent occurred");
        }
    }
    public void vmDisconnected(VMDisconnectEvent event) {
        println("Got VMDisconnectEvent");
        disconnected = true;
        synchronized (syncer) {
            syncer.notifyAll();
        }
    }
    protected void createDefaultVMDeathRequest() {
    }
    protected void runTests() throws Exception {
        startToMain("HelloWorld");
        deathRequest = eventRequestManager().createVMDeathRequest();
        deathRequest.enable();
        List reqs = eventRequestManager().vmDeathRequests();
        if (reqs.size() != 1 || reqs.get(0) != deathRequest) {
            failure("failure: vmDeathRequests()");
        }
        if (!vm().canRequestVMDeathEvent()) {
            failure("failure: canRequestVMDeathEvent() returned false");
        }
        addListener(this);
        synchronized (syncer) {
            vm().resume();
            while (!disconnected) {
                try {
                    syncer.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        if (!requestedVMDeathOccurred) {
            failure("failure: didn't get requested VMDeathEvent");
        }
        if (!defaultVMDeathOccurred) {
            failure("failure: didn't get default VMDeathEvent");
        }
        if (!testFailed) {
            println("VMDeathRequestTest: passed");
        } else {
            throw new Exception("VMDeathRequestTest: failed");
        }
    }
}

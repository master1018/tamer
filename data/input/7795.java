public class VMDeathLastTest extends TestScaffold {
    Object syncer = new Object();
    boolean vmDead = false;
    boolean disconnected = false;
    VMDeathLastTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new VMDeathLastTest(args).startTests();
    }
    public void methodEntered(MethodEntryEvent event) {
        if (vmDead) {
            failure("Failure: Got MethodEntryEvent after VM Dead");
        }
    }
    public void classPrepared(ClassPrepareEvent event) {
        if (vmDead) {
            failure("Failure: Got ClassPrepareEvent after VM Dead");
        }
    }
    public void threadDied(ThreadDeathEvent event) {
        if (vmDead) {
            failure("Failure: Got ThreadDeathEvent after VM Dead");
        }
    }
    public void vmDied(VMDeathEvent event) {
        println("Got VMDeathEvent");
        vmDead = true;
    }
    public void vmDisconnected(VMDisconnectEvent event) {
        println("Got VMDisconnectEvent");
        if (!vmDead) {
            failure("Test failure: didn't get VMDeath");
        }
        disconnected = true;
        synchronized (syncer) {
            syncer.notifyAll();
        }
    }
    protected void createDefaultVMDeathRequest() {
    }
    protected void runTests() throws Exception {
        startToMain("HelloWorld");
        if (!vm().canBeModified()) {
            failure("VM says it is read-only");
        }
        EventRequestManager erm = vm().eventRequestManager();
        erm.createMethodEntryRequest().enable();
        erm.createClassPrepareRequest().enable();
        erm.createThreadDeathRequest().enable();
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
        if (!testFailed) {
            println("VMDeathLastTest: passed");
        } else {
            throw new Exception("VMDeathLastTest: failed");
        }
    }
}

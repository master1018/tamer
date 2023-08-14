class ThreadGroupTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from ThreadGroupTarg!");
    }
}
public class ThreadGroupTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    static class Fetcher implements Runnable {
        public void run() {
            Bootstrap.virtualMachineManager();
        }
    }
    ThreadGroupTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        ThreadGroup tg = new ThreadGroup("Gus");
        Fetcher fetcher = new Fetcher();
        Thread thr = new Thread(tg, fetcher);
        thr.start();
        try {
            thr.join();
        } catch (InterruptedException x) { }
        tg.destroy();
        new ThreadGroupTest(args).startTests();
    }
    protected void runTests() throws Exception {
        startToMain("ThreadGroupTarg");
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("ThreadGroupTest: passed");
        } else {
            throw new Exception("ThreadGroupTest: failed");
        }
    }
}

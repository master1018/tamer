class DebuggerThreadTarg {
    public void ready() {
    }
    public static void main(String[] args){
        System.out.println("Howdy!");
        DebuggerThreadTarg targ = new DebuggerThreadTarg();
        targ.ready();
        System.out.println("Goodbye from DebuggerThreadTarg!");
    }
}
public class DebuggerThreadTest extends TestScaffold {
    DebuggerThreadTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new DebuggerThreadTest(args).startTests();
    }
    public void dumpThreads() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        ThreadGroup parent = tg.getParent();
        while (parent != null) {
            tg = parent;
            parent = tg.getParent();
        }
        int listThreads = tg.activeCount();
        Thread list[] = new Thread[listThreads * 2];
        int gotThreads = tg.enumerate(list, true);
        for (int i = 0; i < Math.min(gotThreads, list.length); i++){
            Thread t = list[i];
            String groupName = t.getThreadGroup().getName();
            System.out.println("Thread [" + i + "] group = '" +
                               groupName +
                               "' name = '" + t.getName() +
                               "' daemon = " + t.isDaemon());
            if (groupName.startsWith("JDI ") &&
                (! t.isDaemon())) {
                failure("FAIL: non-daemon thread '" + t.getName() +
                        "' found in ThreadGroup '" + groupName + "'");
            }
        }
    }
    protected void runTests() throws Exception {
        try {
            startTo("DebuggerThreadTarg", "ready", "()V");
            dumpThreads();
            listenUntilVMDisconnect();
        } finally {
            if (!testFailed) {
                println("DebuggerThreadTest: passed");
            } else {
                throw new Exception("DebuggerThreadTest: failed");
            }
        }
        return;
    }
}

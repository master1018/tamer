public class StackTraces {
    private static Object go = new Object();
    private static Object dumpObj = new Object();
    private static String[] methodNames = {"run", "A", "B", "C", "Done"};
    private static int DONE_DEPTH = 5;
    private static boolean testFailed = false;
    private static Thread one;
    private static boolean trace = false;
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        one = new ThreadOne();
        one.start();
        Thread dt = new DumpThread();
        dt.setDaemon(true);
        dt.start();
        if (testFailed) {
            throw new RuntimeException("Test Failed.");
        }
    }
    static class DumpThread extends Thread {
        public void run() {
            int depth = 2;
            while (true) {
                try {
                    sleep(2000);
                    dumpStacks(depth);
                    depth++;
                    finishDump();
                } catch (Exception e) {
                    e.printStackTrace();
                    testFailed = true;
                }
            }
        }
    }
    static class ThreadOne extends Thread {
        public void run() {
            A();
        }
        private void A() {
            waitForDump();
            B();
        }
        private void B() {
            waitForDump();
            C();
        }
        private void C() {
            waitForDump();
            Done();
        }
        private void Done() {
            waitForDump();
            StackTraceElement[] stack = getStackTrace();
            try {
                checkStack(this, stack, DONE_DEPTH);
            } catch (Exception e) {
                e.printStackTrace();
                testFailed = true;
            }
        }
    }
    static private void waitForDump() {
        synchronized(go) {
            try {
               go.wait();
            } catch (Exception e) {
               throw new RuntimeException("Unexpected exception" + e);
            }
        }
    }
    static private void finishDump() {
        synchronized(go) {
            try {
               go.notifyAll();
            } catch (Exception e) {
               throw new RuntimeException("Unexpected exception" + e);
            }
        }
    }
    public static void dumpStacks(int depth) throws Exception {
        StackTraceElement[] stack = one.getStackTrace();
        checkStack(one, stack, depth);
        Map m = Thread.getAllStackTraces();
        Set s = m.entrySet();
        Iterator iter = s.iterator();
        Map.Entry entry;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            Thread t = (Thread) entry.getKey();
            stack = (StackTraceElement[]) entry.getValue();
            if (t == null || stack == null) {
               throw new RuntimeException("Null thread or stacktrace returned");
            }
            if (t == one) {
                checkStack(t, stack, depth);
            }
        }
    }
    private static void checkStack(Thread t, StackTraceElement[] stack,
                                   int depth) throws Exception {
        if (trace) {
            printStack(t, stack);
        }
        int frame = stack.length - 1;
        for (int i = 0; i < depth && frame >= 0; i++) {
            if (! stack[frame].getMethodName().equals(methodNames[i])) {
                throw new RuntimeException("Expected " + methodNames[i] +
                                           " in frame " + frame + " but got " +
                                           stack[frame].getMethodName());
            }
            frame--;
        }
    }
    private static void printStack(Thread t, StackTraceElement[] stack) {
        System.out.println(t +
                           " stack: (length = " + stack.length + ")");
        if (t != null) {
            for (int j = 0; j < stack.length; j++) {
                System.out.println(stack[j]);
            }
            System.out.println();
        }
    }
}

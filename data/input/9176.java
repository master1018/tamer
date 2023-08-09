public class LoggingDeadlock2 {
    public static void realMain(String arg[]) throws Throwable {
        try {
            System.out.println(javaChildArgs);
            ProcessBuilder pb = new ProcessBuilder(javaChildArgs);
            ProcessResults r = run(pb.start());
            equal(r.exitValue(), 99);
            equal(r.out(), "");
            equal(r.err(), "");
        } catch (Throwable t) { unexpected(t); }
    }
    public static class JavaChild {
        public static void main(String args[]) throws Throwable {
            final CyclicBarrier startingGate = new CyclicBarrier(2);
            final Throwable[] thrown = new Throwable[1];
            final Random rnd = new Random();
            final boolean dojoin = rnd.nextBoolean();
            final int JITTER = 1024;
            final int iters1 = rnd.nextInt(JITTER);
            final int iters2 = JITTER - iters1;
            final AtomicInteger counter = new AtomicInteger(0);
            Thread exiter = new Thread() {
                public void run() {
                    try {
                        startingGate.await();
                        for (int i = 0; i < iters1; i++)
                            counter.getAndIncrement();
                        System.exit(99);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        System.exit(86);
                    }
                }};
            exiter.start();
            startingGate.await();
            for (int i = 0; i < iters2; i++)
                counter.getAndIncrement();
            LogManager log = LogManager.getLogManager();
            if (dojoin) {
                exiter.join();
                if (thrown[0] != null)
                    throw new Error(thrown[0]);
                check(counter.get() == JITTER);
            }
        }
    }
    private static final String javaExe =
        System.getProperty("java.home") +
        File.separator + "bin" + File.separator + "java";
    private static final String classpath =
        System.getProperty("java.class.path");
    private static final List<String> javaChildArgs =
        Arrays.asList(new String[]
            { javaExe, "-classpath", classpath,
              "LoggingDeadlock2$JavaChild"});
    private static class ProcessResults {
        private final String out;
        private final String err;
        private final int exitValue;
        private final Throwable throwable;
        public ProcessResults(String out,
                              String err,
                              int exitValue,
                              Throwable throwable) {
            this.out = out;
            this.err = err;
            this.exitValue = exitValue;
            this.throwable = throwable;
        }
        public String out()          { return out; }
        public String err()          { return err; }
        public int exitValue()       { return exitValue; }
        public Throwable throwable() { return throwable; }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<STDOUT>\n" + out() + "</STDOUT>\n")
                .append("<STDERR>\n" + err() + "</STDERR>\n")
                .append("exitValue = " + exitValue + "\n");
            if (throwable != null)
                sb.append(throwable.getStackTrace());
            return sb.toString();
        }
    }
    private static class StreamAccumulator extends Thread {
        private final InputStream is;
        private final StringBuilder sb = new StringBuilder();
        private Throwable throwable = null;
        public String result () throws Throwable {
            if (throwable != null)
                throw throwable;
            return sb.toString();
        }
        StreamAccumulator (InputStream is) {
            this.is = is;
        }
        public void run() {
            try {
                Reader r = new InputStreamReader(is);
                char[] buf = new char[4096];
                int n;
                while ((n = r.read(buf)) > 0) {
                    sb.append(buf,0,n);
                }
            } catch (Throwable t) {
                throwable = t;
            } finally {
                try { is.close(); }
                catch (Throwable t) { throwable = t; }
            }
        }
    }
    private static ProcessResults run(Process p) {
        Throwable throwable = null;
        int exitValue = -1;
        String out = "";
        String err = "";
        StreamAccumulator outAccumulator =
            new StreamAccumulator(p.getInputStream());
        StreamAccumulator errAccumulator =
            new StreamAccumulator(p.getErrorStream());
        try {
            outAccumulator.start();
            errAccumulator.start();
            exitValue = p.waitFor();
            outAccumulator.join();
            errAccumulator.join();
            out = outAccumulator.result();
            err = errAccumulator.result();
        } catch (Throwable t) {
            throwable = t;
        }
        return new ProcessResults(out, err, exitValue, throwable);
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void check(boolean cond, String m) {if (cond) pass(); else fail(m);}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}

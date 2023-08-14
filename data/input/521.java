public class TimedAcquireLeak {
    static String javahome() {
        String jh = System.getProperty("java.home");
        return (jh.endsWith("jre")) ? jh.substring(0, jh.length() - 4) : jh;
    }
    static final File bin = new File(javahome(), "bin");
    static String javaProgramPath(String programName) {
        return new File(bin, programName).getPath();
    }
    static final String java = javaProgramPath("java");
    static final String jmap = javaProgramPath("jmap");
    static final String jps  = javaProgramPath("jps");
    static String outputOf(Reader r) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final char[] buf = new char[1024];
        int n;
        while ((n = r.read(buf)) > 0)
            sb.append(buf, 0, n);
        return sb.toString();
    }
    static String outputOf(InputStream is) throws IOException {
        return outputOf(new InputStreamReader(is, "UTF-8"));
    }
    static final ExecutorService drainers = Executors.newFixedThreadPool(12);
    static Future<String> futureOutputOf(final InputStream is) {
        return drainers.submit(
            new Callable<String>() { public String call() throws IOException {
                    return outputOf(is); }});}
    static String outputOf(final Process p) {
        try {
            Future<String> outputFuture = futureOutputOf(p.getInputStream());
            Future<String> errorFuture = futureOutputOf(p.getErrorStream());
            final String output = outputFuture.get();
            final String error = errorFuture.get();
            equal(error, "");
            equal(p.waitFor(), 0);
            equal(p.exitValue(), 0);
            return output;
        } catch (Throwable t) { unexpected(t); throw new Error(t); }
    }
    static String commandOutputOf(String... cmd) {
        try { return outputOf(new ProcessBuilder(cmd).start()); }
        catch (Throwable t) { unexpected(t); throw new Error(t); }
    }
    static <T> T rendezvousParent(Process p,
                                  Callable<T> callable) throws Throwable {
        p.getInputStream().read();
        T result = callable.call();
        OutputStream os = p.getOutputStream();
        os.write((byte)'\n'); os.flush();
        return result;
    }
    public static void rendezvousChild() {
        try {
            for (int i = 0; i < 100; i++) {
                System.gc(); System.runFinalization(); Thread.sleep(50);
            }
            System.out.write((byte)'\n'); System.out.flush();
            System.in.read();
        } catch (Throwable t) { throw new Error(t); }
    }
    static String match(String s, String regex, int group) {
        Matcher matcher = Pattern.compile(regex).matcher(s);
        matcher.find();
        return matcher.group(group);
    }
    static int objectsInUse(final Process child,
                            final String childPid,
                            final String className) {
        final String regex =
            "(?m)^ *[0-9]+: +([0-9]+) +[0-9]+ +\\Q"+className+"\\E$";
        final Callable<Integer> objectsInUse =
            new Callable<Integer>() { public Integer call() {
                Integer i = Integer.parseInt(
                    match(commandOutputOf(jmap, "-histo:live", childPid),
                          regex, 1));
                if (i > 100)
                    System.out.print(
                        commandOutputOf(jmap,
                                        "-dump:file=dump,format=b",
                                        childPid));
                return i;
            }};
        try { return rendezvousParent(child, objectsInUse); }
        catch (Throwable t) { unexpected(t); return -1; }
    }
    static void realMain(String[] args) throws Throwable {
        if (System.getProperty("os.name").startsWith("Windows"))
            return;
        final String childClassName = Job.class.getName();
        final String classToCheckForLeaks = Job.classToCheckForLeaks();
        final String uniqueID =
            String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
        final String[] jobCmd = {
            java, "-Xmx8m",
            "-classpath", System.getProperty("test.classes", "."),
            childClassName, uniqueID
        };
        final Process p = new ProcessBuilder(jobCmd).start();
        final String childPid =
            match(commandOutputOf(jps, "-m"),
                  "(?m)^ *([0-9]+) +\\Q"+childClassName+"\\E *"+uniqueID+"$", 1);
        final int n0 = objectsInUse(p, childPid, classToCheckForLeaks);
        final int n1 = objectsInUse(p, childPid, classToCheckForLeaks);
        equal(p.waitFor(), 0);
        equal(p.exitValue(), 0);
        failed += p.exitValue();
        System.out.printf("%d -> %d%n", n0, n1);
        check(Math.abs(n1 - n0) < 2); 
        check(n1 < 20);
        drainers.shutdown();
    }
    public static class Job {
        static String classToCheckForLeaks() {
            return
                "java.util.concurrent.locks.AbstractQueuedSynchronizer$Node";
        }
        public static void main(String[] args) throws Throwable {
            final ReentrantLock lock = new ReentrantLock();
            lock.lock();
            final ReentrantReadWriteLock rwlock
                = new ReentrantReadWriteLock();
            final ReentrantReadWriteLock.ReadLock readLock
                = rwlock.readLock();
            final ReentrantReadWriteLock.WriteLock writeLock
                = rwlock.writeLock();
            rwlock.writeLock().lock();
            final BlockingQueue<Object> q = new LinkedBlockingQueue<Object>();
            final Semaphore fairSem = new Semaphore(0, true);
            final Semaphore unfairSem = new Semaphore(0, false);
            final int threads = 3;
            final int iterations = 1 << 8;
            final CyclicBarrier cb = new CyclicBarrier(threads+1);
            for (int i = 0; i < threads; i++)
                new Thread() { public void run() {
                    try {
                        final Random rnd = new Random();
                        for (int j = 0; j < iterations; j++) {
                            if (j == iterations/10 || j == iterations - 1) {
                                cb.await(); 
                                cb.await(); 
                            }
                            int t = rnd.nextInt(900);
                            check(! lock.tryLock(t, NANOSECONDS));
                            check(! readLock.tryLock(t, NANOSECONDS));
                            check(! writeLock.tryLock(t, NANOSECONDS));
                            equal(null, q.poll(t, NANOSECONDS));
                            check(! fairSem.tryAcquire(t, NANOSECONDS));
                            check(! unfairSem.tryAcquire(t, NANOSECONDS));
                        }
                    } catch (Throwable t) { unexpected(t); }
                }}.start();
            cb.await();         
            rendezvousChild();  
            cb.await();         
            cb.await();         
            rendezvousChild();  
            cb.await();         
            System.exit(failed);
        }
        static void debugPrintf(String format, Object... args) {
            try {
                new PrintStream(new FileOutputStream("/dev/tty"))
                    .printf(format, args);
            } catch (Throwable t) { throw new Error(t); }
        }
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

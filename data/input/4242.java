public class TypeCheckMicroBenchmark {
    abstract static class Job {
        private final String name;
        Job(String name) { this.name = name; }
        String name() { return name; }
        abstract void work() throws Throwable;
    }
    private static void collectAllGarbage() {
        final java.util.concurrent.CountDownLatch drained
            = new java.util.concurrent.CountDownLatch(1);
        try {
            System.gc();        
            new Object() { protected void finalize() {
                drained.countDown(); }};
            System.gc();        
            drained.await();    
            System.gc();        
        } catch (InterruptedException e) { throw new Error(e); }
    }
    private static long[] time0(Job ... jobs) throws Throwable {
        final long warmupNanos = 10L * 1000L * 1000L * 1000L;
        long[] nanoss = new long[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            collectAllGarbage();
            long t0 = System.nanoTime();
            long t;
            int j = 0;
            do { jobs[i].work(); j++; }
            while ((t = System.nanoTime() - t0) < warmupNanos);
            nanoss[i] = t/j;
        }
        return nanoss;
    }
    private static void time(Job ... jobs) throws Throwable {
        long[] warmup = time0(jobs); 
        long[] nanoss = time0(jobs); 
        long[] milliss = new long[jobs.length];
        double[] ratios = new double[jobs.length];
        final String nameHeader   = "Method";
        final String millisHeader = "Millis";
        final String ratioHeader  = "Ratio";
        int nameWidth   = nameHeader.length();
        int millisWidth = millisHeader.length();
        int ratioWidth  = ratioHeader.length();
        for (int i = 0; i < jobs.length; i++) {
            nameWidth = Math.max(nameWidth, jobs[i].name().length());
            milliss[i] = nanoss[i]/(1000L * 1000L);
            millisWidth = Math.max(millisWidth,
                                   String.format("%d", milliss[i]).length());
            ratios[i] = (double) nanoss[i] / (double) nanoss[0];
            ratioWidth = Math.max(ratioWidth,
                                  String.format("%.3f", ratios[i]).length());
        }
        String format = String.format("%%-%ds %%%dd %%%d.3f%%n",
                                      nameWidth, millisWidth, ratioWidth);
        String headerFormat = String.format("%%-%ds %%%ds %%%ds%%n",
                                            nameWidth, millisWidth, ratioWidth);
        System.out.printf(headerFormat, "Method", "Millis", "Ratio");
        for (int i = 0; i < jobs.length; i++)
            System.out.printf(format, jobs[i].name(), milliss[i], ratios[i]);
    }
    private static String keywordValue(String[] args, String keyword) {
        for (String arg : args)
            if (arg.startsWith(keyword))
                return arg.substring(keyword.length() + 1);
        return null;
    }
    private static int intArg(String[] args, String keyword, int defaultValue) {
        String val = keywordValue(args, keyword);
        return val == null ? defaultValue : Integer.parseInt(val);
    }
    private static java.util.regex.Pattern patternArg(String[] args,
                                                      String keyword) {
        String val = keywordValue(args, keyword);
        return val == null ? null : java.util.regex.Pattern.compile(val);
    }
    private static Job[] filter(java.util.regex.Pattern filter,
                                Job[] jobs) {
        if (filter == null) return jobs;
        Job[] newJobs = new Job[jobs.length];
        int n = 0;
        for (Job job : jobs)
            if (filter.matcher(job.name()).find())
                newJobs[n++] = job;
        Job[] ret = new Job[n];
        System.arraycopy(newJobs, 0, ret, 0, n);
        return ret;
    }
    public static void main(String[] args) throws Throwable {
        final int iterations = intArg(args, "iterations", 30000);
        final int size       = intArg(args, "size", 1000);
        final java.util.regex.Pattern filter
            = patternArg(args, "filter");
        final List<Integer> list = new ArrayList<Integer>();
        final Random rnd = new Random();
        for (int i = 0; i < size; i++)
            list.add(rnd.nextInt());
        final Class klazz = Integer.class;
        final Job[] jobs = {
            new Job("toArray(T[])") { void work() {
                Object[] a = new Integer[0];
                for (int i = 0; i < iterations; i++) {
                    try { list.toArray(a); }
                    catch (ArrayStoreException ase) {
                        throw new ClassCastException(); }}}},
            new Job("isInstance") { void work() {
                for (int i = 0; i < iterations; i++) {
                    for (Object x : list.toArray())
                        if (! (x != null && klazz.isInstance(x)))
                            throw new ClassCastException(); }}},
            new Job("Class.cast") { void work() {
                for (int i = 0; i < iterations; i++) {
                    for (Object x : list.toArray())
                        klazz.cast(x); }}},
            new Job("write into array") { void work() {
                Object[] a = new Integer[1];
                for (int i = 0; i < iterations; i++) {
                    for (Object x : list.toArray()) {
                        try { a[0] = x; }
                        catch (ArrayStoreException _) {
                            throw new ClassCastException(); }}}}},
            new Job("write into dynamic array") { void work() {
                for (int i = 0; i < iterations; i++) {
                    for (Object x : list.toArray()) {
                        Object[] a = (Object[])
                            java.lang.reflect.Array.newInstance(klazz, 1);
                        try { a[0] = x; }
                        catch (ArrayStoreException _) {
                            throw new ClassCastException(); }}}}}
        };
        time(filter(filter, jobs));
    }
}

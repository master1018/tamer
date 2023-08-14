public class SamplingProfilerIntegration {
    private static final String TAG = "SamplingProfilerIntegration";
    private static final boolean enabled;
    private static final Executor snapshotWriter;
    static {
        enabled = "1".equals(SystemProperties.get("persist.sampling_profiler"));
        if (enabled) {
            snapshotWriter = Executors.newSingleThreadExecutor();
            Log.i(TAG, "Profiler is enabled.");
        } else {
            snapshotWriter = null;
            Log.i(TAG, "Profiler is disabled.");
        }
    }
    public static boolean isEnabled() {
        return enabled;
    }
    public static void start() {
        if (!enabled) return;
        SamplingProfiler.getInstance().start(10);
    }
    static boolean dirMade = false;
    static volatile boolean pending;
    public static void writeSnapshot(final String name) {
        if (!enabled) return;
        if (!pending) {
            pending = true;
            snapshotWriter.execute(new Runnable() {
                public void run() {
                    String dir = "/sdcard/snapshots";
                    if (!dirMade) {
                        new File(dir).mkdirs();
                        if (new File(dir).isDirectory()) {
                            dirMade = true;
                        } else {
                            Log.w(TAG, "Creation of " + dir + " failed.");
                            return;
                        }
                    }
                    try {
                        writeSnapshot(dir, name);
                    } finally {
                        pending = false;
                    }
                }
            });
        }
    }
    public static void writeZygoteSnapshot() {
        if (!enabled) return;
        String dir = "/data/zygote/snapshots";
        new File(dir).mkdirs();
        writeSnapshot(dir, "zygote");
    }
    private static void writeSnapshot(String dir, String name) {
        byte[] snapshot = SamplingProfiler.getInstance().snapshot();
        if (snapshot == null) {
            return;
        }
        long start = System.currentTimeMillis();
        String path = dir + "/" + name.replace(':', '.') + "-" +
                + System.currentTimeMillis() + ".snapshot";
        try {
            FileOutputStream out;
            int count = 0;
            while (true) {
                try {
                    out = new FileOutputStream(path);
                    break;
                } catch (FileNotFoundException e) {
                    if (++count > 3) {
                        Log.e(TAG, "Could not open " + path + ".");
                        return;
                    }
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e1) {  }
                }
            }
            try {
                out.write(snapshot);
            } finally {
                out.close();
            }
            long elapsed = System.currentTimeMillis() - start;
            Log.i(TAG, "Wrote snapshot for " + name
                    + " in " + elapsed + "ms.");
        } catch (IOException e) {
            Log.e(TAG, "Error writing snapshot.", e);
        }
    }
}

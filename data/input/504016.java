class DeviceMonitor {
    private static final String LOG_TAG = DeviceMonitor.class.getName();
    private static final int SAMPLE_COUNT = 10;
    private static final int INTERVAL = 1000;
    private static final int MAX_FILES = 30;
    private final byte[] buffer = new byte[1024];
    private boolean running = false;
    private DeviceMonitor() {
        new Thread() {
            public void run() {
                monitor();
            }
        }.start();
    }
    @SuppressWarnings("InfiniteLoopStatement")
    private void monitor() {
        while (true) {
            waitForStart();
            purge();
            for (int i = 0; i < SAMPLE_COUNT; i++) {
                try {
                    dump();
                } catch (IOException e) {
                    Slog.w(LOG_TAG, "Dump failed.", e);
                }
                pause();
            }
            stop();
        }
    }
    private static final File PROC = new File("/proc");
    private static final File BASE = new File("/data/anr/");
    static {
        if (!BASE.isDirectory() && !BASE.mkdirs()) {
            throw new AssertionError("Couldn't create " + BASE + ".");
        }
    }
    private static final File[] PATHS = {
        new File(PROC, "zoneinfo"),
        new File(PROC, "interrupts"),
        new File(PROC, "meminfo"),
        new File(PROC, "slabinfo"),
    };
    private void purge() {
        File[] files = BASE.listFiles();
        int count = files.length - MAX_FILES;
        if (count > 0) {
            Arrays.sort(files);
            for (int i = 0; i < count; i++) {
                if (!files[i].delete()) {
                    Slog.w(LOG_TAG, "Couldn't delete " + files[i] + ".");
                }
            }
        }
    }
    private void dump() throws IOException {
        OutputStream out = new FileOutputStream(
                new File(BASE, String.valueOf(System.currentTimeMillis())));
        try {
            for (File processDirectory : PROC.listFiles()) {
                if (isProcessDirectory(processDirectory)) {
                    dump(new File(processDirectory, "stat"), out);
                }
            }
            for (File file : PATHS) {
                dump(file, out);
            }
        } finally {
            closeQuietly(out);
        }
    }
    private static boolean isProcessDirectory(File file) {
        try {
            Integer.parseInt(file.getName());
            return file.isDirectory();
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private void dump(File from, OutputStream out) throws IOException {
        writeHeader(from, out);
        FileInputStream in = null;
        try {
            in = new FileInputStream(from);
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        } finally {
            closeQuietly(in);
        }
    }
    private static void writeHeader(File file, OutputStream out)
            throws IOException {
        String header = "*** " + file.toString() + "\n";
        out.write(header.getBytes());
    }
    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            Slog.w(LOG_TAG, e);
        }
    }
    private void pause() {
        try {
            Thread.sleep(INTERVAL);
        } catch (InterruptedException e) {  }
    }
    private synchronized void stop() {
        running = false;        
    }
    private synchronized void waitForStart() {
        while (!running) {
            try {
                wait();
            } catch (InterruptedException e) {  }
        }
    }
    private synchronized void startMonitoring() {
        if (!running) {
            running = true;
            notifyAll();
        }
    }
    private static DeviceMonitor instance = new DeviceMonitor();
    static void start() {
        instance.startMonitoring();
    }
}

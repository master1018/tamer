public class SamplingProfiler {
    private static final Logger logger = Logger.getLogger(
            SamplingProfiler.class.getName());
    static final boolean DEBUG = false;
    enum State {
        PAUSED,
        RUNNING,
        SHUTTING_DOWN
    }
    int pointer = 0;
    Thread samplingThread;
    volatile int delay; 
    int totalThreadsSampled = 0;
    long totalSampleTime = 0;
    volatile State state = State.PAUSED;
    private SamplingProfiler() {}
    public boolean isRunning() {
        return state == State.RUNNING;
    }
    public synchronized void start(int samplesPerSecond) {
        if (samplesPerSecond < 1) {
            throw new IllegalArgumentException("samplesPerSecond < 1");
        }
        ensureNotShuttingDown();
        delay = 1000 / samplesPerSecond;
        if (!isRunning()) {
            if (DEBUG) logger.info("Starting profiler...");
            state = State.RUNNING;
            if (samplingThread == null) {
                samplingThread = new Thread(new Sampler(), "SamplingProfiler");
                samplingThread.setDaemon(true);
                samplingThread.start();
            } else {
                notifyAll();
            }
        }
    }
    public synchronized void pause() {
        if (isRunning()) {
            if (DEBUG) logger.info("Pausing profiler...");
            state = State.PAUSED;
        }
    }
    public synchronized byte[] snapshot() {
        ensureNotShuttingDown();
        if (pointer == 0 || totalThreadsSampled == 0) {
            return null;
        }
        if (DEBUG) {
            int size = size(pointer);
            int collisions = collisions(pointer);
            long start = System.nanoTime();
            byte[] bytes = snapshot(pointer);
            long elapsed = System.nanoTime() - start;
            long averageSampleTime = ((totalSampleTime / totalThreadsSampled)
                    << 10) / 1000;
            logger.info("Grabbed snapshot in " + (elapsed / 1000) + "us."
                    + " Samples collected: " + totalThreadsSampled
                    + ", Average sample time (per thread): "
                            + averageSampleTime + "us"
                    + ", Set size: " + size
                    + ", Collisions: " + collisions);
            totalThreadsSampled = 0;
            totalSampleTime = 0;
            return bytes;
        } else {
            totalThreadsSampled = 0;
            return snapshot(pointer);
        }
    }
    public synchronized void setEventThread(Thread eventThread) {
        ensureNotShuttingDown();
        if (pointer == 0) {
            pointer = allocate();
        }
        setEventThread(pointer, eventThread);
    }
    private void ensureNotShuttingDown() {
        if (state == State.SHUTTING_DOWN) {
            throw new IllegalStateException("Profiler is shutting down.");
        }
    }
    public void shutDown() {
        Thread toStop;
        synchronized (this) {
            ensureNotShuttingDown();
            toStop = samplingThread;
            if (toStop == null) {
                throw new IllegalStateException(
                        "The profiler was never started.");
            }
            state = State.SHUTTING_DOWN;
            samplingThread = null;
            notifyAll();
        }
        toStop.interrupt();
        while (true) {
            try {
                toStop.join();
                break;
            } catch (InterruptedException e) {  }
        }
        synchronized (this) {
            if (pointer != 0) {
                free(pointer);
                pointer = 0;
            }
            totalThreadsSampled = 0;
            totalSampleTime = 0;
            state = State.PAUSED;
        }
    }
    private static native int sample(int pointer);
    private static native int allocate();
    private static native void free(int pointer);
    private static native int size(int pointer);
    private static native int collisions(int pointer);
    private static native byte[] snapshot(int pointer);
    private static native void setEventThread(int pointer, Thread thread);
    class Sampler implements Runnable {
        public void run() {
            boolean firstSample = true;
            while (true) {
                synchronized (SamplingProfiler.this) {
                    if (!isRunning()) {
                        if (DEBUG) logger.info("Paused profiler.");
                        while (!isRunning()) {
                            if (state == State.SHUTTING_DOWN) {
                                return;
                            }
                            try {
                                SamplingProfiler.this.wait();
                            } catch (InterruptedException e) {  }
                        }
                        firstSample = true;
                    }
                    if (pointer == 0) {
                        pointer = allocate();
                    }
                    if (firstSample) {
                        if (DEBUG) logger.info("Started profiler.");
                        firstSample = false;
                    }
                    if (DEBUG) {
                        long start = System.nanoTime();
                        int threadsSampled = sample(pointer);
                        long elapsed = System.nanoTime() - start;
                        totalThreadsSampled += threadsSampled;
                        totalSampleTime += elapsed >> 10; 
                    } else {
                        totalThreadsSampled += sample(pointer);
                    }
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {  }
            }
        }
    }
    public static void logSnapshot(byte[] snapshot) {
        DataInputStream in = new DataInputStream(
                new ByteArrayInputStream(snapshot));
        try {
            int version = in.readUnsignedShort();
            int classCount = in.readUnsignedShort();
            StringBuilder sb = new StringBuilder();
            sb.append("version=").append(version).append(' ')
                    .append("classes=").append(classCount).append('\n');
            logger.info(sb.toString());
            for (int i = 0; i < classCount; i++) {
                sb = new StringBuilder();
                sb.append("class ").append(in.readUTF()).append('\n');
                int methodCount = in.readUnsignedShort();
                for (int m = 0; m < methodCount; m++) {
                    sb.append("  ").append(in.readUTF()).append(":\n");
                    sb.append("    event:\n");
                    appendCounts(in, sb);
                    sb.append("    other:\n");
                    appendCounts(in, sb);
                }
                logger.info(sb.toString());
            }
        } catch (IOException e) {
            logger.warning(e.toString());
        }
    }
    private static void appendCounts(DataInputStream in, StringBuilder sb)
            throws IOException {
        sb.append("      running:\n");
        sb.append("        caller: ").append(in.readShort()).append('\n');
        sb.append("        leaf: ").append(in.readShort()).append('\n');
        sb.append("      suspended:\n");
        sb.append("        caller: ").append(in.readShort()).append('\n');
        sb.append("        leaf: ").append(in.readShort()).append('\n');
    }
    private static final SamplingProfiler instance = new SamplingProfiler();
    public static synchronized SamplingProfiler getInstance() {
        return instance;
    }
}

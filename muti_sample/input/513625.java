public class EntropyService extends Binder {
    private static final String TAG = "EntropyService";
    private static final int ENTROPY_WHAT = 1;
    private static final int ENTROPY_WRITE_PERIOD = 3 * 60 * 60 * 1000;  
    private static final long START_TIME = System.currentTimeMillis();
    private static final long START_NANOTIME = System.nanoTime();
    private final String randomDevice;
    private final String entropyFile;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != ENTROPY_WHAT) {
                Slog.e(TAG, "Will not process invalid message");
                return;
            }
            writeEntropy();
            scheduleEntropyWriter();
        }
    };
    public EntropyService() {
        this(getSystemDir() + "/entropy.dat", "/dev/urandom");
    }
    public EntropyService(String entropyFile, String randomDevice) {
        if (randomDevice == null) { throw new NullPointerException("randomDevice"); }
        if (entropyFile == null) { throw new NullPointerException("entropyFile"); }
        this.randomDevice = randomDevice;
        this.entropyFile = entropyFile;
        loadInitialEntropy();
        addDeviceSpecificEntropy();
        writeEntropy();
        scheduleEntropyWriter();
    }
    private void scheduleEntropyWriter() {
        mHandler.removeMessages(ENTROPY_WHAT);
        mHandler.sendEmptyMessageDelayed(ENTROPY_WHAT, ENTROPY_WRITE_PERIOD);
    }
    private void loadInitialEntropy() {
        try {
            RandomBlock.fromFile(entropyFile).toFile(randomDevice);
        } catch (IOException e) {
            Slog.w(TAG, "unable to load initial entropy (first boot?)", e);
        }
    }
    private void writeEntropy() {
        try {
            RandomBlock.fromFile(randomDevice).toFile(entropyFile);
        } catch (IOException e) {
            Slog.w(TAG, "unable to write entropy", e);
        }
    }
    private void addDeviceSpecificEntropy() {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileOutputStream(randomDevice));
            out.println("Copyright (C) 2009 The Android Open Source Project");
            out.println("All Your Randomness Are Belong To Us");
            out.println(START_TIME);
            out.println(START_NANOTIME);
            out.println(SystemProperties.get("ro.serialno"));
            out.println(SystemProperties.get("ro.bootmode"));
            out.println(SystemProperties.get("ro.baseband"));
            out.println(SystemProperties.get("ro.carrier"));
            out.println(SystemProperties.get("ro.bootloader"));
            out.println(SystemProperties.get("ro.hardware"));
            out.println(SystemProperties.get("ro.revision"));
            out.println(System.currentTimeMillis());
            out.println(System.nanoTime());
        } catch (IOException e) {
            Slog.w(TAG, "Unable to add device specific data to the entropy pool", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    private static String getSystemDir() {
        File dataDir = Environment.getDataDirectory();
        File systemDir = new File(dataDir, "system");
        systemDir.mkdirs();
        return systemDir.toString();
    }
}

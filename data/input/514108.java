public class RecognizerLogger {
    private static final String TAG = "RecognizerLogger";
    private static final String LOGDIR = "logdir";
    private static final String ENABLED = "enabled";
    private static final int MAX_FILES = 20;
    private final String mDatedPath;
    private final BufferedWriter mWriter;
    public static boolean isEnabled(Context context) {
        File dir = context.getDir(LOGDIR, 0);
        File enabled = new File(dir, ENABLED);
        return enabled.exists();
    }
    public static void enable(Context context) {
        try {
            File dir = context.getDir(LOGDIR, 0);
            File enabled = new File(dir, ENABLED);
            enabled.createNewFile();
        }
        catch (IOException e) {
            Log.e(TAG, "enableLogging " + e);
        }
    }
    public static void disable(Context context) {
        try {
            File dir = context.getDir(LOGDIR, 0);
            File enabled = new File(dir, ENABLED);
            enabled.delete();
        }
        catch (SecurityException e) {
            Log.e(TAG, "disableLogging " + e);
        }
    }
    public RecognizerLogger(Context context) throws IOException {
        if (Config.LOGD) Log.d(TAG, "RecognizerLogger");
        File dir = context.getDir(LOGDIR, 0);
        mDatedPath = dir.toString() + File.separator + "log_" +
                DateFormat.format("yyyy_MM_dd_kk_mm_ss",
                        System.currentTimeMillis());
        deleteOldest(".wav");
        deleteOldest(".log");
        mWriter = new BufferedWriter(new FileWriter(mDatedPath + ".log"), 8192);
        mWriter.write(Build.FINGERPRINT);
        mWriter.newLine();
    }
    public void logLine(String msg) {
        try {
            mWriter.write(msg);
            mWriter.newLine();
        }
        catch (IOException e) {
            Log.e(TAG, "logLine exception: " + e);
        }
    }
    public void logNbestHeader() {
        logLine("Nbest *****************");
    }
    public void logContacts(List<VoiceContact> contacts) {
        logLine("Contacts *****************");
        for (VoiceContact vc : contacts) logLine(vc.toString());
        try {
            mWriter.flush();
        }
        catch (IOException e) {
            Log.e(TAG, "logContacts exception: " + e);
        }
    }
    public void logIntents(ArrayList<Intent> intents) {
        logLine("Intents *********************");
        StringBuffer sb = new StringBuffer();
        for (Intent intent : intents) {
            logLine(intent.toString() + " " + RecognizerEngine.SENTENCE_EXTRA + "=" +
                    intent.getStringExtra(RecognizerEngine.SENTENCE_EXTRA));
        }
        try {
            mWriter.flush();
        }
        catch (IOException e) {
            Log.e(TAG, "logIntents exception: " + e);
        }
    }
    public void close() throws IOException {
        mWriter.close();
    }
    private void deleteOldest(final String suffix) {
        FileFilter ff = new FileFilter() {
            public boolean accept(File f) {
                String name = f.getName();
                return name.startsWith("log_") && name.endsWith(suffix);
            }
        };
        File[] files = (new File(mDatedPath)).getParentFile().listFiles(ff);
        Arrays.sort(files);
        for (int i = 0; i < files.length - MAX_FILES; i++) {
            files[i].delete();            
        }
    }
    public InputStream logInputStream(final InputStream inputStream, final int sampleRate) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(sampleRate * 2 * 20);
        return new InputStream() {
            public int available() throws IOException {
                return inputStream.available();
            }
            public int read(byte[] b, int offset, int length) throws IOException {
                int rtn = inputStream.read(b, offset, length);
                if (rtn > 0) baos.write(b, offset, rtn);
                return rtn;
            }
            public int read(byte[] b) throws IOException {
                int rtn = inputStream.read(b);
                if (rtn > 0) baos.write(b, 0, rtn);
                return rtn;
            }
            public int read() throws IOException {
                int rtn = inputStream.read();
                if (rtn > 0) baos.write(rtn);
                return rtn;
            }
            public long skip(long n) throws IOException {
                throw new UnsupportedOperationException();
            }
            public void close() throws IOException {
                try {
                    OutputStream out = new FileOutputStream(mDatedPath + ".wav");
                    try {
                        byte[] pcm = baos.toByteArray();
                        WaveHeader hdr = new WaveHeader(WaveHeader.FORMAT_PCM,
                                (short)1, sampleRate, (short)16, pcm.length);
                        hdr.write(out);
                        out.write(pcm);
                    }
                    finally {
                        out.close();
                    }
                }
                finally {
                    inputStream.close();
                    baos.close();
                }
            }
        };
    }
}

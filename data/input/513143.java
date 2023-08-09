public class LoggingInputStream extends FilterInputStream {
    private StringBuilder mSb;
    private boolean mDumpEmptyLines;
    private final String mTag;
    public LoggingInputStream(InputStream in) {
        this(in, "RAW", false);
    }
    public LoggingInputStream(InputStream in, String tag, boolean dumpEmptyLines) {
        super(in);
        mTag = tag + " ";
        mDumpEmptyLines = dumpEmptyLines;
        initBuffer();
        Log.d(Email.LOG_TAG, mTag + "dump start");
    }
    private void initBuffer() {
        mSb = new StringBuilder(mTag);
    }
    @Override
    public int read() throws IOException {
        int oneByte = super.read();
        logRaw(oneByte);
        return oneByte;
    }
    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        int bytesRead = super.read(b, offset, length);
        int copyBytes = bytesRead;
        while (copyBytes > 0) {
            logRaw(b[offset] & 0xFF);
            copyBytes--;
            offset++;
        }
        return bytesRead;
    }
    private void logRaw(int oneByte) {
        if (oneByte == '\r') {
        } else if (oneByte == '\n') {
            flushLog();
        } else if (0x20 <= oneByte && oneByte <= 0x7e) { 
            mSb.append((char)oneByte);
        } else {
            mSb.append("\\x" + Utility.byteToHex(oneByte));
        }
    }
    private void flushLog() {
        if (mDumpEmptyLines || (mSb.length() > mTag.length())) {
            Log.d(Email.LOG_TAG, mSb.toString());
            initBuffer();
        }
    }
    @Override
    public void close() throws IOException {
        super.close();
        flushLog();
    }
}

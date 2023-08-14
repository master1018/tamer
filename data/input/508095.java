public class StatusOutputStream extends FilterOutputStream {
    private long mCount = 0;
    public StatusOutputStream(OutputStream out) {
        super(out);
    }
    @Override
    public void write(int oneByte) throws IOException {
        super.write(oneByte);
        mCount++;
        if (Email.LOGD) {
            if (mCount % 1024 == 0) {
                Log.v(Email.LOG_TAG, "# " + mCount);
            }
        }
    }
}

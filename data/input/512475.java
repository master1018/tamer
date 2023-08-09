public final class MicrophoneInputStream extends InputStream {
    static {
        System.loadLibrary("srec_jni");
    }
    private final static String TAG = "MicrophoneInputStream";
    private int mAudioRecord = 0;
    private byte[] mOneByte = new byte[1];
    public MicrophoneInputStream(int sampleRate, int fifoDepth) throws IOException {
        mAudioRecord = AudioRecordNew(sampleRate, fifoDepth);
        if (mAudioRecord == 0) throw new IOException("AudioRecord constructor failed - busy?");
        int status = AudioRecordStart(mAudioRecord);
        if (status != 0) {
            close();
            throw new IOException("AudioRecord start failed: " + status);
        }
    }
    @Override
    public int read() throws IOException {
        if (mAudioRecord == 0) throw new IllegalStateException("not open");
        int rtn = AudioRecordRead(mAudioRecord, mOneByte, 0, 1);
        return rtn == 1 ? ((int)mOneByte[0] & 0xff) : -1;
    }
    @Override
    public int read(byte[] b) throws IOException {
        if (mAudioRecord == 0) throw new IllegalStateException("not open");
        return AudioRecordRead(mAudioRecord, b, 0, b.length);
    }
    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        if (mAudioRecord == 0) throw new IllegalStateException("not open");
        return AudioRecordRead(mAudioRecord, b, offset, length);
    }
    @Override
    public void close() throws IOException {
        if (mAudioRecord != 0) {
            try {
                AudioRecordStop(mAudioRecord);
            } finally {
                try {
                    AudioRecordDelete(mAudioRecord);
                } finally {
                    mAudioRecord = 0;
                }
            }
        }
    }
    @Override
    protected void finalize() throws Throwable {
        if (mAudioRecord != 0) {
            close();
            throw new IOException("someone forgot to close MicrophoneInputStream");
        }
    }
    private static native int AudioRecordNew(int sampleRate, int fifoDepth);
    private static native int AudioRecordStart(int audioRecord);
    private static native int AudioRecordRead(int audioRecord, byte[] b, int offset, int length) throws IOException;
    private static native void AudioRecordStop(int audioRecord) throws IOException;
    private static native void AudioRecordDelete(int audioRecord) throws IOException;
}

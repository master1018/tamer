public class WaveHeader {
    private static final String TAG = "WaveHeader";
    private static final int HEADER_LENGTH = 44;
    public static final short FORMAT_PCM = 1;
    public static final short FORMAT_ALAW = 6;
    public static final short FORMAT_ULAW = 7;
    private short mFormat;
    private short mNumChannels;
    private int mSampleRate;
    private short mBitsPerSample;
    private int mNumBytes;
    public WaveHeader() {
    }
    public WaveHeader(short format, short numChannels, int sampleRate, short bitsPerSample, int numBytes) {
        mFormat = format;
        mSampleRate = sampleRate;
        mNumChannels = numChannels;
        mBitsPerSample = bitsPerSample;
        mNumBytes = numBytes;
    }
    public short getFormat() {
        return mFormat;
    }
    public WaveHeader setFormat(short format) {
        mFormat = format;
        return this;
    }
    public short getNumChannels() {
        return mNumChannels;
    }
    public WaveHeader setNumChannels(short numChannels) {
        mNumChannels = numChannels;
        return this;
    }
    public int getSampleRate() {
        return mSampleRate;
    }
    public WaveHeader setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
        return this;
    }
    public short getBitsPerSample() {
        return mBitsPerSample;
    }
    public WaveHeader setBitsPerSample(short bitsPerSample) {
        mBitsPerSample = bitsPerSample;
        return this;
    }
    public int getNumBytes() {
        return mNumBytes;
    }
    public WaveHeader setNumBytes(int numBytes) {
        mNumBytes = numBytes;
        return this;
    }
    public int read(InputStream in) throws IOException {
        readId(in, "RIFF");
        int numBytes = readInt(in) - 36;
        readId(in, "WAVE");
        readId(in, "fmt ");
        if (16 != readInt(in)) throw new IOException("fmt chunk length not 16");
        mFormat = readShort(in);
        mNumChannels = readShort(in);
        mSampleRate = readInt(in);
        int byteRate = readInt(in);
        short blockAlign = readShort(in);
        mBitsPerSample = readShort(in);
        if (byteRate != mNumChannels * mSampleRate * mBitsPerSample / 8) {
            throw new IOException("fmt.ByteRate field inconsistent");
        }
        if (blockAlign != mNumChannels * mBitsPerSample / 8) {
            throw new IOException("fmt.BlockAlign field inconsistent");
        }
        readId(in, "data");
        mNumBytes = readInt(in);
        return HEADER_LENGTH;
    }
    private static void readId(InputStream in, String id) throws IOException {
        for (int i = 0; i < id.length(); i++) {
            if (id.charAt(i) != in.read()) throw new IOException( id + " tag not present");
        }
    }
    private static int readInt(InputStream in) throws IOException {
        return in.read() | (in.read() << 8) | (in.read() << 16) | (in.read() << 24);
    }
    private static short readShort(InputStream in) throws IOException {
        return (short)(in.read() | (in.read() << 8));
    }
    public int write(OutputStream out) throws IOException {
        writeId(out, "RIFF");
        writeInt(out, 36 + mNumBytes);
        writeId(out, "WAVE");
        writeId(out, "fmt ");
        writeInt(out, 16);
        writeShort(out, mFormat);
        writeShort(out, mNumChannels);
        writeInt(out, mSampleRate);
        writeInt(out, mNumChannels * mSampleRate * mBitsPerSample / 8);
        writeShort(out, (short)(mNumChannels * mBitsPerSample / 8));
        writeShort(out, mBitsPerSample);
        writeId(out, "data");
        writeInt(out, mNumBytes);
        return HEADER_LENGTH;
    }
    private static void writeId(OutputStream out, String id) throws IOException {
        for (int i = 0; i < id.length(); i++) out.write(id.charAt(i));
    }
    private static void writeInt(OutputStream out, int val) throws IOException {
        out.write(val >> 0);
        out.write(val >> 8);
        out.write(val >> 16);
        out.write(val >> 24);
    }
    private static void writeShort(OutputStream out, short val) throws IOException {
        out.write(val >> 0);
        out.write(val >> 8);
    }
    @Override
    public String toString() {
        return String.format(
                "WaveHeader format=%d numChannels=%d sampleRate=%d bitsPerSample=%d numBytes=%d",
                mFormat, mNumChannels, mSampleRate, mBitsPerSample, mNumBytes);
    }
}

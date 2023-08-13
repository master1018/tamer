public class SF2Sample extends SoundbankResource {
    protected String name = "";
    protected long startLoop = 0;
    protected long endLoop = 0;
    protected long sampleRate = 44100;
    protected int originalPitch = 60;
    protected byte pitchCorrection = 0;
    protected int sampleLink = 0;
    protected int sampleType = 0;
    protected ModelByteBuffer data;
    protected ModelByteBuffer data24;
    public SF2Sample(Soundbank soundBank) {
        super(soundBank, null, AudioInputStream.class);
    }
    public SF2Sample() {
        super(null, null, AudioInputStream.class);
    }
    public Object getData() {
        AudioFormat format = getFormat();
        InputStream is = data.getInputStream();
        if (is == null)
            return null;
        return new AudioInputStream(is, format, data.capacity());
    }
    public ModelByteBuffer getDataBuffer() {
        return data;
    }
    public ModelByteBuffer getData24Buffer() {
        return data24;
    }
    public AudioFormat getFormat() {
        return new AudioFormat(sampleRate, 16, 1, true, false);
    }
    public void setData(ModelByteBuffer data) {
        this.data = data;
    }
    public void setData(byte[] data) {
        this.data = new ModelByteBuffer(data);
    }
    public void setData(byte[] data, int offset, int length) {
        this.data = new ModelByteBuffer(data, offset, length);
    }
    public void setData24(ModelByteBuffer data24) {
        this.data24 = data24;
    }
    public void setData24(byte[] data24) {
        this.data24 = new ModelByteBuffer(data24);
    }
    public void setData24(byte[] data24, int offset, int length) {
        this.data24 = new ModelByteBuffer(data24, offset, length);
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getEndLoop() {
        return endLoop;
    }
    public void setEndLoop(long endLoop) {
        this.endLoop = endLoop;
    }
    public int getOriginalPitch() {
        return originalPitch;
    }
    public void setOriginalPitch(int originalPitch) {
        this.originalPitch = originalPitch;
    }
    public byte getPitchCorrection() {
        return pitchCorrection;
    }
    public void setPitchCorrection(byte pitchCorrection) {
        this.pitchCorrection = pitchCorrection;
    }
    public int getSampleLink() {
        return sampleLink;
    }
    public void setSampleLink(int sampleLink) {
        this.sampleLink = sampleLink;
    }
    public long getSampleRate() {
        return sampleRate;
    }
    public void setSampleRate(long sampleRate) {
        this.sampleRate = sampleRate;
    }
    public int getSampleType() {
        return sampleType;
    }
    public void setSampleType(int sampleType) {
        this.sampleType = sampleType;
    }
    public long getStartLoop() {
        return startLoop;
    }
    public void setStartLoop(long startLoop) {
        this.startLoop = startLoop;
    }
    public String toString() {
        return "Sample: " + name;
    }
}

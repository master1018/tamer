public class DLSSample extends SoundbankResource {
    protected byte[] guid = null;
    protected DLSInfo info = new DLSInfo();
    protected DLSSampleOptions sampleoptions;
    protected ModelByteBuffer data;
    protected AudioFormat format;
    public DLSSample(Soundbank soundBank) {
        super(soundBank, null, AudioInputStream.class);
    }
    public DLSSample() {
        super(null, null, AudioInputStream.class);
    }
    public DLSInfo getInfo() {
        return info;
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
    public AudioFormat getFormat() {
        return format;
    }
    public void setFormat(AudioFormat format) {
        this.format = format;
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
    public String getName() {
        return info.name;
    }
    public void setName(String name) {
        info.name = name;
    }
    public DLSSampleOptions getSampleoptions() {
        return sampleoptions;
    }
    public void setSampleoptions(DLSSampleOptions sampleOptions) {
        this.sampleoptions = sampleOptions;
    }
    public String toString() {
        return "Sample: " + info.name;
    }
    public byte[] getGuid() {
        return guid == null ? null : Arrays.copyOf(guid, guid.length);
    }
    public void setGuid(byte[] guid) {
        this.guid = guid == null ? null : Arrays.copyOf(guid, guid.length);
    }
}

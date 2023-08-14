public class MidiFileFormat {
    public static final int UNKNOWN_LENGTH = -1;
    protected int type;
    protected float divisionType;
    protected int resolution;
    protected int byteLength;
    protected long microsecondLength;
    private HashMap<String, Object> properties;
    public MidiFileFormat(int type, float divisionType, int resolution, int bytes, long microseconds) {
        this.type = type;
        this.divisionType = divisionType;
        this.resolution = resolution;
        this.byteLength = bytes;
        this.microsecondLength = microseconds;
        this.properties = null;
    }
    public MidiFileFormat(int type, float divisionType,
                          int resolution, int bytes,
                          long microseconds, Map<String, Object> properties) {
        this(type, divisionType, resolution, bytes, microseconds);
        this.properties = new HashMap<String, Object>(properties);
    }
    public int getType() {
        return type;
    }
    public float getDivisionType() {
        return divisionType;
    }
    public int getResolution() {
        return resolution;
    }
    public int getByteLength() {
        return byteLength;
    }
    public long getMicrosecondLength() {
        return microsecondLength;
    }
    public Map<String,Object> properties() {
        Map<String,Object> ret;
        if (properties == null) {
            ret = new HashMap<String,Object>(0);
        } else {
            ret = (Map<String,Object>) (properties.clone());
        }
        return (Map<String,Object>) Collections.unmodifiableMap(ret);
    }
    public Object getProperty(String key) {
        if (properties == null) {
            return null;
        }
        return properties.get(key);
    }
}

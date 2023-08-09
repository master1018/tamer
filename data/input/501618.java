public class AudioFileFormat {
    private AudioFileFormat.Type type;
    private int byteLength = AudioSystem.NOT_SPECIFIED;
    private AudioFormat format;
    private int frameLength;
    private HashMap<String, Object> prop;
    protected AudioFileFormat(AudioFileFormat.Type type,
            int byteLength,
            AudioFormat format,
            int frameLength) {
        this.type = type;
        this.byteLength = byteLength;
        this.format = format;
        this.frameLength = frameLength;
    }
    public AudioFileFormat(AudioFileFormat.Type type,
            AudioFormat format,
            int frameLength) {
        this.type = type;
        this.format = format;
        this.frameLength = frameLength;
    }
    public AudioFileFormat(AudioFileFormat.Type type,
            AudioFormat format,
            int frameLength,
            Map<String,Object> properties) {
        this.type = type;
        this.format = format;
        this.frameLength = frameLength;
        prop = new HashMap<String, Object>();
        prop.putAll(properties);
    }
    public AudioFileFormat.Type getType() {
        return type;
    }
    public int getByteLength() {
        return byteLength;
    }
    public AudioFormat getFormat() {
        return format;
    }
    public int getFrameLength() {
        return frameLength;
    }
    public Map<String,Object> properties() {
        if (prop == null) {
            return null;
        }
        return Collections.unmodifiableMap(prop);
    }
    public Object getProperty(String key) {
        if (prop == null) {
            return null;
        }
        return prop.get(key);
    }
    public String toString() {
        return type + " (." + type.getExtension() + ") file, data format: " + format + 
            " frame length: " + frameLength; 
    }
    public static class Type {
        private String name;
        private String extension;
        public static final Type AIFC = new Type("AIFF-C", "aifc"); 
        public static final Type AIFF = new Type("AIFF", "aif"); 
        public static final Type AU = new Type("AU", "au"); 
        public static final Type SND = new Type("SND", "snd"); 
        public static final Type WAVE = new Type("WAVE", "wav"); 
        public Type(String name, String extension) {
            this.name = name;
            this.extension = extension;
        }
        @Override
        public final boolean equals(Object another) {
            if (this == another) {
                return true;
            }
            if (another == null || !(another instanceof Type)) {
                return false;
            }
            Type obj = (Type) another;
            return (name == null ? obj.name == null : name.equals(obj.name))
                    && (extension == null ? obj.extension == null : extension
                            .equals(obj.extension));
        }
        public String getExtension() {
            return extension;
        }
        @Override
        public final int hashCode() {
            return (name == null ? 0 : name.hashCode()) + 
                    (extension == null ? 0 : extension.hashCode());
        }
        @Override
        public final String toString() {
            return name;
        }
    }
}

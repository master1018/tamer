public class AudioFormat {
    protected Encoding encoding;
    protected float sampleRate;
    protected int sampleSizeInBits;
    protected int channels;
    protected int frameSize;
    protected float frameRate;
    protected boolean bigEndian;
    private HashMap<String, Object> properties;
    public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits,
                       int channels, int frameSize, float frameRate, boolean bigEndian) {
        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.bigEndian = bigEndian;
        this.properties = null;
    }
    public AudioFormat(Encoding encoding, float sampleRate,
                       int sampleSizeInBits, int channels,
                       int frameSize, float frameRate,
                       boolean bigEndian, Map<String, Object> properties) {
        this(encoding, sampleRate, sampleSizeInBits, channels,
             frameSize, frameRate, bigEndian);
        this.properties = new HashMap<String, Object>(properties);
    }
    public AudioFormat(float sampleRate, int sampleSizeInBits,
                       int channels, boolean signed, boolean bigEndian) {
        this((signed == true ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED),
             sampleRate,
             sampleSizeInBits,
             channels,
             (channels == AudioSystem.NOT_SPECIFIED || sampleSizeInBits == AudioSystem.NOT_SPECIFIED)?
             AudioSystem.NOT_SPECIFIED:
             ((sampleSizeInBits + 7) / 8) * channels,
             sampleRate,
             bigEndian);
    }
    public Encoding getEncoding() {
        return encoding;
    }
    public float getSampleRate() {
        return sampleRate;
    }
    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }
    public int getChannels() {
        return channels;
    }
    public int getFrameSize() {
        return frameSize;
    }
    public float getFrameRate() {
        return frameRate;
    }
    public boolean isBigEndian() {
        return bigEndian;
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
    public boolean matches(AudioFormat format) {
        if (format.getEncoding().equals(getEncoding())
                && (format.getChannels() == AudioSystem.NOT_SPECIFIED
                    || format.getChannels() == getChannels())
                && (format.getSampleRate() == (float)AudioSystem.NOT_SPECIFIED
                    || format.getSampleRate() == getSampleRate())
                && (format.getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED
                    || format.getSampleSizeInBits() == getSampleSizeInBits())
                && (format.getFrameRate() == (float)AudioSystem.NOT_SPECIFIED
                    || format.getFrameRate() == getFrameRate())
                && (format.getFrameSize() == AudioSystem.NOT_SPECIFIED
                    || format.getFrameSize() == getFrameSize())
                && (getSampleSizeInBits() <= 8
                    || format.isBigEndian() == isBigEndian())) {
            return true;
        }
        return false;
    }
    public String toString() {
        String sEncoding = "";
        if (getEncoding() != null) {
            sEncoding = getEncoding().toString() + " ";
        }
        String sSampleRate;
        if (getSampleRate() == (float) AudioSystem.NOT_SPECIFIED) {
            sSampleRate = "unknown sample rate, ";
        } else {
            sSampleRate = "" + getSampleRate() + " Hz, ";
        }
        String sSampleSizeInBits;
        if (getSampleSizeInBits() == (float) AudioSystem.NOT_SPECIFIED) {
            sSampleSizeInBits = "unknown bits per sample, ";
        } else {
            sSampleSizeInBits = "" + getSampleSizeInBits() + " bit, ";
        }
        String sChannels;
        if (getChannels() == 1) {
            sChannels = "mono, ";
        } else
            if (getChannels() == 2) {
                sChannels = "stereo, ";
            } else {
                if (getChannels() == AudioSystem.NOT_SPECIFIED) {
                    sChannels = " unknown number of channels, ";
                } else {
                    sChannels = ""+getChannels()+" channels, ";
                }
            }
        String sFrameSize;
        if (getFrameSize() == (float) AudioSystem.NOT_SPECIFIED) {
            sFrameSize = "unknown frame size, ";
        } else {
            sFrameSize = "" + getFrameSize()+ " bytes/frame, ";
        }
        String sFrameRate = "";
        if (Math.abs(getSampleRate() - getFrameRate()) > 0.00001) {
            if (getFrameRate() == (float) AudioSystem.NOT_SPECIFIED) {
                sFrameRate = "unknown frame rate, ";
            } else {
                sFrameRate = getFrameRate() + " frames/second, ";
            }
        }
        String sEndian = "";
        if ((getEncoding().equals(Encoding.PCM_SIGNED)
             || getEncoding().equals(Encoding.PCM_UNSIGNED))
            && ((getSampleSizeInBits() > 8)
                || (getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED))) {
            if (isBigEndian()) {
                sEndian = "big-endian";
            } else {
                sEndian = "little-endian";
            }
        }
        return sEncoding
            + sSampleRate
            + sSampleSizeInBits
            + sChannels
            + sFrameSize
            + sFrameRate
            + sEndian;
    }
    public static class Encoding {
        public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");
        public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");
        public static final Encoding PCM_FLOAT = new Encoding("PCM_FLOAT");
        public static final Encoding ULAW = new Encoding("ULAW");
        public static final Encoding ALAW = new Encoding("ALAW");
        private String name;
        public Encoding(String name) {
            this.name = name;
        }
        public final boolean equals(Object obj) {
            if (toString() == null) {
                return (obj != null) && (obj.toString() == null);
            }
            if (obj instanceof Encoding) {
                return toString().equals(obj.toString());
            }
            return false;
        }
        public final int hashCode() {
            if (toString() == null) {
                return 0;
            }
            return toString().hashCode();
        }
        public final String toString() {
            return name;
        }
    } 
}

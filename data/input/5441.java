public class ContextCapabilities {
    public static final int CAPS_EMPTY             = (0 << 0);
    public static final int CAPS_RT_PLAIN_ALPHA    = (1 << 1);
    public static final int CAPS_RT_TEXTURE_ALPHA  = (1 << 2);
    public static final int CAPS_RT_TEXTURE_OPAQUE = (1 << 3);
    public static final int CAPS_MULTITEXTURE      = (1 << 4);
    public static final int CAPS_TEXNONPOW2        = (1 << 5);
    public static final int CAPS_TEXNONSQUARE      = (1 << 6);
    public static final int CAPS_PS20              = (1 << 7);
    public static final int CAPS_PS30              = (1 << 8);
    protected static final int FIRST_PRIVATE_CAP   = (1 << 16);
    protected final int caps;
    protected final String adapterId;
    protected ContextCapabilities(int caps, String adapterId) {
        this.caps = caps;
        this.adapterId = adapterId != null ? adapterId : "unknown adapter";
    }
    public String getAdapterId() {
        return adapterId;
    }
    public int getCaps() {
        return caps;
    }
    @Override
    public String toString() {
        StringBuffer buf =
            new StringBuffer("ContextCapabilities: adapter=" +
                             adapterId+", caps=");
        if (caps == CAPS_EMPTY) {
            buf.append("CAPS_EMPTY");
        } else {
            if ((caps & CAPS_RT_PLAIN_ALPHA) != 0) {
                buf.append("CAPS_RT_PLAIN_ALPHA|");
            }
            if ((caps & CAPS_RT_TEXTURE_ALPHA) != 0) {
                buf.append("CAPS_RT_TEXTURE_ALPHA|");
            }
            if ((caps & CAPS_RT_TEXTURE_OPAQUE) != 0) {
                buf.append("CAPS_RT_TEXTURE_OPAQUE|");
            }
            if ((caps & CAPS_MULTITEXTURE) != 0) {
                buf.append("CAPS_MULTITEXTURE|");
            }
            if ((caps & CAPS_TEXNONPOW2) != 0) {
                buf.append("CAPS_TEXNONPOW2|");
            }
            if ((caps & CAPS_TEXNONSQUARE) != 0) {
                buf.append("CAPS_TEXNONSQUARE|");
            }
            if ((caps & CAPS_PS20) != 0) {
                buf.append("CAPS_PS20|");
            }
            if ((caps & CAPS_PS30) != 0) {
                buf.append("CAPS_PS30|");
            }
        }
        return buf.toString();
    }
}

public final class ProtocolVersion implements Comparable<ProtocolVersion> {
    final static int LIMIT_MAX_VALUE = 0xFFFF;
    final static int LIMIT_MIN_VALUE = 0x0000;
    final static ProtocolVersion NONE = new ProtocolVersion(-1, "NONE");
    final static ProtocolVersion SSL20Hello = new ProtocolVersion(0x0002,
                                                                "SSLv2Hello");
    final static ProtocolVersion SSL30 = new ProtocolVersion(0x0300, "SSLv3");
    final static ProtocolVersion TLS10 = new ProtocolVersion(0x0301, "TLSv1");
    final static ProtocolVersion TLS11 = new ProtocolVersion(0x0302, "TLSv1.1");
    final static ProtocolVersion TLS12 = new ProtocolVersion(0x0303, "TLSv1.2");
    private static final boolean FIPS = SunJSSE.isFIPS();
    final static ProtocolVersion MIN = FIPS ? TLS10 : SSL30;
    final static ProtocolVersion MAX = TLS12;
    final static ProtocolVersion DEFAULT = TLS10;
    final static ProtocolVersion DEFAULT_HELLO = FIPS ? TLS10 : SSL30;
    public final int v;
    public final byte major, minor;
    final String name;
    private ProtocolVersion(int v, String name) {
        this.v = v;
        this.name = name;
        major = (byte)(v >>> 8);
        minor = (byte)(v & 0xff);
    }
    private static ProtocolVersion valueOf(int v) {
        if (v == SSL30.v) {
            return SSL30;
        } else if (v == TLS10.v) {
            return TLS10;
        } else if (v == TLS11.v) {
            return TLS11;
        } else if (v == TLS12.v) {
            return TLS12;
        } else if (v == SSL20Hello.v) {
            return SSL20Hello;
        } else {
            int major = (v >>> 8) & 0xff;
            int minor = v & 0xff;
            return new ProtocolVersion(v, "Unknown-" + major + "." + minor);
        }
    }
    public static ProtocolVersion valueOf(int major, int minor) {
        major &= 0xff;
        minor &= 0xff;
        int v = (major << 8) | minor;
        return valueOf(v);
    }
    static ProtocolVersion valueOf(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Protocol cannot be null");
        }
        if (FIPS && (name.equals(SSL30.name) || name.equals(SSL20Hello.name))) {
            throw new IllegalArgumentException
                ("Only TLS 1.0 or later allowed in FIPS mode");
        }
        if (name.equals(SSL30.name)) {
            return SSL30;
        } else if (name.equals(TLS10.name)) {
            return TLS10;
        } else if (name.equals(TLS11.name)) {
            return TLS11;
        } else if (name.equals(TLS12.name)) {
            return TLS12;
        } else if (name.equals(SSL20Hello.name)) {
            return SSL20Hello;
        } else {
            throw new IllegalArgumentException(name);
        }
    }
    public String toString() {
        return name;
    }
    public int compareTo(ProtocolVersion protocolVersion) {
        return this.v - protocolVersion.v;
    }
}

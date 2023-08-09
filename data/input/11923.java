public class GIOPVersion {
    public static final GIOPVersion V1_0 = new GIOPVersion((byte)1, (byte)0);
    public static final GIOPVersion V1_1 = new GIOPVersion((byte)1, (byte)1);
    public static final GIOPVersion V1_2 = new GIOPVersion((byte)1, (byte)2);
    public static final GIOPVersion V1_3 = new GIOPVersion((byte)1, (byte)3);
    public static final GIOPVersion V13_XX =
        new GIOPVersion((byte)13, (byte)Message.JAVA_ENC_VERSION);
    public static final GIOPVersion DEFAULT_VERSION = V1_2;
    public static final int VERSION_1_0 = 0x0100;
    public static final int VERSION_1_1 = 0x0101;
    public static final int VERSION_1_2 = 0x0102;
    public static final int VERSION_1_3 = 0x0103;
    public static final int VERSION_13_XX =
        ((0x0D << 8) & 0x0000FF00) | Message.JAVA_ENC_VERSION;
    private byte major = (byte) 0;
    private byte minor = (byte) 0;
    public GIOPVersion() {}
    public GIOPVersion(byte majorB, byte minorB) {
        this.major = majorB;
        this.minor = minorB;
    }
    public GIOPVersion(int major, int minor) {
        this.major = (byte)major;
        this.minor = (byte)minor;
    }
    public byte getMajor() {
        return this.major;
    }
    public byte getMinor() {
        return this.minor;
    }
    public boolean equals(GIOPVersion gv){
        return gv.major == this.major && gv.minor == this.minor ;
    }
    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof GIOPVersion))
            return equals((GIOPVersion)obj);
        else
            return false;
    }
    public int hashCode()
    {
        return 37*major + minor ;
    }
    public boolean lessThan(GIOPVersion gv) {
        if (this.major < gv.major) {
            return true;
        } else if (this.major == gv.major) {
            if (this.minor < gv.minor) {
                return true;
            }
        }
        return false;
    }
    public int intValue()
    {
        return (major << 8 | minor);
    }
    public String toString()
    {
        return major + "." + minor;
    }
    public static GIOPVersion getInstance(byte major, byte minor)
    {
        switch(((major << 8) | minor)) {
            case VERSION_1_0:
                return GIOPVersion.V1_0;
            case VERSION_1_1:
                return GIOPVersion.V1_1;
            case VERSION_1_2:
                return GIOPVersion.V1_2;
            case VERSION_1_3:
                return GIOPVersion.V1_3;
            case VERSION_13_XX:
                return GIOPVersion.V13_XX;
            default:
                return new GIOPVersion(major, minor);
        }
    }
    public static GIOPVersion parseVersion(String s)
    {
        int dotIdx = s.indexOf('.');
        if (dotIdx < 1 || dotIdx == s.length() - 1)
            throw new NumberFormatException("GIOP major, minor, and decimal point required: " + s);
        int major = Integer.parseInt(s.substring(0, dotIdx));
        int minor = Integer.parseInt(s.substring(dotIdx + 1, s.length()));
        return GIOPVersion.getInstance((byte)major, (byte)minor);
    }
    public static GIOPVersion chooseRequestVersion(ORB orb, IOR ior ) {
        GIOPVersion orbVersion = orb.getORBData().getGIOPVersion();
        IIOPProfile prof = ior.getProfile() ;
        GIOPVersion profVersion = prof.getGIOPVersion();
        ORBVersion targetOrbVersion = prof.getORBVersion();
        if (!(targetOrbVersion.equals(ORBVersionFactory.getFOREIGN())) &&
                targetOrbVersion.lessThan(ORBVersionFactory.getNEWER())) {
            return V1_0;
        }
        byte prof_major = profVersion.getMajor();
        byte prof_minor = profVersion.getMinor();
        byte orb_major = orbVersion.getMajor();
        byte orb_minor = orbVersion.getMinor();
        if (orb_major < prof_major) {
            return orbVersion;
        } else if (orb_major > prof_major) {
            return profVersion;
        } else { 
            if (orb_minor <= prof_minor) {
                return orbVersion;
            } else {
                return profVersion;
            }
        }
    }
    public boolean supportsIORIIOPProfileComponents()
    {
        return getMinor() > 0 || getMajor() > 1;
    }
    public void read(org.omg.CORBA.portable.InputStream istream) {
        this.major = istream.read_octet();
        this.minor = istream.read_octet();
    }
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        ostream.write_octet(this.major);
        ostream.write_octet(this.minor);
    }
}

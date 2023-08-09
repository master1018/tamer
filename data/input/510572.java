public class VersionInfo {
    public final static String UNAVAILABLE = "UNAVAILABLE";
    public final static String VERSION_PROPERTY_FILE = "version.properties";
    public final static String PROPERTY_MODULE    = "info.module";
    public final static String PROPERTY_RELEASE   = "info.release";
    public final static String PROPERTY_TIMESTAMP = "info.timestamp";
    private final String infoPackage;
    private final String infoModule;
    private final String infoRelease;
    private final String infoTimestamp;
    private final String infoClassloader;
    protected VersionInfo(String pckg, String module,
                          String release, String time, String clsldr) {
        if (pckg == null) {
            throw new IllegalArgumentException
                ("Package identifier must not be null.");
        }
        infoPackage     = pckg;
        infoModule      = (module  != null) ? module  : UNAVAILABLE;
        infoRelease     = (release != null) ? release : UNAVAILABLE;
        infoTimestamp   = (time    != null) ? time    : UNAVAILABLE;
        infoClassloader = (clsldr  != null) ? clsldr  : UNAVAILABLE;
    }
    public final String getPackage() {
        return infoPackage;
    }
    public final String getModule() {
        return infoModule;
    }
    public final String getRelease() {
        return infoRelease;
    }
    public final String getTimestamp() {
        return infoTimestamp;
    }
    public final String getClassloader() {
        return infoClassloader;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer
            (20 + infoPackage.length() + infoModule.length() +
             infoRelease.length() + infoTimestamp.length() +
             infoClassloader.length());
        sb.append("VersionInfo(")
            .append(infoPackage).append(':').append(infoModule);
        if (!UNAVAILABLE.equals(infoRelease))
            sb.append(':').append(infoRelease);
        if (!UNAVAILABLE.equals(infoTimestamp))
            sb.append(':').append(infoTimestamp);
        sb.append(')');
        if (!UNAVAILABLE.equals(infoClassloader))
            sb.append('@').append(infoClassloader);
        return sb.toString();
    }
    public final static VersionInfo[] loadVersionInfo(String[] pckgs,
                                                      ClassLoader clsldr) {
        if (pckgs == null) {
            throw new IllegalArgumentException
                ("Package identifier list must not be null.");
        }
        ArrayList vil = new ArrayList(pckgs.length);
        for (int i=0; i<pckgs.length; i++) {
            VersionInfo vi = loadVersionInfo(pckgs[i], clsldr);
            if (vi != null)
                vil.add(vi);
        }
        return (VersionInfo[]) vil.toArray(new VersionInfo[vil.size()]);
    }
    public final static VersionInfo loadVersionInfo(final String pckg,
                                                    ClassLoader clsldr) {
        if (pckg == null) {
            throw new IllegalArgumentException
                ("Package identifier must not be null.");
        }
        if (clsldr == null)
            clsldr = Thread.currentThread().getContextClassLoader();
        Properties vip = null; 
        try {
            InputStream is = clsldr.getResourceAsStream
                (pckg.replace('.', '/') + "/" + VERSION_PROPERTY_FILE);
            if (is != null) {
                try {
                    Properties props = new Properties();
                    props.load(is);
                    vip = props;
                } finally {
                    is.close();
                }
            }
        } catch (IOException ex) {
        }
        VersionInfo result = null;
        if (vip != null)
            result = fromMap(pckg, vip, clsldr);
        return result;
    }
    protected final static VersionInfo fromMap(String pckg, Map info,
                                               ClassLoader clsldr) {
        if (pckg == null) {
            throw new IllegalArgumentException
                ("Package identifier must not be null.");
        }
        String module = null;
        String release = null;
        String timestamp = null;
        if (info != null) {
            module = (String) info.get(PROPERTY_MODULE);
            if ((module != null) && (module.length() < 1))
                module = null;
            release = (String) info.get(PROPERTY_RELEASE);
            if ((release != null) && ((release.length() < 1) ||
                                      (release.equals("${pom.version}"))))
                release = null;
            timestamp = (String) info.get(PROPERTY_TIMESTAMP);
            if ((timestamp != null) &&
                ((timestamp.length() < 1) ||
                 (timestamp.equals("${mvn.timestamp}")))
                )
                timestamp = null;
        } 
        String clsldrstr = null;
        if (clsldr != null)
            clsldrstr = clsldr.toString();
        return new VersionInfo(pckg, module, release, timestamp, clsldrstr);
    }
} 

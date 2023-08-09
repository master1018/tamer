public final class SunNativeProvider extends Provider {
    private static final long serialVersionUID = -238911724858694204L;
    private static final String NAME = "SunNativeGSS";
    private static final String INFO = "Sun Native GSS provider";
    private static final String MF_CLASS =
        "sun.security.jgss.wrapper.NativeGSSFactory";
    private static final String LIB_PROP = "sun.security.jgss.lib";
    private static final String DEBUG_PROP = "sun.security.nativegss.debug";
    private static HashMap MECH_MAP;
    static final Provider INSTANCE = new SunNativeProvider();
    static boolean DEBUG;
    static void debug(String message) {
        if (DEBUG) {
            if (message == null) {
                throw new NullPointerException();
            }
            System.out.println(NAME + ": " + message);
        }
    }
    static {
        MECH_MAP =
            AccessController.doPrivileged(new PrivilegedAction<HashMap>() {
                    public HashMap run() {
                        DEBUG = Boolean.parseBoolean
                            (System.getProperty(DEBUG_PROP));
                        try {
                            System.loadLibrary("j2gss");
                        } catch (Error err) {
                            debug("No j2gss library found!");
                            if (DEBUG) err.printStackTrace();
                            return null;
                        }
                        String gssLibs[] = new String[0];
                        String defaultLib = System.getProperty(LIB_PROP);
                        if (defaultLib == null || defaultLib.trim().equals("")) {
                            String osname = System.getProperty("os.name");
                            if (osname.startsWith("SunOS")) {
                                gssLibs = new String[]{ "libgss.so" };
                            } else if (osname.startsWith("Linux")) {
                                gssLibs = new String[]{
                                    "libgssapi.so",
                                    "libgssapi_krb5.so",
                                    "libgssapi_krb5.so.2",
                                };
                            }
                        } else {
                            gssLibs = new String[]{ defaultLib };
                        }
                        for (String libName: gssLibs) {
                            if (GSSLibStub.init(libName)) {
                                debug("Loaded GSS library: " + libName);
                                Oid[] mechs = GSSLibStub.indicateMechs();
                                HashMap<String, String> map =
                                            new HashMap<String, String>();
                                for (int i = 0; i < mechs.length; i++) {
                                    debug("Native MF for " + mechs[i]);
                                    map.put("GssApiMechanism." + mechs[i],
                                            MF_CLASS);
                                }
                                return map;
                            }
                        }
                        return null;
                    }
                });
    }
    public SunNativeProvider() {
        super(NAME, 1.0, INFO);
        if (MECH_MAP != null) {
            AccessController.doPrivileged(new PutAllAction(this, MECH_MAP));
        }
    }
}

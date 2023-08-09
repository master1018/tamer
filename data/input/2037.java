public final class SunEC extends Provider {
    private static final long serialVersionUID = -2279741672933606418L;
    private static boolean useFullImplementation = true;
    static {
        try {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("sunec"); 
                    return null;
                }
            });
        } catch (UnsatisfiedLinkError e) {
            useFullImplementation = false;
        }
    }
    public SunEC() {
        super("SunEC", 1.7d, "Sun Elliptic Curve provider (EC, ECDSA, ECDH)");
        if (System.getSecurityManager() == null) {
            SunECEntries.putEntries(this, useFullImplementation);
        } else {
            Map<Object, Object> map = new HashMap<Object, Object>();
            SunECEntries.putEntries(map, useFullImplementation);
            AccessController.doPrivileged(new PutAllAction(this, map));
        }
    }
}

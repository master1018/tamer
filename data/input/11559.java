class PlatformPCSC {
    static final Debug debug = Debug.getInstance("pcsc");
    static final Throwable initException;
    private final static String PROP_NAME = "sun.security.smartcardio.library";
    private final static String LIB1 = "/usr/$LIBISA/libpcsclite.so";
    private final static String LIB2 = "/usr/local/$LIBISA/libpcsclite.so";
    PlatformPCSC() {
    }
    static {
        initException = AccessController.doPrivileged(new PrivilegedAction<Throwable>() {
            public Throwable run() {
                try {
                    System.loadLibrary("j2pcsc");
                    String library = getLibraryName();
                    if (debug != null) {
                        debug.println("Using PC/SC library: " + library);
                    }
                    initialize(library);
                    return null;
                } catch (Throwable e) {
                    return e;
                }
            }
        });
    }
    private static String expand(String lib) {
        int k = lib.indexOf("$LIBISA");
        if (k == -1) {
            return lib;
        }
        String s1 = lib.substring(0, k);
        String s2 = lib.substring(k + 7);
        String libDir;
        if ("64".equals(System.getProperty("sun.arch.data.model"))) {
            if ("SunOS".equals(System.getProperty("os.name"))) {
                libDir = "lib/64";
            } else {
                libDir = "lib64";
            }
        } else {
            libDir = "lib";
        }
        String s = s1 + libDir + s2;
        return s;
    }
    private static String getLibraryName() throws IOException {
        String lib = expand(System.getProperty(PROP_NAME, "").trim());
        if (lib.length() != 0) {
            return lib;
        }
        lib = expand(LIB1);
        if (new File(lib).isFile()) {
            return lib;
        }
        lib = expand(LIB2);
        if (new File(lib).isFile()) {
            return lib;
        }
        throw new IOException("No PC/SC library found on this system");
    }
    private static native void initialize(String libraryName);
    final static int SCARD_PROTOCOL_T0     =  0x0001;
    final static int SCARD_PROTOCOL_T1     =  0x0002;
    final static int SCARD_PROTOCOL_RAW    =  0x0004;
    final static int SCARD_UNKNOWN         =  0x0001;
    final static int SCARD_ABSENT          =  0x0002;
    final static int SCARD_PRESENT         =  0x0004;
    final static int SCARD_SWALLOWED       =  0x0008;
    final static int SCARD_POWERED         =  0x0010;
    final static int SCARD_NEGOTIABLE      =  0x0020;
    final static int SCARD_SPECIFIC        =  0x0040;
}

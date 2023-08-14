final class DebugSettings {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.debug.DebugSettings");
    static final String PREFIX = "awtdebug";
    static final String PROP_FILE = "properties";
    private static final String DEFAULT_PROPS[] = {
        "awtdebug.assert=true",
        "awtdebug.trace=false",
        "awtdebug.on=true",
        "awtdebug.ctrace=false"
    };
    private static DebugSettings        instance = null;
    private Properties  props = new Properties();
    static void init() {
        if (instance != null) {
            return;
        }
        NativeLibLoader.loadLibraries();
        instance = new DebugSettings();
        instance.loadNativeSettings();
    }
    private DebugSettings() {
        new java.security.PrivilegedAction() {
            public Object run() {
                loadProperties();
                return null;
            }
        }.run();
    }
    private synchronized void loadProperties() {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction()
        {
            public Object run() {
                loadDefaultProperties();
                loadFileProperties();
                loadSystemProperties();
                return null;
            }
        });
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine("DebugSettings:\n{0}" + this);
        }
    }
    public String toString() {
        Enumeration enum_ = props.propertyNames();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(bout);
        while (enum_.hasMoreElements()) {
            String key = (String)enum_.nextElement();
            String value = props.getProperty(key, "");
            pout.println(key + " = " + value);
        }
        return new String(bout.toByteArray());
    }
    private void loadDefaultProperties() {
        try {
            for ( int nprop = 0; nprop < DEFAULT_PROPS.length; nprop++ ) {
                StringBufferInputStream in = new StringBufferInputStream(DEFAULT_PROPS[nprop]);
                props.load(in);
                in.close();
            }
        } catch(IOException ioe) {
        }
    }
    private void loadFileProperties() {
        String          propPath;
        Properties      fileProps;
        propPath = System.getProperty(PREFIX + "." + PROP_FILE, "");
        if (propPath.equals("")) {
            propPath = System.getProperty("user.home", "") +
                        File.separator +
                        PREFIX + "." + PROP_FILE;
        }
        File    propFile = new File(propPath);
        try {
            println("Reading debug settings from '" + propFile.getCanonicalPath() + "'...");
            FileInputStream     fin = new FileInputStream(propFile);
            props.load(fin);
            fin.close();
        } catch ( FileNotFoundException fne ) {
            println("Did not find settings file.");
        } catch ( IOException ioe ) {
            println("Problem reading settings, IOException: " + ioe.getMessage());
        }
    }
    private void loadSystemProperties() {
        Properties sysProps = System.getProperties();
        Enumeration enum_ = sysProps.propertyNames();
        while ( enum_.hasMoreElements() ) {
            String key = (String)enum_.nextElement();
            String value = sysProps.getProperty(key,"");
            if ( key.startsWith(PREFIX) ) {
                props.setProperty(key, value);
            }
        }
    }
    public synchronized boolean getBoolean(String key, boolean defval) {
        String  value = getString(key, String.valueOf(defval));
        return value.equalsIgnoreCase("true");
    }
    public synchronized int getInt(String key, int defval) {
        String  value = getString(key, String.valueOf(defval));
        return Integer.parseInt(value);
    }
    public synchronized String getString(String key, String defval) {
        String  actualKeyName = PREFIX + "." + key;
        String  value = props.getProperty(actualKeyName, defval);
        return value;
    }
    public synchronized Enumeration getPropertyNames() {
        Vector          propNames = new Vector();
        Enumeration     enum_ = props.propertyNames();
        while ( enum_.hasMoreElements() ) {
            String propName = (String)enum_.nextElement();
            propName = propName.substring(PREFIX.length()+1);
            propNames.addElement(propName);
        }
        return propNames.elements();
    }
    private void println(Object object) {
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer(object.toString());
        }
    }
    private static final String PROP_CTRACE = "ctrace";
    private static final int PROP_CTRACE_LEN = PROP_CTRACE.length();
    private native synchronized void setCTracingOn(boolean enabled);
    private native synchronized void setCTracingOn(boolean enabled, String file);
    private native synchronized void setCTracingOn(boolean enabled, String file, int line);
    private void loadNativeSettings() {
        boolean        ctracingOn;
        ctracingOn = getBoolean(PROP_CTRACE, false);
        setCTracingOn(ctracingOn);
        Vector                traces = new Vector();
        Enumeration         enum_ = getPropertyNames();
        while ( enum_.hasMoreElements() ) {
            String key = (String)enum_.nextElement();
            if ( key.startsWith(PROP_CTRACE) && key.length() > PROP_CTRACE_LEN ) {
                traces.addElement(key);
            }
        }
        Collections.sort(traces);
        Enumeration        enumTraces = traces.elements();
        while ( enumTraces.hasMoreElements() ) {
            String        key = (String)enumTraces.nextElement();
            String         trace = key.substring(PROP_CTRACE_LEN+1);
            String        filespec;
            String        linespec;
            int                delim= trace.indexOf('@');
            boolean        enabled;
            filespec = delim != -1 ? trace.substring(0, delim) : trace;
            linespec = delim != -1 ? trace.substring(delim+1) : "";
            enabled = getBoolean(key, false);
            if ( linespec.length() == 0 ) {
                    setCTracingOn(enabled, filespec);
            } else {
                int        linenum = Integer.parseInt(linespec, 10);
                setCTracingOn(enabled, filespec, linenum);
            }
        }
    }
}

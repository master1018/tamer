public class Cursor implements java.io.Serializable {
    public static final int     DEFAULT_CURSOR                  = 0;
    public static final int     CROSSHAIR_CURSOR                = 1;
    public static final int     TEXT_CURSOR                     = 2;
    public static final int     WAIT_CURSOR                     = 3;
    public static final int     SW_RESIZE_CURSOR                = 4;
    public static final int     SE_RESIZE_CURSOR                = 5;
    public static final int     NW_RESIZE_CURSOR                = 6;
    public static final int     NE_RESIZE_CURSOR                = 7;
    public static final int     N_RESIZE_CURSOR                 = 8;
    public static final int     S_RESIZE_CURSOR                 = 9;
    public static final int     W_RESIZE_CURSOR                 = 10;
    public static final int     E_RESIZE_CURSOR                 = 11;
    public static final int     HAND_CURSOR                     = 12;
    public static final int     MOVE_CURSOR                     = 13;
    @Deprecated
    protected static Cursor predefined[] = new Cursor[14];
    private final static Cursor[] predefinedPrivate = new Cursor[14];
    static final String[][] cursorProperties = {
        { "AWT.DefaultCursor", "Default Cursor" },
        { "AWT.CrosshairCursor", "Crosshair Cursor" },
        { "AWT.TextCursor", "Text Cursor" },
        { "AWT.WaitCursor", "Wait Cursor" },
        { "AWT.SWResizeCursor", "Southwest Resize Cursor" },
        { "AWT.SEResizeCursor", "Southeast Resize Cursor" },
        { "AWT.NWResizeCursor", "Northwest Resize Cursor" },
        { "AWT.NEResizeCursor", "Northeast Resize Cursor" },
        { "AWT.NResizeCursor", "North Resize Cursor" },
        { "AWT.SResizeCursor", "South Resize Cursor" },
        { "AWT.WResizeCursor", "West Resize Cursor" },
        { "AWT.EResizeCursor", "East Resize Cursor" },
        { "AWT.HandCursor", "Hand Cursor" },
        { "AWT.MoveCursor", "Move Cursor" },
    };
    int type = DEFAULT_CURSOR;
    public static final int     CUSTOM_CURSOR                   = -1;
    private static final Hashtable  systemCustomCursors         = new Hashtable(1);
    private static final String systemCustomCursorDirPrefix = initCursorDir();
    private static String initCursorDir() {
        String jhome =  (String) java.security.AccessController.doPrivileged(
               new sun.security.action.GetPropertyAction("java.home"));
        return jhome +
            File.separator + "lib" + File.separator + "images" +
            File.separator + "cursors" + File.separator;
    }
    private static final String     systemCustomCursorPropertiesFile = systemCustomCursorDirPrefix + "cursors.properties";
    private static       Properties systemCustomCursorProperties = null;
    private static final String CursorDotPrefix  = "Cursor.";
    private static final String DotFileSuffix    = ".File";
    private static final String DotHotspotSuffix = ".HotSpot";
    private static final String DotNameSuffix    = ".Name";
    private static final long serialVersionUID = 8028237497568985504L;
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Cursor");
    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }
    private static native void initIDs();
    private transient long pData;
    private transient Object anchor = new Object();
    static class CursorDisposer implements sun.java2d.DisposerRecord {
        volatile long pData;
        public CursorDisposer(long pData) {
            this.pData = pData;
        }
        public void dispose() {
            if (pData != 0) {
                finalizeImpl(pData);
            }
        }
    }
    transient CursorDisposer disposer;
    private void setPData(long pData) {
        this.pData = pData;
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        if (disposer == null) {
            disposer = new CursorDisposer(pData);
            if (anchor == null) {
                anchor = new Object();
            }
            sun.java2d.Disposer.addRecord(anchor, disposer);
        } else {
            disposer.pData = pData;
        }
    }
    protected String name;
    static public Cursor getPredefinedCursor(int type) {
        if (type < Cursor.DEFAULT_CURSOR || type > Cursor.MOVE_CURSOR) {
            throw new IllegalArgumentException("illegal cursor type");
        }
        Cursor c = predefinedPrivate[type];
        if (c == null) {
            predefinedPrivate[type] = c = new Cursor(type);
        }
        if (predefined[type] == null) {
            predefined[type] = c;
        }
        return c;
    }
    static public Cursor getSystemCustomCursor(final String name)
        throws AWTException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        Cursor cursor = (Cursor)systemCustomCursors.get(name);
        if (cursor == null) {
            synchronized(systemCustomCursors) {
                if (systemCustomCursorProperties == null)
                    loadSystemCustomCursorProperties();
            }
            String prefix = CursorDotPrefix + name;
            String key    = prefix + DotFileSuffix;
            if (!systemCustomCursorProperties.containsKey(key)) {
                if (log.isLoggable(PlatformLogger.FINER)) {
                    log.finer("Cursor.getSystemCustomCursor(" + name + ") returned null");
                }
                return null;
            }
            final String fileName =
                systemCustomCursorProperties.getProperty(key);
            String localized = (String)systemCustomCursorProperties.getProperty(prefix + DotNameSuffix);
            if (localized == null) localized = name;
            String hotspot = (String)systemCustomCursorProperties.getProperty(prefix + DotHotspotSuffix);
            if (hotspot == null)
                throw new AWTException("no hotspot property defined for cursor: " + name);
            StringTokenizer st = new StringTokenizer(hotspot, ",");
            if (st.countTokens() != 2)
                throw new AWTException("failed to parse hotspot property for cursor: " + name);
            int x = 0;
            int y = 0;
            try {
                x = Integer.parseInt(st.nextToken());
                y = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                throw new AWTException("failed to parse hotspot property for cursor: " + name);
            }
            try {
                final int fx = x;
                final int fy = y;
                final String flocalized = localized;
                cursor = (Cursor) java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        Toolkit toolkit = Toolkit.getDefaultToolkit();
                        Image image = toolkit.getImage(
                           systemCustomCursorDirPrefix + fileName);
                        return toolkit.createCustomCursor(
                                    image, new Point(fx,fy), flocalized);
                    }
                });
            } catch (Exception e) {
                throw new AWTException(
                    "Exception: " + e.getClass() + " " + e.getMessage() +
                    " occurred while creating cursor " + name);
            }
            if (cursor == null) {
                if (log.isLoggable(PlatformLogger.FINER)) {
                    log.finer("Cursor.getSystemCustomCursor(" + name + ") returned null");
                }
            } else {
                systemCustomCursors.put(name, cursor);
            }
        }
        return cursor;
    }
    static public Cursor getDefaultCursor() {
        return getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    @ConstructorProperties({"type"})
    public Cursor(int type) {
        if (type < Cursor.DEFAULT_CURSOR || type > Cursor.MOVE_CURSOR) {
            throw new IllegalArgumentException("illegal cursor type");
        }
        this.type = type;
        name = Toolkit.getProperty(cursorProperties[type][0],
                                   cursorProperties[type][1]);
    }
    protected Cursor(String name) {
        this.type = Cursor.CUSTOM_CURSOR;
        this.name = name;
    }
    public int getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return getClass().getName() + "[" + getName() + "]";
    }
    private static void loadSystemCustomCursorProperties() throws AWTException {
        synchronized(systemCustomCursors) {
            systemCustomCursorProperties = new Properties();
            try {
                AccessController.doPrivileged(
                      new java.security.PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(
                                           systemCustomCursorPropertiesFile);
                            systemCustomCursorProperties.load(fis);
                        } finally {
                            if (fis != null)
                                fis.close();
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                systemCustomCursorProperties = null;
                 throw new AWTException("Exception: " + e.getClass() + " " +
                   e.getMessage() + " occurred while loading: " +
                                        systemCustomCursorPropertiesFile);
            }
        }
    }
    private native static void finalizeImpl(long pData);
}

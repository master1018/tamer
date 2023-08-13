public class UIManager implements Serializable
{
    private static class LAFState
    {
        Properties swingProps;
        private UIDefaults[] tables = new UIDefaults[2];
        boolean initialized = false;
        MultiUIDefaults multiUIDefaults = new MultiUIDefaults(tables);
        LookAndFeel lookAndFeel;
        LookAndFeel multiLookAndFeel = null;
        Vector<LookAndFeel> auxLookAndFeels = null;
        SwingPropertyChangeSupport changeSupport;
        LookAndFeelInfo[] installedLAFs;
        UIDefaults getLookAndFeelDefaults() { return tables[0]; }
        void setLookAndFeelDefaults(UIDefaults x) { tables[0] = x; }
        UIDefaults getSystemDefaults() { return tables[1]; }
        void setSystemDefaults(UIDefaults x) { tables[1] = x; }
        public synchronized SwingPropertyChangeSupport
                                 getPropertyChangeSupport(boolean create) {
            if (create && changeSupport == null) {
                changeSupport = new SwingPropertyChangeSupport(
                                         UIManager.class);
            }
            return changeSupport;
        }
    }
    private static final Object classLock = new Object();
    private static LAFState getLAFState() {
        LAFState rv = (LAFState)SwingUtilities.appContextGet(
                SwingUtilities2.LAF_STATE_KEY);
        if (rv == null) {
            synchronized (classLock) {
                rv = (LAFState)SwingUtilities.appContextGet(
                        SwingUtilities2.LAF_STATE_KEY);
                if (rv == null) {
                    SwingUtilities.appContextPut(
                            SwingUtilities2.LAF_STATE_KEY,
                            (rv = new LAFState()));
                }
            }
        }
        return rv;
    }
    private static final String defaultLAFKey = "swing.defaultlaf";
    private static final String auxiliaryLAFsKey = "swing.auxiliarylaf";
    private static final String multiplexingLAFKey = "swing.plaf.multiplexinglaf";
    private static final String installedLAFsKey = "swing.installedlafs";
    private static final String disableMnemonicKey = "swing.disablenavaids";
    private static String makeInstalledLAFKey(String laf, String attr) {
        return "swing.installedlaf." + laf + "." + attr;
    }
    private static String makeSwingPropertiesFilename() {
        String sep = File.separator;
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            javaHome = "<java.home undefined>";
        }
        return javaHome + sep + "lib" + sep + "swing.properties";
    }
    public static class LookAndFeelInfo {
        private String name;
        private String className;
        public LookAndFeelInfo(String name, String className) {
            this.name = name;
            this.className = className;
        }
        public String getName() {
            return name;
        }
        public String getClassName() {
            return className;
        }
        public String toString() {
            return getClass().getName() + "[" + getName() + " " + getClassName() + "]";
        }
    }
    private static LookAndFeelInfo[] installedLAFs;
    static {
        ArrayList<LookAndFeelInfo> iLAFs = new ArrayList<LookAndFeelInfo>(4);
        iLAFs.add(new LookAndFeelInfo(
                      "Metal", "javax.swing.plaf.metal.MetalLookAndFeel"));
        iLAFs.add(new LookAndFeelInfo(
                      "Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel"));
        iLAFs.add(new LookAndFeelInfo("CDE/Motif",
                  "com.sun.java.swing.plaf.motif.MotifLookAndFeel"));
        OSInfo.OSType osType = AccessController.doPrivileged(OSInfo.getOSTypeAction());
        if (osType == OSInfo.OSType.WINDOWS) {
            iLAFs.add(new LookAndFeelInfo("Windows",
                        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"));
            if (Toolkit.getDefaultToolkit().getDesktopProperty(
                    "win.xpstyle.themeActive") != null) {
                iLAFs.add(new LookAndFeelInfo("Windows Classic",
                 "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"));
            }
        }
        else {
            iLAFs.add(new LookAndFeelInfo("GTK+",
                  "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"));
        }
        installedLAFs = iLAFs.toArray(new LookAndFeelInfo[iLAFs.size()]);
    }
    public static LookAndFeelInfo[] getInstalledLookAndFeels() {
        maybeInitialize();
        LookAndFeelInfo[] ilafs = getLAFState().installedLAFs;
        if (ilafs == null) {
            ilafs = installedLAFs;
        }
        LookAndFeelInfo[] rv = new LookAndFeelInfo[ilafs.length];
        System.arraycopy(ilafs, 0, rv, 0, ilafs.length);
        return rv;
    }
    public static void setInstalledLookAndFeels(LookAndFeelInfo[] infos)
        throws SecurityException
    {
        maybeInitialize();
        LookAndFeelInfo[] newInfos = new LookAndFeelInfo[infos.length];
        System.arraycopy(infos, 0, newInfos, 0, infos.length);
        getLAFState().installedLAFs = newInfos;
    }
    public static void installLookAndFeel(LookAndFeelInfo info) {
        LookAndFeelInfo[] infos = getInstalledLookAndFeels();
        LookAndFeelInfo[] newInfos = new LookAndFeelInfo[infos.length + 1];
        System.arraycopy(infos, 0, newInfos, 0, infos.length);
        newInfos[infos.length] = info;
        setInstalledLookAndFeels(newInfos);
    }
    public static void installLookAndFeel(String name, String className) {
        installLookAndFeel(new LookAndFeelInfo(name, className));
    }
    public static LookAndFeel getLookAndFeel() {
        maybeInitialize();
        return getLAFState().lookAndFeel;
    }
    public static void setLookAndFeel(LookAndFeel newLookAndFeel)
        throws UnsupportedLookAndFeelException
    {
        if ((newLookAndFeel != null) && !newLookAndFeel.isSupportedLookAndFeel()) {
            String s = newLookAndFeel.toString() + " not supported on this platform";
            throw new UnsupportedLookAndFeelException(s);
        }
        LAFState lafState = getLAFState();
        LookAndFeel oldLookAndFeel = lafState.lookAndFeel;
        if (oldLookAndFeel != null) {
            oldLookAndFeel.uninitialize();
        }
        lafState.lookAndFeel = newLookAndFeel;
        if (newLookAndFeel != null) {
            sun.swing.DefaultLookup.setDefaultLookup(null);
            newLookAndFeel.initialize();
            lafState.setLookAndFeelDefaults(newLookAndFeel.getDefaults());
        }
        else {
            lafState.setLookAndFeelDefaults(null);
        }
        SwingPropertyChangeSupport changeSupport = lafState.
                                         getPropertyChangeSupport(false);
        if (changeSupport != null) {
            changeSupport.firePropertyChange("lookAndFeel", oldLookAndFeel,
                                             newLookAndFeel);
        }
    }
    public static void setLookAndFeel(String className)
        throws ClassNotFoundException,
               InstantiationException,
               IllegalAccessException,
               UnsupportedLookAndFeelException
    {
        if ("javax.swing.plaf.metal.MetalLookAndFeel".equals(className)) {
            setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        }
        else {
            Class lnfClass = SwingUtilities.loadSystemClass(className);
            setLookAndFeel((LookAndFeel)(lnfClass.newInstance()));
        }
    }
    public static String getSystemLookAndFeelClassName() {
        String systemLAF = AccessController.doPrivileged(
                             new GetPropertyAction("swing.systemlaf"));
        if (systemLAF != null) {
            return systemLAF;
        }
        OSInfo.OSType osType = AccessController.doPrivileged(OSInfo.getOSTypeAction());
        if (osType == OSInfo.OSType.WINDOWS) {
            return "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        } else {
            String desktop = AccessController.doPrivileged(new GetPropertyAction("sun.desktop"));
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if ("gnome".equals(desktop) &&
                    toolkit instanceof SunToolkit &&
                    ((SunToolkit) toolkit).isNativeGTKAvailable()) {
                return "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            }
            if (osType == OSInfo.OSType.SOLARIS) {
                return "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            }
        }
        return getCrossPlatformLookAndFeelClassName();
    }
    public static String getCrossPlatformLookAndFeelClassName() {
        String laf = AccessController.doPrivileged(
                             new GetPropertyAction("swing.crossplatformlaf"));
        if (laf != null) {
            return laf;
        }
        return "javax.swing.plaf.metal.MetalLookAndFeel";
    }
    public static UIDefaults getDefaults() {
        maybeInitialize();
        return getLAFState().multiUIDefaults;
    }
    public static Font getFont(Object key) {
        return getDefaults().getFont(key);
    }
    public static Font getFont(Object key, Locale l) {
        return getDefaults().getFont(key,l);
    }
    public static Color getColor(Object key) {
        return getDefaults().getColor(key);
    }
    public static Color getColor(Object key, Locale l) {
        return getDefaults().getColor(key,l);
    }
    public static Icon getIcon(Object key) {
        return getDefaults().getIcon(key);
    }
    public static Icon getIcon(Object key, Locale l) {
        return getDefaults().getIcon(key,l);
    }
    public static Border getBorder(Object key) {
        return getDefaults().getBorder(key);
    }
    public static Border getBorder(Object key, Locale l) {
        return getDefaults().getBorder(key,l);
    }
    public static String getString(Object key) {
        return getDefaults().getString(key);
    }
    public static String getString(Object key, Locale l) {
        return getDefaults().getString(key,l);
    }
    static String getString(Object key, Component c) {
        Locale l = (c == null) ? Locale.getDefault() : c.getLocale();
        return getString(key, l);
    }
    public static int getInt(Object key) {
        return getDefaults().getInt(key);
    }
    public static int getInt(Object key, Locale l) {
        return getDefaults().getInt(key,l);
    }
    public static boolean getBoolean(Object key) {
        return getDefaults().getBoolean(key);
    }
    public static boolean getBoolean(Object key, Locale l) {
        return getDefaults().getBoolean(key,l);
    }
    public static Insets getInsets(Object key) {
        return getDefaults().getInsets(key);
    }
    public static Insets getInsets(Object key, Locale l) {
        return getDefaults().getInsets(key,l);
    }
    public static Dimension getDimension(Object key) {
        return getDefaults().getDimension(key);
    }
    public static Dimension getDimension(Object key, Locale l) {
        return getDefaults().getDimension(key,l);
    }
    public static Object get(Object key) {
        return getDefaults().get(key);
    }
    public static Object get(Object key, Locale l) {
        return getDefaults().get(key,l);
    }
    public static Object put(Object key, Object value) {
        return getDefaults().put(key, value);
    }
    public static ComponentUI getUI(JComponent target) {
        maybeInitialize();
        ComponentUI ui = null;
        LookAndFeel multiLAF = getLAFState().multiLookAndFeel;
        if (multiLAF != null) {
            ui = multiLAF.getDefaults().getUI(target);
        }
        if (ui == null) {
            ui = getDefaults().getUI(target);
        }
        return ui;
    }
    public static UIDefaults getLookAndFeelDefaults() {
        maybeInitialize();
        return getLAFState().getLookAndFeelDefaults();
    }
    private static LookAndFeel getMultiLookAndFeel() {
        LookAndFeel multiLookAndFeel = getLAFState().multiLookAndFeel;
        if (multiLookAndFeel == null) {
            String defaultName = "javax.swing.plaf.multi.MultiLookAndFeel";
            String className = getLAFState().swingProps.getProperty(multiplexingLAFKey, defaultName);
            try {
                Class lnfClass = SwingUtilities.loadSystemClass(className);
                multiLookAndFeel = (LookAndFeel)lnfClass.newInstance();
            } catch (Exception exc) {
                System.err.println("UIManager: failed loading " + className);
            }
        }
        return multiLookAndFeel;
    }
    static public void addAuxiliaryLookAndFeel(LookAndFeel laf) {
        maybeInitialize();
        if (!laf.isSupportedLookAndFeel()) {
            return;
        }
        Vector<LookAndFeel> v = getLAFState().auxLookAndFeels;
        if (v == null) {
            v = new Vector<LookAndFeel>();
        }
        if (!v.contains(laf)) {
            v.addElement(laf);
            laf.initialize();
            getLAFState().auxLookAndFeels = v;
            if (getLAFState().multiLookAndFeel == null) {
                getLAFState().multiLookAndFeel = getMultiLookAndFeel();
            }
        }
    }
    static public boolean removeAuxiliaryLookAndFeel(LookAndFeel laf) {
        maybeInitialize();
        boolean result;
        Vector<LookAndFeel> v = getLAFState().auxLookAndFeels;
        if ((v == null) || (v.size() == 0)) {
            return false;
        }
        result = v.removeElement(laf);
        if (result) {
            if (v.size() == 0) {
                getLAFState().auxLookAndFeels = null;
                getLAFState().multiLookAndFeel = null;
            } else {
                getLAFState().auxLookAndFeels = v;
            }
        }
        laf.uninitialize();
        return result;
    }
    static public LookAndFeel[] getAuxiliaryLookAndFeels() {
        maybeInitialize();
        Vector<LookAndFeel> v = getLAFState().auxLookAndFeels;
        if ((v == null) || (v.size() == 0)) {
            return null;
        }
        else {
            LookAndFeel[] rv = new LookAndFeel[v.size()];
            for (int i = 0; i < rv.length; i++) {
                rv[i] = v.elementAt(i);
            }
            return rv;
        }
    }
    public static void addPropertyChangeListener(PropertyChangeListener listener)
    {
        synchronized (classLock) {
            getLAFState().getPropertyChangeSupport(true).
                             addPropertyChangeListener(listener);
        }
    }
    public static void removePropertyChangeListener(PropertyChangeListener listener)
    {
        synchronized (classLock) {
            getLAFState().getPropertyChangeSupport(true).
                          removePropertyChangeListener(listener);
        }
    }
    public static PropertyChangeListener[] getPropertyChangeListeners() {
        synchronized(classLock) {
            return getLAFState().getPropertyChangeSupport(true).
                      getPropertyChangeListeners();
        }
    }
    private static Properties loadSwingProperties()
    {
        if (UIManager.class.getClassLoader() != null) {
            return new Properties();
        }
        else {
            final Properties props = new Properties();
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        File file = new File(makeSwingPropertiesFilename());
                        if (file.exists()) {
                            FileInputStream ins = new FileInputStream(file);
                            props.load(ins);
                            ins.close();
                        }
                    }
                    catch (Exception e) {
                    }
                    checkProperty(props, defaultLAFKey);
                    checkProperty(props, auxiliaryLAFsKey);
                    checkProperty(props, multiplexingLAFKey);
                    checkProperty(props, installedLAFsKey);
                    checkProperty(props, disableMnemonicKey);
                    return null;
                }
            });
            return props;
        }
    }
    private static void checkProperty(Properties props, String key) {
        String value = System.getProperty(key);
        if (value != null) {
            props.put(key, value);
        }
    }
    private static void initializeInstalledLAFs(Properties swingProps)
    {
        String ilafsString = swingProps.getProperty(installedLAFsKey);
        if (ilafsString == null) {
            return;
        }
        Vector<String> lafs = new Vector<String>();
        StringTokenizer st = new StringTokenizer(ilafsString, ",", false);
        while (st.hasMoreTokens()) {
            lafs.addElement(st.nextToken());
        }
        Vector<LookAndFeelInfo> ilafs = new Vector<LookAndFeelInfo>(lafs.size());
        for (String laf : lafs) {
            String name = swingProps.getProperty(makeInstalledLAFKey(laf, "name"), laf);
            String cls = swingProps.getProperty(makeInstalledLAFKey(laf, "class"));
            if (cls != null) {
                ilafs.addElement(new LookAndFeelInfo(name, cls));
            }
        }
        LookAndFeelInfo[] installedLAFs = new LookAndFeelInfo[ilafs.size()];
        for(int i = 0; i < ilafs.size(); i++) {
            installedLAFs[i] = ilafs.elementAt(i);
        }
        getLAFState().installedLAFs = installedLAFs;
    }
    private static void initializeDefaultLAF(Properties swingProps)
    {
        if (getLAFState().lookAndFeel != null) {
            return;
        }
        String lafName = null;
        HashMap lafData =
                (HashMap) AppContext.getAppContext().remove("swing.lafdata");
        if (lafData != null) {
            lafName = (String) lafData.remove("defaultlaf");
        }
        if (lafName == null) {
            lafName = getCrossPlatformLookAndFeelClassName();
        }
        lafName = swingProps.getProperty(defaultLAFKey, lafName);
        try {
            setLookAndFeel(lafName);
        } catch (Exception e) {
            throw new Error("Cannot load " + lafName);
        }
        if (lafData != null) {
            for (Object key: lafData.keySet()) {
                UIManager.put(key, lafData.get(key));
            }
        }
    }
    private static void initializeAuxiliaryLAFs(Properties swingProps)
    {
        String auxLookAndFeelNames = swingProps.getProperty(auxiliaryLAFsKey);
        if (auxLookAndFeelNames == null) {
            return;
        }
        Vector<LookAndFeel> auxLookAndFeels = new Vector<LookAndFeel>();
        StringTokenizer p = new StringTokenizer(auxLookAndFeelNames,",");
        String factoryName;
        while (p.hasMoreTokens()) {
            String className = p.nextToken();
            try {
                Class lnfClass = SwingUtilities.loadSystemClass(className);
                LookAndFeel newLAF = (LookAndFeel)lnfClass.newInstance();
                newLAF.initialize();
                auxLookAndFeels.addElement(newLAF);
            }
            catch (Exception e) {
                System.err.println("UIManager: failed loading auxiliary look and feel " + className);
            }
        }
        if (auxLookAndFeels.size() == 0) {
            auxLookAndFeels = null;
        }
        else {
            getLAFState().multiLookAndFeel = getMultiLookAndFeel();
            if (getLAFState().multiLookAndFeel == null) {
                auxLookAndFeels = null;
            }
        }
        getLAFState().auxLookAndFeels = auxLookAndFeels;
    }
    private static void initializeSystemDefaults(Properties swingProps) {
        getLAFState().swingProps = swingProps;
    }
    private static void maybeInitialize() {
        synchronized (classLock) {
            if (!getLAFState().initialized) {
                getLAFState().initialized = true;
                initialize();
            }
        }
    }
    private static void initialize() {
        Properties swingProps = loadSwingProperties();
        initializeSystemDefaults(swingProps);
        initializeDefaultLAF(swingProps);
        initializeAuxiliaryLAFs(swingProps);
        initializeInstalledLAFs(swingProps);
        String toolkitName = Toolkit.getDefaultToolkit().getClass().getName();
        if (!"sun.awt.X11.XToolkit".equals(toolkitName)) {
            if (FocusManager.isFocusManagerEnabled()) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    setDefaultFocusTraversalPolicy(
                        new LayoutFocusTraversalPolicy());
            }
        }
        if (RepaintManager.HANDLE_TOP_LEVEL_PAINT) {
            sun.awt.PaintEventDispatcher.setPaintEventDispatcher(
                                        new SwingPaintEventDispatcher());
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                addKeyEventPostProcessor(new KeyEventPostProcessor() {
                    public boolean postProcessKeyEvent(KeyEvent e) {
                        Component c = e.getComponent();
                        if ((!(c instanceof JComponent) ||
                             (c != null && !c.isEnabled())) &&
                                JComponent.KeyboardState.shouldProcess(e) &&
                                SwingUtilities.processKeyBindings(e)) {
                            e.consume();
                            return true;
                        }
                        return false;
                    }
                });
        AWTAccessor.getComponentAccessor().
            setRequestFocusController(JComponent.focusController);
    }
}

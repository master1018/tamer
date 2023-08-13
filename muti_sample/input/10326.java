public class MToolkit extends UNIXToolkit implements Runnable {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.motif.MToolkit");
    protected static boolean dynamicLayoutSetting = false;
    private boolean loadedXSettings;
    private XSettings xs;
    static final X11GraphicsConfig config;
    private static final boolean motifdnd;
    static {
        if (GraphicsEnvironment.isHeadless()) {
            config = null;
        } else {
            config = (X11GraphicsConfig) (GraphicsEnvironment.
                             getLocalGraphicsEnvironment().
                             getDefaultScreenDevice().
                             getDefaultConfiguration());
        }
        motifdnd = ((Boolean)java.security.AccessController.doPrivileged(
            new GetBooleanAction("awt.dnd.motifdnd"))).booleanValue();
    }
    public MToolkit() {
        super();
        if (PerformanceLogger.loggingEnabled()) {
            PerformanceLogger.setTime("MToolkit construction");
        }
        if (!GraphicsEnvironment.isHeadless()) {
            String mainClassName = null;
            StackTraceElement trace[] = (new Throwable()).getStackTrace();
            int bottom = trace.length - 1;
            if (bottom >= 0) {
                mainClassName = trace[bottom].getClassName();
            }
            if (mainClassName == null || mainClassName.equals("")) {
                mainClassName = "AWT";
            }
            init(mainClassName);
            Thread toolkitThread = new Thread(this, "AWT-Motif");
            toolkitThread.setPriority(Thread.NORM_PRIORITY + 1);
            toolkitThread.setDaemon(true);
            PrivilegedAction<Void> a = new PrivilegedAction<Void>() {
                public Void run() {
                    ThreadGroup mainTG = Thread.currentThread().getThreadGroup();
                    ThreadGroup parentTG = mainTG.getParent();
                    while (parentTG != null) {
                        mainTG = parentTG;
                        parentTG = mainTG.getParent();
                    }
                    Thread shutdownThread = new Thread(mainTG, new Runnable() {
                            public void run() {
                                shutdown();
                            }
                        }, "Shutdown-Thread");
                    shutdownThread.setContextClassLoader(null);
                    Runtime.getRuntime().addShutdownHook(shutdownThread);
                    return null;
                }
            };
            AccessController.doPrivileged(a);
            AWTAutoShutdown.notifyToolkitThreadBusy();
            toolkitThread.start();
        }
    }
    public native void init(String mainClassName);
    public native void run();
    private native void shutdown();
    public ButtonPeer createButton(Button target) {
        return null;
    }
    public TextFieldPeer createTextField(TextField target) {
        return null;
    }
    public LabelPeer createLabel(Label target) {
        return null;
    }
    public ListPeer createList(List target) {
        return null;
    }
    public CheckboxPeer createCheckbox(Checkbox target) {
        return null;
    }
    public ScrollbarPeer createScrollbar(Scrollbar target) {
        return null;
    }
    public ScrollPanePeer createScrollPane(ScrollPane target) {
        return null;
    }
    public TextAreaPeer createTextArea(TextArea target) {
        return null;
    }
    public ChoicePeer createChoice(Choice target) {
        return null;
    }
    public FramePeer  createFrame(Frame target) {
        return null;
    }
    public CanvasPeer createCanvas(Canvas target) {
        return null;
    }
    public PanelPeer createPanel(Panel target) {
        return null;
    }
    public WindowPeer createWindow(Window target) {
        return null;
    }
    public DialogPeer createDialog(Dialog target) {
        return null;
    }
    public FileDialogPeer createFileDialog(FileDialog target) {
        return null;
    }
    public MenuBarPeer createMenuBar(MenuBar target) {
        return null;
    }
    public MenuPeer createMenu(Menu target) {
        return null;
    }
    public PopupMenuPeer createPopupMenu(PopupMenu target) {
        return null;
    }
    public MenuItemPeer createMenuItem(MenuItem target) {
        return null;
    }
    public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) {
        return null;
    }
    public KeyboardFocusManagerPeer createKeyboardFocusManagerPeer(KeyboardFocusManager manager) {
        return null;
    }
    public FontPeer getFontPeer(String name, int style){
        return new MFontPeer(name, style);
    }
    public void setDynamicLayout(boolean b) {
        dynamicLayoutSetting = b;
    }
    protected boolean isDynamicLayoutSet() {
        return dynamicLayoutSetting;
    }
    protected native boolean isDynamicLayoutSupportedNative();
    public boolean isDynamicLayoutActive() {
        return isDynamicLayoutSupportedNative();
    }
    public native boolean isFrameStateSupported(int state);
    public TrayIconPeer createTrayIcon(TrayIcon target) throws HeadlessException {
        return null;
    }
    public SystemTrayPeer createSystemTray(SystemTray target) throws HeadlessException {
        return null;
    }
    public boolean isTraySupported() {
        return false;
    }
    static native ColorModel makeColorModel();
    static ColorModel screenmodel;
    static ColorModel getStaticColorModel() {
        if (screenmodel == null) {
            screenmodel = config.getColorModel ();
        }
        return screenmodel;
    }
    public ColorModel getColorModel() {
        return getStaticColorModel();
    }
    public native int getScreenResolution();
    public Insets getScreenInsets(GraphicsConfiguration gc) {
        return new Insets(0,0,0,0);
    }
    protected native int getScreenWidth();
    protected native int getScreenHeight();
    public FontMetrics getFontMetrics(Font font) {
        return super.getFontMetrics(font);
    }
    public PrintJob getPrintJob(final Frame frame, final String doctitle,
                                final Properties props) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalArgumentException();
        }
        PrintJob2D printJob = new PrintJob2D(frame, doctitle, props);
        if (printJob.printDialog() == false) {
            printJob = null;
        }
        return printJob;
    }
    public PrintJob getPrintJob(final Frame frame, final String doctitle,
                                final JobAttributes jobAttributes,
                                final PageAttributes pageAttributes) {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalArgumentException();
        }
        PrintJob2D printJob = new PrintJob2D(frame, doctitle,
                                             jobAttributes, pageAttributes);
        if (printJob.printDialog() == false) {
            printJob = null;
        }
        return printJob;
    }
    public native void beep();
    public  Clipboard getSystemClipboard() {
        return null;
    }
    public Clipboard getSystemSelection() {
        return null;
    }
    public boolean getLockingKeyState(int key) {
        if (! (key == KeyEvent.VK_CAPS_LOCK || key == KeyEvent.VK_NUM_LOCK ||
               key == KeyEvent.VK_SCROLL_LOCK || key == KeyEvent.VK_KANA_LOCK)) {
            throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
        }
        return getLockingKeyStateNative(key);
    }
    public native boolean getLockingKeyStateNative(int key);
    public native void loadSystemColors(int[] systemColors);
    public static Container getNativeContainer(Component c) {
        return Toolkit.getNativeContainer(c);
    }
    protected static final Object targetToPeer(Object target) {
        return SunToolkit.targetToPeer(target);
    }
    protected static final void targetDisposedPeer(Object target, Object peer) {
        SunToolkit.targetDisposedPeer(target, peer);
    }
    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        return null;
    }
    public <T extends DragGestureRecognizer> T
        createDragGestureRecognizer(Class<T> abstractRecognizerClass,
                                    DragSource ds, Component c, int srcActions,
                                    DragGestureListener dgl)
    {
            return null;
    }
    public InputMethodDescriptor getInputMethodAdapterDescriptor() throws AWTException {
        return null; 
    }
    public Map mapInputMethodHighlight(InputMethodHighlight highlight) {
        return null; 
    }
    public Cursor createCustomCursor(Image cursor, Point hotSpot, String name)
        throws IndexOutOfBoundsException {
        return null; 
    }
    public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) {
        return null; 
    }
    public int getMaximumCursorColors() {
        return 2;  
    }
    private final static String prefix  = "DnD.Cursor.";
    private final static String postfix = ".32x32";
    private static final String dndPrefix  = "DnD.";
    protected Object lazilyLoadDesktopProperty(String name) {
        if (name.startsWith(prefix)) {
            String cursorName = name.substring(prefix.length(), name.length()) + postfix;
            try {
                return Cursor.getSystemCustomCursor(cursorName);
            } catch (AWTException awte) {
                System.err.println("cannot load system cursor: " + cursorName);
                return null;
            }
        }
        if (name.equals("awt.dynamicLayoutSupported")) {
            return lazilyLoadDynamicLayoutSupportedProperty(name);
        }
        if (!loadedXSettings &&
            (name.startsWith("gnome.") ||
             name.equals(SunToolkit.DESKTOPFONTHINTS) ||
             name.startsWith(dndPrefix))) {
            loadedXSettings = true;
            if (!GraphicsEnvironment.isHeadless()) {
                loadXSettings();
                desktopProperties.put(SunToolkit.DESKTOPFONTHINTS,
                                      SunToolkit.getDesktopFontHints());
                return desktopProperties.get(name);
            }
        }
        return super.lazilyLoadDesktopProperty(name);
    }
    protected Boolean lazilyLoadDynamicLayoutSupportedProperty(String name) {
        boolean nativeDynamic = isDynamicLayoutSupportedNative();
        if (log.isLoggable(PlatformLogger.FINER)) {
            log.finer("nativeDynamic == " + nativeDynamic);
        }
        return Boolean.valueOf(nativeDynamic);
    }
    private native int getMulticlickTime();
    protected void initializeDesktopProperties() {
        desktopProperties.put("DnD.Autoscroll.initialDelay",     Integer.valueOf(50));
        desktopProperties.put("DnD.Autoscroll.interval",         Integer.valueOf(50));
        desktopProperties.put("DnD.Autoscroll.cursorHysteresis", Integer.valueOf(5));
        if (!GraphicsEnvironment.isHeadless()) {
            desktopProperties.put("awt.multiClickInterval",
                                  Integer.valueOf(getMulticlickTime()));
            desktopProperties.put("awt.mouse.numButtons",
                                  Integer.valueOf(getNumberOfButtons()));
        }
    }
    public RobotPeer createRobot(Robot target, GraphicsDevice screen) {
        return null;
    }
    static boolean useMotifDnD() {
        return motifdnd;
    }
    private native void loadXSettings();
    private void parseXSettings(int screen_XXX_ignored, byte[] data) {
        if (xs == null) {
            xs = new XSettings();
        }
        Map updatedSettings = xs.update(data);
        if (updatedSettings == null || updatedSettings.isEmpty()) {
            return;
        }
        Iterator i = updatedSettings.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry)i.next();
            String name = (String)e.getKey();
            name = "gnome." + name;
            setDesktopProperty(name, e.getValue());
        }
        setDesktopProperty(SunToolkit.DESKTOPFONTHINTS,
                           SunToolkit.getDesktopFontHints());
        Integer dragThreshold = null;
        synchronized (this) {
            dragThreshold = (Integer)desktopProperties.get("gnome.Net/DndDragThreshold");
        }
        if (dragThreshold != null) {
            setDesktopProperty("DnD.gestureMotionThreshold", dragThreshold);
        }
    }
    protected boolean needsXEmbedImpl() {
        return true;
    }
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        return (modalityType == Dialog.ModalityType.MODELESS) ||
               (modalityType == Dialog.ModalityType.APPLICATION_MODAL);
    }
    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return (exclusionType == Dialog.ModalExclusionType.NO_EXCLUDE);
    }
    private native boolean isSyncUpdated();
    private native boolean isSyncFailed();
    private native int getEventNumber();
    private native void updateSyncSelection();
    private static final long WORKAROUND_SLEEP = 100;
    protected boolean syncNativeQueue(final long timeout) {
        awtLock();
        try {
            long event_number = getEventNumber();
            updateSyncSelection();
            long start = System.currentTimeMillis();
            while (!isSyncUpdated() && !isSyncFailed()) {
                try {
                    awtLockWait(timeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (((System.currentTimeMillis() - start) > timeout) && (timeout >= 0)) {
                    throw new OperationTimedOut();
                }
            }
            if (isSyncFailed() && getEventNumber() - event_number == 1) {
                awtUnlock();
                try {
                    Thread.sleep(WORKAROUND_SLEEP);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                } finally {
                    awtLock();
                }
            }
            return getEventNumber() - event_number > 2;
        } finally {
            awtUnlock();
        }
    }
    public  void grab(Window w) {
        WindowPeer peer = (WindowPeer)w.getPeer();
        if (peer != null) {
            nativeGrab(peer);
        }
    }
    public void ungrab(Window w) {
        WindowPeer peer = (WindowPeer)w.getPeer();
        if (peer != null) {
            nativeUnGrab(peer);
        }
    }
    private native void nativeGrab(WindowPeer peer);
    private native void nativeUnGrab(WindowPeer peer);
    public boolean isDesktopSupported(){
        return false;
    }
    public DesktopPeer createDesktopPeer(Desktop target)
    throws HeadlessException{
        throw new UnsupportedOperationException();
    }
    public final static int
        UNDETERMINED_WM = 1,
        NO_WM = 2,
        OTHER_WM = 3,
        OPENLOOK_WM = 4,
        MOTIF_WM = 5,
        CDE_WM = 6,
        ENLIGHTEN_WM = 7,
        KDE2_WM = 8,
        SAWFISH_WM = 9,
        ICE_WM = 10,
        METACITY_WM = 11,
        COMPIZ_WM = 12,
        LG3D_WM = 13;
    public static int getWMID() {
        String wmName = getWMName();
        if ("NO_WM".equals(wmName)) {
            return NO_WM;
        } else if ("OTHER_WM".equals(wmName)) {
            return OTHER_WM;
        } else if ("ENLIGHTEN_WM".equals(wmName)) {
            return ENLIGHTEN_WM;
        } else if ("KDE2_WM".equals(wmName)) {
            return KDE2_WM;
        } else if ("SAWFISH_WM".equals(wmName)) {
            return SAWFISH_WM;
        } else if ("ICE_WM".equals(wmName)) {
            return ICE_WM;
        } else if ("METACITY_WM".equals(wmName)) {
            return METACITY_WM;
        } else if ("OPENLOOK_WM".equals(wmName)) {
            return OPENLOOK_WM;
        } else if ("MOTIF_WM".equals(wmName)) {
            return MOTIF_WM;
        } else if ("CDE_WM".equals(wmName)) {
            return CDE_WM;
        } else if ("COMPIZ_WM".equals(wmName)) {
            return COMPIZ_WM;
        } else if ("LG3D_WM".equals(wmName)) {
            return LG3D_WM;
        }
        return UNDETERMINED_WM;
    }
    private static native String getWMName();
} 

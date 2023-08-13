class XAWTXSettings extends XSettings implements XMSelectionListener {
    private final XAtom xSettingsPropertyAtom = XAtom.get("_XSETTINGS_SETTINGS");
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XAWTXSettings");
    public static final long MAX_LENGTH = 1000000;
    XMSelection settings;
    public XAWTXSettings() {
        initXSettings();
    }
    void initXSettings() {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Initializing XAWT XSettings");
        settings = new XMSelection("_XSETTINGS");
        settings.addSelectionListener(this);
        initPerScreenXSettings();
    }
    void dispose() {
        settings.removeSelectionListener(this);
    }
    public void ownerDeath(int screen, XMSelection sel, long deadOwner) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Owner " + deadOwner + " died for selection " + sel + " screen "+ screen);
    }
    public void ownerChanged(int screen, XMSelection sel, long newOwner, long data, long timestamp) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("New Owner "+ newOwner + " for selection = " + sel + " screen " +screen );
    }
    public void selectionChanged(int screen, XMSelection sel, long owner , XPropertyEvent event) {
        log.fine("Selection changed on sel " + sel + " screen = " + screen + " owner = " + owner + " event = " + event);
        updateXSettings(screen,owner);
    }
    void initPerScreenXSettings() {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Updating Per XSettings changes");
        Map updatedSettings = null;
        XToolkit.awtLock();
        try {
            long display = XToolkit.getDisplay();
            int screen = (int) XlibWrapper.DefaultScreen(display);
            updatedSettings = getUpdatedSettings(settings.getOwner(screen));
        } finally {
            XToolkit.awtUnlock();
        }
        ((XToolkit)Toolkit.getDefaultToolkit()).parseXSettings(0,updatedSettings);
    }
    private void updateXSettings(int screen, long owner) {
        final Map updatedSettings = getUpdatedSettings(owner);
        EventQueue.invokeLater( new Runnable() {
            public void run() {
                ((XToolkit) Toolkit.getDefaultToolkit()).parseXSettings( 0, updatedSettings);
            }
        });
    }
    private Map getUpdatedSettings(final long owner) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("owner =" + owner);
        if (0 == owner) {
            return null;
        }
        Map settings = null;
        try {
            WindowPropertyGetter getter =
                new WindowPropertyGetter(owner, xSettingsPropertyAtom, 0, MAX_LENGTH,
                        false, xSettingsPropertyAtom.getAtom() );
            try {
                int status = getter.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (status != XConstants.Success || getter.getData() == 0) {
                    if (log.isLoggable(PlatformLogger.FINE)) log.fine("OH OH : getter failed  status = " + status );
                    settings = null;
                }
                long ptr = getter.getData();
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("noItems = " + getter.getNumberOfItems());
                byte array[] = Native.toBytes(ptr,getter.getNumberOfItems());
                if (array != null) {
                    settings = update(array);
                }
            } finally {
                getter.dispose();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return settings;
    }
}

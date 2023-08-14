public class DesktopProperty implements UIDefaults.ActiveValue {
    private static boolean updatePending;
    private static final ReferenceQueue<DesktopProperty> queue = new ReferenceQueue<DesktopProperty>();
    private WeakPCL pcl;
    private final String key;
    private Object value;
    private final Object fallback;
    static void flushUnreferencedProperties() {
        WeakPCL pcl;
        while ((pcl = (WeakPCL)queue.poll()) != null) {
            pcl.dispose();
        }
    }
    private static synchronized void setUpdatePending(boolean update) {
        updatePending = update;
    }
    private static synchronized boolean isUpdatePending() {
        return updatePending;
    }
    private static void updateAllUIs() {
        Class uiClass = UIManager.getLookAndFeel().getClass();
        if (uiClass.getPackage().equals(DesktopProperty.class.getPackage())) {
            XPStyle.invalidateStyle();
        }
        Frame appFrames[] = Frame.getFrames();
        for (Frame appFrame : appFrames) {
            updateWindowUI(appFrame);
        }
    }
    private static void updateWindowUI(Window window) {
        SwingUtilities.updateComponentTreeUI(window);
        Window ownedWins[] = window.getOwnedWindows();
        for (Window ownedWin : ownedWins) {
            updateWindowUI(ownedWin);
        }
    }
    public DesktopProperty(String key, Object fallback) {
        this.key = key;
        this.fallback = fallback;
        flushUnreferencedProperties();
    }
    public Object createValue(UIDefaults table) {
        if (value == null) {
            value = configureValue(getValueFromDesktop());
            if (value == null) {
                value = configureValue(getDefaultValue());
            }
        }
        return value;
    }
    protected Object getValueFromDesktop() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (pcl == null) {
            pcl = new WeakPCL(this, getKey(), UIManager.getLookAndFeel());
            toolkit.addPropertyChangeListener(getKey(), pcl);
        }
        return toolkit.getDesktopProperty(getKey());
    }
    protected Object getDefaultValue() {
        return fallback;
    }
    public void invalidate(LookAndFeel laf) {
        invalidate();
    }
    public void invalidate() {
        value = null;
    }
    protected void updateUI() {
        if (!isUpdatePending()) {
            setUpdatePending(true);
            Runnable uiUpdater = new Runnable() {
                public void run() {
                    updateAllUIs();
                    setUpdatePending(false);
                }
            };
            SwingUtilities.invokeLater(uiUpdater);
        }
    }
    protected Object configureValue(Object value) {
        if (value != null) {
            if (value instanceof Color) {
                return new ColorUIResource((Color)value);
            }
            else if (value instanceof Font) {
                return new FontUIResource((Font)value);
            }
            else if (value instanceof UIDefaults.LazyValue) {
                value = ((UIDefaults.LazyValue)value).createValue(null);
            }
            else if (value instanceof UIDefaults.ActiveValue) {
                value = ((UIDefaults.ActiveValue)value).createValue(null);
            }
        }
        return value;
    }
    protected String getKey() {
        return key;
    }
    private static class WeakPCL extends WeakReference<DesktopProperty>
                               implements PropertyChangeListener {
        private String key;
        private LookAndFeel laf;
        WeakPCL(DesktopProperty target, String key, LookAndFeel laf) {
            super(target, queue);
            this.key = key;
            this.laf = laf;
        }
        public void propertyChange(PropertyChangeEvent pce) {
            DesktopProperty property = get();
            if (property == null || laf != UIManager.getLookAndFeel()) {
                dispose();
            }
            else {
                property.invalidate(laf);
                property.updateUI();
            }
        }
        void dispose() {
            Toolkit.getDefaultToolkit().removePropertyChangeListener(key, this);
        }
    }
}

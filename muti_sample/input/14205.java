public class SynthLookAndFeel extends BasicLookAndFeel {
    static final Insets EMPTY_UIRESOURCE_INSETS = new InsetsUIResource(
                                                            0, 0, 0, 0);
    private static final Object STYLE_FACTORY_KEY =
                  new StringBuffer("com.sun.java.swing.plaf.gtk.StyleCache");
    private static SynthStyleFactory lastFactory;
    private static boolean multipleApps;
    private static AppContext lastContext;
    static ComponentUI selectedUI;
    static int selectedUIState;
    private SynthStyleFactory factory;
    private Map<String, Object> defaultsMap;
    private Handler _handler;
    static void setSelectedUI(ComponentUI uix, boolean selected,
                              boolean focused, boolean enabled,
                              boolean rollover) {
        selectedUI = uix;
        selectedUIState = 0;
        if (selected) {
            selectedUIState = SynthConstants.SELECTED;
            if (focused) {
                selectedUIState |= SynthConstants.FOCUSED;
            }
        }
        else if (rollover && enabled) {
            selectedUIState |=
                    SynthConstants.MOUSE_OVER | SynthConstants.ENABLED;
            if (focused) {
                selectedUIState |= SynthConstants.FOCUSED;
            }
        }
        else {
            if (enabled) {
                selectedUIState |= SynthConstants.ENABLED;
                selectedUIState = SynthConstants.FOCUSED;
            }
            else {
                selectedUIState |= SynthConstants.DISABLED;
            }
        }
    }
    static void resetSelectedUI() {
        selectedUI = null;
    }
    public static void setStyleFactory(SynthStyleFactory cache) {
        synchronized(SynthLookAndFeel.class) {
            AppContext context = AppContext.getAppContext();
            if (!multipleApps && context != lastContext &&
                                 lastContext != null) {
                multipleApps = true;
            }
            lastFactory = cache;
            lastContext = context;
            context.put(STYLE_FACTORY_KEY, cache);
        }
    }
    public static SynthStyleFactory getStyleFactory() {
        synchronized(SynthLookAndFeel.class) {
            if (!multipleApps) {
                return lastFactory;
            }
            AppContext context = AppContext.getAppContext();
            if (lastContext == context) {
                return lastFactory;
            }
            lastContext = context;
            lastFactory = (SynthStyleFactory)AppContext.getAppContext().get
                                           (STYLE_FACTORY_KEY);
            return lastFactory;
        }
    }
    static int getComponentState(Component c) {
        if (c.isEnabled()) {
            if (c.isFocusOwner()) {
                return SynthUI.ENABLED | SynthUI.FOCUSED;
            }
            return SynthUI.ENABLED;
        }
        return SynthUI.DISABLED;
    }
    public static SynthStyle getStyle(JComponent c, Region region) {
        return getStyleFactory().getStyle(c, region);
    }
    static boolean shouldUpdateStyle(PropertyChangeEvent event) {
        LookAndFeel laf = UIManager.getLookAndFeel();
        return (laf instanceof SynthLookAndFeel &&
                ((SynthLookAndFeel) laf).shouldUpdateStyleOnEvent(event));
    }
    static SynthStyle updateStyle(SynthContext context, SynthUI ui) {
        SynthStyle newStyle = getStyle(context.getComponent(),
                                       context.getRegion());
        SynthStyle oldStyle = context.getStyle();
        if (newStyle != oldStyle) {
            if (oldStyle != null) {
                oldStyle.uninstallDefaults(context);
            }
            context.setStyle(newStyle);
            newStyle.installDefaults(context, ui);
        }
        return newStyle;
    }
    public static void updateStyles(Component c) {
        if (c instanceof JComponent) {
            String name = c.getName();
            c.setName(null);
            if (name != null) {
                c.setName(name);
            }
            ((JComponent)c).revalidate();
        }
        Component[] children = null;
        if (c instanceof JMenu) {
            children = ((JMenu)c).getMenuComponents();
        }
        else if (c instanceof Container) {
            children = ((Container)c).getComponents();
        }
        if (children != null) {
            for (Component child : children) {
                updateStyles(child);
            }
        }
        c.repaint();
    }
    public static Region getRegion(JComponent c) {
        return Region.getRegion(c);
    }
    static Insets getPaintingInsets(SynthContext state, Insets insets) {
        if (state.isSubregion()) {
            insets = state.getStyle().getInsets(state, insets);
        }
        else {
            insets = state.getComponent().getInsets(insets);
        }
        return insets;
    }
    static void update(SynthContext state, Graphics g) {
        paintRegion(state, g, null);
    }
    static void updateSubregion(SynthContext state, Graphics g,
                                Rectangle bounds) {
        paintRegion(state, g, bounds);
    }
    private static void paintRegion(SynthContext state, Graphics g,
                                    Rectangle bounds) {
        JComponent c = state.getComponent();
        SynthStyle style = state.getStyle();
        int x, y, width, height;
        if (bounds == null) {
            x = 0;
            y = 0;
            width = c.getWidth();
            height = c.getHeight();
        }
        else {
            x = bounds.x;
            y = bounds.y;
            width = bounds.width;
            height = bounds.height;
        }
        boolean subregion = state.isSubregion();
        if ((subregion && style.isOpaque(state)) ||
                          (!subregion && c.isOpaque())) {
            g.setColor(style.getColor(state, ColorType.BACKGROUND));
            g.fillRect(x, y, width, height);
        }
    }
    static boolean isLeftToRight(Component c) {
        return c.getComponentOrientation().isLeftToRight();
    }
    static Object getUIOfType(ComponentUI ui, Class klass) {
        if (klass.isInstance(ui)) {
            return ui;
        }
        return null;
    }
    public static ComponentUI createUI(JComponent c) {
        String key = c.getUIClassID().intern();
        if (key == "ButtonUI") {
            return SynthButtonUI.createUI(c);
        }
        else if (key == "CheckBoxUI") {
            return SynthCheckBoxUI.createUI(c);
        }
        else if (key == "CheckBoxMenuItemUI") {
            return SynthCheckBoxMenuItemUI.createUI(c);
        }
        else if (key == "ColorChooserUI") {
            return SynthColorChooserUI.createUI(c);
        }
        else if (key == "ComboBoxUI") {
            return SynthComboBoxUI.createUI(c);
        }
        else if (key == "DesktopPaneUI") {
            return SynthDesktopPaneUI.createUI(c);
        }
        else if (key == "DesktopIconUI") {
            return SynthDesktopIconUI.createUI(c);
        }
        else if (key == "EditorPaneUI") {
            return SynthEditorPaneUI.createUI(c);
        }
        else if (key == "FileChooserUI") {
            return SynthFileChooserUI.createUI(c);
        }
        else if (key == "FormattedTextFieldUI") {
            return SynthFormattedTextFieldUI.createUI(c);
        }
        else if (key == "InternalFrameUI") {
            return SynthInternalFrameUI.createUI(c);
        }
        else if (key == "LabelUI") {
            return SynthLabelUI.createUI(c);
        }
        else if (key == "ListUI") {
            return SynthListUI.createUI(c);
        }
        else if (key == "MenuBarUI") {
            return SynthMenuBarUI.createUI(c);
        }
        else if (key == "MenuUI") {
            return SynthMenuUI.createUI(c);
        }
        else if (key == "MenuItemUI") {
            return SynthMenuItemUI.createUI(c);
        }
        else if (key == "OptionPaneUI") {
            return SynthOptionPaneUI.createUI(c);
        }
        else if (key == "PanelUI") {
            return SynthPanelUI.createUI(c);
        }
        else if (key == "PasswordFieldUI") {
            return SynthPasswordFieldUI.createUI(c);
        }
        else if (key == "PopupMenuSeparatorUI") {
            return SynthSeparatorUI.createUI(c);
        }
        else if (key == "PopupMenuUI") {
            return SynthPopupMenuUI.createUI(c);
        }
        else if (key == "ProgressBarUI") {
            return SynthProgressBarUI.createUI(c);
        }
        else if (key == "RadioButtonUI") {
            return SynthRadioButtonUI.createUI(c);
        }
        else if (key == "RadioButtonMenuItemUI") {
            return SynthRadioButtonMenuItemUI.createUI(c);
        }
        else if (key == "RootPaneUI") {
            return SynthRootPaneUI.createUI(c);
        }
        else if (key == "ScrollBarUI") {
            return SynthScrollBarUI.createUI(c);
        }
        else if (key == "ScrollPaneUI") {
            return SynthScrollPaneUI.createUI(c);
        }
        else if (key == "SeparatorUI") {
            return SynthSeparatorUI.createUI(c);
        }
        else if (key == "SliderUI") {
            return SynthSliderUI.createUI(c);
        }
        else if (key == "SpinnerUI") {
            return SynthSpinnerUI.createUI(c);
        }
        else if (key == "SplitPaneUI") {
            return SynthSplitPaneUI.createUI(c);
        }
        else if (key == "TabbedPaneUI") {
            return SynthTabbedPaneUI.createUI(c);
        }
        else if (key == "TableUI") {
            return SynthTableUI.createUI(c);
        }
        else if (key == "TableHeaderUI") {
            return SynthTableHeaderUI.createUI(c);
        }
        else if (key == "TextAreaUI") {
            return SynthTextAreaUI.createUI(c);
        }
        else if (key == "TextFieldUI") {
            return SynthTextFieldUI.createUI(c);
        }
        else if (key == "TextPaneUI") {
            return SynthTextPaneUI.createUI(c);
        }
        else if (key == "ToggleButtonUI") {
            return SynthToggleButtonUI.createUI(c);
        }
        else if (key == "ToolBarSeparatorUI") {
            return SynthSeparatorUI.createUI(c);
        }
        else if (key == "ToolBarUI") {
            return SynthToolBarUI.createUI(c);
        }
        else if (key == "ToolTipUI") {
            return SynthToolTipUI.createUI(c);
        }
        else if (key == "TreeUI") {
            return SynthTreeUI.createUI(c);
        }
        else if (key == "ViewportUI") {
            return SynthViewportUI.createUI(c);
        }
        return null;
    }
    public SynthLookAndFeel() {
        factory = new DefaultSynthStyleFactory();
        _handler = new Handler();
    }
    public void load(InputStream input, Class<?> resourceBase) throws
                       ParseException {
        if (resourceBase == null) {
            throw new IllegalArgumentException(
                "You must supply a valid resource base Class");
        }
        if (defaultsMap == null) {
            defaultsMap = new HashMap<String, Object>();
        }
        new SynthParser().parse(input, (DefaultSynthStyleFactory) factory,
                                null, resourceBase, defaultsMap);
    }
    public void load(URL url) throws ParseException, IOException {
        if (url == null) {
            throw new IllegalArgumentException(
                "You must supply a valid Synth set URL");
        }
        if (defaultsMap == null) {
            defaultsMap = new HashMap<String, Object>();
        }
        InputStream input = url.openStream();
        new SynthParser().parse(input, (DefaultSynthStyleFactory) factory,
                                url, null, defaultsMap);
    }
    @Override
    public void initialize() {
        super.initialize();
        DefaultLookup.setDefaultLookup(new SynthDefaultLookup());
        setStyleFactory(factory);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            addPropertyChangeListener(_handler);
    }
    @Override
    public void uninitialize() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            removePropertyChangeListener(_handler);
        super.uninitialize();
    }
    @Override
    public UIDefaults getDefaults() {
        UIDefaults table = new UIDefaults(60, 0.75f);
        Region.registerUIs(table);
        table.setDefaultLocale(Locale.getDefault());
        table.addResourceBundle(
              "com.sun.swing.internal.plaf.basic.resources.basic" );
        table.addResourceBundle("com.sun.swing.internal.plaf.synth.resources.synth");
        table.put("TabbedPane.isTabRollover", Boolean.TRUE);
        table.put("ColorChooser.swatchesRecentSwatchSize",
                  new Dimension(10, 10));
        table.put("ColorChooser.swatchesDefaultRecentColor", Color.RED);
        table.put("ColorChooser.swatchesSwatchSize", new Dimension(10, 10));
        table.put("html.pendingImage", SwingUtilities2.makeIcon(getClass(),
                                BasicLookAndFeel.class,
                                "icons/image-delayed.png"));
        table.put("html.missingImage", SwingUtilities2.makeIcon(getClass(),
                                BasicLookAndFeel.class,
                                "icons/image-failed.png"));
        table.put("PopupMenu.selectedWindowInputMapBindings", new Object[] {
                  "ESCAPE", "cancel",
                    "DOWN", "selectNext",
                 "KP_DOWN", "selectNext",
                      "UP", "selectPrevious",
                   "KP_UP", "selectPrevious",
                    "LEFT", "selectParent",
                 "KP_LEFT", "selectParent",
                   "RIGHT", "selectChild",
                "KP_RIGHT", "selectChild",
                   "ENTER", "return",
                   "SPACE", "return"
        });
        table.put("PopupMenu.selectedWindowInputMapBindings.RightToLeft",
                  new Object[] {
                    "LEFT", "selectChild",
                 "KP_LEFT", "selectChild",
                   "RIGHT", "selectParent",
                "KP_RIGHT", "selectParent",
                  });
        flushUnreferenced();
        Object aaTextInfo = getAATextInfo();
        table.put(SwingUtilities2.AA_TEXT_PROPERTY_KEY, aaTextInfo);
        new AATextListener(this);
        if (defaultsMap != null) {
            table.putAll(defaultsMap);
        }
        return table;
    }
    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }
    @Override
    public boolean isNativeLookAndFeel() {
        return false;
    }
    @Override
    public String getDescription() {
        return "Synth look and feel";
    }
    @Override
    public String getName() {
        return "Synth look and feel";
    }
    @Override
    public String getID() {
        return "Synth";
    }
    public boolean shouldUpdateStyleOnAncestorChanged() {
        return false;
    }
    protected boolean shouldUpdateStyleOnEvent(PropertyChangeEvent ev) {
        String eName = ev.getPropertyName();
        if ("name" == eName || "componentOrientation" == eName) {
            return true;
        }
        if ("ancestor" == eName && ev.getNewValue() != null) {
            return shouldUpdateStyleOnAncestorChanged();
        }
        return false;
    }
    private static Object getAATextInfo() {
        String language = Locale.getDefault().getLanguage();
        String desktop =
            AccessController.doPrivileged(new GetPropertyAction("sun.desktop"));
        boolean isCjkLocale = (Locale.CHINESE.getLanguage().equals(language) ||
                Locale.JAPANESE.getLanguage().equals(language) ||
                Locale.KOREAN.getLanguage().equals(language));
        boolean isGnome = "gnome".equals(desktop);
        boolean isLocal = SwingUtilities2.isLocalDisplay();
        boolean setAA = isLocal && (!isGnome || !isCjkLocale);
        Object aaTextInfo = SwingUtilities2.AATextInfo.getAATextInfo(setAA);
        return aaTextInfo;
    }
    private static ReferenceQueue<LookAndFeel> queue = new ReferenceQueue<LookAndFeel>();
    private static void flushUnreferenced() {
        AATextListener aatl;
        while ((aatl = (AATextListener) queue.poll()) != null) {
            aatl.dispose();
        }
    }
    private static class AATextListener
        extends WeakReference<LookAndFeel> implements PropertyChangeListener {
        private String key = SunToolkit.DESKTOPFONTHINTS;
        AATextListener(LookAndFeel laf) {
            super(laf, queue);
            Toolkit tk = Toolkit.getDefaultToolkit();
            tk.addPropertyChangeListener(key, this);
        }
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            if (defaults.getBoolean("Synth.doNotSetTextAA")) {
                dispose();
                return;
            }
            LookAndFeel laf = get();
            if (laf == null || laf != UIManager.getLookAndFeel()) {
                dispose();
                return;
            }
            Object aaTextInfo = getAATextInfo();
            defaults.put(SwingUtilities2.AA_TEXT_PROPERTY_KEY, aaTextInfo);
            updateUI();
        }
        void dispose() {
            Toolkit tk = Toolkit.getDefaultToolkit();
            tk.removePropertyChangeListener(key, this);
        }
        private static void updateWindowUI(Window window) {
            updateStyles(window);
            Window ownedWins[] = window.getOwnedWindows();
            for (Window w : ownedWins) {
                updateWindowUI(w);
            }
        }
        private static void updateAllUIs() {
            Frame appFrames[] = Frame.getFrames();
            for (Frame frame : appFrames) {
                updateWindowUI(frame);
            }
        }
        private static boolean updatePending;
        private static synchronized void setUpdatePending(boolean update) {
            updatePending = update;
        }
        private static synchronized boolean isUpdatePending() {
            return updatePending;
        }
        protected void updateUI() {
            if (!isUpdatePending()) {
                setUpdatePending(true);
                Runnable uiUpdater = new Runnable() {
                    @Override
                    public void run() {
                        updateAllUIs();
                        setUpdatePending(false);
                    }
                };
                SwingUtilities.invokeLater(uiUpdater);
            }
        }
    }
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        throw new NotSerializableException(this.getClass().getName());
    }
    private class Handler implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            Object newValue = evt.getNewValue();
            Object oldValue = evt.getOldValue();
            if ("focusOwner" == propertyName) {
                if (oldValue instanceof JComponent) {
                    repaintIfBackgroundsDiffer((JComponent)oldValue);
                }
                if (newValue instanceof JComponent) {
                    repaintIfBackgroundsDiffer((JComponent)newValue);
                }
            }
            else if ("managingFocus" == propertyName) {
                KeyboardFocusManager manager =
                    (KeyboardFocusManager)evt.getSource();
                if (newValue.equals(Boolean.FALSE)) {
                    manager.removePropertyChangeListener(_handler);
                }
                else {
                    manager.addPropertyChangeListener(_handler);
                }
            }
        }
        private void repaintIfBackgroundsDiffer(JComponent comp) {
            ComponentUI ui = (ComponentUI)comp.getClientProperty(
                    SwingUtilities2.COMPONENT_UI_PROPERTY_KEY);
            if (ui instanceof SynthUI) {
                SynthUI synthUI = (SynthUI)ui;
                SynthContext context = synthUI.getContext(comp);
                SynthStyle style = context.getStyle();
                int state = context.getComponentState();
                Color currBG = style.getColor(context, ColorType.BACKGROUND);
                state ^= SynthConstants.FOCUSED;
                context.setComponentState(state);
                Color lastBG = style.getColor(context, ColorType.BACKGROUND);
                state ^= SynthConstants.FOCUSED;
                context.setComponentState(state);
                if (currBG != null && !currBG.equals(lastBG)) {
                    comp.repaint();
                }
                context.dispose();
            }
        }
    }
}

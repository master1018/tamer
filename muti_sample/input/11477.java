class GTKStyle extends SynthStyle implements GTKConstants {
    private static native int nativeGetXThickness(int widgetType);
    private static native int nativeGetYThickness(int widgetType);
    private static native int nativeGetColorForState(int widgetType,
                                                     int state, int typeID);
    private static native Object nativeGetClassValue(int widgetType,
                                                     String key);
    private static native String nativeGetPangoFontName(int widgetType);
    private static final String ICON_PROPERTY_PREFIX = "gtk.icon.";
    static final Color BLACK_COLOR = new ColorUIResource(Color.BLACK);
    static final Color WHITE_COLOR = new ColorUIResource(Color.WHITE);
    static final Font DEFAULT_FONT = new FontUIResource("sansserif",
                                                        Font.PLAIN, 10  );
    static final Insets BUTTON_DEFAULT_BORDER_INSETS = new Insets(1, 1, 1, 1);
    private static final GTKGraphicsUtils GTK_GRAPHICS = new GTKGraphicsUtils();
    private static final Map<String,String> CLASS_SPECIFIC_MAP;
    private static final Map<String,GTKStockIcon> ICONS_MAP;
    private final Font font;
    private final int widgetType;
    private final int xThickness, yThickness;
    GTKStyle(Font userFont, WidgetType widgetType) {
        this.widgetType = widgetType.ordinal();
        String pangoFontName;
        synchronized (sun.awt.UNIXToolkit.GTK_LOCK) {
            xThickness = nativeGetXThickness(this.widgetType);
            yThickness = nativeGetYThickness(this.widgetType);
            pangoFontName = nativeGetPangoFontName(this.widgetType);
        }
        Font pangoFont = null;
        if (pangoFontName != null) {
            pangoFont = PangoFonts.lookupFont(pangoFontName);
        }
        if (pangoFont != null) {
            this.font = pangoFont;
        } else if (userFont != null) {
            this.font = userFont;
        } else {
            this.font = DEFAULT_FONT;
        }
    }
    @Override
    public void installDefaults(SynthContext context) {
        super.installDefaults(context);
        if (!context.getRegion().isSubregion()) {
            context.getComponent().putClientProperty(
                SwingUtilities2.AA_TEXT_PROPERTY_KEY,
                GTKLookAndFeel.aaTextInfo);
        }
    }
    @Override
    public SynthGraphicsUtils getGraphicsUtils(SynthContext context) {
        return GTK_GRAPHICS;
    }
    @Override
    public SynthPainter getPainter(SynthContext state) {
        return GTKPainter.INSTANCE;
    }
    protected Color getColorForState(SynthContext context, ColorType type) {
        if (type == ColorType.FOCUS || type == GTKColorType.BLACK) {
            return BLACK_COLOR;
        }
        else if (type == GTKColorType.WHITE) {
            return WHITE_COLOR;
        }
        Region id = context.getRegion();
        int state = context.getComponentState();
        state = GTKLookAndFeel.synthStateToGTKState(id, state);
        if (type == ColorType.TEXT_FOREGROUND &&
               (id == Region.BUTTON ||
                id == Region.CHECK_BOX ||
                id == Region.CHECK_BOX_MENU_ITEM ||
                id == Region.MENU ||
                id == Region.MENU_ITEM ||
                id == Region.RADIO_BUTTON ||
                id == Region.RADIO_BUTTON_MENU_ITEM ||
                id == Region.TABBED_PANE_TAB ||
                id == Region.TOGGLE_BUTTON ||
                id == Region.TOOL_TIP ||
                id == Region.MENU_ITEM_ACCELERATOR ||
                id == Region.TABBED_PANE_TAB)) {
            type = ColorType.FOREGROUND;
        } else if (id == Region.TABLE ||
                   id == Region.LIST ||
                   id == Region.TREE ||
                   id == Region.TREE_CELL) {
            if (type == ColorType.FOREGROUND) {
                type = ColorType.TEXT_FOREGROUND;
                if (state == SynthConstants.PRESSED) {
                    state = SynthConstants.SELECTED;
                }
            } else if (type == ColorType.BACKGROUND) {
                type = ColorType.TEXT_BACKGROUND;
            }
        }
        return getStyleSpecificColor(context, state, type);
    }
    private Color getStyleSpecificColor(SynthContext context, int state,
                                        ColorType type)
    {
        state = GTKLookAndFeel.synthStateToGTKStateType(state).ordinal();
        synchronized (sun.awt.UNIXToolkit.GTK_LOCK) {
            int rgb = nativeGetColorForState(widgetType, state,
                                             type.getID());
            return new ColorUIResource(rgb);
        }
    }
    Color getGTKColor(int state, ColorType type) {
        return getGTKColor(null, state, type);
    }
    Color getGTKColor(SynthContext context, int state, ColorType type) {
        if (context != null) {
            JComponent c = context.getComponent();
            Region id = context.getRegion();
            state = GTKLookAndFeel.synthStateToGTKState(id, state);
            if (!id.isSubregion() &&
                (state & SynthConstants.ENABLED) != 0) {
                if (type == ColorType.BACKGROUND ||
                    type == ColorType.TEXT_BACKGROUND) {
                    Color bg = c.getBackground();
                    if (!(bg instanceof UIResource)) {
                        return bg;
                    }
                }
                else if (type == ColorType.FOREGROUND ||
                         type == ColorType.TEXT_FOREGROUND) {
                    Color fg = c.getForeground();
                    if (!(fg instanceof UIResource)) {
                        return fg;
                    }
                }
            }
        }
        return getStyleSpecificColor(context, state, type);
    }
    @Override
    public Color getColor(SynthContext context, ColorType type) {
        JComponent c = context.getComponent();
        Region id = context.getRegion();
        int state = context.getComponentState();
        if (c.getName() == "Table.cellRenderer") {
             if (type == ColorType.BACKGROUND) {
                 return c.getBackground();
             }
             if (type == ColorType.FOREGROUND) {
                 return c.getForeground();
             }
        }
        if (id == Region.LABEL && type == ColorType.TEXT_FOREGROUND) {
            type = ColorType.FOREGROUND;
        }
        if (!id.isSubregion() && (state & SynthConstants.ENABLED) != 0) {
            if (type == ColorType.BACKGROUND) {
                return c.getBackground();
            }
            else if (type == ColorType.FOREGROUND) {
                return c.getForeground();
            }
            else if (type == ColorType.TEXT_FOREGROUND) {
                Color color = c.getForeground();
                if (color != null && !(color instanceof UIResource)) {
                    return color;
                }
            }
        }
        return getColorForState(context, type);
    }
    protected Font getFontForState(SynthContext context) {
        return font;
    }
    int getXThickness() {
        return xThickness;
    }
    int getYThickness() {
        return yThickness;
    }
    @Override
    public Insets getInsets(SynthContext state, Insets insets) {
        Region id = state.getRegion();
        JComponent component = state.getComponent();
        String name = (id.isSubregion()) ? null : component.getName();
        if (insets == null) {
            insets = new Insets(0, 0, 0, 0);
        } else {
            insets.top = insets.bottom = insets.left = insets.right = 0;
        }
        if (id == Region.ARROW_BUTTON || id == Region.BUTTON ||
                id == Region.TOGGLE_BUTTON) {
            if ("Spinner.previousButton" == name ||
                    "Spinner.nextButton" == name) {
                return getSimpleInsets(state, insets, 1);
            } else {
                return getButtonInsets(state, insets);
            }
        }
        else if (id == Region.CHECK_BOX || id == Region.RADIO_BUTTON) {
            return getRadioInsets(state, insets);
        }
        else if (id == Region.MENU_BAR) {
            return getMenuBarInsets(state, insets);
        }
        else if (id == Region.MENU ||
                 id == Region.MENU_ITEM ||
                 id == Region.CHECK_BOX_MENU_ITEM ||
                 id == Region.RADIO_BUTTON_MENU_ITEM) {
            return getMenuItemInsets(state, insets);
        }
        else if (id == Region.FORMATTED_TEXT_FIELD) {
            return getTextFieldInsets(state, insets);
        }
        else if (id == Region.INTERNAL_FRAME) {
            insets = Metacity.INSTANCE.getBorderInsets(state, insets);
        }
        else if (id == Region.LABEL) {
            if ("TableHeader.renderer" == name) {
                return getButtonInsets(state, insets);
            }
            else if (component instanceof ListCellRenderer) {
                return getTextFieldInsets(state, insets);
            }
            else if ("Tree.cellRenderer" == name) {
                return getSimpleInsets(state, insets, 1);
            }
        }
        else if (id == Region.OPTION_PANE) {
            return getSimpleInsets(state, insets, 6);
        }
        else if (id == Region.POPUP_MENU) {
            return getSimpleInsets(state, insets, 2);
        }
        else if (id == Region.PROGRESS_BAR || id == Region.SLIDER ||
                 id == Region.TABBED_PANE  || id == Region.TABBED_PANE_CONTENT ||
                 id == Region.TOOL_BAR     ||
                 id == Region.TOOL_BAR_DRAG_WINDOW ||
                 id == Region.TOOL_TIP) {
            return getThicknessInsets(state, insets);
        }
        else if (id == Region.SCROLL_BAR) {
            return getScrollBarInsets(state, insets);
        }
        else if (id == Region.SLIDER_TRACK) {
            return getSliderTrackInsets(state, insets);
        }
        else if (id == Region.TABBED_PANE_TAB) {
            return getTabbedPaneTabInsets(state, insets);
        }
        else if (id == Region.TEXT_FIELD || id == Region.PASSWORD_FIELD) {
            if (name == "Tree.cellEditor") {
                return getSimpleInsets(state, insets, 1);
            }
            return getTextFieldInsets(state, insets);
        } else if (id == Region.SEPARATOR ||
                   id == Region.POPUP_MENU_SEPARATOR ||
                   id == Region.TOOL_BAR_SEPARATOR) {
            return getSeparatorInsets(state, insets);
        } else if (id == GTKEngine.CustomRegion.TITLED_BORDER) {
            return getThicknessInsets(state, insets);
        }
        return insets;
    }
    private Insets getButtonInsets(SynthContext context, Insets insets) {
        int CHILD_SPACING = 1;
        int focusSize = getClassSpecificIntValue(context, "focus-line-width",1);
        int focusPad = getClassSpecificIntValue(context, "focus-padding", 1);
        int xThickness = getXThickness();
        int yThickness = getYThickness();
        int w = focusSize + focusPad + xThickness + CHILD_SPACING;
        int h = focusSize + focusPad + yThickness + CHILD_SPACING;
        insets.left = insets.right = w;
        insets.top = insets.bottom = h;
        Component component = context.getComponent();
        if ((component instanceof JButton) &&
            !(component.getParent() instanceof JToolBar) &&
            ((JButton)component).isDefaultCapable())
        {
            Insets defaultInsets = getClassSpecificInsetsValue(context,
                          "default-border", BUTTON_DEFAULT_BORDER_INSETS);
            insets.left += defaultInsets.left;
            insets.right += defaultInsets.right;
            insets.top += defaultInsets.top;
            insets.bottom += defaultInsets.bottom;
        }
        return insets;
    }
    private Insets getRadioInsets(SynthContext context, Insets insets) {
        int focusSize =
            getClassSpecificIntValue(context, "focus-line-width", 1);
        int focusPad =
            getClassSpecificIntValue(context, "focus-padding", 1);
        int totalFocus = focusSize + focusPad;
        insets.top    = totalFocus;
        insets.bottom = totalFocus;
        if (context.getComponent().getComponentOrientation().isLeftToRight()) {
            insets.left  = 0;
            insets.right = totalFocus;
        } else {
            insets.left  = totalFocus;
            insets.right = 0;
        }
        return insets;
    }
    private Insets getMenuBarInsets(SynthContext context, Insets insets) {
        int internalPadding = getClassSpecificIntValue(context,
                                                       "internal-padding", 1);
        int xThickness = getXThickness();
        int yThickness = getYThickness();
        insets.left = insets.right = xThickness + internalPadding;
        insets.top = insets.bottom = yThickness + internalPadding;
        return insets;
    }
    private Insets getMenuItemInsets(SynthContext context, Insets insets) {
        int horizPadding = getClassSpecificIntValue(context,
                                                    "horizontal-padding", 3);
        int xThickness = getXThickness();
        int yThickness = getYThickness();
        insets.left = insets.right = xThickness + horizPadding;
        insets.top = insets.bottom = yThickness;
        return insets;
    }
    private Insets getThicknessInsets(SynthContext context, Insets insets) {
        insets.left = insets.right = getXThickness();
        insets.top = insets.bottom = getYThickness();
        return insets;
    }
    private Insets getSeparatorInsets(SynthContext context, Insets insets) {
        int horizPadding = 0;
        if (context.getRegion() == Region.POPUP_MENU_SEPARATOR) {
            horizPadding =
                getClassSpecificIntValue(context, "horizontal-padding", 3);
        }
        insets.right = insets.left = getXThickness() + horizPadding;
        insets.top = insets.bottom = getYThickness();
        return insets;
    }
    private Insets getSliderTrackInsets(SynthContext context, Insets insets) {
        int focusSize = getClassSpecificIntValue(context, "focus-line-width", 1);
        int focusPad = getClassSpecificIntValue(context, "focus-padding", 1);
        insets.top = insets.bottom =
                insets.left = insets.right = focusSize + focusPad;
        return insets;
    }
    private Insets getSimpleInsets(SynthContext context, Insets insets, int n) {
        insets.top = insets.bottom = insets.right = insets.left = n;
        return insets;
    }
    private Insets getTabbedPaneTabInsets(SynthContext context, Insets insets) {
        int xThickness = getXThickness();
        int yThickness = getYThickness();
        int focusSize = getClassSpecificIntValue(context, "focus-line-width",1);
        int pad = 2;
        insets.left = insets.right = focusSize + pad + xThickness;
        insets.top = insets.bottom = focusSize + pad + yThickness;
        return insets;
    }
    private Insets getTextFieldInsets(SynthContext context, Insets insets) {
        insets = getClassSpecificInsetsValue(context, "inner-border",
                                    getSimpleInsets(context, insets, 2));
        int xThickness = getXThickness();
        int yThickness = getYThickness();
        boolean interiorFocus =
                getClassSpecificBoolValue(context, "interior-focus", true);
        int focusSize = 0;
        if (!interiorFocus) {
            focusSize = getClassSpecificIntValue(context, "focus-line-width",1);
        }
        insets.left   += focusSize + xThickness;
        insets.right  += focusSize + xThickness;
        insets.top    += focusSize + yThickness;
        insets.bottom += focusSize + yThickness;
        return insets;
    }
    private Insets getScrollBarInsets(SynthContext context, Insets insets) {
        int troughBorder =
            getClassSpecificIntValue(context, "trough-border", 1);
        insets.left = insets.right = insets.top = insets.bottom = troughBorder;
        JComponent c = context.getComponent();
        if (c.getParent() instanceof JScrollPane) {
            int spacing =
                getClassSpecificIntValue(WidgetType.SCROLL_PANE,
                                         "scrollbar-spacing", 3);
            if (((JScrollBar)c).getOrientation() == JScrollBar.HORIZONTAL) {
                insets.top += spacing;
            } else {
                if (c.getComponentOrientation().isLeftToRight()) {
                    insets.left += spacing;
                } else {
                    insets.right += spacing;
                }
            }
        } else {
            if (c.isFocusable()) {
                int focusSize =
                    getClassSpecificIntValue(context, "focus-line-width", 1);
                int focusPad =
                    getClassSpecificIntValue(context, "focus-padding", 1);
                int totalFocus = focusSize + focusPad;
                insets.left   += totalFocus;
                insets.right  += totalFocus;
                insets.top    += totalFocus;
                insets.bottom += totalFocus;
            }
        }
        return insets;
    }
    private static Object getClassSpecificValue(WidgetType wt, String key) {
        synchronized (UNIXToolkit.GTK_LOCK) {
            return nativeGetClassValue(wt.ordinal(), key);
        }
    }
    private static int getClassSpecificIntValue(WidgetType wt, String key,
                                                int defaultValue)
    {
        Object value = getClassSpecificValue(wt, key);
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        return defaultValue;
    }
    Object getClassSpecificValue(String key) {
        synchronized (sun.awt.UNIXToolkit.GTK_LOCK) {
            return nativeGetClassValue(widgetType, key);
        }
    }
    int getClassSpecificIntValue(SynthContext context, String key,
                                 int defaultValue)
    {
        Object value = getClassSpecificValue(key);
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        return defaultValue;
    }
    Insets getClassSpecificInsetsValue(SynthContext context, String key,
                                       Insets defaultValue)
    {
        Object value = getClassSpecificValue(key);
        if (value instanceof Insets) {
            return (Insets)value;
        }
        return defaultValue;
    }
    boolean getClassSpecificBoolValue(SynthContext context, String key,
                                      boolean defaultValue)
    {
        Object value = getClassSpecificValue(key);
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }
        return defaultValue;
    }
    @Override
    public boolean isOpaque(SynthContext context) {
        Region region = context.getRegion();
        if (region == Region.COMBO_BOX ||
              region == Region.DESKTOP_PANE ||
              region == Region.DESKTOP_ICON ||
              region == Region.EDITOR_PANE ||
              region == Region.FORMATTED_TEXT_FIELD ||
              region == Region.INTERNAL_FRAME ||
              region == Region.LIST ||
              region == Region.MENU_BAR ||
              region == Region.PANEL ||
              region == Region.PASSWORD_FIELD ||
              region == Region.POPUP_MENU ||
              region == Region.PROGRESS_BAR ||
              region == Region.ROOT_PANE ||
              region == Region.SCROLL_PANE ||
              region == Region.SPINNER ||
              region == Region.SPLIT_PANE_DIVIDER ||
              region == Region.TABLE ||
              region == Region.TEXT_AREA ||
              region == Region.TEXT_FIELD ||
              region == Region.TEXT_PANE ||
              region == Region.TOOL_BAR_DRAG_WINDOW ||
              region == Region.TOOL_TIP ||
              region == Region.TREE ||
              region == Region.VIEWPORT) {
            return true;
        }
        Component c = context.getComponent();
        String name = c.getName();
        if (name == "ComboBox.renderer" || name == "ComboBox.listRenderer") {
            return true;
        }
        return false;
    }
    @Override
    public Object get(SynthContext context, Object key) {
        String classKey = CLASS_SPECIFIC_MAP.get(key);
        if (classKey != null) {
            Object value = getClassSpecificValue(classKey);
            if (value != null) {
                return value;
            }
        }
        if (key == "ScrollPane.viewportBorderInsets") {
            return getThicknessInsets(context, new Insets(0, 0, 0, 0));
        }
        else if (key == "Slider.tickColor") {
            return getColorForState(context, ColorType.FOREGROUND);
        }
        else if (key == "ScrollBar.minimumThumbSize") {
            int len =
                getClassSpecificIntValue(context, "min-slider-length", 21);
            JScrollBar sb = (JScrollBar)context.getComponent();
            if (sb.getOrientation() == JScrollBar.HORIZONTAL) {
                return new DimensionUIResource(len, 0);
            } else {
                return new DimensionUIResource(0, len);
            }
        }
        else if (key == "Separator.thickness") {
            JSeparator sep = (JSeparator)context.getComponent();
            if (sep.getOrientation() == JSeparator.HORIZONTAL) {
                return getYThickness();
            } else {
                return getXThickness();
            }
        }
        else if (key == "ToolBar.separatorSize") {
            int size = getClassSpecificIntValue(WidgetType.TOOL_BAR,
                                                "space-size", 12);
            return new DimensionUIResource(size, size);
        }
        else if (key == "ScrollBar.buttonSize") {
            JScrollBar sb = (JScrollBar)context.getComponent().getParent();
            boolean horiz = (sb.getOrientation() == JScrollBar.HORIZONTAL);
            WidgetType wt = horiz ?
                WidgetType.HSCROLL_BAR : WidgetType.VSCROLL_BAR;
            int sliderWidth = getClassSpecificIntValue(wt, "slider-width", 14);
            int stepperSize = getClassSpecificIntValue(wt, "stepper-size", 14);
            return horiz ?
                new DimensionUIResource(stepperSize, sliderWidth) :
                new DimensionUIResource(sliderWidth, stepperSize);
        }
        else if (key == "ArrowButton.size") {
            String name = context.getComponent().getName();
            if (name != null && name.startsWith("Spinner")) {
                String pangoFontName;
                synchronized (sun.awt.UNIXToolkit.GTK_LOCK) {
                    pangoFontName =
                        nativeGetPangoFontName(WidgetType.SPINNER.ordinal());
                }
                int arrowSize = (pangoFontName != null) ?
                    PangoFonts.getFontSize(pangoFontName) : 10;
                return (arrowSize + (getXThickness() * 2));
            }
        }
        else if ("CheckBox.iconTextGap".equals(key) ||
                 "RadioButton.iconTextGap".equals(key))
        {
            int indicatorSpacing =
                getClassSpecificIntValue(context, "indicator-spacing", 2);
            int focusSize =
                getClassSpecificIntValue(context, "focus-line-width", 1);
            int focusPad =
                getClassSpecificIntValue(context, "focus-padding", 1);
            return indicatorSpacing + focusSize + focusPad;
        }
        GTKStockIcon stockIcon = null;
        synchronized (ICONS_MAP) {
            stockIcon = ICONS_MAP.get(key);
        }
        if (stockIcon != null) {
            return stockIcon;
        }
        if (key != "engine") {
            Object value = UIManager.get(key);
            if (key == "Table.rowHeight") {
                int focusLineWidth = getClassSpecificIntValue(context,
                        "focus-line-width", 0);
                if (value == null && focusLineWidth > 0) {
                    value = Integer.valueOf(16 + 2 * focusLineWidth);
                }
            }
            return value;
        }
        return null;
    }
    private Icon getStockIcon(SynthContext context, String key, int type) {
        TextDirection direction = TextDirection.LTR;
        if (context != null) {
            ComponentOrientation co = context.getComponent().
                                              getComponentOrientation();
            if (co != null && !co.isLeftToRight()) {
                direction = TextDirection.RTL;
            }
        }
        Icon icon = getStyleSpecificIcon(key, direction, type);
        if (icon != null) {
            return icon;
        }
        String propName = ICON_PROPERTY_PREFIX + key + '.' + type + '.' +
                          (direction == TextDirection.RTL ? "rtl" : "ltr");
        Image img = (Image)
            Toolkit.getDefaultToolkit().getDesktopProperty(propName);
        if (img != null) {
            return new ImageIcon(img);
        }
        return null;
    }
    private Icon getStyleSpecificIcon(String key,
                                      TextDirection direction, int type)
    {
        UNIXToolkit tk = (UNIXToolkit)Toolkit.getDefaultToolkit();
        Image img =
            tk.getStockIcon(widgetType, key, type, direction.ordinal(), null);
        return (img != null) ? new ImageIcon(img) : null;
    }
    static class GTKStockIconInfo {
        private static Map<String,Integer> ICON_TYPE_MAP;
        private static final Object ICON_SIZE_KEY = new StringBuffer("IconSize");
        private static Dimension[] getIconSizesMap() {
            AppContext appContext = AppContext.getAppContext();
            Dimension[] iconSizes = (Dimension[])appContext.get(ICON_SIZE_KEY);
            if (iconSizes == null) {
                iconSizes = new Dimension[7];
                iconSizes[0] = null;                  
                iconSizes[1] = new Dimension(16, 16); 
                iconSizes[2] = new Dimension(18, 18); 
                iconSizes[3] = new Dimension(24, 24); 
                iconSizes[4] = new Dimension(20, 20); 
                iconSizes[5] = new Dimension(32, 32); 
                iconSizes[6] = new Dimension(48, 48); 
                appContext.put(ICON_SIZE_KEY, iconSizes);
            }
            return iconSizes;
        }
        public static Dimension getIconSize(int type) {
            Dimension[] iconSizes = getIconSizesMap();
            return type >= 0 && type < iconSizes.length ?
                iconSizes[type] : null;
        }
        public static void setIconSize(int type, int w, int h) {
            Dimension[] iconSizes = getIconSizesMap();
            if (type >= 0 && type < iconSizes.length) {
                iconSizes[type] = new Dimension(w, h);
            }
        }
        public static int getIconType(String size) {
            if (size == null) {
                return UNDEFINED;
            }
            if (ICON_TYPE_MAP == null) {
                initIconTypeMap();
            }
            Integer n = ICON_TYPE_MAP.get(size);
            return n != null ? n.intValue() : UNDEFINED;
        }
        private static void initIconTypeMap() {
            ICON_TYPE_MAP = new HashMap<String,Integer>();
            ICON_TYPE_MAP.put("gtk-menu", Integer.valueOf(1));
            ICON_TYPE_MAP.put("gtk-small-toolbar", Integer.valueOf(2));
            ICON_TYPE_MAP.put("gtk-large-toolbar", Integer.valueOf(3));
            ICON_TYPE_MAP.put("gtk-button", Integer.valueOf(4));
            ICON_TYPE_MAP.put("gtk-dnd", Integer.valueOf(5));
            ICON_TYPE_MAP.put("gtk-dialog", Integer.valueOf(6));
        }
    }
    private static class GTKStockIcon extends SynthIcon {
        private String key;
        private int size;
        private boolean loadedLTR;
        private boolean loadedRTL;
        private Icon ltrIcon;
        private Icon rtlIcon;
        private SynthStyle style;
        GTKStockIcon(String key, int size) {
            this.key = key;
            this.size = size;
        }
        public void paintIcon(SynthContext context, Graphics g, int x,
                              int y, int w, int h) {
            Icon icon = getIcon(context);
            if (icon != null) {
                if (context == null) {
                    icon.paintIcon(null, g, x, y);
                }
                else {
                    icon.paintIcon(context.getComponent(), g, x, y);
                }
            }
        }
        public int getIconWidth(SynthContext context) {
            Icon icon = getIcon(context);
            if (icon != null) {
                return icon.getIconWidth();
            }
            return 0;
        }
        public int getIconHeight(SynthContext context) {
            Icon icon = getIcon(context);
            if (icon != null) {
                return icon.getIconHeight();
            }
            return 0;
        }
        private Icon getIcon(SynthContext context) {
            if (context != null) {
                ComponentOrientation co = context.getComponent().
                                                  getComponentOrientation();
                SynthStyle style = context.getStyle();
                if (style != this.style) {
                    this.style = style;
                    loadedLTR = loadedRTL = false;
                }
                if (co == null || co.isLeftToRight()) {
                    if (!loadedLTR) {
                        loadedLTR = true;
                        ltrIcon = ((GTKStyle)context.getStyle()).
                                  getStockIcon(context, key, size);
                    }
                    return ltrIcon;
                }
                else if (!loadedRTL) {
                    loadedRTL = true;
                    rtlIcon = ((GTKStyle)context.getStyle()).
                              getStockIcon(context, key,size);
                }
                return rtlIcon;
            }
            return ltrIcon;
        }
    }
    static class GTKLazyValue implements UIDefaults.LazyValue {
        private String className;
        private String methodName;
        GTKLazyValue(String name) {
            this(name, null);
        }
        GTKLazyValue(String name, String methodName) {
            this.className = name;
            this.methodName = methodName;
        }
        public Object createValue(UIDefaults table) {
            try {
                Class c = Class.forName(className, true,Thread.currentThread().
                                        getContextClassLoader());
                if (methodName == null) {
                    return c.newInstance();
                }
                Method m = c.getMethod(methodName, (Class[])null);
                return m.invoke(c, (Object[])null);
            } catch (ClassNotFoundException cnfe) {
            } catch (IllegalAccessException iae) {
            } catch (InvocationTargetException ite) {
            } catch (NoSuchMethodException nsme) {
            } catch (InstantiationException ie) {
            }
            return null;
        }
    }
    static {
        CLASS_SPECIFIC_MAP = new HashMap<String,String>();
        CLASS_SPECIFIC_MAP.put("Slider.thumbHeight", "slider-width");
        CLASS_SPECIFIC_MAP.put("Slider.trackBorder", "trough-border");
        CLASS_SPECIFIC_MAP.put("SplitPane.size", "handle-size");
        CLASS_SPECIFIC_MAP.put("Tree.expanderSize", "expander-size");
        CLASS_SPECIFIC_MAP.put("ScrollBar.thumbHeight", "slider-width");
        CLASS_SPECIFIC_MAP.put("ScrollBar.width", "slider-width");
        CLASS_SPECIFIC_MAP.put("TextArea.caretForeground", "cursor-color");
        CLASS_SPECIFIC_MAP.put("TextArea.caretAspectRatio", "cursor-aspect-ratio");
        CLASS_SPECIFIC_MAP.put("TextField.caretForeground", "cursor-color");
        CLASS_SPECIFIC_MAP.put("TextField.caretAspectRatio", "cursor-aspect-ratio");
        CLASS_SPECIFIC_MAP.put("PasswordField.caretForeground", "cursor-color");
        CLASS_SPECIFIC_MAP.put("PasswordField.caretAspectRatio", "cursor-aspect-ratio");
        CLASS_SPECIFIC_MAP.put("FormattedTextField.caretForeground", "cursor-color");
        CLASS_SPECIFIC_MAP.put("FormattedTextField.caretAspectRatio", "cursor-aspect-");
        CLASS_SPECIFIC_MAP.put("TextPane.caretForeground", "cursor-color");
        CLASS_SPECIFIC_MAP.put("TextPane.caretAspectRatio", "cursor-aspect-ratio");
        CLASS_SPECIFIC_MAP.put("EditorPane.caretForeground", "cursor-color");
        CLASS_SPECIFIC_MAP.put("EditorPane.caretAspectRatio", "cursor-aspect-ratio");
        ICONS_MAP = new HashMap<String, GTKStockIcon>();
        ICONS_MAP.put("FileChooser.cancelIcon", new GTKStockIcon("gtk-cancel", 4));
        ICONS_MAP.put("FileChooser.okIcon",     new GTKStockIcon("gtk-ok",     4));
        ICONS_MAP.put("OptionPane.errorIcon", new GTKStockIcon("gtk-dialog-error", 6));
        ICONS_MAP.put("OptionPane.informationIcon", new GTKStockIcon("gtk-dialog-info", 6));
        ICONS_MAP.put("OptionPane.warningIcon", new GTKStockIcon("gtk-dialog-warning", 6));
        ICONS_MAP.put("OptionPane.questionIcon", new GTKStockIcon("gtk-dialog-question", 6));
        ICONS_MAP.put("OptionPane.yesIcon", new GTKStockIcon("gtk-yes", 4));
        ICONS_MAP.put("OptionPane.noIcon", new GTKStockIcon("gtk-no", 4));
        ICONS_MAP.put("OptionPane.cancelIcon", new GTKStockIcon("gtk-cancel", 4));
        ICONS_MAP.put("OptionPane.okIcon", new GTKStockIcon("gtk-ok", 4));
    }
}

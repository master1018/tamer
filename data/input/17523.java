class XPStyle {
    private static XPStyle xp;
    private static SkinPainter skinPainter = new SkinPainter();
    private static Boolean themeActive = null;
    private HashMap<String, Border> borderMap;
    private HashMap<String, Color>  colorMap;
    private boolean flatMenus;
    static {
        invalidateStyle();
    }
    static synchronized void invalidateStyle() {
        xp = null;
        themeActive = null;
        skinPainter.flush();
    }
    static synchronized XPStyle getXP() {
        if (themeActive == null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            themeActive =
                (Boolean)toolkit.getDesktopProperty("win.xpstyle.themeActive");
            if (themeActive == null) {
                themeActive = Boolean.FALSE;
            }
            if (themeActive.booleanValue()) {
                GetPropertyAction propertyAction =
                    new GetPropertyAction("swing.noxp");
                if (AccessController.doPrivileged(propertyAction) == null &&
                    ThemeReader.isThemed() &&
                    !(UIManager.getLookAndFeel()
                      instanceof WindowsClassicLookAndFeel)) {
                    xp = new XPStyle();
                }
            }
        }
        return xp;
    }
    static boolean isVista() {
        XPStyle xp = XPStyle.getXP();
        return (xp != null && xp.isSkinDefined(null, Part.CP_DROPDOWNBUTTONRIGHT));
    }
    String getString(Component c, Part part, State state, Prop prop) {
        return getTypeEnumName(c, part, state, prop);
    }
    TypeEnum getTypeEnum(Component c, Part part, State state, Prop prop) {
        int enumValue = ThemeReader.getEnum(part.getControlName(c), part.getValue(),
                                            State.getValue(part, state),
                                            prop.getValue());
        return TypeEnum.getTypeEnum(prop, enumValue);
    }
    private static String getTypeEnumName(Component c, Part part, State state, Prop prop) {
        int enumValue = ThemeReader.getEnum(part.getControlName(c), part.getValue(),
                                            State.getValue(part, state),
                                            prop.getValue());
        if (enumValue == -1) {
            return null;
        }
        return TypeEnum.getTypeEnum(prop, enumValue).getName();
    }
    int getInt(Component c, Part part, State state, Prop prop, int fallback) {
        return ThemeReader.getInt(part.getControlName(c), part.getValue(),
                                  State.getValue(part, state),
                                  prop.getValue());
    }
    Dimension getDimension(Component c, Part part, State state, Prop prop) {
        return ThemeReader.getPosition(part.getControlName(c), part.getValue(),
                                       State.getValue(part, state),
                                       prop.getValue());
    }
    Point getPoint(Component c, Part part, State state, Prop prop) {
        Dimension d = ThemeReader.getPosition(part.getControlName(c), part.getValue(),
                                              State.getValue(part, state),
                                              prop.getValue());
        if (d != null) {
            return new Point(d.width, d.height);
        } else {
            return null;
        }
    }
    Insets getMargin(Component c, Part part, State state, Prop prop) {
        return ThemeReader.getThemeMargins(part.getControlName(c), part.getValue(),
                                           State.getValue(part, state),
                                           prop.getValue());
    }
    synchronized Color getColor(Skin skin, Prop prop, Color fallback) {
        String key = skin.toString() + "." + prop.name();
        Part part = skin.part;
        Color color = colorMap.get(key);
        if (color == null) {
            color = ThemeReader.getColor(part.getControlName(null), part.getValue(),
                                         State.getValue(part, skin.state),
                                         prop.getValue());
            if (color != null) {
                color = new ColorUIResource(color);
                colorMap.put(key, color);
            }
        }
        return (color != null) ? color : fallback;
    }
    Color getColor(Component c, Part part, State state, Prop prop, Color fallback) {
        return getColor(new Skin(c, part, state), prop, fallback);
    }
    synchronized Border getBorder(Component c, Part part) {
        if (part == Part.MENU) {
            if (flatMenus) {
                return new XPFillBorder(UIManager.getColor("InternalFrame.borderShadow"),
                                        1);
            } else {
                return null;    
            }
        }
        Skin skin = new Skin(c, part, null);
        Border border = borderMap.get(skin.string);
        if (border == null) {
            String bgType = getTypeEnumName(c, part, null, Prop.BGTYPE);
            if ("borderfill".equalsIgnoreCase(bgType)) {
                int thickness = getInt(c, part, null, Prop.BORDERSIZE, 1);
                Color color = getColor(skin, Prop.BORDERCOLOR, Color.black);
                border = new XPFillBorder(color, thickness);
                if (part == Part.CP_COMBOBOX) {
                    border = new XPStatefulFillBorder(color, thickness, part, Prop.BORDERCOLOR);
                }
            } else if ("imagefile".equalsIgnoreCase(bgType)) {
                Insets m = getMargin(c, part, null, Prop.SIZINGMARGINS);
                if (m != null) {
                    if (getBoolean(c, part, null, Prop.BORDERONLY)) {
                        border = new XPImageBorder(c, part);
                    } else if (part == Part.CP_COMBOBOX) {
                        border = new EmptyBorder(1, 1, 1, 1);
                    } else {
                        if(part == Part.TP_BUTTON) {
                            border = new XPEmptyBorder(new Insets(3,3,3,3));
                        } else {
                            border = new XPEmptyBorder(m);
                        }
                    }
                }
            }
            if (border != null) {
                borderMap.put(skin.string, border);
            }
        }
        return border;
    }
    private class XPFillBorder extends LineBorder implements UIResource {
        XPFillBorder(Color color, int thickness) {
            super(color, thickness);
        }
        public Insets getBorderInsets(Component c, Insets insets)       {
            Insets margin = null;
           if (c instanceof AbstractButton) {
               margin = ((AbstractButton)c).getMargin();
           } else if (c instanceof JToolBar) {
               margin = ((JToolBar)c).getMargin();
           } else if (c instanceof JTextComponent) {
               margin = ((JTextComponent)c).getMargin();
           }
           insets.top    = (margin != null? margin.top : 0)    + thickness;
           insets.left   = (margin != null? margin.left : 0)   + thickness;
           insets.bottom = (margin != null? margin.bottom : 0) + thickness;
           insets.right =  (margin != null? margin.right : 0)  + thickness;
           return insets;
        }
    }
    private class XPStatefulFillBorder extends XPFillBorder {
        private final Part part;
        private final Prop prop;
        XPStatefulFillBorder(Color color, int thickness, Part part, Prop prop) {
            super(color, thickness);
            this.part = part;
            this.prop = prop;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            State state = State.NORMAL;
            if(c instanceof JComboBox) {
                JComboBox cb = (JComboBox)c;
                if(cb.getUI() instanceof WindowsComboBoxUI) {
                    WindowsComboBoxUI wcb = (WindowsComboBoxUI)cb.getUI();
                    state = wcb.getXPComboBoxState(cb);
                }
            }
            lineColor = getColor(c, part, state, prop, Color.black);
            super.paintBorder(c, g, x, y, width, height);
        }
    }
    private class XPImageBorder extends AbstractBorder implements UIResource {
        Skin skin;
        XPImageBorder(Component c, Part part) {
            this.skin = getSkin(c, part);
        }
        public void paintBorder(Component c, Graphics g,
                                int x, int y, int width, int height) {
            skin.paintSkin(g, x, y, width, height, null);
        }
        public Insets getBorderInsets(Component c, Insets insets)       {
            Insets margin = null;
            Insets borderInsets = skin.getContentMargin();
            if(borderInsets == null) {
                borderInsets = new Insets(0, 0, 0, 0);
            }
           if (c instanceof AbstractButton) {
               margin = ((AbstractButton)c).getMargin();
           } else if (c instanceof JToolBar) {
               margin = ((JToolBar)c).getMargin();
           } else if (c instanceof JTextComponent) {
               margin = ((JTextComponent)c).getMargin();
           }
           insets.top    = (margin != null? margin.top : 0)    + borderInsets.top;
           insets.left   = (margin != null? margin.left : 0)   + borderInsets.left;
           insets.bottom = (margin != null? margin.bottom : 0) + borderInsets.bottom;
           insets.right  = (margin != null? margin.right : 0)  + borderInsets.right;
           return insets;
        }
    }
    private class XPEmptyBorder extends EmptyBorder implements UIResource {
        XPEmptyBorder(Insets m) {
            super(m.top+2, m.left+2, m.bottom+2, m.right+2);
        }
        public Insets getBorderInsets(Component c, Insets insets)       {
            insets = super.getBorderInsets(c, insets);
            Insets margin = null;
            if (c instanceof AbstractButton) {
                Insets m = ((AbstractButton)c).getMargin();
                if(c.getParent() instanceof JToolBar
                   && ! (c instanceof JRadioButton)
                   && ! (c instanceof JCheckBox)
                   && m instanceof InsetsUIResource) {
                    insets.top -= 2;
                    insets.left -= 2;
                    insets.bottom -= 2;
                    insets.right -= 2;
                } else {
                    margin = m;
                }
            } else if (c instanceof JToolBar) {
                margin = ((JToolBar)c).getMargin();
            } else if (c instanceof JTextComponent) {
                margin = ((JTextComponent)c).getMargin();
            }
            if (margin != null) {
                insets.top    = margin.top + 2;
                insets.left   = margin.left + 2;
                insets.bottom = margin.bottom + 2;
                insets.right  = margin.right + 2;
            }
            return insets;
        }
    }
    boolean isSkinDefined(Component c, Part part) {
        return (part.getValue() == 0)
            || ThemeReader.isThemePartDefined(
                   part.getControlName(c), part.getValue(), 0);
    }
    synchronized Skin getSkin(Component c, Part part) {
        assert isSkinDefined(c, part) : "part " + part + " is not defined";
        return new Skin(c, part, null);
    }
    long getThemeTransitionDuration(Component c, Part part, State stateFrom,
                                    State stateTo, Prop prop) {
         return ThemeReader.getThemeTransitionDuration(part.getControlName(c),
                                          part.getValue(),
                                          State.getValue(part, stateFrom),
                                          State.getValue(part, stateTo),
                                          (prop != null) ? prop.getValue() : 0);
    }
    static class Skin {
        final Component component;
        final Part part;
        final State state;
        private final String string;
        private Dimension size = null;
        Skin(Component component, Part part) {
            this(component, part, null);
        }
        Skin(Part part, State state) {
            this(null, part, state);
        }
        Skin(Component component, Part part, State state) {
            this.component = component;
            this.part  = part;
            this.state = state;
            String str = part.getControlName(component) +"." + part.name();
            if (state != null) {
                str += "("+state.name()+")";
            }
            string = str;
        }
        Insets getContentMargin() {
            int boundingWidth = 100;
            int boundingHeight = 100;
            return ThemeReader.getThemeBackgroundContentMargins(
                part.getControlName(null), part.getValue(),
                0, boundingWidth, boundingHeight);
        }
        private int getWidth(State state) {
            if (size == null) {
                size = getPartSize(part, state);
            }
            return size.width;
        }
        int getWidth() {
            return getWidth((state != null) ? state : State.NORMAL);
        }
        private int getHeight(State state) {
            if (size == null) {
                size = getPartSize(part, state);
            }
            return size.height;
        }
        int getHeight() {
            return getHeight((state != null) ? state : State.NORMAL);
        }
        public String toString() {
            return string;
        }
        public boolean equals(Object obj) {
            return (obj instanceof Skin && ((Skin)obj).string.equals(string));
        }
        public int hashCode() {
            return string.hashCode();
        }
        void paintSkin(Graphics g, int dx, int dy, State state) {
            if (state == null) {
                state = this.state;
            }
            paintSkin(g, dx, dy, getWidth(state), getHeight(state), state);
        }
        void paintSkin(Graphics g, Rectangle r, State state) {
            paintSkin(g, r.x, r.y, r.width, r.height, state);
        }
        void paintSkin(Graphics g, int dx, int dy, int dw, int dh, State state) {
            if (ThemeReader.isGetThemeTransitionDurationDefined()
                  && component instanceof JComponent
                  && SwingUtilities.getAncestorOfClass(CellRendererPane.class,
                                                       component) == null) {
                AnimationController.paintSkin((JComponent) component, this,
                                              g, dx, dy, dw, dh, state);
            } else {
                paintSkinRaw(g, dx, dy, dw, dh, state);
            }
        }
        void paintSkinRaw(Graphics g, int dx, int dy, int dw, int dh, State state) {
            skinPainter.paint(null, g, dx, dy, dw, dh, this, state);
        }
        void paintSkin(Graphics g, int dx, int dy, int dw, int dh, State state,
                boolean borderFill) {
            if(borderFill && "borderfill".equals(getTypeEnumName(component, part,
                    state, Prop.BGTYPE))) {
                return;
            }
            skinPainter.paint(null, g, dx, dy, dw, dh, this, state);
        }
    }
    private static class SkinPainter extends CachedPainter {
        SkinPainter() {
            super(30);
            flush();
        }
        public void flush() {
            super.flush();
        }
        protected void paintToImage(Component c, Image image, Graphics g,
                                    int w, int h, Object[] args) {
            boolean accEnabled = false;
            Skin skin = (Skin)args[0];
            Part part = skin.part;
            State state = (State)args[1];
            if (state == null) {
                state = skin.state;
            }
            if (c == null) {
                c = skin.component;
            }
            BufferedImage bi = (BufferedImage)image;
            WritableRaster raster = bi.getRaster();
            DataBufferInt dbi = (DataBufferInt)raster.getDataBuffer();
            ThemeReader.paintBackground(SunWritableRaster.stealData(dbi, 0),
                                        part.getControlName(c), part.getValue(),
                                        State.getValue(part, state),
                                        0, 0, w, h, w);
            SunWritableRaster.markDirty(dbi);
        }
        protected Image createImage(Component c, int w, int h,
                                    GraphicsConfiguration config, Object[] args) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
    }
    static class GlyphButton extends JButton {
        private Skin skin;
        public GlyphButton(Component parent, Part part) {
            XPStyle xp = getXP();
            skin = xp.getSkin(parent, part);
            setBorder(null);
            setContentAreaFilled(false);
            setMinimumSize(new Dimension(5, 5));
            setPreferredSize(new Dimension(16, 16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        public boolean isFocusTraversable() {
            return false;
        }
        protected State getState() {
            State state = State.NORMAL;
            if (!isEnabled()) {
                state = State.DISABLED;
            } else if (getModel().isPressed()) {
                state = State.PRESSED;
            } else if (getModel().isRollover()) {
                state = State.HOT;
            }
            return state;
        }
        public void paintComponent(Graphics g) {
            Dimension d = getSize();
            skin.paintSkin(g, 0, 0, d.width, d.height, getState());
        }
        public void setPart(Component parent, Part part) {
            XPStyle xp = getXP();
            skin = xp.getSkin(parent, part);
            revalidate();
            repaint();
        }
        protected void paintBorder(Graphics g) {
        }
    }
    private XPStyle() {
        flatMenus = getSysBoolean(Prop.FLATMENUS);
        colorMap  = new HashMap<String, Color>();
        borderMap = new HashMap<String, Border>();
    }
    private boolean getBoolean(Component c, Part part, State state, Prop prop) {
        return ThemeReader.getBoolean(part.getControlName(c), part.getValue(),
                                      State.getValue(part, state),
                                      prop.getValue());
    }
    static Dimension getPartSize(Part part, State state) {
        return ThemeReader.getPartSize(part.getControlName(null), part.getValue(),
                                       State.getValue(part, state));
    }
    private static boolean getSysBoolean(Prop prop) {
        return ThemeReader.getSysBoolean("window", prop.getValue());
    }
}

public class XMenuItemPeer implements MenuItemPeer {
    private XBaseMenuWindow container;
    private MenuItem target;
    private Rectangle bounds;
    private Point textOrigin;
    private final static int SEPARATOR_WIDTH = 20;
    private final static int SEPARATOR_HEIGHT = 5;
    private final static Field f_enabled;
    private final static Field f_label;
    private final static Field f_shortcut;
    private final static Method m_getFont;
    private final static Method m_isItemEnabled;
    private final static Method m_getActionCommand;
    static {
        f_enabled = SunToolkit.getField(MenuItem.class, "enabled");
        f_label = SunToolkit.getField(MenuItem.class, "label");
        f_shortcut = SunToolkit.getField(MenuItem.class, "shortcut");
        m_getFont = SunToolkit.getMethod(MenuComponent.class, "getFont_NoClientCode", null);
        m_getActionCommand = SunToolkit.getMethod(MenuItem.class, "getActionCommandImpl", null);
        m_isItemEnabled = SunToolkit.getMethod(MenuItem.class, "isItemEnabled", null);
    }
    private TextMetrics textMetrics;
    static class TextMetrics implements Cloneable {
        private Dimension textDimension;
        private int shortcutWidth;
        private int textBaseline;
        TextMetrics(Dimension textDimension, int shortcutWidth, int textBaseline) {
            this.textDimension = textDimension;
            this.shortcutWidth = shortcutWidth;
            this.textBaseline = textBaseline;
        }
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException ex) {
                throw new InternalError();
            }
        }
        Dimension getTextDimension() {
            return this.textDimension;
        }
        int getShortcutWidth() {
            return this.shortcutWidth;
        }
        int getTextBaseline() {
            return this.textBaseline;
        }
    }
    XMenuItemPeer(MenuItem target) {
        this.target = target;
    }
    public void dispose() {
    }
    public void setFont(Font font) {
        resetTextMetrics();
        repaintIfShowing();
    }
    public void setLabel(String label) {
        resetTextMetrics();
        repaintIfShowing();
    }
    public void setEnabled(boolean enabled) {
        repaintIfShowing();
    }
    public void enable() {
        setEnabled( true );
    }
    public void disable() {
        setEnabled( false );
    }
    MenuItem getTarget() {
        return this.target;
    }
    Font getTargetFont() {
        if (target == null) {
            return XWindow.getDefaultFont();
        }
        try {
            return (Font)m_getFont.invoke(target, new Object[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return XWindow.getDefaultFont();
    }
    String getTargetLabel() {
        if (target == null) {
            return "";
        }
        try {
            String label = (String)f_label.get(target);
            return (label == null) ? "" : label;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }
    boolean isTargetEnabled() {
        if (target == null) {
            return false;
        }
        try {
            return f_enabled.getBoolean(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
    boolean isTargetItemEnabled() {
        if (target == null) {
            return false;
        }
        try {
            return ((Boolean)m_isItemEnabled.invoke(target, new Object[0])).booleanValue();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }
    String getTargetActionCommand() {
        if (target == null) {
            return "";
        }
        try {
            return (String) m_getActionCommand.invoke(target,(Object[]) null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return "";
    }
    MenuShortcut getTargetShortcut() {
        if (target == null) {
            return null;
        }
        try {
            return (MenuShortcut)f_shortcut.get(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    String getShortcutText() {
        if (container == null) {
            return null;
        }
        if (container.getRootMenuWindow() instanceof XPopupMenuPeer) {
            return null;
        }
        MenuShortcut sc = getTargetShortcut();
        return (sc == null) ? null : sc.toString();
    }
    void setContainer(XBaseMenuWindow container) {
        synchronized(XBaseMenuWindow.getMenuTreeLock()) {
            this.container = container;
        }
    }
    XBaseMenuWindow getContainer() {
        return this.container;
    }
    boolean isSeparator() {
        boolean r = (getTargetLabel().equals("-"));
        return r;
    }
    boolean isContainerShowing() {
        if (container == null) {
            return false;
        }
        return container.isShowing();
    }
    void repaintIfShowing() {
        if (isContainerShowing()) {
            container.postPaintEvent();
        }
    }
    void action(long when) {
        if (!isSeparator() && isTargetItemEnabled()) {
            XWindow.postEventStatic(new ActionEvent(target, ActionEvent.ACTION_PERFORMED,
                                                    getTargetActionCommand(), when,
                                                    0));
        }
    }
    TextMetrics getTextMetrics() {
        TextMetrics textMetrics = this.textMetrics;
        if (textMetrics == null) {
            textMetrics = calcTextMetrics();
            this.textMetrics = textMetrics;
        }
        return textMetrics;
    }
    TextMetrics calcTextMetrics() {
        if (container == null) {
            return null;
        }
        if (isSeparator()) {
            return new TextMetrics(new Dimension(SEPARATOR_WIDTH, SEPARATOR_HEIGHT), 0, 0);
        }
        Graphics g = container.getGraphics();
        if (g == null) {
            return null;
        }
        try {
            g.setFont(getTargetFont());
            FontMetrics fm = g.getFontMetrics();
            String str = getTargetLabel();
            int width = fm.stringWidth(str);
            int height = fm.getHeight();
            Dimension textDimension = new Dimension(width, height);
            int textBaseline = fm.getHeight() - fm.getAscent();
            String sc = getShortcutText();
            int shortcutWidth = (sc == null) ? 0 : fm.stringWidth(sc);
            return new TextMetrics(textDimension, shortcutWidth, textBaseline);
        } finally {
            g.dispose();
        }
    }
    void resetTextMetrics() {
        textMetrics = null;
        if (container != null) {
            container.updateSize();
        }
    }
    void map(Rectangle bounds, Point textOrigin) {
        this.bounds = bounds;
        this.textOrigin = textOrigin;
    }
    Rectangle getBounds() {
        return bounds;
    }
    Point getTextOrigin() {
        return textOrigin;
    }
}

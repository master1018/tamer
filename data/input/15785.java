public class MotifButtonUI extends BasicButtonUI {
    protected Color selectColor;
    private boolean defaults_initialized = false;
    private static final Object MOTIF_BUTTON_UI_KEY = new Object();
    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        MotifButtonUI motifButtonUI =
                (MotifButtonUI) appContext.get(MOTIF_BUTTON_UI_KEY);
        if (motifButtonUI == null) {
            motifButtonUI = new MotifButtonUI();
            appContext.put(MOTIF_BUTTON_UI_KEY, motifButtonUI);
        }
        return motifButtonUI;
    }
    protected BasicButtonListener createButtonListener(AbstractButton b){
        return new MotifButtonListener(b);
    }
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if(!defaults_initialized) {
            selectColor = UIManager.getColor(getPropertyPrefix() + "select");
            defaults_initialized = true;
        }
        LookAndFeel.installProperty(b, "opaque", Boolean.FALSE);
    }
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }
    protected Color getSelectColor() {
        return selectColor;
    }
    public void paint(Graphics g, JComponent c) {
        fillContentArea( g, (AbstractButton)c , c.getBackground() );
        super.paint(g,c);
    }
    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
        Shape oldClip = g.getClip();
        Rectangle newClip =
            AbstractBorder.getInteriorRectangle(c, c.getBorder(), 0, 0,
                                                c.getWidth(), c.getHeight());
        Rectangle r = oldClip.getBounds();
        newClip =
            SwingUtilities.computeIntersection(r.x, r.y, r.width, r.height,
                                               newClip);
        g.setClip(newClip);
        super.paintIcon(g, c, iconRect);
        g.setClip(oldClip);
    }
    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect){
    }
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        fillContentArea( g, b , selectColor );
    }
    protected void fillContentArea( Graphics g, AbstractButton b, Color fillColor) {
        if (b.isContentAreaFilled()) {
            Insets margin = b.getMargin();
            Insets insets = b.getInsets();
            Dimension size = b.getSize();
            g.setColor(fillColor);
            g.fillRect(insets.left - margin.left,
                       insets.top - margin.top,
                       size.width - (insets.left-margin.left) - (insets.right - margin.right),
                       size.height - (insets.top-margin.top) - (insets.bottom - margin.bottom));
        }
    }
}

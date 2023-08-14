public class BasicToolTipUI extends ToolTipUI
{
    static BasicToolTipUI sharedInstance = new BasicToolTipUI();
    private static PropertyChangeListener sharedPropertyChangedListener;
    private PropertyChangeListener propertyChangeListener;
    public static ComponentUI createUI(JComponent c) {
        return sharedInstance;
    }
    public BasicToolTipUI() {
        super();
    }
    public void installUI(JComponent c) {
        installDefaults(c);
        installComponents(c);
        installListeners(c);
    }
    public void uninstallUI(JComponent c) {
        uninstallDefaults(c);
        uninstallComponents(c);
        uninstallListeners(c);
    }
    protected void installDefaults(JComponent c){
        LookAndFeel.installColorsAndFont(c, "ToolTip.background",
                                         "ToolTip.foreground",
                                         "ToolTip.font");
        LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);
        componentChanged(c);
    }
   protected void uninstallDefaults(JComponent c){
        LookAndFeel.uninstallBorder(c);
    }
    private void installComponents(JComponent c){
        BasicHTML.updateRenderer(c, ((JToolTip)c).getTipText());
    }
    private void uninstallComponents(JComponent c){
        BasicHTML.updateRenderer(c, "");
    }
    protected void installListeners(JComponent c) {
        propertyChangeListener = createPropertyChangeListener(c);
        c.addPropertyChangeListener(propertyChangeListener);
    }
    protected void uninstallListeners(JComponent c) {
        c.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = null;
    }
    private PropertyChangeListener createPropertyChangeListener(JComponent c) {
        if (sharedPropertyChangedListener == null) {
            sharedPropertyChangedListener = new PropertyChangeHandler();
        }
        return sharedPropertyChangedListener;
    }
    public void paint(Graphics g, JComponent c) {
        Font font = c.getFont();
        FontMetrics metrics = SwingUtilities2.getFontMetrics(c, g, font);
        Dimension size = c.getSize();
        g.setColor(c.getForeground());
        String tipText = ((JToolTip)c).getTipText();
        if (tipText == null) {
            tipText = "";
        }
        Insets insets = c.getInsets();
        Rectangle paintTextR = new Rectangle(
            insets.left + 3,
            insets.top,
            size.width - (insets.left + insets.right) - 6,
            size.height - (insets.top + insets.bottom));
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            v.paint(g, paintTextR);
        } else {
            g.setFont(font);
            SwingUtilities2.drawString(c, g, tipText, paintTextR.x,
                                  paintTextR.y + metrics.getAscent());
        }
    }
    public Dimension getPreferredSize(JComponent c) {
        Font font = c.getFont();
        FontMetrics fm = c.getFontMetrics(font);
        Insets insets = c.getInsets();
        Dimension prefSize = new Dimension(insets.left+insets.right,
                                           insets.top+insets.bottom);
        String text = ((JToolTip)c).getTipText();
        if ((text == null) || text.equals("")) {
            text = "";
        }
        else {
            View v = (c != null) ? (View) c.getClientProperty("html") : null;
            if (v != null) {
                prefSize.width += (int) v.getPreferredSpan(View.X_AXIS) + 6;
                prefSize.height += (int) v.getPreferredSpan(View.Y_AXIS);
            } else {
                prefSize.width += SwingUtilities2.stringWidth(c,fm,text) + 6;
                prefSize.height += fm.getHeight();
            }
        }
        return prefSize;
    }
    public Dimension getMinimumSize(JComponent c) {
        Dimension d = getPreferredSize(c);
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);
        }
        return d;
    }
    public Dimension getMaximumSize(JComponent c) {
        Dimension d = getPreferredSize(c);
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
        }
        return d;
    }
    private void componentChanged(JComponent c) {
        JComponent comp = ((JToolTip)c).getComponent();
        if (comp != null && !(comp.isEnabled())) {
            if (UIManager.getBorder("ToolTip.borderInactive") != null) {
                LookAndFeel.installBorder(c, "ToolTip.borderInactive");
            }
            else {
                LookAndFeel.installBorder(c, "ToolTip.border");
            }
            if (UIManager.getColor("ToolTip.backgroundInactive") != null) {
                LookAndFeel.installColors(c,"ToolTip.backgroundInactive",
                                          "ToolTip.foregroundInactive");
            }
            else {
                LookAndFeel.installColors(c,"ToolTip.background",
                                          "ToolTip.foreground");
            }
        } else {
            LookAndFeel.installBorder(c, "ToolTip.border");
            LookAndFeel.installColors(c, "ToolTip.background",
                                      "ToolTip.foreground");
        }
    }
    private static class PropertyChangeHandler implements
                                 PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name.equals("tiptext") || "font".equals(name) ||
                "foreground".equals(name)) {
                JToolTip tip = ((JToolTip) e.getSource());
                String text = tip.getTipText();
                BasicHTML.updateRenderer(tip, text);
            }
            else if ("component".equals(name)) {
                JToolTip tip = ((JToolTip) e.getSource());
                if (tip.getUI() instanceof BasicToolTipUI) {
                    ((BasicToolTipUI)tip.getUI()).componentChanged(tip);
                }
            }
        }
    }
}

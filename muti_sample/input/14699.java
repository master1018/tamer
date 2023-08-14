public class MetalToolTipUI extends BasicToolTipUI {
    static MetalToolTipUI sharedInstance = new MetalToolTipUI();
    private Font smallFont;
    private JToolTip tip;
    public static final int padSpaceBetweenStrings = 12;
    private String acceleratorDelimiter;
    public MetalToolTipUI() {
        super();
    }
    public static ComponentUI createUI(JComponent c) {
        return sharedInstance;
    }
    public void installUI(JComponent c) {
        super.installUI(c);
        tip = (JToolTip)c;
        Font f = c.getFont();
        smallFont = new Font( f.getName(), f.getStyle(), f.getSize() - 2 );
        acceleratorDelimiter = UIManager.getString( "MenuItem.acceleratorDelimiter" );
        if ( acceleratorDelimiter == null ) { acceleratorDelimiter = "-"; }
    }
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        tip = null;
    }
    public void paint(Graphics g, JComponent c) {
        JToolTip tip = (JToolTip)c;
        Font font = c.getFont();
        FontMetrics metrics = SwingUtilities2.getFontMetrics(c, g, font);
        Dimension size = c.getSize();
        int accelBL;
        g.setColor(c.getForeground());
        String tipText = tip.getTipText();
        if (tipText == null) {
            tipText = "";
        }
        String accelString = getAcceleratorString(tip);
        FontMetrics accelMetrics = SwingUtilities2.getFontMetrics(c, g, smallFont);
        int accelSpacing = calcAccelSpacing(c, accelMetrics, accelString);
        Insets insets = tip.getInsets();
        Rectangle paintTextR = new Rectangle(
            insets.left + 3,
            insets.top,
            size.width - (insets.left + insets.right) - 6 - accelSpacing,
            size.height - (insets.top + insets.bottom));
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            v.paint(g, paintTextR);
            accelBL = BasicHTML.getHTMLBaseline(v, paintTextR.width,
                                                  paintTextR.height);
        } else {
            g.setFont(font);
            SwingUtilities2.drawString(tip, g, tipText, paintTextR.x,
                                  paintTextR.y + metrics.getAscent());
            accelBL = metrics.getAscent();
        }
        if (!accelString.equals("")) {
            g.setFont(smallFont);
            g.setColor( MetalLookAndFeel.getPrimaryControlDarkShadow() );
            SwingUtilities2.drawString(tip, g, accelString,
                                       tip.getWidth() - 1 - insets.right
                                           - accelSpacing
                                           + padSpaceBetweenStrings
                                           - 3,
                                       paintTextR.y + accelBL);
        }
    }
    private int calcAccelSpacing(JComponent c, FontMetrics fm, String accel) {
        return accel.equals("")
               ? 0
               : padSpaceBetweenStrings +
                 SwingUtilities2.stringWidth(c, fm, accel);
    }
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        String key = getAcceleratorString((JToolTip)c);
        if (!(key.equals(""))) {
            d.width += calcAccelSpacing(c, c.getFontMetrics(smallFont), key);
        }
        return d;
    }
    protected boolean isAcceleratorHidden() {
        Boolean b = (Boolean)UIManager.get("ToolTip.hideAccelerator");
        return b != null && b.booleanValue();
    }
    private String getAcceleratorString(JToolTip tip) {
        this.tip = tip;
        String retValue = getAcceleratorString();
        this.tip = null;
        return retValue;
    }
    public String getAcceleratorString() {
        if (tip == null || isAcceleratorHidden()) {
            return "";
        }
        JComponent comp = tip.getComponent();
        if (!(comp instanceof AbstractButton)) {
            return "";
        }
        KeyStroke[] keys = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).keys();
        if (keys == null) {
            return "";
        }
        String controlKeyStr = "";
        for (int i = 0; i < keys.length; i++) {
            int mod = keys[i].getModifiers();
            controlKeyStr = KeyEvent.getKeyModifiersText(mod) +
                            acceleratorDelimiter +
                            KeyEvent.getKeyText(keys[i].getKeyCode());
            break;
        }
        return controlKeyStr;
    }
}

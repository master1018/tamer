public class MetalRadioButtonUI extends BasicRadioButtonUI {
    private static final Object METAL_RADIO_BUTTON_UI_KEY = new Object();
    protected Color focusColor;
    protected Color selectColor;
    protected Color disabledTextColor;
    private boolean defaults_initialized = false;
    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        MetalRadioButtonUI metalRadioButtonUI =
                (MetalRadioButtonUI) appContext.get(METAL_RADIO_BUTTON_UI_KEY);
        if (metalRadioButtonUI == null) {
            metalRadioButtonUI = new MetalRadioButtonUI();
            appContext.put(METAL_RADIO_BUTTON_UI_KEY, metalRadioButtonUI);
        }
        return metalRadioButtonUI;
    }
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if(!defaults_initialized) {
            focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
            selectColor = UIManager.getColor(getPropertyPrefix() + "select");
            disabledTextColor = UIManager.getColor(getPropertyPrefix() + "disabledText");
            defaults_initialized = true;
        }
        LookAndFeel.installProperty(b, "opaque", Boolean.TRUE);
    }
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }
    protected Color getSelectColor() {
        return selectColor;
    }
    protected Color getDisabledTextColor() {
        return disabledTextColor;
    }
    protected Color getFocusColor() {
        return focusColor;
    }
    public synchronized void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        Dimension size = c.getSize();
        int w = size.width;
        int h = size.height;
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = SwingUtilities2.getFontMetrics(c, g, f);
        Rectangle viewRect = new Rectangle(size);
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Insets i = c.getInsets();
        viewRect.x += i.left;
        viewRect.y += i.top;
        viewRect.width -= (i.right + viewRect.x);
        viewRect.height -= (i.bottom + viewRect.y);
        Icon altIcon = b.getIcon();
        Icon selectedIcon = null;
        Icon disabledIcon = null;
        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), altIcon != null ? altIcon : getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect, b.getIconTextGap());
        if(c.isOpaque()) {
            g.setColor(b.getBackground());
            g.fillRect(0,0, size.width, size.height);
        }
        if(altIcon != null) {
            if(!model.isEnabled()) {
                if(model.isSelected()) {
                   altIcon = b.getDisabledSelectedIcon();
                } else {
                   altIcon = b.getDisabledIcon();
                }
            } else if(model.isPressed() && model.isArmed()) {
                altIcon = b.getPressedIcon();
                if(altIcon == null) {
                    altIcon = b.getSelectedIcon();
                }
            } else if(model.isSelected()) {
                if(b.isRolloverEnabled() && model.isRollover()) {
                        altIcon = b.getRolloverSelectedIcon();
                        if (altIcon == null) {
                                altIcon = b.getSelectedIcon();
                        }
                } else {
                        altIcon = b.getSelectedIcon();
                }
            } else if(b.isRolloverEnabled() && model.isRollover()) {
                altIcon = b.getRolloverIcon();
            }
            if(altIcon == null) {
                altIcon = b.getIcon();
            }
            altIcon.paintIcon(c, g, iconRect.x, iconRect.y);
        } else {
            getDefaultIcon().paintIcon(c, g, iconRect.x, iconRect.y);
        }
        if(text != null) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, textRect);
            } else {
               int mnemIndex = b.getDisplayedMnemonicIndex();
               if(model.isEnabled()) {
                   g.setColor(b.getForeground());
               } else {
                   g.setColor(getDisabledTextColor());
               }
               SwingUtilities2.drawStringUnderlineCharAt(c,g,text,
                       mnemIndex, textRect.x, textRect.y + fm.getAscent());
           }
           if(b.hasFocus() && b.isFocusPainted() &&
              textRect.width > 0 && textRect.height > 0 ) {
               paintFocus(g,textRect,size);
           }
        }
    }
    protected void paintFocus(Graphics g, Rectangle t, Dimension d){
        g.setColor(getFocusColor());
        g.drawRect(t.x-1, t.y-1, t.width+1, t.height+1);
    }
}

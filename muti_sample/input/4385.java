public class BasicButtonUI extends ButtonUI{
    protected int defaultTextIconGap;
    private int shiftOffset = 0;
    protected int defaultTextShiftOffset;
    private final static String propertyPrefix = "Button" + ".";
    private static final Object BASIC_BUTTON_UI_KEY = new Object();
    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        BasicButtonUI buttonUI =
                (BasicButtonUI) appContext.get(BASIC_BUTTON_UI_KEY);
        if (buttonUI == null) {
            buttonUI = new BasicButtonUI();
            appContext.put(BASIC_BUTTON_UI_KEY, buttonUI);
        }
        return buttonUI;
    }
    protected String getPropertyPrefix() {
        return propertyPrefix;
    }
    public void installUI(JComponent c) {
        installDefaults((AbstractButton) c);
        installListeners((AbstractButton) c);
        installKeyboardActions((AbstractButton) c);
        BasicHTML.updateRenderer(c, ((AbstractButton) c).getText());
    }
    protected void installDefaults(AbstractButton b) {
        String pp = getPropertyPrefix();
        defaultTextShiftOffset = UIManager.getInt(pp + "textShiftOffset");
        if (b.isContentAreaFilled()) {
            LookAndFeel.installProperty(b, "opaque", Boolean.TRUE);
        } else {
            LookAndFeel.installProperty(b, "opaque", Boolean.FALSE);
        }
        if(b.getMargin() == null || (b.getMargin() instanceof UIResource)) {
            b.setMargin(UIManager.getInsets(pp + "margin"));
        }
        LookAndFeel.installColorsAndFont(b, pp + "background",
                                         pp + "foreground", pp + "font");
        LookAndFeel.installBorder(b, pp + "border");
        Object rollover = UIManager.get(pp + "rollover");
        if (rollover != null) {
            LookAndFeel.installProperty(b, "rolloverEnabled", rollover);
        }
        LookAndFeel.installProperty(b, "iconTextGap", Integer.valueOf(4));
    }
    protected void installListeners(AbstractButton b) {
        BasicButtonListener listener = createButtonListener(b);
        if(listener != null) {
            b.addMouseListener(listener);
            b.addMouseMotionListener(listener);
            b.addFocusListener(listener);
            b.addPropertyChangeListener(listener);
            b.addChangeListener(listener);
        }
    }
    protected void installKeyboardActions(AbstractButton b){
        BasicButtonListener listener = getButtonListener(b);
        if(listener != null) {
            listener.installKeyboardActions(b);
        }
    }
    public void uninstallUI(JComponent c) {
        uninstallKeyboardActions((AbstractButton) c);
        uninstallListeners((AbstractButton) c);
        uninstallDefaults((AbstractButton) c);
        BasicHTML.updateRenderer(c, "");
    }
    protected void uninstallKeyboardActions(AbstractButton b) {
        BasicButtonListener listener = getButtonListener(b);
        if(listener != null) {
            listener.uninstallKeyboardActions(b);
        }
    }
    protected void uninstallListeners(AbstractButton b) {
        BasicButtonListener listener = getButtonListener(b);
        if(listener != null) {
            b.removeMouseListener(listener);
            b.removeMouseMotionListener(listener);
            b.removeFocusListener(listener);
            b.removeChangeListener(listener);
            b.removePropertyChangeListener(listener);
        }
    }
    protected void uninstallDefaults(AbstractButton b) {
        LookAndFeel.uninstallBorder(b);
    }
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new BasicButtonListener(b);
    }
    public int getDefaultTextIconGap(AbstractButton b) {
        return defaultTextIconGap;
    }
    private static Rectangle viewRect = new Rectangle();
    private static Rectangle textRect = new Rectangle();
    private static Rectangle iconRect = new Rectangle();
    public void paint(Graphics g, JComponent c)
    {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        String text = layout(b, SwingUtilities2.getFontMetrics(b, g),
               b.getWidth(), b.getHeight());
        clearTextShiftOffset();
        if (model.isArmed() && model.isPressed()) {
            paintButtonPressed(g,b);
        }
        if(b.getIcon() != null) {
            paintIcon(g,c,iconRect);
        }
        if (text != null && !text.equals("")){
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, textRect);
            } else {
                paintText(g, b, textRect, text);
            }
        }
        if (b.isFocusPainted() && b.hasFocus()) {
            paintFocus(g,b,viewRect,textRect,iconRect);
        }
    }
    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect){
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            Icon icon = b.getIcon();
            Icon tmpIcon = null;
            if(icon == null) {
               return;
            }
            Icon selectedIcon = null;
            if (model.isSelected()) {
                selectedIcon = b.getSelectedIcon();
                if (selectedIcon != null) {
                    icon = selectedIcon;
                }
            }
            if(!model.isEnabled()) {
                if(model.isSelected()) {
                   tmpIcon = b.getDisabledSelectedIcon();
                   if (tmpIcon == null) {
                       tmpIcon = selectedIcon;
                   }
                }
                if (tmpIcon == null) {
                    tmpIcon = b.getDisabledIcon();
                }
            } else if(model.isPressed() && model.isArmed()) {
                tmpIcon = b.getPressedIcon();
                if(tmpIcon != null) {
                    clearTextShiftOffset();
                }
            } else if(b.isRolloverEnabled() && model.isRollover()) {
                if(model.isSelected()) {
                   tmpIcon = b.getRolloverSelectedIcon();
                   if (tmpIcon == null) {
                       tmpIcon = selectedIcon;
                   }
                }
                if (tmpIcon == null) {
                    tmpIcon = b.getRolloverIcon();
                }
            }
            if(tmpIcon != null) {
                icon = tmpIcon;
            }
            if(model.isPressed() && model.isArmed()) {
                icon.paintIcon(c, g, iconRect.x + getTextShiftOffset(),
                        iconRect.y + getTextShiftOffset());
            } else {
                icon.paintIcon(c, g, iconRect.x, iconRect.y);
            }
    }
    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        FontMetrics fm = SwingUtilities2.getFontMetrics(c, g);
        int mnemonicIndex = b.getDisplayedMnemonicIndex();
        if(model.isEnabled()) {
            g.setColor(b.getForeground());
            SwingUtilities2.drawStringUnderlineCharAt(c, g,text, mnemonicIndex,
                                          textRect.x + getTextShiftOffset(),
                                          textRect.y + fm.getAscent() + getTextShiftOffset());
        }
        else {
            g.setColor(b.getBackground().brighter());
            SwingUtilities2.drawStringUnderlineCharAt(c, g,text, mnemonicIndex,
                                          textRect.x, textRect.y + fm.getAscent());
            g.setColor(b.getBackground().darker());
            SwingUtilities2.drawStringUnderlineCharAt(c, g,text, mnemonicIndex,
                                          textRect.x - 1, textRect.y + fm.getAscent() - 1);
        }
    }
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        paintText(g, (JComponent)b, textRect, text);
    }
    protected void paintFocus(Graphics g, AbstractButton b,
                              Rectangle viewRect, Rectangle textRect, Rectangle iconRect){
    }
    protected void paintButtonPressed(Graphics g, AbstractButton b){
    }
    protected void clearTextShiftOffset(){
        this.shiftOffset = 0;
    }
    protected void setTextShiftOffset(){
        this.shiftOffset = defaultTextShiftOffset;
    }
    protected int getTextShiftOffset() {
        return shiftOffset;
    }
    public Dimension getMinimumSize(JComponent c) {
        Dimension d = getPreferredSize(c);
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);
        }
        return d;
    }
    public Dimension getPreferredSize(JComponent c) {
        AbstractButton b = (AbstractButton)c;
        return BasicGraphicsUtils.getPreferredButtonSize(b, b.getIconTextGap());
    }
    public Dimension getMaximumSize(JComponent c) {
        Dimension d = getPreferredSize(c);
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
        }
        return d;
    }
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        AbstractButton b = (AbstractButton)c;
        String text = b.getText();
        if (text == null || "".equals(text)) {
            return -1;
        }
        FontMetrics fm = b.getFontMetrics(b.getFont());
        layout(b, fm, width, height);
        return BasicHTML.getBaseline(b, textRect.y, fm.getAscent(),
                                     textRect.width, textRect.height);
    }
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c) {
        super.getBaselineResizeBehavior(c);
        if (c.getClientProperty(BasicHTML.propertyKey) != null) {
            return Component.BaselineResizeBehavior.OTHER;
        }
        switch(((AbstractButton)c).getVerticalAlignment()) {
        case AbstractButton.TOP:
            return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
        case AbstractButton.BOTTOM:
            return Component.BaselineResizeBehavior.CONSTANT_DESCENT;
        case AbstractButton.CENTER:
            return Component.BaselineResizeBehavior.CENTER_OFFSET;
        }
        return Component.BaselineResizeBehavior.OTHER;
    }
    private String layout(AbstractButton b, FontMetrics fm,
                          int width, int height) {
        Insets i = b.getInsets();
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = width - (i.right + viewRect.x);
        viewRect.height = height - (i.bottom + viewRect.y);
        textRect.x = textRect.y = textRect.width = textRect.height = 0;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        return SwingUtilities.layoutCompoundLabel(
            b, fm, b.getText(), b.getIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect,
            b.getText() == null ? 0 : b.getIconTextGap());
    }
    private BasicButtonListener getButtonListener(AbstractButton b) {
        MouseMotionListener[] listeners = b.getMouseMotionListeners();
        if (listeners != null) {
            for (MouseMotionListener listener : listeners) {
                if (listener instanceof BasicButtonListener) {
                    return (BasicButtonListener) listener;
                }
            }
        }
        return null;
    }
}

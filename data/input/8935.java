public class BasicToggleButtonUI extends BasicButtonUI {
    private static final Object BASIC_TOGGLE_BUTTON_UI_KEY = new Object();
    private final static String propertyPrefix = "ToggleButton" + ".";
    public static ComponentUI createUI(JComponent b) {
        AppContext appContext = AppContext.getAppContext();
        BasicToggleButtonUI toggleButtonUI =
                (BasicToggleButtonUI) appContext.get(BASIC_TOGGLE_BUTTON_UI_KEY);
        if (toggleButtonUI == null) {
            toggleButtonUI = new BasicToggleButtonUI();
            appContext.put(BASIC_TOGGLE_BUTTON_UI_KEY, toggleButtonUI);
        }
        return toggleButtonUI;
    }
    protected String getPropertyPrefix() {
        return propertyPrefix;
    }
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        Dimension size = b.getSize();
        FontMetrics fm = g.getFontMetrics();
        Insets i = c.getInsets();
        Rectangle viewRect = new Rectangle(size);
        viewRect.x += i.left;
        viewRect.y += i.top;
        viewRect.width -= (i.right + viewRect.x);
        viewRect.height -= (i.bottom + viewRect.y);
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Font f = c.getFont();
        g.setFont(f);
        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), b.getIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect,
            b.getText() == null ? 0 : b.getIconTextGap());
        g.setColor(b.getBackground());
        if (model.isArmed() && model.isPressed() || model.isSelected()) {
            paintButtonPressed(g,b);
        }
        if(b.getIcon() != null) {
            paintIcon(g, b, iconRect);
        }
        if(text != null && !text.equals("")) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
               v.paint(g, textRect);
            } else {
               paintText(g, b, textRect, text);
            }
        }
        if (b.isFocusPainted() && b.hasFocus()) {
            paintFocus(g, b, viewRect, textRect, iconRect);
        }
    }
    protected void paintIcon(Graphics g, AbstractButton b, Rectangle iconRect) {
        ButtonModel model = b.getModel();
        Icon icon = null;
        if(!model.isEnabled()) {
            if(model.isSelected()) {
               icon = b.getDisabledSelectedIcon();
            } else {
               icon = b.getDisabledIcon();
            }
        } else if(model.isPressed() && model.isArmed()) {
            icon = b.getPressedIcon();
            if(icon == null) {
                icon = b.getSelectedIcon();
            }
        } else if(model.isSelected()) {
            if(b.isRolloverEnabled() && model.isRollover()) {
                icon = b.getRolloverSelectedIcon();
                if (icon == null) {
                    icon = b.getSelectedIcon();
                }
            } else {
                icon = b.getSelectedIcon();
            }
        } else if(b.isRolloverEnabled() && model.isRollover()) {
            icon = b.getRolloverIcon();
        }
        if(icon == null) {
            icon = b.getIcon();
        }
        icon.paintIcon(b, g, iconRect.x, iconRect.y);
    }
    protected int getTextShiftOffset() {
        return 0;
    }
}

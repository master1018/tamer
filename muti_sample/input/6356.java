public class WindowsButtonUI extends BasicButtonUI
{
    protected int dashedRectGapX;
    protected int dashedRectGapY;
    protected int dashedRectGapWidth;
    protected int dashedRectGapHeight;
    protected Color focusColor;
    private boolean defaults_initialized = false;
    private static final Object WINDOWS_BUTTON_UI_KEY = new Object();
    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        WindowsButtonUI windowsButtonUI =
                (WindowsButtonUI) appContext.get(WINDOWS_BUTTON_UI_KEY);
        if (windowsButtonUI == null) {
            windowsButtonUI = new WindowsButtonUI();
            appContext.put(WINDOWS_BUTTON_UI_KEY, windowsButtonUI);
        }
        return windowsButtonUI;
    }
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if(!defaults_initialized) {
            String pp = getPropertyPrefix();
            dashedRectGapX = UIManager.getInt(pp + "dashedRectGapX");
            dashedRectGapY = UIManager.getInt(pp + "dashedRectGapY");
            dashedRectGapWidth = UIManager.getInt(pp + "dashedRectGapWidth");
            dashedRectGapHeight = UIManager.getInt(pp + "dashedRectGapHeight");
            focusColor = UIManager.getColor(pp + "focus");
            defaults_initialized = true;
        }
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            b.setBorder(xp.getBorder(b, getXPButtonType(b)));
            LookAndFeel.installProperty(b, "rolloverEnabled", Boolean.TRUE);
        }
    }
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }
    protected Color getFocusColor() {
        return focusColor;
    }
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        WindowsGraphicsUtils.paintText(g, b, textRect, text, getTextShiftOffset());
    }
    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect){
        int width = b.getWidth();
        int height = b.getHeight();
        g.setColor(getFocusColor());
        BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
                                          width - dashedRectGapWidth, height - dashedRectGapHeight);
    }
    protected void paintButtonPressed(Graphics g, AbstractButton b){
        setTextShiftOffset();
    }
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        AbstractButton b = (AbstractButton)c;
        if (d != null && b.isFocusPainted()) {
            if(d.width % 2 == 0) { d.width += 1; }
            if(d.height % 2 == 0) { d.height += 1; }
        }
        return d;
    }
    private Rectangle viewRect = new Rectangle();
    public void paint(Graphics g, JComponent c) {
        if (XPStyle.getXP() != null) {
            WindowsButtonUI.paintXPButtonBackground(g, c);
        }
        super.paint(g, c);
    }
    static Part getXPButtonType(AbstractButton b) {
        if(b instanceof JCheckBox) {
            return Part.BP_CHECKBOX;
        }
        if(b instanceof JRadioButton) {
            return Part.BP_RADIOBUTTON;
        }
        boolean toolbar = (b.getParent() instanceof JToolBar);
        return toolbar ? Part.TP_BUTTON : Part.BP_PUSHBUTTON;
    }
    static State getXPButtonState(AbstractButton b) {
        Part part = getXPButtonType(b);
        ButtonModel model = b.getModel();
        State state = State.NORMAL;
        switch (part) {
        case BP_RADIOBUTTON:
        case BP_CHECKBOX:
            if (! model.isEnabled()) {
                state = (model.isSelected()) ? State.CHECKEDDISABLED
                    : State.UNCHECKEDDISABLED;
            } else if (model.isPressed() && model.isArmed()) {
                state = (model.isSelected()) ? State.CHECKEDPRESSED
                    : State.UNCHECKEDPRESSED;
            } else if (model.isRollover()) {
                state = (model.isSelected()) ? State.CHECKEDHOT
                    : State.UNCHECKEDHOT;
            } else {
                state = (model.isSelected()) ? State.CHECKEDNORMAL
                    : State.UNCHECKEDNORMAL;
            }
            break;
        case BP_PUSHBUTTON:
        case TP_BUTTON:
            boolean toolbar = (b.getParent() instanceof JToolBar);
            if (toolbar) {
                if (model.isArmed() && model.isPressed()) {
                    state = State.PRESSED;
                } else if (!model.isEnabled()) {
                    state = State.DISABLED;
                } else if (model.isSelected() && model.isRollover()) {
                    state = State.HOTCHECKED;
                } else if (model.isSelected()) {
                    state = State.CHECKED;
                } else if (model.isRollover()) {
                    state = State.HOT;
                } else if (b.hasFocus()) {
                    state = State.HOT;
                }
            } else {
                if ((model.isArmed() && model.isPressed())
                      || model.isSelected()) {
                    state = State.PRESSED;
                } else if (!model.isEnabled()) {
                    state = State.DISABLED;
                } else if (model.isRollover() || model.isPressed()) {
                    state = State.HOT;
                } else if (b instanceof JButton
                           && ((JButton)b).isDefaultButton()) {
                    state = State.DEFAULTED;
                } else if (b.hasFocus()) {
                    state = State.HOT;
                }
            }
            break;
        default :
            state = State.NORMAL;
        }
        return state;
    }
    static void paintXPButtonBackground(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton)c;
        XPStyle xp = XPStyle.getXP();
        Part part = getXPButtonType(b);
        if (b.isContentAreaFilled() && xp != null) {
            Skin skin = xp.getSkin(b, part);
            State state = getXPButtonState(b);
            Dimension d = c.getSize();
            int dx = 0;
            int dy = 0;
            int dw = d.width;
            int dh = d.height;
            Border border = c.getBorder();
            Insets insets;
            if (border != null) {
                insets = WindowsButtonUI.getOpaqueInsets(border, c);
            } else {
                insets = c.getInsets();
            }
            if (insets != null) {
                dx += insets.left;
                dy += insets.top;
                dw -= (insets.left + insets.right);
                dh -= (insets.top + insets.bottom);
            }
            skin.paintSkin(g, dx, dy, dw, dh, state);
        }
    }
    private static Insets getOpaqueInsets(Border b, Component c) {
        if (b == null) {
            return null;
        }
        if (b.isBorderOpaque()) {
            return b.getBorderInsets(c);
        } else if (b instanceof CompoundBorder) {
            CompoundBorder cb = (CompoundBorder)b;
            Insets iOut = getOpaqueInsets(cb.getOutsideBorder(), c);
            if (iOut != null && iOut.equals(cb.getOutsideBorder().getBorderInsets(c))) {
                Insets iIn = getOpaqueInsets(cb.getInsideBorder(), c);
                if (iIn == null) {
                    return iOut;
                } else {
                    return new Insets(iOut.top + iIn.top, iOut.left + iIn.left,
                                      iOut.bottom + iIn.bottom, iOut.right + iIn.right);
                }
            } else {
                return iOut;
            }
        } else {
            return null;
        }
    }
}

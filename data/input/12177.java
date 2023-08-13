public class WindowsGraphicsUtils {
    public static void paintText(Graphics g, AbstractButton b,
                                        Rectangle textRect, String text,
                                        int textShiftOffset) {
        FontMetrics fm = SwingUtilities2.getFontMetrics(b, g);
        int mnemIndex = b.getDisplayedMnemonicIndex();
        if (WindowsLookAndFeel.isMnemonicHidden() == true) {
            mnemIndex = -1;
        }
        XPStyle xp = XPStyle.getXP();
        if (xp != null && !(b instanceof JMenuItem)) {
            paintXPText(b, g, textRect.x + textShiftOffset,
                        textRect.y + fm.getAscent() + textShiftOffset,
                        text, mnemIndex);
        } else {
            paintClassicText(b, g, textRect.x + textShiftOffset,
                             textRect.y + fm.getAscent() + textShiftOffset,
                             text, mnemIndex);
        }
    }
    static void paintClassicText(AbstractButton b, Graphics g, int x, int y,
                                 String text, int mnemIndex) {
        ButtonModel model = b.getModel();
        Color color = b.getForeground();
        if(model.isEnabled()) {
            if(!(b instanceof JMenuItem && model.isArmed())
                && !(b instanceof JMenu && (model.isSelected() || model.isRollover()))) {
                g.setColor(b.getForeground());
            }
            SwingUtilities2.drawStringUnderlineCharAt(b, g,text, mnemIndex, x, y);
        } else {        
            color        = UIManager.getColor("Button.shadow");
            Color shadow = UIManager.getColor("Button.disabledShadow");
            if(model.isArmed()) {
                color = UIManager.getColor("Button.disabledForeground");
            } else {
                if (shadow == null) {
                    shadow = b.getBackground().darker();
                }
                g.setColor(shadow);
                SwingUtilities2.drawStringUnderlineCharAt(b, g, text, mnemIndex,
                                                          x + 1, y + 1);
            }
            if (color == null) {
                color = b.getBackground().brighter();
            }
            g.setColor(color);
            SwingUtilities2.drawStringUnderlineCharAt(b, g, text, mnemIndex, x, y);
        }
    }
    static void paintXPText(AbstractButton b, Graphics g, int x, int y,
                            String text, int mnemIndex) {
        Part part = WindowsButtonUI.getXPButtonType(b);
        State state = WindowsButtonUI.getXPButtonState(b);
        paintXPText(b, part, state, g, x, y, text, mnemIndex);
    }
    static void paintXPText(AbstractButton b, Part part, State state,
            Graphics g, int x, int y, String text, int mnemIndex) {
        XPStyle xp = XPStyle.getXP();
        Color textColor = b.getForeground();
        if (textColor instanceof UIResource) {
            textColor = xp.getColor(b, part, state, Prop.TEXTCOLOR, b.getForeground());
            if (part == Part.TP_BUTTON && state == State.DISABLED) {
                Color enabledColor = xp.getColor(b, part, State.NORMAL,
                                     Prop.TEXTCOLOR, b.getForeground());
                if(textColor.equals(enabledColor)) {
                    textColor = xp.getColor(b, Part.BP_PUSHBUTTON, state,
                                Prop.TEXTCOLOR, textColor);
                }
            }
            TypeEnum shadowType = xp.getTypeEnum(b, part,
                                                 state, Prop.TEXTSHADOWTYPE);
            if (shadowType == TypeEnum.TST_SINGLE ||
                        shadowType == TypeEnum.TST_CONTINUOUS) {
                Color shadowColor = xp.getColor(b, part, state,
                                                Prop.TEXTSHADOWCOLOR, Color.black);
                Point offset = xp.getPoint(b, part, state, Prop.TEXTSHADOWOFFSET);
                if (offset != null) {
                    g.setColor(shadowColor);
                    SwingUtilities2.drawStringUnderlineCharAt(b, g, text, mnemIndex,
                                                              x + offset.x,
                                                              y + offset.y);
                }
            }
        }
        g.setColor(textColor);
        SwingUtilities2.drawStringUnderlineCharAt(b, g, text, mnemIndex, x, y);
    }
    static boolean isLeftToRight(Component c) {
        return c.getComponentOrientation().isLeftToRight();
    }
    static void repaintMnemonicsInWindow(Window w) {
        if(w == null || !w.isShowing()) {
            return;
        }
        Window[] ownedWindows = w.getOwnedWindows();
        for(int i=0;i<ownedWindows.length;i++) {
            repaintMnemonicsInWindow(ownedWindows[i]);
        }
        repaintMnemonicsInContainer(w);
    }
    static void repaintMnemonicsInContainer(Container cont) {
        Component c;
        for(int i=0; i<cont.getComponentCount(); i++) {
            c = cont.getComponent(i);
            if(c == null || !c.isVisible()) {
                continue;
            }
            if(c instanceof AbstractButton
               && ((AbstractButton)c).getMnemonic() != '\0') {
                c.repaint();
                continue;
            } else if(c instanceof JLabel
                      && ((JLabel)c).getDisplayedMnemonic() != '\0') {
                c.repaint();
                continue;
            }
            if(c instanceof Container) {
                repaintMnemonicsInContainer((Container)c);
            }
        }
    }
}

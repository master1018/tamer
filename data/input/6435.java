public class WindowsMenuItemUI extends BasicMenuItemUI {
    final WindowsMenuItemUIAccessor accessor =
        new  WindowsMenuItemUIAccessor() {
            public JMenuItem getMenuItem() {
                return menuItem;
            }
            public State getState(JMenuItem menuItem) {
                return WindowsMenuItemUI.getState(this, menuItem);
            }
            public Part getPart(JMenuItem menuItem) {
                return WindowsMenuItemUI.getPart(this, menuItem);
            }
    };
    public static ComponentUI createUI(JComponent c) {
        return new WindowsMenuItemUI();
    }
    protected void paintText(Graphics g, JMenuItem menuItem,
                             Rectangle textRect, String text) {
        if (WindowsMenuItemUI.isVistaPainting()) {
            WindowsMenuItemUI.paintText(accessor, g, menuItem, textRect, text);
            return;
        }
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();
        if(model.isEnabled() &&
            (model.isArmed() || (menuItem instanceof JMenu &&
             model.isSelected()))) {
            g.setColor(selectionForeground); 
        }
        WindowsGraphicsUtils.paintText(g, menuItem, textRect, text, 0);
        g.setColor(oldColor);
    }
    @Override
    protected void paintBackground(Graphics g, JMenuItem menuItem,
            Color bgColor) {
        if (WindowsMenuItemUI.isVistaPainting()) {
            WindowsMenuItemUI.paintBackground(accessor, g, menuItem, bgColor);
            return;
        }
        super.paintBackground(g, menuItem, bgColor);
    }
    static void paintBackground(WindowsMenuItemUIAccessor menuItemUI,
            Graphics g, JMenuItem menuItem, Color bgColor) {
        assert isVistaPainting();
        if (isVistaPainting()) {
            int menuWidth = menuItem.getWidth();
            int menuHeight = menuItem.getHeight();
            if (menuItem.isOpaque()) {
                Color oldColor = g.getColor();
                g.setColor(menuItem.getBackground());
                g.fillRect(0,0, menuWidth, menuHeight);
                g.setColor(oldColor);
            }
            XPStyle xp = XPStyle.getXP();
            Part part = menuItemUI.getPart(menuItem);
            Skin skin = xp.getSkin(menuItem, part);
            skin.paintSkin(g, 0 , 0,
                menuWidth,
                menuHeight,
                menuItemUI.getState(menuItem));
        }
    }
    static void paintText(WindowsMenuItemUIAccessor menuItemUI, Graphics g,
                                JMenuItem menuItem, Rectangle textRect,
                                String text) {
        assert isVistaPainting();
        if (isVistaPainting()) {
            State state = menuItemUI.getState(menuItem);
            FontMetrics fm = SwingUtilities2.getFontMetrics(menuItem, g);
            int mnemIndex = menuItem.getDisplayedMnemonicIndex();
            if (WindowsLookAndFeel.isMnemonicHidden() == true) {
                mnemIndex = -1;
            }
            WindowsGraphicsUtils.paintXPText(menuItem,
                menuItemUI.getPart(menuItem), state,
                g, textRect.x,
                textRect.y + fm.getAscent(),
                text, mnemIndex);
        }
    }
    static State getState(WindowsMenuItemUIAccessor menuItemUI, JMenuItem menuItem) {
        State state;
        ButtonModel model = menuItem.getModel();
        if (model.isArmed()) {
            state = (model.isEnabled()) ? State.HOT : State.DISABLEDHOT;
        } else {
            state = (model.isEnabled()) ? State.NORMAL : State.DISABLED;
        }
        return state;
    }
    static Part getPart(WindowsMenuItemUIAccessor menuItemUI, JMenuItem menuItem) {
        return Part.MP_POPUPITEM;
    }
    static boolean isVistaPainting() {
        XPStyle xp = XPStyle.getXP();
        return xp != null && xp.isSkinDefined(null, Part.MP_POPUPITEM);
    }
}

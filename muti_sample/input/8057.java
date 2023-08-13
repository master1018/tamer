public class WindowsMenuUI extends BasicMenuUI {
    protected Integer menuBarHeight;
    protected boolean hotTrackingOn;
    final WindowsMenuItemUIAccessor accessor =
        new WindowsMenuItemUIAccessor() {
            public JMenuItem getMenuItem() {
                return menuItem;
            }
            public State getState(JMenuItem menu) {
                State state = menu.isEnabled() ? State.NORMAL
                        : State.DISABLED;
                ButtonModel model = menu.getModel();
                if (model.isArmed() || model.isSelected()) {
                    state = (menu.isEnabled()) ? State.PUSHED
                            : State.DISABLEDPUSHED;
                } else if (model.isRollover()
                           && ((JMenu) menu).isTopLevelMenu()) {
                    State stateTmp = state;
                    state = (menu.isEnabled()) ? State.HOT
                            : State.DISABLEDHOT;
                    for (MenuElement menuElement :
                        ((JMenuBar) menu.getParent()).getSubElements()) {
                        if (((JMenuItem) menuElement).isSelected()) {
                            state = stateTmp;
                            break;
                        }
                    }
                }
                if (!((JMenu) menu).isTopLevelMenu()) {
                    if (state == State.PUSHED) {
                        state = State.HOT;
                    } else if (state == State.DISABLEDPUSHED) {
                        state = State.DISABLEDHOT;
                    }
                }
                if (((JMenu) menu).isTopLevelMenu() && WindowsMenuItemUI.isVistaPainting()) {
                    if (! WindowsMenuBarUI.isActive(menu)) {
                        state = State.DISABLED;
                    }
                }
                return state;
            }
            public Part getPart(JMenuItem menuItem) {
                return ((JMenu) menuItem).isTopLevelMenu() ? Part.MP_BARITEM
                        : Part.MP_POPUPITEM;
            }
    };
    public static ComponentUI createUI(JComponent x) {
        return new WindowsMenuUI();
    }
    protected void installDefaults() {
        super.installDefaults();
        if (!WindowsLookAndFeel.isClassicWindows()) {
            menuItem.setRolloverEnabled(true);
        }
        menuBarHeight = (Integer)UIManager.getInt("MenuBar.height");
        Object obj      = UIManager.get("MenuBar.rolloverEnabled");
        hotTrackingOn = (obj instanceof Boolean) ? (Boolean)obj : true;
    }
    protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
        if (WindowsMenuItemUI.isVistaPainting()) {
            WindowsMenuItemUI.paintBackground(accessor, g, menuItem, bgColor);
            return;
        }
        JMenu menu = (JMenu)menuItem;
        ButtonModel model = menu.getModel();
        if (WindowsLookAndFeel.isClassicWindows() ||
            !menu.isTopLevelMenu() ||
            (XPStyle.getXP() != null && (model.isArmed() || model.isSelected()))) {
            super.paintBackground(g, menu, bgColor);
            return;
        }
        Color oldColor = g.getColor();
        int menuWidth = menu.getWidth();
        int menuHeight = menu.getHeight();
        UIDefaults table = UIManager.getLookAndFeelDefaults();
        Color highlight = table.getColor("controlLtHighlight");
        Color shadow = table.getColor("controlShadow");
        g.setColor(menu.getBackground());
        g.fillRect(0,0, menuWidth, menuHeight);
        if (menu.isOpaque()) {
            if (model.isArmed() || model.isSelected()) {
                g.setColor(shadow);
                g.drawLine(0,0, menuWidth - 1,0);
                g.drawLine(0,0, 0,menuHeight - 2);
                g.setColor(highlight);
                g.drawLine(menuWidth - 1,0, menuWidth - 1,menuHeight - 2);
                g.drawLine(0,menuHeight - 2, menuWidth - 1,menuHeight - 2);
            } else if (model.isRollover() && model.isEnabled()) {
                boolean otherMenuSelected = false;
                MenuElement[] menus = ((JMenuBar)menu.getParent()).getSubElements();
                for (int i = 0; i < menus.length; i++) {
                    if (((JMenuItem)menus[i]).isSelected()) {
                        otherMenuSelected = true;
                        break;
                    }
                }
                if (!otherMenuSelected) {
                    if (XPStyle.getXP() != null) {
                        g.setColor(selectionBackground); 
                        g.fillRect(0, 0, menuWidth, menuHeight);
                    } else {
                        g.setColor(highlight);
                        g.drawLine(0,0, menuWidth - 1,0);
                        g.drawLine(0,0, 0,menuHeight - 2);
                        g.setColor(shadow);
                        g.drawLine(menuWidth - 1,0, menuWidth - 1,menuHeight - 2);
                        g.drawLine(0,menuHeight - 2, menuWidth - 1,menuHeight - 2);
                    }
                }
            }
        }
        g.setColor(oldColor);
    }
    protected void paintText(Graphics g, JMenuItem menuItem,
                             Rectangle textRect, String text) {
        if (WindowsMenuItemUI.isVistaPainting()) {
            WindowsMenuItemUI.paintText(accessor, g, menuItem, textRect, text);
            return;
        }
        JMenu menu = (JMenu)menuItem;
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();
        boolean paintRollover = model.isRollover();
        if (paintRollover && menu.isTopLevelMenu()) {
            MenuElement[] menus = ((JMenuBar)menu.getParent()).getSubElements();
            for (int i = 0; i < menus.length; i++) {
                if (((JMenuItem)menus[i]).isSelected()) {
                    paintRollover = false;
                    break;
                }
            }
        }
        if ((model.isSelected() && (WindowsLookAndFeel.isClassicWindows() ||
                                    !menu.isTopLevelMenu())) ||
            (XPStyle.getXP() != null && (paintRollover ||
                                         model.isArmed() ||
                                         model.isSelected()))) {
            g.setColor(selectionForeground); 
        }
        WindowsGraphicsUtils.paintText(g, menuItem, textRect, text, 0);
        g.setColor(oldColor);
    }
    protected MouseInputListener createMouseInputListener(JComponent c) {
        return new WindowsMouseInputHandler();
    }
    protected class WindowsMouseInputHandler extends BasicMenuUI.MouseInputHandler {
        public void mouseEntered(MouseEvent evt) {
            super.mouseEntered(evt);
            JMenu menu = (JMenu)evt.getSource();
            if (hotTrackingOn && menu.isTopLevelMenu() && menu.isRolloverEnabled()) {
                menu.getModel().setRollover(true);
                menuItem.repaint();
            }
        }
        public void mouseExited(MouseEvent evt) {
            super.mouseExited(evt);
            JMenu menu = (JMenu)evt.getSource();
            ButtonModel model = menu.getModel();
            if (menu.isRolloverEnabled()) {
                model.setRollover(false);
                menuItem.repaint();
            }
        }
    }
    protected Dimension getPreferredMenuItemSize(JComponent c,
                                                     Icon checkIcon,
                                                     Icon arrowIcon,
                                                     int defaultTextIconGap) {
        Dimension d = super.getPreferredMenuItemSize(c, checkIcon, arrowIcon,
                                                     defaultTextIconGap);
        if (c instanceof JMenu && ((JMenu)c).isTopLevelMenu() &&
            menuBarHeight != null && d.height < menuBarHeight) {
            d.height = menuBarHeight;
        }
        return d;
    }
}

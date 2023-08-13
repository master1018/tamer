public class WindowsRadioButtonMenuItemUI extends BasicRadioButtonMenuItemUI {
    final WindowsMenuItemUIAccessor accessor =
        new WindowsMenuItemUIAccessor() {
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
    public static ComponentUI createUI(JComponent b) {
        return new WindowsRadioButtonMenuItemUI();
    }
    @Override
    protected  void paintBackground(Graphics g, JMenuItem menuItem,
            Color bgColor) {
        if (WindowsMenuItemUI.isVistaPainting()) {
            WindowsMenuItemUI.paintBackground(accessor, g, menuItem, bgColor);
            return;
        }
        super.paintBackground(g, menuItem, bgColor);
    }
    protected void paintText(Graphics g, JMenuItem menuItem,
            Rectangle textRect, String text) {
        if (WindowsMenuItemUI.isVistaPainting()) {
            WindowsMenuItemUI.paintText(accessor, g, menuItem, textRect, text);
            return;
        }
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();
        if(model.isEnabled() && model.isArmed()) {
            g.setColor(selectionForeground); 
        }
        WindowsGraphicsUtils.paintText(g, menuItem, textRect, text, 0);
        g.setColor(oldColor);
    }
}

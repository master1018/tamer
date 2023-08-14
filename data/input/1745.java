public class WindowsPopupMenuUI extends BasicPopupMenuUI {
    static MnemonicListener mnemonicListener = null;
    static final Object GUTTER_OFFSET_KEY =
        new StringUIClientPropertyKey("GUTTER_OFFSET_KEY");
    public static ComponentUI createUI(JComponent c) {
        return new WindowsPopupMenuUI();
    }
    public void installListeners() {
        super.installListeners();
        if (! UIManager.getBoolean("Button.showMnemonics") &&
            mnemonicListener == null) {
            mnemonicListener = new MnemonicListener();
            MenuSelectionManager.defaultManager().
                addChangeListener(mnemonicListener);
        }
    }
    public Popup getPopup(JPopupMenu popupMenu, int x, int y) {
        PopupFactory popupFactory = PopupFactory.getSharedInstance();
        return popupFactory.getPopup(popupMenu.getInvoker(), popupMenu, x, y);
    }
    static class MnemonicListener implements ChangeListener {
        JRootPane repaintRoot = null;
        public void stateChanged(ChangeEvent ev) {
            MenuSelectionManager msm = (MenuSelectionManager)ev.getSource();
            MenuElement[] path = msm.getSelectedPath();
            if (path.length == 0) {
                if(!WindowsLookAndFeel.isMnemonicHidden()) {
                    WindowsLookAndFeel.setMnemonicHidden(true);
                    if (repaintRoot != null) {
                        Window win =
                            SwingUtilities.getWindowAncestor(repaintRoot);
                        WindowsGraphicsUtils.repaintMnemonicsInWindow(win);
                    }
                }
            } else {
                Component c = (Component)path[0];
                if (c instanceof JPopupMenu) c = ((JPopupMenu)c).getInvoker();
                repaintRoot = SwingUtilities.getRootPane(c);
            }
        }
    }
    static int getTextOffset(JComponent c) {
        int rv = -1;
        Object maxTextOffset =
            c.getClientProperty(BASICMENUITEMUI_MAX_TEXT_OFFSET);
        if (maxTextOffset instanceof Integer) {
            rv = (Integer) maxTextOffset;
            int menuItemOffset = 0;
            Component component = c.getComponent(0);
            if (component != null) {
                menuItemOffset = component.getX();
            }
            rv += menuItemOffset;
        }
        return rv;
    }
    static int getSpanBeforeGutter() {
        return 3;
    }
    static int getSpanAfterGutter() {
        return 3;
    }
    static int getGutterWidth() {
        int rv = 2;
        XPStyle xp = XPStyle.getXP();
        if (xp != null) {
            Skin skin = xp.getSkin(null, Part.MP_POPUPGUTTER);
            rv = skin.getWidth();
        }
        return rv;
    }
    private static boolean isLeftToRight(JComponent c) {
        boolean leftToRight = true;
        for (int i = c.getComponentCount() - 1; i >=0 && leftToRight; i-- ) {
            leftToRight =
                c.getComponent(i).getComponentOrientation().isLeftToRight();
        }
        return leftToRight;
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        if (WindowsMenuItemUI.isVistaPainting()) {
            XPStyle xp = XPStyle.getXP();
            Skin skin = xp.getSkin(c, Part.MP_POPUPBACKGROUND);
            skin.paintSkin(g, 0, 0, c.getWidth(),c.getHeight(), State.NORMAL);
            int textOffset = getTextOffset(c);
            if (textOffset >= 0
                    && isLeftToRight(c)) {
                skin = xp.getSkin(c, Part.MP_POPUPGUTTER);
                int gutterWidth = getGutterWidth();
                int gutterOffset =
                    textOffset - getSpanAfterGutter() - gutterWidth;
                c.putClientProperty(GUTTER_OFFSET_KEY,
                    Integer.valueOf(gutterOffset));
                Insets insets = c.getInsets();
                skin.paintSkin(g, gutterOffset, insets.top,
                    gutterWidth, c.getHeight() - insets.bottom - insets.top,
                    State.NORMAL);
            } else {
                if (c.getClientProperty(GUTTER_OFFSET_KEY) != null) {
                    c.putClientProperty(GUTTER_OFFSET_KEY, null);
                }
            }
        } else {
            super.paint(g, c);
        }
    }
}

class WindowsPopupWindow extends JWindow {
    static final int UNDEFINED_WINDOW_TYPE      = 0;
    static final int TOOLTIP_WINDOW_TYPE        = 1;
    static final int MENU_WINDOW_TYPE           = 2;
    static final int SUBMENU_WINDOW_TYPE        = 3;
    static final int POPUPMENU_WINDOW_TYPE      = 4;
    static final int COMBOBOX_POPUP_WINDOW_TYPE = 5;
    private int windowType;
    WindowsPopupWindow(Window parent) {
        super(parent);
        setFocusableWindowState(false);
    }
    void setWindowType(int type) {
        windowType = type;
    }
    int getWindowType() {
        return windowType;
    }
    public void update(Graphics g) {
        paint(g);
    }
    public void hide() {
        super.hide();
        removeNotify();
    }
    public void show() {
        super.show();
        this.pack();
    }
}

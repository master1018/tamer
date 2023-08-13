public class WSystemTrayPeer extends WObjectPeer implements SystemTrayPeer {
    WSystemTrayPeer(SystemTray target) {
        this.target = target;
    }
    public Dimension getTrayIconSize() {
        return new Dimension(WTrayIconPeer.TRAY_ICON_WIDTH, WTrayIconPeer.TRAY_ICON_HEIGHT);
    }
    public boolean isSupported() {
        return ((WToolkit)Toolkit.getDefaultToolkit()).isTraySupported();
    }
    protected void disposeImpl() {
    }
}

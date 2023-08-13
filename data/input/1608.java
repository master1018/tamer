class WMenuBarPeer extends WMenuPeer implements MenuBarPeer {
    public native void addMenu(Menu m);
    public native void delMenu(int index);
    public void addHelpMenu(Menu m) {
        addMenu(m);
    }
    WMenuBarPeer(MenuBar target) {
        this.target = target;
        WFramePeer framePeer = (WFramePeer)
            WToolkit.targetToPeer(target.getParent());
        create(framePeer);
        checkMenuCreation();
    }
    native void create(WFramePeer f);
}

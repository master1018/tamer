class WMenuPeer extends WMenuItemPeer implements MenuPeer {
    public native void addSeparator();
    public void addItem(MenuItem item) {
        WMenuItemPeer itemPeer = (WMenuItemPeer) WToolkit.targetToPeer(item);
    }
    public native void delItem(int index);
    WMenuPeer() {}   
    WMenuPeer(Menu target) {
        this.target = target;
        MenuContainer parent = target.getParent();
        if (parent instanceof MenuBar) {
            WMenuBarPeer mbPeer = (WMenuBarPeer) WToolkit.targetToPeer(parent);
            this.parent = mbPeer;
            createMenu(mbPeer);
        }
        else if (parent instanceof Menu) {
            this.parent = (WMenuPeer) WToolkit.targetToPeer(parent);
            createSubMenu(this.parent);
        }
        else {
            throw new IllegalArgumentException("unknown menu container class");
        }
        checkMenuCreation();
    }
    native void createMenu(WMenuBarPeer parent);
    native void createSubMenu(WMenuPeer parent);
}

public class WPopupMenuPeer extends WMenuPeer implements PopupMenuPeer {
    public WPopupMenuPeer(PopupMenu target) {
        this.target = target;
        MenuContainer parent = null;
        boolean isTrayIconPopup = AWTAccessor.getPopupMenuAccessor().isTrayIconPopup(target);
        if (isTrayIconPopup) {
            parent = AWTAccessor.getMenuComponentAccessor().getParent(target);
        } else {
            parent = target.getParent();
        }
        if (parent instanceof Component) {
            WComponentPeer parentPeer = (WComponentPeer) WToolkit.targetToPeer(parent);
            if (parentPeer == null) {
                parent = WToolkit.getNativeContainer((Component)parent);
                parentPeer = (WComponentPeer) WToolkit.targetToPeer(parent);
            }
            createMenu(parentPeer);
            checkMenuCreation();
        } else {
            throw new IllegalArgumentException(
                "illegal popup menu container class");
        }
    }
    native void createMenu(WComponentPeer parent);
    public void show(Event e) {
        Component origin = (Component)e.target;
        WComponentPeer peer = (WComponentPeer) WToolkit.targetToPeer(origin);
        if (peer == null) {
            Component nativeOrigin = WToolkit.getNativeContainer(origin);
            e.target = nativeOrigin;
            for (Component c = origin; c != nativeOrigin; c = c.getParent()) {
                Point p = c.getLocation();
                e.x += p.x;
                e.y += p.y;
            }
        }
        _show(e);
    }
    void show(Component origin, Point p) {
        WComponentPeer peer = (WComponentPeer) WToolkit.targetToPeer(origin);
        Event e = new Event(origin, 0, Event.MOUSE_DOWN, p.x, p.y, 0, 0);
        if (peer == null) {
            Component nativeOrigin = WToolkit.getNativeContainer(origin);
            e.target = nativeOrigin;
        }
        e.x = p.x;
        e.y = p.y;
        _show(e);
    }
    public native void _show(Event e);
}

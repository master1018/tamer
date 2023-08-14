public class XFocusProxyWindow extends XBaseWindow {
    XWindowPeer owner;
    public XFocusProxyWindow(XWindowPeer owner) {
        super(new XCreateWindowParams(new Object[] {
            BOUNDS, new Rectangle(-1, -1, 1, 1),
            PARENT_WINDOW, new Long(owner.getWindow()),
            EVENT_MASK, new Long(XConstants.FocusChangeMask | XConstants.KeyPressMask | XConstants.KeyReleaseMask)
        }));
        this.owner = owner;
    }
    public void postInit(XCreateWindowParams params){
        super.postInit(params);
        setWMClass(getWMClass());
        xSetVisible(true);
    }
    protected String getWMName() {
        return "FocusProxy";
    }
    protected String[] getWMClass() {
        return new String[] {"Focus-Proxy-Window", "FocusProxy"};
    }
    public XWindowPeer getOwner() {
        return owner;
    }
    public void dispatchEvent(XEvent ev) {
        int type = ev.get_type();
        switch (type)
        {
          case XConstants.FocusIn:
          case XConstants.FocusOut:
              handleFocusEvent(ev);
              break;
        }
        super.dispatchEvent(ev);
    }
    public void handleFocusEvent(XEvent xev) {
        owner.handleFocusEvent(xev);
    }
    public void handleKeyPress(XEvent xev) {
        owner.handleKeyPress(xev);
    }
    public void handleKeyRelease(XEvent xev) {
        owner.handleKeyRelease(xev);
    }
}

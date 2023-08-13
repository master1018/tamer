public class XEmbeddedFrame extends EmbeddedFrame {
    private static final Logger log = Logger.getLogger(XEmbeddedFrame.class.getName());
    long handle;
    public XEmbeddedFrame() {
    }
    public XEmbeddedFrame(long handle) {
        this(handle, false);
    }
    public XEmbeddedFrame(long handle, boolean supportsXEmbed, boolean isTrayIconWindow) {
        super(handle, supportsXEmbed);
        if (isTrayIconWindow) {
            XTrayIconPeer.suppressWarningString(this);
        }
        this.handle = handle;
        if (handle != 0) { 
            addNotify();
            if (!isTrayIconWindow) {
                show();
            }
        }
    }
    public void addNotify()
    {
        if (getPeer() == null) {
            XToolkit toolkit = (XToolkit)Toolkit.getDefaultToolkit();
            setPeer(toolkit.createEmbeddedFrame(this));
        }
        super.addNotify();
    }
    public XEmbeddedFrame(long handle, boolean supportsXEmbed) {
        this(handle, supportsXEmbed, false);
    }
    public boolean traverseIn(boolean direction) {
        XEmbeddedFramePeer peer = (XEmbeddedFramePeer)getPeer();
        if (peer != null) {
            if (peer.supportsXEmbed() && peer.isXEmbedActive()) {
                log.fine("The method shouldn't be called when XEmbed is active!");
            } else {
                return super.traverseIn(direction);
            }
        }
        return false;
    }
    protected boolean traverseOut(boolean direction) {
        XEmbeddedFramePeer xefp = (XEmbeddedFramePeer) getPeer();
        if (direction == FORWARD) {
            xefp.traverseOutForward();
        }
        else {
            xefp.traverseOutBackward();
        }
        return true;
    }
    public void synthesizeWindowActivation(boolean doActivate) {
        XEmbeddedFramePeer peer = (XEmbeddedFramePeer)getPeer();
        if (peer != null) {
            if (peer.supportsXEmbed() && peer.isXEmbedActive()) {
                log.fine("The method shouldn't be called when XEmbed is active!");
            } else {
                peer.synthesizeFocusInOut(doActivate);
            }
        }
    }
    public void registerAccelerator(AWTKeyStroke stroke) {
        XEmbeddedFramePeer xefp = (XEmbeddedFramePeer) getPeer();
        if (xefp != null) {
            xefp.registerAccelerator(stroke);
        }
    }
    public void unregisterAccelerator(AWTKeyStroke stroke) {
        XEmbeddedFramePeer xefp = (XEmbeddedFramePeer) getPeer();
        if (xefp != null) {
            xefp.unregisterAccelerator(stroke);
        }
    }
}

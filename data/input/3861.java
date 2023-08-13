public class WEmbeddedFramePeer extends WFramePeer {
    public WEmbeddedFramePeer(EmbeddedFrame target) {
        super(target);
    }
    native void create(WComponentPeer parent);
    public void print(Graphics g) {}
    public void updateMinimumSize() {}
    @Override
    public void modalDisable(Dialog blocker, long blockerHWnd)
    {
        super.modalDisable(blocker, blockerHWnd);
        ((EmbeddedFrame)target).notifyModalBlocked(blocker, true);
    }
    @Override
    public void modalEnable(Dialog blocker)
    {
        super.modalEnable(blocker);
        ((EmbeddedFrame)target).notifyModalBlocked(blocker, false);
    }
    public void setBoundsPrivate(int x, int y, int width, int height) {
        setBounds(x, y, width, height, SET_BOUNDS | NO_EMBEDDED_CHECK);
    }
    public native Rectangle getBoundsPrivate();
    public native void synthesizeWmActivate(boolean doActivate);
    @Override
    public boolean isAccelCapable() {
        return !Win32GraphicsEnvironment.isDWMCompositionEnabled();
    }
}

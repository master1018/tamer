public class DefaultMouseInfoPeer implements MouseInfoPeer {
    DefaultMouseInfoPeer() {
    }
    public native int fillPointWithCoords(Point point);
    public native boolean isWindowUnderMouse(Window w);
}

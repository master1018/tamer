public class WBufferStrategy {
    private static native void initIDs(Class componentClass);
    static {
        initIDs(Component.class);
    }
    public static native Image getDrawBuffer(Component comp);
}

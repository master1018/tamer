public class PaintEventDispatcher {
    private static PaintEventDispatcher dispatcher;
    public static void setPaintEventDispatcher(
                          PaintEventDispatcher dispatcher) {
        synchronized(PaintEventDispatcher.class) {
            PaintEventDispatcher.dispatcher = dispatcher;
        }
    }
    public static PaintEventDispatcher getPaintEventDispatcher() {
        synchronized(PaintEventDispatcher.class) {
            if (dispatcher == null) {
                dispatcher = new PaintEventDispatcher();
            }
            return dispatcher;
        }
    }
    public PaintEvent createPaintEvent(Component target, int x, int y, int w,
                                       int h) {
        return new PaintEvent((Component)target, PaintEvent.PAINT,
                              new Rectangle(x, y, w, h));
    }
    public boolean shouldDoNativeBackgroundErase(Component c) {
        return true;
    }
    public boolean queueSurfaceDataReplacing(Component c, Runnable r) {
        return false;
    }
}

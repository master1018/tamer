public abstract class WTK {
    public abstract GraphicsFactory getGraphicsFactory();
    public abstract NativeEventQueue getNativeEventQueue();
    public abstract WindowFactory getWindowFactory();
    public abstract CursorFactory getCursorFactory();
    public abstract NativeMouseInfo getNativeMouseInfo();
    public abstract SystemProperties getSystemProperties();
    public abstract NativeRobot getNativeRobot(GraphicsDevice screen);
    public abstract NativeIM getNativeIM();
}

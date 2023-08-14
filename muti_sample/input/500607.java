public class AndroidWTK extends WTK {
    private AndroidGraphicsFactory mAgf;
    private AndroidNativeEventQueue mAneq;
    @Override
    public CursorFactory getCursorFactory() {
        return null;
    }
    @Override
    public GraphicsFactory getGraphicsFactory() {
        if(mAgf == null) {
            mAgf = new AndroidGraphicsFactory();
        }
        return mAgf;
    }
    @Override
    public NativeEventQueue getNativeEventQueue() {
        if(mAneq == null) {
            mAneq = new AndroidNativeEventQueue();
        }
        return mAneq;
    }
    @Override
    public NativeIM getNativeIM() {
        return null;
    }
    @Override
    public NativeMouseInfo getNativeMouseInfo() {
        return null;
    }
    @Override
    public NativeRobot getNativeRobot(GraphicsDevice screen) {
        return null;
    }
    @Override
    public SystemProperties getSystemProperties() {
        return null;
    }
    @Override
    public WindowFactory getWindowFactory() {
        return null;
    }
}

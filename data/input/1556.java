class XRobotPeer implements RobotPeer {
    private X11GraphicsConfig   xgc = null;
    static Object robotLock = new Object();
    XRobotPeer(GraphicsConfiguration gc) {
        this.xgc = (X11GraphicsConfig)gc;
        SunToolkit tk = (SunToolkit)Toolkit.getDefaultToolkit();
        setup(tk.getNumberOfButtons(), AWTAccessor.getInputEventAccessor().getButtonDownMasks());
    }
    public void dispose() {
    }
    public void mouseMove(int x, int y) {
        mouseMoveImpl(xgc, x, y);
    }
    public void mousePress(int buttons) {
        mousePressImpl(buttons);
    }
    public void mouseRelease(int buttons) {
        mouseReleaseImpl(buttons);
    }
    public void mouseWheel(int wheelAmt) {
    mouseWheelImpl(wheelAmt);
    }
    public void keyPress(int keycode) {
        keyPressImpl(keycode);
    }
    public void keyRelease(int keycode) {
        keyReleaseImpl(keycode);
    }
    public int getRGBPixel(int x, int y) {
        int pixelArray[] = new int[1];
        getRGBPixelsImpl(xgc, x, y, 1, 1, pixelArray);
        return pixelArray[0];
    }
    public int [] getRGBPixels(Rectangle bounds) {
        int pixelArray[] = new int[bounds.width*bounds.height];
        getRGBPixelsImpl(xgc, bounds.x, bounds.y, bounds.width, bounds.height, pixelArray);
        return pixelArray;
    }
    private static native synchronized void setup(int numberOfButtons, int[] buttonDownMasks);
    private static native synchronized void mouseMoveImpl(X11GraphicsConfig xgc, int x, int y);
    private static native synchronized void mousePressImpl(int buttons);
    private static native synchronized void mouseReleaseImpl(int buttons);
    private static native synchronized void mouseWheelImpl(int wheelAmt);
    private static native synchronized void keyPressImpl(int keycode);
    private static native synchronized void keyReleaseImpl(int keycode);
    private static native synchronized void getRGBPixelsImpl(X11GraphicsConfig xgc, int x, int y, int width, int height, int pixelArray[]);
}

public class X11GraphicsEnvironment
    extends SunGraphicsEnvironment
{
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11GraphicsEnvironment");
    private static final PlatformLogger screenLog = PlatformLogger.getLogger("sun.awt.screen.X11GraphicsEnvironment");
    private static Boolean xinerState;
    static {
        java.security.AccessController.doPrivileged(
                          new java.security.PrivilegedAction() {
            public Object run() {
                System.loadLibrary("awt");
                if (!isHeadless()) {
                    boolean glxRequested = false;
                    String prop = System.getProperty("sun.java2d.opengl");
                    if (prop != null) {
                        if (prop.equals("true") || prop.equals("t")) {
                            glxRequested = true;
                        } else if (prop.equals("True") || prop.equals("T")) {
                            glxRequested = true;
                            glxVerbose = true;
                        }
                    }
                    boolean xRenderRequested = false;
                    String xProp = System.getProperty("sun.java2d.xrender");
                        if (xProp != null) {
                        if (xProp.equals("true") || xProp.equals("t")) {
                            xRenderRequested = true;
                        } else if (xProp.equals("True") || xProp.equals("T")) {
                            xRenderRequested = true;
                            xRenderVerbose = true;
                        }
                    }
                    initDisplay(glxRequested);
                    if (glxRequested) {
                        glxAvailable = initGLX();
                        if (glxVerbose && !glxAvailable) {
                            System.out.println(
                                "Could not enable OpenGL " +
                                "pipeline (GLX 1.3 not available)");
                        }
                    }
                    if (xRenderRequested) {
                        xRenderAvailable = initXRender(xRenderVerbose);
                        if (xRenderVerbose && !xRenderAvailable) {
                            System.out.println(
                                         "Could not enable XRender pipeline");
                        }
                    }
                    if (xRenderAvailable) {
                        XRSurfaceData.initXRSurfaceData();
                    }
                }
                return null;
            }
         });
        SurfaceManagerFactory.setInstance(new UnixSurfaceManagerFactory());
    }
    private static boolean glxAvailable;
    private static boolean glxVerbose;
    private static native boolean initGLX();
    public static boolean isGLXAvailable() {
        return glxAvailable;
    }
    public static boolean isGLXVerbose() {
        return glxVerbose;
    }
    private static boolean xRenderVerbose;
    private static boolean xRenderAvailable;
    private static native boolean initXRender(boolean verbose);
    public static boolean isXRenderAvailable() {
        return xRenderAvailable;
    }
    public static boolean isXRenderVerbose() {
        return xRenderVerbose;
    }
    private static native int checkShmExt();
    private static  native String getDisplayString();
    private Boolean isDisplayLocal;
    private static native void initDisplay(boolean glxRequested);
    public X11GraphicsEnvironment() {
    }
    protected native int getNumScreens();
    protected GraphicsDevice makeScreenDevice(int screennum) {
        return new X11GraphicsDevice(screennum);
    }
    protected native int getDefaultScreenNum();
    public GraphicsDevice getDefaultScreenDevice() {
        return getScreenDevices()[getDefaultScreenNum()];
    }
    public boolean isDisplayLocal() {
        if (isDisplayLocal == null) {
            SunToolkit.awtLock();
            try {
                if (isDisplayLocal == null) {
                    isDisplayLocal = Boolean.valueOf(_isDisplayLocal());
                }
            } finally {
                SunToolkit.awtUnlock();
            }
        }
        return isDisplayLocal.booleanValue();
    }
    private static boolean _isDisplayLocal() {
        if (isHeadless()) {
            return true;
        }
        String isRemote = (String)java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.remote"));
        if (isRemote != null) {
            return isRemote.equals("false");
        }
        int shm = checkShmExt();
        if (shm != -1) {
            return (shm == 1);
        }
        String display = getDisplayString();
        int ind = display.indexOf(':');
        final String hostName = display.substring(0, ind);
        if (ind <= 0) {
            return true;
        }
        Boolean result = (Boolean)java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
            public Object run() {
                InetAddress remAddr[] = null;
                Enumeration locals = null;
                Enumeration interfaces = null;
                try {
                    interfaces = NetworkInterface.getNetworkInterfaces();
                    remAddr = InetAddress.getAllByName(hostName);
                    if (remAddr == null) {
                        return Boolean.FALSE;
                    }
                } catch (UnknownHostException e) {
                    System.err.println("Unknown host: " + hostName);
                    return Boolean.FALSE;
                } catch (SocketException e1) {
                    System.err.println(e1.getMessage());
                    return Boolean.FALSE;
                }
                for (; interfaces.hasMoreElements();) {
                    locals = ((NetworkInterface)interfaces.nextElement()).getInetAddresses();
                    for (; locals.hasMoreElements();) {
                        for (int i = 0; i < remAddr.length; i++) {
                            if (locals.nextElement().equals(remAddr[i])) {
                                return Boolean.TRUE;
                            }
                        }
                    }
                }
                return Boolean.FALSE;
            }});
        return result.booleanValue();
    }
    public String getDefaultFontFaceName() {
        return null;
    }
    private static native boolean pRunningXinerama();
    private static native Point getXineramaCenterPoint();
    public Point getCenterPoint() {
        if (runningXinerama()) {
            Point p = getXineramaCenterPoint();
            if (p != null) {
                return p;
            }
        }
        return super.getCenterPoint();
    }
    public Rectangle getMaximumWindowBounds() {
        if (runningXinerama()) {
            return getXineramaWindowBounds();
        } else {
            return super.getMaximumWindowBounds();
        }
    }
    public boolean runningXinerama() {
        if (xinerState == null) {
            xinerState = Boolean.valueOf(pRunningXinerama());
            if (screenLog.isLoggable(PlatformLogger.FINER)) {
                screenLog.finer("Running Xinerama: " + xinerState);
            }
        }
        return xinerState.booleanValue();
    }
    protected Rectangle getXineramaWindowBounds() {
        Point center = getCenterPoint();
        Rectangle unionRect, tempRect;
        GraphicsDevice[] gds = getScreenDevices();
        Rectangle centerMonitorRect = null;
        int i;
        unionRect = getUsableBounds(gds[0]);
        for (i = 0; i < gds.length; i++) {
            tempRect = getUsableBounds(gds[i]);
            if (centerMonitorRect == null &&
                (tempRect.width / 2) + tempRect.x > center.x - 1 &&
                (tempRect.height / 2) + tempRect.y > center.y - 1 &&
                (tempRect.width / 2) + tempRect.x < center.x + 1 &&
                (tempRect.height / 2) + tempRect.y < center.y + 1) {
                centerMonitorRect = tempRect;
            }
            unionRect = unionRect.union(tempRect);
        }
        if ((unionRect.width / 2) + unionRect.x > center.x - 1 &&
            (unionRect.height / 2) + unionRect.y > center.y - 1 &&
            (unionRect.width / 2) + unionRect.x < center.x + 1 &&
            (unionRect.height / 2) + unionRect.y < center.y + 1) {
            if (screenLog.isLoggable(PlatformLogger.FINER)) {
                screenLog.finer("Video Wall: center point is at center of all displays.");
            }
            return unionRect;
        }
        if (centerMonitorRect != null) {
            if (screenLog.isLoggable(PlatformLogger.FINER)) {
                screenLog.finer("Center point at center of a particular " +
                                "monitor, but not of the entire virtual display.");
            }
            return centerMonitorRect;
        }
        if (screenLog.isLoggable(PlatformLogger.FINER)) {
            screenLog.finer("Center point is somewhere strange - return union of all bounds.");
        }
        return unionRect;
    }
    @Override
    public void paletteChanged() {
    }
}

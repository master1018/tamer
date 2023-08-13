public class ScreenUpdateManager {
    private static ScreenUpdateManager theInstance;
    protected ScreenUpdateManager() {
    }
    public synchronized Graphics2D createGraphics(SurfaceData sd,
            WComponentPeer peer, Color fgColor, Color bgColor, Font font)
    {
        return new SunGraphics2D(sd, fgColor, bgColor, font);
    }
    public SurfaceData createScreenSurface(Win32GraphicsConfig gc,
                                           WComponentPeer peer, int bbNum,
                                           boolean isResize)
    {
        return gc.createSurfaceData(peer, bbNum);
    }
    public void dropScreenSurface(SurfaceData sd) {}
    public SurfaceData getReplacementScreenSurface(WComponentPeer peer,
                                                   SurfaceData oldsd)
    {
        return peer.getSurfaceData();
    }
    public static synchronized ScreenUpdateManager getInstance() {
        if (theInstance == null) {
            if (WindowsFlags.isD3DEnabled()) {
                theInstance = new D3DScreenUpdateManager();
            } else {
                theInstance = new ScreenUpdateManager();
            }
        }
        return theInstance;
    }
}

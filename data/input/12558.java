public class XMouseInfoPeer implements MouseInfoPeer {
    XMouseInfoPeer() {
    }
    public int fillPointWithCoords(Point point) {
        long display = XToolkit.getDisplay();
        GraphicsEnvironment ge = GraphicsEnvironment.
                                     getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        int gdslen = gds.length;
        XToolkit.awtLock();
        try {
            for (int i = 0; i < gdslen; i++) {
                long screenRoot = XlibWrapper.RootWindow(display, i);
                boolean pointerFound = XlibWrapper.XQueryPointer(
                                           display, screenRoot,
                                           XlibWrapper.larg1,  
                                           XlibWrapper.larg2,  
                                           XlibWrapper.larg3,  
                                           XlibWrapper.larg4,  
                                           XlibWrapper.larg5,  
                                           XlibWrapper.larg6,  
                                           XlibWrapper.larg7); 
                if (pointerFound) {
                    point.x = Native.getInt(XlibWrapper.larg3);
                    point.y = Native.getInt(XlibWrapper.larg4);
                    return i;
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
        assert false : "No pointer found in the system.";
        return 0;
    }
    public boolean isWindowUnderMouse(Window w) {
        long display = XToolkit.getDisplay();
        long contentWindow = ((XWindow)w.getPeer()).getContentWindow();
        long parent = XlibUtil.getParentWindow(contentWindow);
        XToolkit.awtLock();
        try
        {
            boolean windowOnTheSameScreen = XlibWrapper.XQueryPointer(display, parent,
                                  XlibWrapper.larg1, 
                                  XlibWrapper.larg8, 
                                  XlibWrapper.larg3, 
                                  XlibWrapper.larg4, 
                                  XlibWrapper.larg5, 
                                  XlibWrapper.larg6, 
                                  XlibWrapper.larg7); 
            long siblingWindow = Native.getWindow(XlibWrapper.larg8);
            return (siblingWindow == contentWindow && windowOnTheSameScreen);
        }
        finally
        {
            XToolkit.awtUnlock();
        }
    }
}

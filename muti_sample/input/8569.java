public class XlibUtil
{
    private XlibUtil()
    {
    }
    public static long getRootWindow(int screenNumber)
    {
        XToolkit.awtLock();
        try
        {
            X11GraphicsEnvironment x11ge = (X11GraphicsEnvironment)
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (x11ge.runningXinerama())
            {
                return XlibWrapper.RootWindow(XToolkit.getDisplay(), 0);
            }
            else
            {
                return XlibWrapper.RootWindow(XToolkit.getDisplay(), screenNumber);
            }
        }
        finally
        {
            XToolkit.awtUnlock();
        }
    }
    static boolean isRoot(long rootCandidate, long screenNumber)
    {
        long root;
        XToolkit.awtLock();
        try
        {
            root = XlibWrapper.RootWindow(XToolkit.getDisplay(),
                                          screenNumber);
        }
        finally
        {
            XToolkit.awtUnlock();
        }
        return root == rootCandidate;
    }
    static Rectangle getWindowGeometry(long window)
    {
        XToolkit.awtLock();
        try
        {
            int res = XlibWrapper.XGetGeometry(XToolkit.getDisplay(),
                                               window,
                                               XlibWrapper.larg1, 
                                               XlibWrapper.larg2, 
                                               XlibWrapper.larg3, 
                                               XlibWrapper.larg4, 
                                               XlibWrapper.larg5, 
                                               XlibWrapper.larg6, 
                                               XlibWrapper.larg7); 
            if (res == 0)
            {
                return null;
            }
            int x = Native.getInt(XlibWrapper.larg2);
            int y = Native.getInt(XlibWrapper.larg3);
            long width = Native.getUInt(XlibWrapper.larg4);
            long height = Native.getUInt(XlibWrapper.larg5);
            return new Rectangle(x, y, (int)width, (int)height);
        }
        finally
        {
            XToolkit.awtUnlock();
        }
    }
    static Point translateCoordinates(long src, long dst, Point p)
    {
        Point translated = null;
        XToolkit.awtLock();
        try
        {
            XTranslateCoordinates xtc =
                new XTranslateCoordinates(src, dst, p.x, p.y);
            try
            {
                int status = xtc.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if ((status != 0) &&
                    ((XToolkit.saved_error == null) ||
                     (XToolkit.saved_error.get_error_code() == XConstants.Success)))
                {
                    translated = new Point(xtc.get_dest_x(), xtc.get_dest_y());
                }
            }
            finally
            {
                xtc.dispose();
            }
        }
        finally
        {
            XToolkit.awtUnlock();
        }
        return translated;
    }
    static Rectangle translateCoordinates(long src, long dst, Rectangle r)
    {
        Point translatedLoc = translateCoordinates(src, dst, r.getLocation());
        if (translatedLoc == null)
        {
            return null;
        }
        else
        {
            return new Rectangle(translatedLoc, r.getSize());
        }
    }
    static long getParentWindow(long window)
    {
        XToolkit.awtLock();
        try
        {
            XBaseWindow bw = XToolkit.windowToXWindow(window);
            if (bw != null)
            {
                XBaseWindow pbw = bw.getParentWindow();
                if (pbw != null)
                {
                    return pbw.getWindow();
                }
            }
            XQueryTree qt = new XQueryTree(window);
            try
            {
                if (qt.execute() == 0)
                {
                    return 0;
                }
                else
                {
                    return qt.get_parent();
                }
            }
            finally
            {
                qt.dispose();
            }
        }
        finally
        {
            XToolkit.awtUnlock();
        }
    }
    static Set<Long> getChildWindows(long window)
    {
        XToolkit.awtLock();
        try
        {
            XBaseWindow bw = XToolkit.windowToXWindow(window);
            if (bw != null)
            {
                return bw.getChildren();
            }
            XQueryTree xqt = new XQueryTree(window);
            try
            {
                int status = xqt.execute();
                if (status == 0)
                {
                    return Collections.emptySet();
                }
                long children = xqt.get_children();
                if (children == 0)
                {
                    return Collections.emptySet();
                }
                int childrenCount = xqt.get_nchildren();
                Set<Long> childrenSet = new HashSet<Long>(childrenCount);
                for (int i = 0; i < childrenCount; i++)
                {
                    childrenSet.add(Native.getWindow(children, i));
                }
                return childrenSet;
            }
            finally
            {
                xqt.dispose();
            }
        }
        finally
        {
            XToolkit.awtUnlock();
        }
    }
    static boolean isXAWTToplevelWindow(long window)
    {
        return XToolkit.windowToXWindow(window) instanceof XWindowPeer;
    }
    static boolean isToplevelWindow(long window)
    {
        if (XToolkit.windowToXWindow(window) instanceof XDecoratedPeer)
        {
            return true;
        }
        XToolkit.awtLock();
        try
        {
            WindowPropertyGetter wpg =
                new WindowPropertyGetter(window, XWM.XA_WM_STATE, 0, 1, false,
                                         XWM.XA_WM_STATE);
            try
            {
                wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (wpg.getActualType() == XWM.XA_WM_STATE.getAtom())
                {
                    return true;
                }
            }
            finally
            {
                wpg.dispose();
            }
            return false;
        }
        finally
        {
            XToolkit.awtUnlock();
        }
    }
    static boolean isTrueToplevelWindow(long window)
    {
        if (XToolkit.windowToXWindow(window) instanceof XEmbeddedFramePeer)
        {
            return false;
        }
        return isToplevelWindow(window);
    }
    static int getWindowMapState(long window)
    {
        XToolkit.awtLock();
        XWindowAttributes wattr = new XWindowAttributes();
        try
        {
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            int status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                          window, wattr.pData);
            XToolkit.RESTORE_XERROR_HANDLER();
            if ((status != 0) &&
                ((XToolkit.saved_error == null) ||
                 (XToolkit.saved_error.get_error_code() == XConstants.Success)))
            {
                return wattr.get_map_state();
            }
        }
        finally
        {
            wattr.dispose();
            XToolkit.awtUnlock();
        }
        return XConstants.IsUnmapped;
    }
    static Boolean isShapingSupported = null;
    static synchronized boolean isShapingSupported() {
        if (isShapingSupported == null) {
            XToolkit.awtLock();
            try {
                isShapingSupported =
                    XlibWrapper.XShapeQueryExtension(
                            XToolkit.getDisplay(),
                            XlibWrapper.larg1,
                            XlibWrapper.larg2);
            } finally {
                XToolkit.awtUnlock();
            }
        }
        return isShapingSupported.booleanValue();
    }
}

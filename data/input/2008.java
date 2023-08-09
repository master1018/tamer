final class XWM
{
    private final static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XWM");
    private final static PlatformLogger insLog = PlatformLogger.getLogger("sun.awt.X11.insets.XWM");
    private final static PlatformLogger stateLog = PlatformLogger.getLogger("sun.awt.X11.states.XWM");
    static final XAtom XA_MWM_HINTS = new XAtom();
    private static Unsafe unsafe = XlibWrapper.unsafe;
    static XAtom XA_WM_STATE = new XAtom();
    XAtom XA_UTF8_STRING = XAtom.get("UTF8_STRING");    
    final static int AWT_NET_N_KNOWN_STATES=2;
    final static XAtom XA_E_FRAME_SIZE = new XAtom();
    final static XAtom XA_KDE_NET_WM_FRAME_STRUT = new XAtom();
    final static XAtom XA_KWM_WIN_ICONIFIED = new XAtom();
    final static XAtom XA_KWM_WIN_MAXIMIZED = new XAtom();
    final static XAtom XA_OL_DECOR_DEL = new XAtom();
    final static XAtom XA_OL_DECOR_HEADER = new XAtom();
    final static XAtom XA_OL_DECOR_RESIZE = new XAtom();
    final static XAtom XA_OL_DECOR_PIN = new XAtom();
    final static XAtom XA_OL_DECOR_CLOSE = new XAtom();
    final static XAtom XA_NET_FRAME_EXTENTS = new XAtom();
    final static XAtom XA_NET_REQUEST_FRAME_EXTENTS = new XAtom();
    final static int
        UNDETERMINED_WM = 1,
        NO_WM = 2,
        OTHER_WM = 3,
        OPENLOOK_WM = 4,
        MOTIF_WM = 5,
        CDE_WM = 6,
        ENLIGHTEN_WM = 7,
        KDE2_WM = 8,
        SAWFISH_WM = 9,
        ICE_WM = 10,
        METACITY_WM = 11,
        COMPIZ_WM = 12,
        LG3D_WM = 13;
    public String toString() {
        switch  (WMID) {
          case NO_WM:
              return "NO WM";
          case OTHER_WM:
              return "Other WM";
          case OPENLOOK_WM:
              return "OPENLOOK";
          case MOTIF_WM:
              return "MWM";
          case CDE_WM:
              return "DTWM";
          case ENLIGHTEN_WM:
              return "Enlightenment";
          case KDE2_WM:
              return "KWM2";
          case SAWFISH_WM:
              return "Sawfish";
          case ICE_WM:
              return "IceWM";
          case METACITY_WM:
              return "Metacity";
          case COMPIZ_WM:
              return "Compiz";
          case LG3D_WM:
              return "LookingGlass";
          case UNDETERMINED_WM:
          default:
              return "Undetermined WM";
        }
    }
    int WMID;
    static final Insets zeroInsets = new Insets(0, 0, 0, 0);
    static final Insets defaultInsets = new Insets(25, 5, 5, 5);
    XWM(int WMID) {
        this.WMID = WMID;
        initializeProtocols();
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Window manager: " + toString());
    }
    int getID() {
        return WMID;
    }
    static Insets normalize(Insets insets) {
        if (insets.top > 64 || insets.top < 0) {
            insets.top = 28;
        }
        if (insets.left > 32 || insets.left < 0) {
            insets.left = 6;
        }
        if (insets.right > 32 || insets.right < 0) {
            insets.right = 6;
        }
        if (insets.bottom > 32 || insets.bottom < 0) {
            insets.bottom = 6;
        }
        return insets;
    }
    static XNETProtocol g_net_protocol = null;
    static XWINProtocol g_win_protocol = null;
    static boolean isNetWMName(String name) {
        if (g_net_protocol != null) {
            return g_net_protocol.isWMName(name);
        } else {
            return false;
        }
    }
    static void initAtoms() {
        final Object[][] atomInitList ={
            { XA_WM_STATE,                      "WM_STATE"                  },
            { XA_KDE_NET_WM_FRAME_STRUT,    "_KDE_NET_WM_FRAME_STRUT"       },
            { XA_E_FRAME_SIZE,              "_E_FRAME_SIZE"                 },
            { XA_KWM_WIN_ICONIFIED,          "KWM_WIN_ICONIFIED"             },
            { XA_KWM_WIN_MAXIMIZED,          "KWM_WIN_MAXIMIZED"             },
            { XA_OL_DECOR_DEL,               "_OL_DECOR_DEL"                 },
            { XA_OL_DECOR_HEADER,            "_OL_DECOR_HEADER"              },
            { XA_OL_DECOR_RESIZE,            "_OL_DECOR_RESIZE"              },
            { XA_OL_DECOR_PIN,               "_OL_DECOR_PIN"                 },
            { XA_OL_DECOR_CLOSE,             "_OL_DECOR_CLOSE"               },
            { XA_MWM_HINTS,                  "_MOTIF_WM_HINTS"               },
            { XA_NET_FRAME_EXTENTS,          "_NET_FRAME_EXTENTS"            },
            { XA_NET_REQUEST_FRAME_EXTENTS,  "_NET_REQUEST_FRAME_EXTENTS"    },
        };
        String[] names = new String[atomInitList.length];
        for (int index = 0; index < names.length; index++) {
            names[index] = (String)atomInitList[index][1];
        }
        int atomSize = XAtom.getAtomSize();
        long atoms = unsafe.allocateMemory(names.length*atomSize);
        XToolkit.awtLock();
        try {
            int status = XlibWrapper.XInternAtoms(XToolkit.getDisplay(), names, false, atoms);
            if (status == 0) {
                return;
            }
            for (int atom = 0, atomPtr = 0; atom < names.length; atom++, atomPtr += atomSize) {
                ((XAtom)(atomInitList[atom][0])).setValues(XToolkit.getDisplay(), names[atom], XAtom.getAtom(atoms + atomPtr));
            }
        } finally {
            XToolkit.awtUnlock();
            unsafe.freeMemory(atoms);
        }
    }
    private static boolean isNoWM() {
        String vendor_string = XlibWrapper.ServerVendor(XToolkit.getDisplay());
        if (vendor_string.indexOf("eXcursion") != -1) {
            if (insLog.isLoggable(PlatformLogger.FINE)) {
                insLog.finer("eXcursion means NO_WM");
            }
            return true;
        }
        XSetWindowAttributes substruct = new XSetWindowAttributes();
        try {
            final long default_screen_number =
                XlibWrapper.DefaultScreen(XToolkit.getDisplay());
            final String selection_name = "WM_S" + default_screen_number;
            long selection_owner =
                XlibWrapper.XGetSelectionOwner(XToolkit.getDisplay(),
                                               XAtom.get(selection_name).getAtom());
            if (insLog.isLoggable(PlatformLogger.FINE)) {
                insLog.finer("selection owner of " + selection_name
                             + " is " + selection_owner);
            }
            if (selection_owner != XConstants.None) {
                return false;
            }
            winmgr_running = false;
            substruct.set_event_mask(XConstants.SubstructureRedirectMask);
            XToolkit.WITH_XERROR_HANDLER(detectWMHandler);
            XlibWrapper.XChangeWindowAttributes(XToolkit.getDisplay(),
                                                XToolkit.getDefaultRootWindow(),
                                                XConstants.CWEventMask,
                                                substruct.pData);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (!winmgr_running) {
                substruct.set_event_mask(0);
                XlibWrapper.XChangeWindowAttributes(XToolkit.getDisplay(),
                                                    XToolkit.getDefaultRootWindow(),
                                                    XConstants.CWEventMask,
                                                    substruct.pData);
                if (insLog.isLoggable(PlatformLogger.FINE)) {
                    insLog.finer("It looks like there is no WM thus NO_WM");
                }
            }
            return !winmgr_running;
        } finally {
            substruct.dispose();
        }
    }
    static XAtom XA_ENLIGHTENMENT_COMMS = new XAtom("ENLIGHTENMENT_COMMS", false);
    static long getECommsWindowIDProperty(long window) {
        if (!XA_ENLIGHTENMENT_COMMS.isInterned()) {
            return 0;
        }
        WindowPropertyGetter getter =
            new WindowPropertyGetter(window, XA_ENLIGHTENMENT_COMMS, 0, 14, false,
                                     XAtom.XA_STRING);
        try {
            int status = getter.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status != XConstants.Success || getter.getData() == 0) {
                return 0;
            }
            if (getter.getActualType() != XAtom.XA_STRING
                || getter.getActualFormat() != 8
                || getter.getNumberOfItems() != 14 || getter.getBytesAfter() != 0)
            {
                return 0;
            }
            byte[] bytes = XlibWrapper.getStringBytes(getter.getData());
            String id = new String(bytes);
            log.finer("ENLIGHTENMENT_COMMS is " + id);
            Pattern winIdPat = Pattern.compile("WINID\\s+(\\p{XDigit}{0,8})");
            try {
                Matcher match = winIdPat.matcher(id);
                if (match.matches()) {
                    log.finest("Match group count: " + match.groupCount());
                    String longId = match.group(1);
                    log.finest("Match group 1 " + longId);
                    long winid = Long.parseLong(longId, 16);
                    log.finer("Enlightenment communication window " + winid);
                    return winid;
                } else {
                    log.finer("ENLIGHTENMENT_COMMS has wrong format");
                    return 0;
                }
            } catch (Exception e) {
                if (log.isLoggable(PlatformLogger.FINER)) {
                    e.printStackTrace();
                }
                return 0;
            }
        } finally {
            getter.dispose();
        }
    }
    static boolean isEnlightenment() {
        long root_xref = getECommsWindowIDProperty(XToolkit.getDefaultRootWindow());
        if (root_xref == 0) {
            return false;
        }
        long self_xref = getECommsWindowIDProperty(root_xref);
        if (self_xref != root_xref) {
            return false;
        }
        return true;
    }
    static final XAtom XA_DT_SM_WINDOW_INFO = new XAtom("_DT_SM_WINDOW_INFO", false);
    static final XAtom XA_DT_SM_STATE_INFO = new XAtom("_DT_SM_STATE_INFO", false);
    static boolean isCDE() {
        if (!XA_DT_SM_WINDOW_INFO.isInterned()) {
            log.finer("{0} is not interned", XA_DT_SM_WINDOW_INFO);
            return false;
        }
        WindowPropertyGetter getter =
            new WindowPropertyGetter(XToolkit.getDefaultRootWindow(),
                                     XA_DT_SM_WINDOW_INFO, 0, 2,
                                     false, XA_DT_SM_WINDOW_INFO);
        try {
            int status = getter.execute();
            if (status != XConstants.Success || getter.getData() == 0) {
                log.finer("Getting of _DT_SM_WINDOW_INFO is not successfull");
                return false;
            }
            if (getter.getActualType() != XA_DT_SM_WINDOW_INFO.getAtom()
                || getter.getActualFormat() != 32
                || getter.getNumberOfItems() != 2 || getter.getBytesAfter() != 0)
            {
                log.finer("Wrong format of _DT_SM_WINDOW_INFO");
                return false;
            }
            long wmwin = Native.getWindow(getter.getData(), 1); 
            if (wmwin == 0) {
                log.fine("WARNING: DT_SM_WINDOW_INFO exists but returns zero windows");
                return false;
            }
            if (!XA_DT_SM_STATE_INFO.isInterned()) {
                log.finer("{0} is not interned", XA_DT_SM_STATE_INFO);
                return false;
            }
            WindowPropertyGetter getter2 =
                new WindowPropertyGetter(wmwin, XA_DT_SM_STATE_INFO, 0, 1,
                                         false, XA_DT_SM_STATE_INFO);
            try {
                status = getter2.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (status != XConstants.Success || getter2.getData() == 0) {
                    log.finer("Getting of _DT_SM_STATE_INFO is not successfull");
                    return false;
                }
                if (getter2.getActualType() != XA_DT_SM_STATE_INFO.getAtom()
                    || getter2.getActualFormat() != 32)
                {
                    log.finer("Wrong format of _DT_SM_STATE_INFO");
                    return false;
                }
                return true;
            } finally {
                getter2.dispose();
            }
        } finally {
            getter.dispose();
        }
    }
    static final XAtom XA_MOTIF_WM_INFO = new XAtom("_MOTIF_WM_INFO", false);
    static final XAtom XA_DT_WORKSPACE_CURRENT = new XAtom("_DT_WORKSPACE_CURRENT", false);
    static boolean isMotif() {
        if (!(XA_MOTIF_WM_INFO.isInterned()) ) {
            return false;
        }
        WindowPropertyGetter getter =
            new WindowPropertyGetter(XToolkit.getDefaultRootWindow(),
                                     XA_MOTIF_WM_INFO, 0,
                                     MWMConstants.PROP_MOTIF_WM_INFO_ELEMENTS,
                                     false, XA_MOTIF_WM_INFO);
        try {
            int status = getter.execute();
            if (status != XConstants.Success || getter.getData() == 0) {
                return false;
            }
            if (getter.getActualType() != XA_MOTIF_WM_INFO.getAtom()
                || getter.getActualFormat() != 32
                || getter.getNumberOfItems() != MWMConstants.PROP_MOTIF_WM_INFO_ELEMENTS
                || getter.getBytesAfter() != 0)
            {
                return false;
            }
            long wmwin = Native.getLong(getter.getData(), 1);
            if (wmwin != 0) {
                if (XA_DT_WORKSPACE_CURRENT.isInterned()) {
                    XAtom[] curws = XA_DT_WORKSPACE_CURRENT.getAtomListProperty(wmwin);
                    if (curws.length == 0) {
                        return false;
                    }
                    return true;
                } else {
                    WindowPropertyGetter state_getter =
                        new WindowPropertyGetter(wmwin,
                                                 XA_WM_STATE,
                                                 0, 1, false,
                                                 XA_WM_STATE);
                    try {
                        if (state_getter.execute() == XConstants.Success &&
                            state_getter.getData() != 0 &&
                            state_getter.getActualType() == XA_WM_STATE.getAtom())
                        {
                            return true;
                        }
                    } finally {
                        state_getter.dispose();
                    }
                }
            }
        } finally {
            getter.dispose();
        }
        return false;
    }
    static boolean isSawfish() {
        return isNetWMName("Sawfish");
    }
    static boolean isKDE2() {
        return isNetWMName("KWin");
    }
    static boolean isCompiz() {
        return isNetWMName("compiz");
    }
    static boolean isLookingGlass() {
        return isNetWMName("LG3D");
    }
    static boolean isMetacity() {
        return isNetWMName("Metacity");
    }
    static boolean isNonReparentingWM() {
        return (XWM.getWMID() == XWM.COMPIZ_WM || XWM.getWMID() == XWM.LG3D_WM);
    }
    static final XAtom XA_ICEWM_WINOPTHINT = new XAtom("_ICEWM_WINOPTHINT", false);
    static final char opt[] = {
        'A','W','T','_','I','C','E','W','M','_','T','E','S','T','\0',
        'a','l','l','W','o','r','k','s','p','a','c','e','s','\0',
        '0','\0'
    };
    static boolean prepareIsIceWM() {
        if (!XA_ICEWM_WINOPTHINT.isInterned()) {
            log.finer("{0} is not interned", XA_ICEWM_WINOPTHINT);
            return false;
        }
        XToolkit.awtLock();
        try {
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XlibWrapper.XChangePropertyS(XToolkit.getDisplay(), XToolkit.getDefaultRootWindow(),
                                         XA_ICEWM_WINOPTHINT.getAtom(),
                                         XA_ICEWM_WINOPTHINT.getAtom(),
                                         8, XConstants.PropModeReplace,
                                         new String(opt));
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null && XToolkit.saved_error.get_error_code() != XConstants.Success) {
                log.finer("Erorr getting XA_ICEWM_WINOPTHINT property");
                return false;
            }
            log.finer("Prepared for IceWM detection");
            return true;
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static boolean isIceWM() {
        if (!XA_ICEWM_WINOPTHINT.isInterned()) {
            log.finer("{0} is not interned", XA_ICEWM_WINOPTHINT);
            return false;
        }
        WindowPropertyGetter getter =
            new WindowPropertyGetter(XToolkit.getDefaultRootWindow(),
                                     XA_ICEWM_WINOPTHINT, 0, 0xFFFF,
                                     true, XA_ICEWM_WINOPTHINT);
        try {
            int status = getter.execute();
            boolean res = (status == XConstants.Success && getter.getActualType() != 0);
            log.finer("Status getting XA_ICEWM_WINOPTHINT: " + !res);
            return !res || isNetWMName("IceWM");
        } finally {
            getter.dispose();
        }
    }
    static final XAtom XA_SUN_WM_PROTOCOLS = new XAtom("_SUN_WM_PROTOCOLS", false);
    static boolean isOpenLook() {
        if (!XA_SUN_WM_PROTOCOLS.isInterned()) {
            return false;
        }
        XAtom[] list = XA_SUN_WM_PROTOCOLS.getAtomListProperty(XToolkit.getDefaultRootWindow());
        return (list.length != 0);
    }
    private static boolean winmgr_running = false;
    private static XErrorHandler detectWMHandler = new XErrorHandler.XBaseErrorHandler() {
        @Override
        public int handleError(long display, XErrorEvent err) {
            if ((err.get_request_code() == XProtocolConstants.X_ChangeWindowAttributes) &&
                (err.get_error_code() == XConstants.BadAccess))
            {
                winmgr_running = true;
                return 0;
            }
            return super.handleError(display, err);
        }
    };
    static int awt_wmgr = XWM.UNDETERMINED_WM;
    static XWM wm;
    static XWM getWM() {
        if (wm == null) {
            wm = new XWM(awt_wmgr = getWMID());
        }
        return wm;
    }
    static int getWMID() {
        if (insLog.isLoggable(PlatformLogger.FINEST)) {
            insLog.finest("awt_wmgr = " + awt_wmgr);
        }
        if (awt_wmgr != XWM.UNDETERMINED_WM) {
            return awt_wmgr;
        }
        XSetWindowAttributes substruct = new XSetWindowAttributes();
        XToolkit.awtLock();
        try {
            if (isNoWM()) {
                awt_wmgr = XWM.NO_WM;
                return awt_wmgr;
            }
            XNETProtocol l_net_protocol = g_net_protocol = new XNETProtocol();
            l_net_protocol.detect();
            if (log.isLoggable(PlatformLogger.FINE) && l_net_protocol.active()) {
                log.fine("_NET_WM_NAME is " + l_net_protocol.getWMName());
            }
            XWINProtocol win = g_win_protocol = new XWINProtocol();
            win.detect();
            boolean doIsIceWM = prepareIsIceWM(); 
            if (isEnlightenment()) {
                awt_wmgr = XWM.ENLIGHTEN_WM;
            } else if (isMetacity()) {
                awt_wmgr = XWM.METACITY_WM;
            } else if (isSawfish()) {
                awt_wmgr = XWM.SAWFISH_WM;
            } else if (isKDE2()) {
                awt_wmgr =XWM.KDE2_WM;
            } else if (isCompiz()) {
                awt_wmgr = XWM.COMPIZ_WM;
            } else if (isLookingGlass()) {
                awt_wmgr = LG3D_WM;
            } else if (doIsIceWM && isIceWM()) {
                awt_wmgr = XWM.ICE_WM;
            }
            else if (l_net_protocol.active()) {
                awt_wmgr = XWM.OTHER_WM;
            } else if (win.active()) {
                awt_wmgr = XWM.OTHER_WM;
            }
            else if (isCDE()) { 
                awt_wmgr = XWM.CDE_WM;
            } else if (isMotif()) {
                awt_wmgr = XWM.MOTIF_WM;
            } else if (isOpenLook()) {
                awt_wmgr = XWM.OPENLOOK_WM;
            } else {
                awt_wmgr = XWM.OTHER_WM;
            }
            return awt_wmgr;
        } finally {
            XToolkit.awtUnlock();
            substruct.dispose();
        }
    }
    static void removeSizeHints(XDecoratedPeer window, long mask) {
        mask &= XUtilConstants.PMaxSize | XUtilConstants.PMinSize;
        XToolkit.awtLock();
        try {
            XSizeHints hints = window.getHints();
            if ((hints.get_flags() & mask) == 0) {
                return;
            }
            hints.set_flags(hints.get_flags() & ~mask);
            if (insLog.isLoggable(PlatformLogger.FINER)) insLog.finer("Setting hints, flags " + XlibWrapper.hintsToString(hints.get_flags()));
            XlibWrapper.XSetWMNormalHints(XToolkit.getDisplay(),
                                          window.getWindow(),
                                          hints.pData);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static int normalizeMotifDecor(int decorations) {
        if ((decorations & MWMConstants.MWM_DECOR_ALL) == 0) {
            return decorations;
        }
        int d = MWMConstants.MWM_DECOR_BORDER | MWMConstants.MWM_DECOR_RESIZEH
            | MWMConstants.MWM_DECOR_TITLE
            | MWMConstants.MWM_DECOR_MENU | MWMConstants.MWM_DECOR_MINIMIZE
            | MWMConstants.MWM_DECOR_MAXIMIZE;
        d &= ~decorations;
        return d;
    }
    static int normalizeMotifFunc(int functions) {
        if ((functions & MWMConstants.MWM_FUNC_ALL) == 0) {
            return functions;
        }
        int f = MWMConstants.MWM_FUNC_RESIZE |
                MWMConstants.MWM_FUNC_MOVE |
                MWMConstants.MWM_FUNC_MAXIMIZE |
                MWMConstants.MWM_FUNC_MINIMIZE |
                MWMConstants.MWM_FUNC_CLOSE;
        f &= ~functions;
        return f;
    }
    static void setOLDecor(XWindow window, boolean resizable, int decorations) {
        if (window == null) {
            return;
        }
        XAtomList decorDel = new XAtomList();
        decorations = normalizeMotifDecor(decorations);
        if (insLog.isLoggable(PlatformLogger.FINER)) insLog.finer("Setting OL_DECOR to " + Integer.toBinaryString(decorations));
        if ((decorations & MWMConstants.MWM_DECOR_TITLE) == 0) {
            decorDel.add(XA_OL_DECOR_HEADER);
        }
        if ((decorations & (MWMConstants.MWM_DECOR_RESIZEH | MWMConstants.MWM_DECOR_MAXIMIZE)) == 0) {
            decorDel.add(XA_OL_DECOR_RESIZE);
        }
        if ((decorations & (MWMConstants.MWM_DECOR_MENU |
                            MWMConstants.MWM_DECOR_MAXIMIZE |
                            MWMConstants.MWM_DECOR_MINIMIZE)) == 0)
        {
            decorDel.add(XA_OL_DECOR_CLOSE);
        }
        if (decorDel.size() == 0) {
            insLog.finer("Deleting OL_DECOR");
            XA_OL_DECOR_DEL.DeleteProperty(window);
        } else {
            if (insLog.isLoggable(PlatformLogger.FINER)) insLog.finer("Setting OL_DECOR to " + decorDel);
            XA_OL_DECOR_DEL.setAtomListProperty(window, decorDel);
        }
    }
    static void setMotifDecor(XWindow window, boolean resizable, int decorations, int functions) {
        if ((decorations & MWMConstants.MWM_DECOR_ALL) != 0
            && (decorations != MWMConstants.MWM_DECOR_ALL))
        {
            decorations = normalizeMotifDecor(decorations);
        }
        if ((functions & MWMConstants.MWM_FUNC_ALL) != 0
            && (functions != MWMConstants.MWM_FUNC_ALL))
        {
            functions = normalizeMotifFunc(functions);
        }
        PropMwmHints hints = window.getMWMHints();
        hints.set_flags(hints.get_flags() |
                        MWMConstants.MWM_HINTS_FUNCTIONS |
                        MWMConstants.MWM_HINTS_DECORATIONS);
        hints.set_functions(functions);
        hints.set_decorations(decorations);
        if (stateLog.isLoggable(PlatformLogger.FINER)) stateLog.finer("Setting MWM_HINTS to " + hints);
        window.setMWMHints(hints);
    }
    static boolean needRemap(XDecoratedPeer window) {
        return !window.isEmbedded();
    }
    static void setShellDecor(XDecoratedPeer window) {
        int decorations = window.getDecorations();
        int functions = window.getFunctions();
        boolean resizable = window.isResizable();
        if (!resizable) {
            if ((decorations & MWMConstants.MWM_DECOR_ALL) != 0) {
                decorations |= MWMConstants.MWM_DECOR_RESIZEH | MWMConstants.MWM_DECOR_MAXIMIZE;
            } else {
                decorations &= ~(MWMConstants.MWM_DECOR_RESIZEH | MWMConstants.MWM_DECOR_MAXIMIZE);
            }
        }
        setMotifDecor(window, resizable, decorations, functions);
        setOLDecor(window, resizable, decorations);
        if (window.isShowing() && needRemap(window)) {
            window.xSetVisible(false);
            XToolkit.XSync();
            window.xSetVisible(true);
        }
    }
    static void setShellResizable(XDecoratedPeer window) {
        if (insLog.isLoggable(PlatformLogger.FINE)) insLog.fine("Setting shell resizable " + window);
        XToolkit.awtLock();
        try {
            Rectangle shellBounds = window.getShellBounds();
            shellBounds.translate(-window.currentInsets.left, -window.currentInsets.top);
            window.updateSizeHints(window.getDimensions());
            requestWMExtents(window.getWindow());
            XlibWrapper.XMoveResizeWindow(XToolkit.getDisplay(), window.getShell(),
                                          shellBounds.x, shellBounds.y, shellBounds.width, shellBounds.height);
            removeSizeHints(window, XUtilConstants.PMaxSize);
            window.updateMinimumSize();
            setShellDecor(window);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static void setShellNotResizable(XDecoratedPeer window, WindowDimensions newDimensions, Rectangle shellBounds,
                                     boolean justChangeSize)
    {
        if (insLog.isLoggable(PlatformLogger.FINE)) insLog.fine("Setting non-resizable shell " + window + ", dimensions " + newDimensions +
                                                       ", shellBounds " + shellBounds +", just change size: " + justChangeSize);
        XToolkit.awtLock();
        try {
            if (!shellBounds.isEmpty()) {
                window.updateSizeHints(newDimensions);
                requestWMExtents(window.getWindow());
                XToolkit.XSync();
                XlibWrapper.XMoveResizeWindow(XToolkit.getDisplay(), window.getShell(),
                                              shellBounds.x, shellBounds.y, shellBounds.width, shellBounds.height);
            }
            if (!justChangeSize) {  
                setShellDecor(window);
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    private HashMap<Class<?>, Collection<?>> protocolsMap = new HashMap<Class<?>, Collection<?>>();
    <T> Collection<T> getProtocols(Class<T> protocolInterface) {
        Collection<T> res = (Collection<T>) protocolsMap.get(protocolInterface);
        if (res != null) {
            return res;
        } else {
            return new LinkedList<T>();
        }
    }
    private <T> void addProtocol(Class<T> protocolInterface, T protocol) {
        Collection<T> protocols = getProtocols(protocolInterface);
        protocols.add(protocol);
        protocolsMap.put(protocolInterface, protocols);
    }
    boolean supportsDynamicLayout() {
        int wm = getWMID();
        switch (wm) {
          case XWM.ENLIGHTEN_WM:
          case XWM.KDE2_WM:
          case XWM.SAWFISH_WM:
          case XWM.ICE_WM:
          case XWM.METACITY_WM:
              return true;
          case XWM.OPENLOOK_WM:
          case XWM.MOTIF_WM:
          case XWM.CDE_WM:
              return false;
          default:
              return false;
        }
    }
    boolean supportsExtendedState(int state) {
        switch (state) {
          case Frame.MAXIMIZED_VERT:
          case Frame.MAXIMIZED_HORIZ:
              if (getWMID() == METACITY_WM) {
                  return false;
              }
          case Frame.MAXIMIZED_BOTH:
              for (XStateProtocol proto : getProtocols(XStateProtocol.class)) {
                  if (proto.supportsState(state)) {
                      return true;
                  }
              }
          default:
              return false;
        }
    }
    int getExtendedState(XWindowPeer window) {
        int state = 0;
        for (XStateProtocol proto : getProtocols(XStateProtocol.class)) {
            state |= proto.getState(window);
        }
        if (state != 0) {
            return state;
        } else {
            return Frame.NORMAL;
        }
    }
    boolean isStateChange(XDecoratedPeer window, XPropertyEvent e) {
        if (!window.isShowing()) {
            stateLog.finer("Window is not showing");
            return false;
        }
        int wm_state = window.getWMState();
        if (wm_state == XUtilConstants.WithdrawnState) {
            stateLog.finer("WithdrawnState");
            return false;
        } else {
            stateLog.finer("Window WM_STATE is " + wm_state);
        }
        boolean is_state_change = false;
        if (e.get_atom() == XA_WM_STATE.getAtom()) {
            is_state_change = true;
        }
        for (XStateProtocol proto : getProtocols(XStateProtocol.class)) {
            is_state_change |= proto.isStateChange(e);
            stateLog.finest(proto + ": is state changed = " + is_state_change);
        }
        return is_state_change;
    }
    int getState(XDecoratedPeer window) {
        int res = 0;
        final int wm_state = window.getWMState();
        if (wm_state == XUtilConstants.IconicState) {
            res = Frame.ICONIFIED;
        } else {
            res = Frame.NORMAL;
        }
        res |= getExtendedState(window);
        return res;
    }
    void setLayer(XWindowPeer window, int layer) {
        for (XLayerProtocol proto : getProtocols(XLayerProtocol.class)) {
            if (proto.supportsLayer(layer)) {
                proto.setLayer(window, layer);
            }
        }
        XToolkit.XSync();
    }
    void setExtendedState(XWindowPeer window, int state) {
        for (XStateProtocol proto : getProtocols(XStateProtocol.class)) {
            if (proto.supportsState(state)) {
                proto.setState(window, state);
                break;
            }
        }
        if (!window.isShowing()) {
            XToolkit.awtLock();
            try {
                XlibWrapper.XDeleteProperty(XToolkit.getDisplay(),
                                            window.getWindow(),
                                            XA_KWM_WIN_ICONIFIED.getAtom());
                XlibWrapper.XDeleteProperty(XToolkit.getDisplay(),
                                            window.getWindow(),
                                            XA_KWM_WIN_MAXIMIZED.getAtom());
            }
            finally {
                XToolkit.awtUnlock();
            }
        }
        XToolkit.XSync();
    }
    void unshadeKludge(XDecoratedPeer window) {
        assert(window.isShowing());
        for (XStateProtocol proto : getProtocols(XStateProtocol.class)) {
            proto.unshadeKludge(window);
        }
        XToolkit.XSync();
    }
    static boolean inited = false;
    static void init() {
        if (inited) {
            return;
        }
        initAtoms();
        getWM();
        inited = true;
    }
    void initializeProtocols() {
        XNETProtocol net_protocol = g_net_protocol;
        if (net_protocol != null) {
            if (!net_protocol.active()) {
                net_protocol = null;
            } else {
                if (net_protocol.doStateProtocol()) {
                    addProtocol(XStateProtocol.class, net_protocol);
                }
                if (net_protocol.doLayerProtocol()) {
                    addProtocol(XLayerProtocol.class, net_protocol);
                }
            }
        }
        XWINProtocol win = g_win_protocol;
        if (win != null) {
            if (win.active()) {
                if (win.doStateProtocol()) {
                    addProtocol(XStateProtocol.class, win);
                }
                if (win.doLayerProtocol()) {
                    addProtocol(XLayerProtocol.class, win);
                }
            }
        }
    }
    HashMap storedInsets = new HashMap();
    Insets guessInsets(XDecoratedPeer window) {
        Insets res = (Insets)storedInsets.get(window.getClass());
        if (res == null) {
            switch (WMID) {
              case ENLIGHTEN_WM:
                  res = new Insets(19, 4, 4, 4);
                  break;
              case CDE_WM:
                  res = new Insets(28, 6, 6, 6);
                  break;
              case NO_WM:
              case LG3D_WM:
                  res = zeroInsets;
                  break;
              case MOTIF_WM:
              case OPENLOOK_WM:
              default:
                  res = defaultInsets;
            }
        }
        if (insLog.isLoggable(PlatformLogger.FINEST)) insLog.finest("WM guessed insets: " + res);
        return res;
    }
    static int awtWMStaticGravity = -1;
    static boolean configureGravityBuggy() {
        if (awtWMStaticGravity == -1) {
            awtWMStaticGravity = (XToolkit.getEnv("_JAVA_AWT_WM_STATIC_GRAVITY") != null) ? 1 : 0;
        }
        if (awtWMStaticGravity == 1) {
            return true;
        }
        switch(getWMID()) {
          case XWM.ICE_WM:
              if (g_net_protocol != null) {
                  String wm_name = g_net_protocol.getWMName();
                  Pattern pat = Pattern.compile("^IceWM (\\d+)\\.(\\d+)\\.(\\d+).*$");
                  try {
                      Matcher match = pat.matcher(wm_name);
                      if (match.matches()) {
                          int v1 = Integer.parseInt(match.group(1));
                          int v2 = Integer.parseInt(match.group(2));
                          int v3 = Integer.parseInt(match.group(3));
                          return !(v1 > 1 || (v1 == 1 && (v2 > 2 || (v2 == 2 && v3 >=2))));
                      }
                  } catch (Exception e) {
                      return true;
                  }
              }
              return true;
          case XWM.ENLIGHTEN_WM:
              return true;
          default:
              return false;
        }
    }
    public static Insets getInsetsFromExtents(long window) {
        if (window == XConstants.None) {
            return null;
        }
        XNETProtocol net_protocol = getWM().getNETProtocol();
        if (net_protocol != null && net_protocol.active()) {
            Insets insets = getInsetsFromProp(window, XA_NET_FRAME_EXTENTS);
            insLog.fine("_NET_FRAME_EXTENTS: {0}", insets);
            if (insets != null) {
                return insets;
            }
        }
        switch(getWMID()) {
          case XWM.KDE2_WM:
              return getInsetsFromProp(window, XA_KDE_NET_WM_FRAME_STRUT);
          case XWM.ENLIGHTEN_WM:
              return getInsetsFromProp(window, XA_E_FRAME_SIZE);
          default:
              return null;
        }
    }
    public static Insets getInsetsFromProp(long window, XAtom atom) {
        if (window == XConstants.None) {
            return null;
        }
        WindowPropertyGetter getter =
            new WindowPropertyGetter(window, atom,
                                     0, 4, false, XAtom.XA_CARDINAL);
        try {
            if (getter.execute() != XConstants.Success
                || getter.getData() == 0
                || getter.getActualType() != XAtom.XA_CARDINAL
                || getter.getActualFormat() != 32)
            {
                return null;
            } else {
                return new Insets((int)Native.getCard32(getter.getData(), 2), 
                                  (int)Native.getCard32(getter.getData(), 0), 
                                  (int)Native.getCard32(getter.getData(), 3), 
                                  (int)Native.getCard32(getter.getData(), 1)); 
            }
        } finally {
            getter.dispose();
        }
    }
    public static void requestWMExtents(long window) {
        if (window == XConstants.None) { 
            return;
        }
        log.fine("Requesting FRAME_EXTENTS");
        XClientMessageEvent msg = new XClientMessageEvent();
        msg.zero();
        msg.set_type(XConstants.ClientMessage);
        msg.set_display(XToolkit.getDisplay());
        msg.set_window(window);
        msg.set_format(32);
        XToolkit.awtLock();
        try {
            XNETProtocol net_protocol = getWM().getNETProtocol();
            if (net_protocol != null && net_protocol.active()) {
                msg.set_message_type(XA_NET_REQUEST_FRAME_EXTENTS.getAtom());
                XlibWrapper.XSendEvent(XToolkit.getDisplay(), XToolkit.getDefaultRootWindow(),
                                       false,
                                       XConstants.SubstructureRedirectMask | XConstants.SubstructureNotifyMask,
                                       msg.getPData());
            }
            if (getWMID() == XWM.KDE2_WM) {
                msg.set_message_type(XA_KDE_NET_WM_FRAME_STRUT.getAtom());
                XlibWrapper.XSendEvent(XToolkit.getDisplay(), XToolkit.getDefaultRootWindow(),
                                       false,
                                       XConstants.SubstructureRedirectMask | XConstants.SubstructureNotifyMask,
                                       msg.getPData());
            }
        } finally {
            XToolkit.awtUnlock();
            msg.dispose();
        }
    }
    boolean syncTopLevelPos(long window, XWindowAttributes attrs) {
        int tries = 0;
        XToolkit.awtLock();
        try {
            do {
                XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(), window, attrs.pData);
                if (attrs.get_x() != 0 || attrs.get_y() != 0) {
                    return true;
                }
                tries++;
                XToolkit.XSync();
            } while (tries < 50);
        }
        finally {
            XToolkit.awtUnlock();
        }
        return false;
    }
    Insets getInsets(XDecoratedPeer win, long window, long parent) {
        Insets correctWM = XWM.getInsetsFromExtents(window);
        insLog.finer("Got insets from property: {0}", correctWM);
        if (correctWM == null) {
            correctWM = new Insets(0,0,0,0);
            correctWM.top = -1;
            correctWM.left = -1;
            XWindowAttributes lwinAttr = new XWindowAttributes();
            XWindowAttributes pattr = new XWindowAttributes();
            try {
                switch (XWM.getWMID()) {
                  case XWM.ENLIGHTEN_WM: {
                      syncTopLevelPos(parent, lwinAttr);
                      correctWM.left = lwinAttr.get_x();
                      correctWM.top = lwinAttr.get_y();
                      XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                       XlibUtil.getParentWindow(parent),
                                                       pattr.pData);
                      correctWM.right = pattr.get_width() -
                          (lwinAttr.get_width() + correctWM.left);
                      correctWM.bottom = pattr.get_height() -
                          (lwinAttr.get_height() + correctWM.top);
                      break;
                  }
                  case XWM.ICE_WM: 
                  case XWM.KDE2_WM: 
                  case XWM.CDE_WM:
                  case XWM.MOTIF_WM: {
                      if (syncTopLevelPos(parent, lwinAttr)) {
                          correctWM.top = lwinAttr.get_y();
                          correctWM.left = lwinAttr.get_x();
                          correctWM.right = correctWM.left;
                          correctWM.bottom = correctWM.left;
                      } else {
                          return null;
                      }
                      break;
                  }
                  case XWM.SAWFISH_WM:
                  case XWM.OPENLOOK_WM: {
                      syncTopLevelPos(window, lwinAttr);
                      correctWM.top    = lwinAttr.get_y();
                      correctWM.left   = lwinAttr.get_x();
                      correctWM.right  = correctWM.left;
                      correctWM.bottom = correctWM.left;
                      break;
                  }
                  case XWM.OTHER_WM:
                  default: {                
                      insLog.finest("Getting correct insets for OTHER_WM/default, parent: {0}", parent);
                      syncTopLevelPos(parent, lwinAttr);
                      int status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                                    window, lwinAttr.pData);
                      status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                                parent, pattr.pData);
                      if (lwinAttr.get_root() == parent) {
                          insLog.finest("our parent is root so insets should be zero");
                          correctWM = new Insets(0, 0, 0, 0);
                          break;
                      }
                      if (lwinAttr.get_x() == 0 && lwinAttr.get_y() == 0
                          && lwinAttr.get_width()+2*lwinAttr.get_border_width() == pattr.get_width()
                          && lwinAttr.get_height()+2*lwinAttr.get_border_width() == pattr.get_height())
                      {
                          insLog.finest("Double reparenting detected, pattr({2})={0}, lwinAttr({3})={1}",
                                        lwinAttr, pattr, parent, window);
                          lwinAttr.set_x(pattr.get_x());
                          lwinAttr.set_y(pattr.get_y());
                          lwinAttr.set_border_width(lwinAttr.get_border_width()+pattr.get_border_width());
                          final long grand_parent = XlibUtil.getParentWindow(parent);
                          if (grand_parent == lwinAttr.get_root()) {
                              return null;
                          } else {
                              parent = grand_parent;
                              XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                               parent,
                                                               pattr.pData);
                          }
                      }
                      insLog.finest("Attrs before calculation: pattr({2})={0}, lwinAttr({3})={1}",
                                    lwinAttr, pattr, parent, window);
                      correctWM = new Insets(lwinAttr.get_y() + lwinAttr.get_border_width(),
                                             lwinAttr.get_x() + lwinAttr.get_border_width(),
                                             pattr.get_height() - (lwinAttr.get_y() + lwinAttr.get_height() + 2*lwinAttr.get_border_width()),
                                             pattr.get_width() -  (lwinAttr.get_x() + lwinAttr.get_width() + 2*lwinAttr.get_border_width()));
                      break;
                  } 
                } 
            } finally {
                lwinAttr.dispose();
                pattr.dispose();
            }
        }
        if (storedInsets.get(win.getClass()) == null) {
            storedInsets.put(win.getClass(), correctWM);
        }
        return correctWM;
    }
    boolean isDesktopWindow( long w ) {
        if (g_net_protocol != null) {
            XAtomList wtype = XAtom.get("_NET_WM_WINDOW_TYPE").getAtomListPropertyList( w );
            return wtype.contains( XAtom.get("_NET_WM_WINDOW_TYPE_DESKTOP") );
        } else {
            return false;
        }
    }
    public XNETProtocol getNETProtocol() {
        return g_net_protocol;
    }
    public boolean setNetWMIcon(XWindowPeer window, java.util.List<XIconInfo> icons) {
        if (g_net_protocol != null && g_net_protocol.active()) {
            g_net_protocol.setWMIcons(window, icons);
            return getWMID() != ICE_WM;
        }
        return false;
    }
}

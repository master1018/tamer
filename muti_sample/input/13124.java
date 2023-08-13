class XAwtState {
    private static WeakReference componentMouseEnteredRef = null;
    static void setComponentMouseEntered(Component component) {
        XToolkit.awtLock();
        try {
            if (component == null) {
                componentMouseEnteredRef = null;
                return;
            }
            if (component != getComponentMouseEntered()) {
                componentMouseEnteredRef = new WeakReference(component);
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static Component getComponentMouseEntered() {
        XToolkit.awtLock();
        try {
            if (componentMouseEnteredRef == null) {
                return null;
            }
            return (Component)componentMouseEnteredRef.get();
        } finally {
            XToolkit.awtUnlock();
        }
    }
    private static boolean inManualGrab = false;
    static boolean isManualGrab() {
        return inManualGrab;
    }
    private static WeakReference grabWindowRef = null;
    static void setGrabWindow(XBaseWindow grabWindow) {
        setGrabWindow(grabWindow, false);
    }
    static void setAutoGrabWindow(XBaseWindow grabWindow) {
        setGrabWindow(grabWindow, true);
    }
    private static void setGrabWindow(XBaseWindow grabWindow, boolean isAutoGrab) {
        XToolkit.awtLock();
        try {
            if (inManualGrab && isAutoGrab) {
                return;
            }
            inManualGrab = grabWindow != null && !isAutoGrab;
            if (grabWindow == null) {
                grabWindowRef = null;
                return;
            }
            if (grabWindow != getGrabWindow()) {
                grabWindowRef = new WeakReference(grabWindow);
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static XBaseWindow getGrabWindow() {
        XToolkit.awtLock();
        try {
            if (grabWindowRef == null) {
                return null;
            }
            XBaseWindow xbw = (XBaseWindow)grabWindowRef.get();
            if( xbw != null && xbw.isDisposed() ) {
                xbw = null;
                grabWindowRef = null;
            }else if( xbw == null ) {
                grabWindowRef = null;
            }
            return xbw;
        } finally {
            XToolkit.awtUnlock();
        }
    }
}

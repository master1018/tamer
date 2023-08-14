class XDialogPeer extends XDecoratedPeer implements DialogPeer {
    private Boolean undecorated;
    XDialogPeer(Dialog target) {
        super(target);
    }
    public void preInit(XCreateWindowParams params) {
        super.preInit(params);
        Dialog target = (Dialog)(this.target);
        undecorated = Boolean.valueOf(target.isUndecorated());
        winAttr.nativeDecor = !target.isUndecorated();
        if (winAttr.nativeDecor) {
            winAttr.decorations = winAttr.AWT_DECOR_ALL;
        } else {
            winAttr.decorations = winAttr.AWT_DECOR_NONE;
        }
        winAttr.functions = MWMConstants.MWM_FUNC_ALL;
        winAttr.isResizable =  true; 
        winAttr.initialResizability =  target.isResizable();
        winAttr.title = target.getTitle();
        winAttr.initialState = XWindowAttributesData.NORMAL;
    }
    public void setVisible(boolean vis) {
        XToolkit.awtLock();
        try {
            Dialog target = (Dialog)this.target;
            if (vis) {
                if (target.getModalityType() != Dialog.ModalityType.MODELESS) {
                    if (!isModalBlocked()) {
                        XBaseWindow.ungrabInput();
                    }
                }
            } else {
                restoreTransientFor(this);
                prevTransientFor = null;
                nextTransientFor = null;
            }
        } finally {
            XToolkit.awtUnlock();
        }
        super.setVisible(vis);
    }
    @Override
    boolean isTargetUndecorated() {
        if (undecorated != null) {
            return undecorated.booleanValue();
        } else {
            return ((Dialog)target).isUndecorated();
        }
    }
    int getDecorations() {
        int d = super.getDecorations();
        if ((d & MWMConstants.MWM_DECOR_ALL) != 0) {
            d |= (MWMConstants.MWM_DECOR_MINIMIZE | MWMConstants.MWM_DECOR_MAXIMIZE);
        } else {
            d &= ~(MWMConstants.MWM_DECOR_MINIMIZE | MWMConstants.MWM_DECOR_MAXIMIZE);
        }
        return d;
    }
    int getFunctions() {
        int f = super.getFunctions();
        if ((f & MWMConstants.MWM_FUNC_ALL) != 0) {
            f |= (MWMConstants.MWM_FUNC_MINIMIZE | MWMConstants.MWM_FUNC_MAXIMIZE);
        } else {
            f &= ~(MWMConstants.MWM_FUNC_MINIMIZE | MWMConstants.MWM_FUNC_MAXIMIZE);
        }
        return f;
    }
    public void blockWindows(java.util.List<Window> toBlock) {
        Vector<XWindowPeer> javaToplevels = null;
        XToolkit.awtLock();
        try {
            javaToplevels = XWindowPeer.collectJavaToplevels();
            for (Window w : toBlock) {
                XWindowPeer wp = (XWindowPeer)AWTAccessor.getComponentAccessor().getPeer(w);
                if (wp != null) {
                    wp.setModalBlocked((Dialog)target, true, javaToplevels);
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    boolean isFocusedWindowModalBlocker() {
        Window focusedWindow = XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow();
        XWindowPeer focusedWindowPeer = null;
        if (focusedWindow != null) {
            focusedWindowPeer = (XWindowPeer)AWTAccessor.getComponentAccessor().getPeer(focusedWindow);
        } else {
            focusedWindowPeer = getNativeFocusedWindowPeer();
        }
        synchronized (getStateLock()) {
            if (focusedWindowPeer != null && focusedWindowPeer.modalBlocker == target) {
                return true;
            }
        }
        return super.isFocusedWindowModalBlocker();
    }
}

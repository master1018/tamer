public final class XContentWindow extends XWindow {
    private static PlatformLogger insLog = PlatformLogger.getLogger("sun.awt.X11.insets.XContentWindow");
    static XContentWindow createContent(XDecoratedPeer parentFrame) {
        final WindowDimensions dims = parentFrame.getDimensions();
        Rectangle rec = dims.getBounds();
        Insets ins = dims.getInsets();
        if (ins != null) {
            rec.x = -ins.left;
            rec.y = -ins.top;
        } else {
            rec.x = 0;
            rec.y = 0;
        }
        final XContentWindow cw = new XContentWindow(parentFrame, rec);
        cw.xSetVisible(true);
        return cw;
    }
    private final XDecoratedPeer parentFrame;
    private final java.util.List<SavedExposeEvent> iconifiedExposeEvents =
            new java.util.ArrayList<SavedExposeEvent>();
    private XContentWindow(XDecoratedPeer parentFrame, Rectangle bounds) {
        super((Component)parentFrame.getTarget(), parentFrame.getShell(), bounds);
        this.parentFrame = parentFrame;
    }
    void preInit(XCreateWindowParams params) {
        super.preInit(params);
        params.putIfNull(BIT_GRAVITY, Integer.valueOf(XConstants.NorthWestGravity));
        Long eventMask = (Long)params.get(EVENT_MASK);
        if (eventMask != null) {
            eventMask = eventMask & ~(XConstants.StructureNotifyMask);
            params.put(EVENT_MASK, eventMask);
        }
    }
    protected String getWMName() {
        return "Content window";
    }
    protected boolean isEventDisabled(XEvent e) {
        switch (e.get_type()) {
          case XConstants.EnterNotify:
          case XConstants.LeaveNotify:
              return false;
          case XConstants.ConfigureNotify:
              return true;
          case XConstants.MapNotify:
          case XConstants.UnmapNotify:
              return true;
          default:
              return super.isEventDisabled(e) || parentFrame.isEventDisabled(e);
        }
    }
    void setContentBounds(WindowDimensions dims) {
        XToolkit.awtLock();
        try {
            Rectangle newBounds = dims.getBounds();
            Insets in = dims.getInsets();
            if (in != null) {
                newBounds.setLocation(-in.left, -in.top);
            }
            if (insLog.isLoggable(PlatformLogger.FINE)) insLog.fine("Setting content bounds {0}, old bounds {1}",
                                                                    newBounds, getBounds());
            boolean needHandleResize = !(newBounds.equals(getBounds()));
            reshape(newBounds);
            if (needHandleResize) {
                insLog.fine("Sending RESIZED");
                handleResize(newBounds);
            }
        } finally {
            XToolkit.awtUnlock();
        }
        validateSurface();
    }
    public void handleResize(Rectangle bounds) {
        AWTAccessor.getComponentAccessor().setSize((Component)target, bounds.width, bounds.height);
        postEvent(new ComponentEvent(target, ComponentEvent.COMPONENT_RESIZED));
    }
    public void handleExposeEvent(Component target, int x, int y, int w, int h) {
        if (parentFrame instanceof XFramePeer &&
                (((XFramePeer)parentFrame).getState() & java.awt.Frame.ICONIFIED) != 0) {
            iconifiedExposeEvents.add(new SavedExposeEvent(target, x, y, w, h));
        } else {
            super.handleExposeEvent(target, x, y, w, h);
        }
    }
    void purgeIconifiedExposeEvents() {
        for (SavedExposeEvent evt : iconifiedExposeEvents) {
            super.handleExposeEvent(evt.target, evt.x, evt.y, evt.w, evt.h);
        }
        iconifiedExposeEvents.clear();
    }
    private static class SavedExposeEvent {
        Component target;
        int x, y, w, h;
        SavedExposeEvent(Component target, int x, int y, int w, int h) {
            this.target = target;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
    public String toString() {
        return getClass().getName() + "[" + getBounds() + "]";
    }
}

class WScrollPanePeer extends WPanelPeer implements ScrollPanePeer {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WScrollPanePeer");
    int scrollbarWidth;
    int scrollbarHeight;
    int prevx;
    int prevy;
    static {
        initIDs();
    }
    static native void initIDs();
    native void create(WComponentPeer parent);
    native int getOffset(int orient);
    WScrollPanePeer(Component target) {
        super(target);
        scrollbarWidth = _getVScrollbarWidth();
        scrollbarHeight = _getHScrollbarHeight();
    }
    void initialize() {
        super.initialize();
        setInsets();
        Insets i = getInsets();
        setScrollPosition(-i.left,-i.top);
    }
    public void setUnitIncrement(Adjustable adj, int p) {
    }
    public Insets insets() {
        return getInsets();
    }
    private native void setInsets();
    public native synchronized void setScrollPosition(int x, int y);
    public int getHScrollbarHeight() {
        return scrollbarHeight;
    }
    private native int _getHScrollbarHeight();
    public int getVScrollbarWidth() {
        return scrollbarWidth;
    }
    private native int _getVScrollbarWidth();
    public Point getScrollOffset() {
        int x = getOffset(Adjustable.HORIZONTAL);
        int y = getOffset(Adjustable.VERTICAL);
        return new Point(x, y);
    }
    public void childResized(int width, int height) {
        ScrollPane sp = (ScrollPane)target;
        Dimension vs = sp.getSize();
        setSpans(vs.width, vs.height, width, height);
        setInsets();
    }
    native synchronized void setSpans(int viewWidth, int viewHeight,
                                      int childWidth, int childHeight);
    public void setValue(Adjustable adj, int v) {
        Component c = getScrollChild();
        if (c == null) {
            return;
        }
        Point p = c.getLocation();
        switch(adj.getOrientation()) {
        case Adjustable.VERTICAL:
            setScrollPosition(-(p.x), v);
            break;
        case Adjustable.HORIZONTAL:
            setScrollPosition(v, -(p.y));
            break;
        }
    }
    private Component getScrollChild() {
        ScrollPane sp = (ScrollPane)target;
        Component child = null;
        try {
            child = sp.getComponent(0);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return child;
    }
    private void postScrollEvent(int orient, int type,
                                 int pos, boolean isAdjusting)
    {
        Runnable adjustor = new Adjustor(orient, type, pos, isAdjusting);
        WToolkit.executeOnEventHandlerThread(new ScrollEvent(target, adjustor));
    }
    class ScrollEvent extends PeerEvent {
        ScrollEvent(Object source, Runnable runnable) {
            super(source, runnable, 0L);
        }
        public PeerEvent coalesceEvents(PeerEvent newEvent) {
            if (log.isLoggable(PlatformLogger.FINEST)) {
                log.finest("ScrollEvent coalesced: " + newEvent);
            }
            if (newEvent instanceof ScrollEvent) {
                return newEvent;
            }
            return null;
        }
    }
    native void setTypedValue(ScrollPaneAdjustable adjustable, int newpos, int type);
    class Adjustor implements Runnable {
        int orient;             
        int type;               
        int pos;                
        boolean isAdjusting;    
        Adjustor(int orient, int type, int pos, boolean isAdjusting) {
            this.orient = orient;
            this.type = type;
            this.pos = pos;
            this.isAdjusting = isAdjusting;
        }
        public void run() {
            if (getScrollChild() == null) {
                return;
            }
            ScrollPane sp = (ScrollPane)WScrollPanePeer.this.target;
            ScrollPaneAdjustable adj = null;
            if (orient == Adjustable.VERTICAL) {
                adj = (ScrollPaneAdjustable)sp.getVAdjustable();
            } else if (orient == Adjustable.HORIZONTAL) {
                adj = (ScrollPaneAdjustable)sp.getHAdjustable();
            } else {
                if (log.isLoggable(PlatformLogger.FINE)) {
                    log.fine("Assertion failed: unknown orient");
                }
            }
            if (adj == null) {
                return;
            }
            int newpos = adj.getValue();
            switch (type) {
              case AdjustmentEvent.UNIT_DECREMENT:
                  newpos -= adj.getUnitIncrement();
                  break;
              case AdjustmentEvent.UNIT_INCREMENT:
                  newpos += adj.getUnitIncrement();
                  break;
              case AdjustmentEvent.BLOCK_DECREMENT:
                  newpos -= adj.getBlockIncrement();
                  break;
              case AdjustmentEvent.BLOCK_INCREMENT:
                  newpos += adj.getBlockIncrement();
                  break;
              case AdjustmentEvent.TRACK:
                  newpos = this.pos;
                  break;
              default:
                  if (log.isLoggable(PlatformLogger.FINE)) {
                      log.fine("Assertion failed: unknown type");
                  }
                  return;
            }
            newpos = Math.max(adj.getMinimum(), newpos);
            newpos = Math.min(adj.getMaximum(), newpos);
            adj.setValueIsAdjusting(isAdjusting);
            setTypedValue(adj, newpos, type);
            Component hwAncestor = getScrollChild();
            while (hwAncestor != null
                   && !(hwAncestor.getPeer() instanceof WComponentPeer))
            {
                hwAncestor = hwAncestor.getParent();
            }
            if (log.isLoggable(PlatformLogger.FINE)) {
                if (hwAncestor == null) {
                    log.fine("Assertion (hwAncestor != null) failed, " +
                             "couldn't find heavyweight ancestor of scroll pane child");
                }
            }
            WComponentPeer hwPeer = (WComponentPeer)hwAncestor.getPeer();
            hwPeer.paintDamagedAreaImmediately();
        }
    }
}

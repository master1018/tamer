public class ScrollPane extends Container implements Accessible {
    private static native void initIDs();
    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }
    public static final int SCROLLBARS_AS_NEEDED = 0;
    public static final int SCROLLBARS_ALWAYS = 1;
    public static final int SCROLLBARS_NEVER = 2;
    private int scrollbarDisplayPolicy;
    private ScrollPaneAdjustable vAdjustable;
    private ScrollPaneAdjustable hAdjustable;
    private static final String base = "scrollpane";
    private static int nameCounter = 0;
    private static final boolean defaultWheelScroll = true;
    private boolean wheelScrollingEnabled = defaultWheelScroll;
    private static final long serialVersionUID = 7956609840827222915L;
    public ScrollPane() throws HeadlessException {
        this(SCROLLBARS_AS_NEEDED);
    }
    @ConstructorProperties({"scrollbarDisplayPolicy"})
    public ScrollPane(int scrollbarDisplayPolicy) throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        this.layoutMgr = null;
        this.width = 100;
        this.height = 100;
        switch (scrollbarDisplayPolicy) {
            case SCROLLBARS_NEVER:
            case SCROLLBARS_AS_NEEDED:
            case SCROLLBARS_ALWAYS:
                this.scrollbarDisplayPolicy = scrollbarDisplayPolicy;
                break;
            default:
                throw new IllegalArgumentException("illegal scrollbar display policy");
        }
        vAdjustable = new ScrollPaneAdjustable(this, new PeerFixer(this),
                                               Adjustable.VERTICAL);
        hAdjustable = new ScrollPaneAdjustable(this, new PeerFixer(this),
                                               Adjustable.HORIZONTAL);
        setWheelScrollingEnabled(defaultWheelScroll);
    }
    String constructComponentName() {
        synchronized (ScrollPane.class) {
            return base + nameCounter++;
        }
    }
    private void addToPanel(Component comp, Object constraints, int index) {
        Panel child = new Panel();
        child.setLayout(new BorderLayout());
        child.add(comp);
        super.addImpl(child, constraints, index);
        validate();
    }
    protected final void addImpl(Component comp, Object constraints, int index) {
        synchronized (getTreeLock()) {
            if (getComponentCount() > 0) {
                remove(0);
            }
            if (index > 0) {
                throw new IllegalArgumentException("position greater than 0");
            }
            if (!SunToolkit.isLightweightOrUnknown(comp)) {
                super.addImpl(comp, constraints, index);
            } else {
                addToPanel(comp, constraints, index);
            }
        }
    }
    public int getScrollbarDisplayPolicy() {
        return scrollbarDisplayPolicy;
    }
    public Dimension getViewportSize() {
        Insets i = getInsets();
        return new Dimension(width - i.right - i.left,
                             height - i.top - i.bottom);
    }
    public int getHScrollbarHeight() {
        int h = 0;
        if (scrollbarDisplayPolicy != SCROLLBARS_NEVER) {
            ScrollPanePeer peer = (ScrollPanePeer)this.peer;
            if (peer != null) {
                h = peer.getHScrollbarHeight();
            }
        }
        return h;
    }
    public int getVScrollbarWidth() {
        int w = 0;
        if (scrollbarDisplayPolicy != SCROLLBARS_NEVER) {
            ScrollPanePeer peer = (ScrollPanePeer)this.peer;
            if (peer != null) {
                w = peer.getVScrollbarWidth();
            }
        }
        return w;
    }
    public Adjustable getVAdjustable() {
        return vAdjustable;
    }
    public Adjustable getHAdjustable() {
        return hAdjustable;
    }
    public void setScrollPosition(int x, int y) {
        synchronized (getTreeLock()) {
            if (getComponentCount()==0) {
                throw new NullPointerException("child is null");
            }
            hAdjustable.setValue(x);
            vAdjustable.setValue(y);
        }
    }
    public void setScrollPosition(Point p) {
        setScrollPosition(p.x, p.y);
    }
    @Transient
    public Point getScrollPosition() {
        synchronized (getTreeLock()) {
            if (getComponentCount()==0) {
                throw new NullPointerException("child is null");
            }
            return new Point(hAdjustable.getValue(), vAdjustable.getValue());
        }
    }
    public final void setLayout(LayoutManager mgr) {
        throw new AWTError("ScrollPane controls layout");
    }
    public void doLayout() {
        layout();
    }
    Dimension calculateChildSize() {
        Dimension       size = getSize();
        Insets          insets = getInsets();
        int             viewWidth = size.width - insets.left*2;
        int             viewHeight = size.height - insets.top*2;
        boolean vbarOn;
        boolean hbarOn;
        Component child = getComponent(0);
        Dimension childSize = new Dimension(child.getPreferredSize());
        if (scrollbarDisplayPolicy == SCROLLBARS_AS_NEEDED) {
            vbarOn = childSize.height > viewHeight;
            hbarOn = childSize.width  > viewWidth;
        } else if (scrollbarDisplayPolicy == SCROLLBARS_ALWAYS) {
            vbarOn = hbarOn = true;
        } else { 
            vbarOn = hbarOn = false;
        }
        int vbarWidth = getVScrollbarWidth();
        int hbarHeight = getHScrollbarHeight();
        if (vbarOn) {
            viewWidth -= vbarWidth;
        }
        if(hbarOn) {
            viewHeight -= hbarHeight;
        }
        if (childSize.width < viewWidth) {
            childSize.width = viewWidth;
        }
        if (childSize.height < viewHeight) {
            childSize.height = viewHeight;
        }
        return childSize;
    }
    @Deprecated
    public void layout() {
        if (getComponentCount()==0) {
            return;
        }
        Component c = getComponent(0);
        Point p = getScrollPosition();
        Dimension cs = calculateChildSize();
        Dimension vs = getViewportSize();
        Insets i = getInsets();
        c.reshape(i.left - p.x, i.top - p.y, cs.width, cs.height);
        ScrollPanePeer peer = (ScrollPanePeer)this.peer;
        if (peer != null) {
            peer.childResized(cs.width, cs.height);
        }
        vs = getViewportSize();
        hAdjustable.setSpan(0, cs.width, vs.width);
        vAdjustable.setSpan(0, cs.height, vs.height);
    }
    public void printComponents(Graphics g) {
        if (getComponentCount()==0) {
            return;
        }
        Component c = getComponent(0);
        Point p = c.getLocation();
        Dimension vs = getViewportSize();
        Insets i = getInsets();
        Graphics cg = g.create();
        try {
            cg.clipRect(i.left, i.top, vs.width, vs.height);
            cg.translate(p.x, p.y);
            c.printAll(cg);
        } finally {
            cg.dispose();
        }
    }
    public void addNotify() {
        synchronized (getTreeLock()) {
            int vAdjustableValue = 0;
            int hAdjustableValue = 0;
            if (getComponentCount() > 0) {
                vAdjustableValue = vAdjustable.getValue();
                hAdjustableValue = hAdjustable.getValue();
                vAdjustable.setValue(0);
                hAdjustable.setValue(0);
            }
            if (peer == null)
                peer = getToolkit().createScrollPane(this);
            super.addNotify();
            if (getComponentCount() > 0) {
                vAdjustable.setValue(vAdjustableValue);
                hAdjustable.setValue(hAdjustableValue);
            }
        }
    }
    public String paramString() {
        String sdpStr;
        switch (scrollbarDisplayPolicy) {
            case SCROLLBARS_AS_NEEDED:
                sdpStr = "as-needed";
                break;
            case SCROLLBARS_ALWAYS:
                sdpStr = "always";
                break;
            case SCROLLBARS_NEVER:
                sdpStr = "never";
                break;
            default:
                sdpStr = "invalid display policy";
        }
        Point p = (getComponentCount()>0)? getScrollPosition() : new Point(0,0);
        Insets i = getInsets();
        return super.paramString()+",ScrollPosition=("+p.x+","+p.y+")"+
            ",Insets=("+i.top+","+i.left+","+i.bottom+","+i.right+")"+
            ",ScrollbarDisplayPolicy="+sdpStr+
        ",wheelScrollingEnabled="+isWheelScrollingEnabled();
    }
    void autoProcessMouseWheel(MouseWheelEvent e) {
        processMouseWheelEvent(e);
    }
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        if (isWheelScrollingEnabled()) {
            ScrollPaneWheelScroller.handleWheelScrolling(this, e);
            e.consume();
        }
        super.processMouseWheelEvent(e);
    }
    protected boolean eventTypeEnabled(int type) {
        if (type == MouseEvent.MOUSE_WHEEL && isWheelScrollingEnabled()) {
            return true;
        }
        else {
            return super.eventTypeEnabled(type);
        }
    }
    public void setWheelScrollingEnabled(boolean handleWheel) {
        wheelScrollingEnabled = handleWheel;
    }
    public boolean isWheelScrollingEnabled() {
        return wheelScrollingEnabled;
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException, HeadlessException
    {
        GraphicsEnvironment.checkHeadless();
        ObjectInputStream.GetField f = s.readFields();
        scrollbarDisplayPolicy = f.get("scrollbarDisplayPolicy",
                                       SCROLLBARS_AS_NEEDED);
        hAdjustable = (ScrollPaneAdjustable)f.get("hAdjustable", null);
        vAdjustable = (ScrollPaneAdjustable)f.get("vAdjustable", null);
        wheelScrollingEnabled = f.get("wheelScrollingEnabled",
                                      defaultWheelScroll);
    }
    class PeerFixer implements AdjustmentListener, java.io.Serializable
    {
        private static final long serialVersionUID = 1043664721353696630L;
        PeerFixer(ScrollPane scroller) {
            this.scroller = scroller;
        }
        public void adjustmentValueChanged(AdjustmentEvent e) {
            Adjustable adj = e.getAdjustable();
            int value = e.getValue();
            ScrollPanePeer peer = (ScrollPanePeer) scroller.peer;
            if (peer != null) {
                peer.setValue(adj, value);
            }
            Component c = scroller.getComponent(0);
            switch(adj.getOrientation()) {
            case Adjustable.VERTICAL:
                c.move(c.getLocation().x, -(value));
                break;
            case Adjustable.HORIZONTAL:
                c.move(-(value), c.getLocation().y);
                break;
            default:
                throw new IllegalArgumentException("Illegal adjustable orientation");
            }
        }
        private ScrollPane scroller;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTScrollPane();
        }
        return accessibleContext;
    }
    protected class AccessibleAWTScrollPane extends AccessibleAWTContainer
    {
        private static final long serialVersionUID = 6100703663886637L;
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SCROLL_PANE;
        }
    } 
}
class PeerFixer implements AdjustmentListener, java.io.Serializable {
    private static final long serialVersionUID = 7051237413532574756L;
    PeerFixer(ScrollPane scroller) {
        this.scroller = scroller;
    }
    public void adjustmentValueChanged(AdjustmentEvent e) {
        Adjustable adj = e.getAdjustable();
        int value = e.getValue();
        ScrollPanePeer peer = (ScrollPanePeer) scroller.peer;
        if (peer != null) {
            peer.setValue(adj, value);
        }
        Component c = scroller.getComponent(0);
        switch(adj.getOrientation()) {
        case Adjustable.VERTICAL:
            c.move(c.getLocation().x, -(value));
            break;
        case Adjustable.HORIZONTAL:
            c.move(-(value), c.getLocation().y);
            break;
        default:
            throw new IllegalArgumentException("Illegal adjustable orientation");
        }
    }
    private ScrollPane scroller;
}

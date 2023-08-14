public class Canvas extends Component implements Accessible {
    private static final String base = "canvas";
    private static int nameCounter = 0;
     private static final long serialVersionUID = -2284879212465893870L;
    public Canvas() {
    }
    public Canvas(GraphicsConfiguration config) {
        this();
        setGraphicsConfiguration(config);
    }
    @Override
    void setGraphicsConfiguration(GraphicsConfiguration gc) {
        synchronized(getTreeLock()) {
            CanvasPeer peer = (CanvasPeer)getPeer();
            if (peer != null) {
                gc = peer.getAppropriateGraphicsConfiguration(gc);
            }
            super.setGraphicsConfiguration(gc);
        }
    }
    String constructComponentName() {
        synchronized (Canvas.class) {
            return base + nameCounter++;
        }
    }
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getToolkit().createCanvas(this);
            super.addNotify();
        }
    }
    public void paint(Graphics g) {
        g.clearRect(0, 0, width, height);
    }
    public void update(Graphics g) {
        g.clearRect(0, 0, width, height);
        paint(g);
    }
    boolean postsOldMouseEvents() {
        return true;
    }
    public void createBufferStrategy(int numBuffers) {
        super.createBufferStrategy(numBuffers);
    }
    public void createBufferStrategy(int numBuffers,
        BufferCapabilities caps) throws AWTException {
        super.createBufferStrategy(numBuffers, caps);
    }
    public BufferStrategy getBufferStrategy() {
        return super.getBufferStrategy();
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTCanvas();
        }
        return accessibleContext;
    }
    protected class AccessibleAWTCanvas extends AccessibleAWTComponent
    {
        private static final long serialVersionUID = -6325592262103146699L;
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.CANVAS;
        }
    } 
}

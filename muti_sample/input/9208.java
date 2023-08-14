public class XPanelPeer extends XCanvasPeer implements PanelPeer {
    XEmbeddingContainer embedder = null; 
    public void xembed(long window) {
        if (embedder != null) {
            embedder.add(window);
        }
    }
    XPanelPeer() {}
    XPanelPeer(XCreateWindowParams params) {
        super(params);
    }
    XPanelPeer(Component target) {
        super(target);
    }
    void postInit(XCreateWindowParams params) {
        super.postInit(params);
        if (embedder != null) {
            embedder.install(this);
        }
    }
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }
    public void paint(Graphics g) {
        super.paint(g);
         }
    public void print(Graphics g) {
        super.print(g);
        SunGraphicsCallback.PrintHeavyweightComponentsCallback.getInstance().
            runComponents(((Container)target).getComponents(), g,
                          SunGraphicsCallback.LIGHTWEIGHTS |
                          SunGraphicsCallback.HEAVYWEIGHTS);
    }
    public void setBackground(Color c) {
        Component comp;
        int i;
        Container cont = (Container) target;
        synchronized(target.getTreeLock()) {
            int n = cont.getComponentCount();
            for(i=0; i < n; i++) {
                comp = cont.getComponent(i);
                ComponentPeer peer = comp.getPeer();
                if (peer != null) {
                    Color color = comp.getBackground();
                    if (color == null || color.equals(c)) {
                        peer.setBackground(c);
                    }
                }
            }
        }
        super.setBackground(c);
    }
    public void setForeground(Color c) {
        setForegroundForHierarchy((Container) target, c);
    }
    private void setForegroundForHierarchy(Container cont, Color c) {
        synchronized(target.getTreeLock()) {
            int n = cont.getComponentCount();
            for(int i=0; i < n; i++) {
                Component comp = cont.getComponent(i);
                Color color = comp.getForeground();
                if (color == null || color.equals(c)) {
                    ComponentPeer cpeer = comp.getPeer();
                    if (cpeer != null) {
                        cpeer.setForeground(c);
                    }
                    if (cpeer instanceof LightweightPeer
                        && comp instanceof Container)
                    {
                        setForegroundForHierarchy((Container) comp, c);
                    }
                }
            }
        }
    }
    public Insets insets() {
        return getInsets();
    }
    public void dispose() {
        if (embedder != null) {
            embedder.deinstall();
        }
        super.dispose();
    }
    protected boolean shouldFocusOnClick() {
        return ((Container)target).getComponentCount() == 0;
    }
}

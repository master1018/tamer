public class ComponentState {
    private JComponent component;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private Image componentSnapshot;
    public ComponentState(JComponent component) {
        this.component = component;
        this.x = component.getX();
        this.y = component.getY();
        this.width = component.getWidth();
        this.height = component.getHeight();
        componentSnapshot = createSnapshot(component);
    }
    private Image createSnapshot(JComponent component) {
        GraphicsConfiguration gc = component.getGraphicsConfiguration();
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        Image snapshot = gc.createCompatibleImage(width, height, component.isOpaque() ? Transparency.OPAQUE : Transparency.TRANSLUCENT);
        Graphics2D gImg = (Graphics2D) snapshot.getGraphics();
        Composite defaultComposite = gImg.getComposite();
        gImg.setComposite(AlphaComposite.Src);
        gImg.setColor(new Color(0, 0, 0, 0));
        gImg.fillRect(0, 0, width, height);
        gImg.setComposite(defaultComposite);
        paintComponentSingleBuffered(component, gImg);
        gImg.dispose();
        return snapshot;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public JComponent getComponent() {
        return component;
    }
    public Image getSnapshot() {
        if (componentSnapshot == null) {
            componentSnapshot = createSnapshot(component);
        }
        return componentSnapshot;
    }
    private static final Integer ANCESTOR_USING_BUFFER = 1;
    private static Method JCOMPONENT_SET_FLAG_METHOD;
    static {
        Method[] methods = JComponent.class.getDeclaredMethods();
        for (Method method : methods) {
            if ("setFlag".equals(method.getName())) {
                JCOMPONENT_SET_FLAG_METHOD = method;
                JCOMPONENT_SET_FLAG_METHOD.setAccessible(true);
                break;
            }
        }
    }
    private static void setSingleBuffered(JComponent component, boolean singleBuffered) {
        try {
            JCOMPONENT_SET_FLAG_METHOD.invoke(component, ANCESTOR_USING_BUFFER, singleBuffered);
        } catch (Exception e) {
            System.err.println("error invoking: " + e);
            e.printStackTrace();
        }
    }
    public static void paintComponentSingleBuffered(JComponent component, Graphics g) {
        setSingleBuffered(component, true);
        component.paint(g);
        setSingleBuffered(component, false);
    }
    public static void paintComponentHierarchySingleBuffered(JComponent component, Graphics g) {
        int x = 0, y = 0;
        int w = component.getWidth();
        int h = component.getHeight();
        JComponent topmost = component;
        JComponent prevTopmost = component;
        while (!topmost.isOpaque() && topmost.getParent() != null && topmost.getParent() instanceof JComponent) {
            topmost = (JComponent) topmost.getParent();
            x += prevTopmost.getX();
            y += prevTopmost.getY();
            prevTopmost = topmost;
        }
        setSingleBuffered(topmost, true);
        g.setClip(0, 0, w, h);
        g.translate(-x, -y);
        topmost.paint(g);
        setSingleBuffered(topmost, false);
    }
}

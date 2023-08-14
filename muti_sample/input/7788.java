public class NullComponentPeer implements LightweightPeer,
    CanvasPeer, PanelPeer {
    public boolean isObscured() {
        return false;
    }
    public boolean canDetermineObscurity() {
        return false;
    }
    public boolean isFocusable() {
        return false;
    }
    public void setVisible(boolean b) {
    }
    public void show() {
    }
    public void hide() {
    }
    public void setEnabled(boolean b) {
    }
    public void enable() {
    }
    public void disable() {
    }
    public void paint(Graphics g) {
    }
    public void repaint(long tm, int x, int y, int width, int height) {
    }
    public void print(Graphics g) {
    }
    public void setBounds(int x, int y, int width, int height, int op) {
    }
    public void reshape(int x, int y, int width, int height) {
    }
    public void coalescePaintEvent(PaintEvent e) {
    }
    public boolean handleEvent(Event e) {
        return false;
    }
    public void handleEvent(java.awt.AWTEvent arg0) {
    }
    public Dimension getPreferredSize() {
        return new Dimension(1,1);
    }
    public Dimension getMinimumSize() {
        return new Dimension(1,1);
    }
    public java.awt.Toolkit getToolkit() {
        return null;
    }
    public ColorModel getColorModel() {
        return null;
    }
    public Graphics getGraphics() {
        return null;
    }
    public GraphicsConfiguration getGraphicsConfiguration() {
        return null;
    }
    public FontMetrics  getFontMetrics(Font font) {
        return null;
    }
    public void dispose() {
    }
    public void setForeground(Color c) {
    }
    public void setBackground(Color c) {
    }
    public void setFont(Font f) {
    }
    public void updateCursorImmediately() {
    }
    public void setCursor(Cursor cursor) {
    }
    public boolean requestFocus
        (Component lightweightChild, boolean temporary,
         boolean focusedWindowChangeAllowed, long time, CausedFocusEvent.Cause cause) {
        return false;
    }
    public Image createImage(ImageProducer producer) {
        return null;
    }
    public Image createImage(int width, int height) {
        return null;
    }
    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        return false;
    }
    public int  checkImage(Image img, int w, int h, ImageObserver o) {
        return 0;
    }
    public Dimension preferredSize() {
        return getPreferredSize();
    }
    public Dimension minimumSize() {
        return getMinimumSize();
    }
    public Point getLocationOnScreen() {
        return new Point(0,0);
    }
    public Insets getInsets() {
        return insets();
    }
    public void beginValidate() {
    }
    public void endValidate() {
    }
    public Insets insets() {
        return new Insets(0, 0, 0, 0);
    }
    public boolean isPaintPending() {
        return false;
    }
    public boolean handlesWheelScrolling() {
        return false;
    }
    public VolatileImage createVolatileImage(int width, int height) {
        return null;
    }
    public void beginLayout() {
    }
    public void endLayout() {
    }
    public void createBuffers(int numBuffers, BufferCapabilities caps)
        throws AWTException {
        throw new AWTException(
            "Page-flipping is not allowed on a lightweight component");
    }
    public Image getBackBuffer() {
        throw new IllegalStateException(
            "Page-flipping is not allowed on a lightweight component");
    }
    public void flip(int x1, int y1, int x2, int y2,
                     BufferCapabilities.FlipContents flipAction)
    {
        throw new IllegalStateException(
            "Page-flipping is not allowed on a lightweight component");
    }
    public void destroyBuffers() {
    }
    public boolean isReparentSupported() {
        return false;
    }
    public void reparent(ContainerPeer newNativeParent) {
        throw new UnsupportedOperationException();
    }
    public void layout() {
    }
    public Rectangle getBounds() {
        return new Rectangle(0, 0, 0, 0);
    }
    public void applyShape(Region shape) {
    }
    public void setZOrder(ComponentPeer above) {
    }
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        return false;
    }
    public GraphicsConfiguration getAppropriateGraphicsConfiguration(
                        GraphicsConfiguration gc)
    {
        return gc;
    }
}

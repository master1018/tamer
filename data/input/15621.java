public class StubPeerCrash {
    public static int ITERATIONS = 20;
    public static void main(String []s)
    {
        for (int i = 0; i < ITERATIONS; i++){
            showFrame(i);
        }
    }
    private static void showFrame(int i){
        System.out.println("iteration = "+i);
        Frame f = new Frame();
        f.add(new AHeavyweightComponent());
        f.setVisible(true);
        f.setVisible(false);
    }
}
class AHeavyweightComponent extends Component {
    private ComponentPeer peer = new StubComponentPeer();
    public AHeavyweightComponent(){
    }
    public boolean isLightweight() {
        return false;
    }
    public ComponentPeer getPeer(){
        return peer;
    }
}
class StubComponentPeer implements ComponentPeer {
    public boolean isObscured(){return true;};
    public boolean canDetermineObscurity(){return true;};
    public void                setVisible(boolean b){};
    public void                setEnabled(boolean b){};
    public void                paint(Graphics g){};
    public void                repaint(long tm, int x, int y, int width, int height){};
    public void                print(Graphics g){};
    public void                setBounds(int x, int y, int width, int height, int op){};
    public void                handleEvent(AWTEvent e){};
    public void                coalescePaintEvent(PaintEvent e){};
    public Point               getLocationOnScreen(){return null;};
    public Dimension           getPreferredSize(){return null;};
    public Dimension           getMinimumSize(){return null;};
    public ColorModel          getColorModel(){return null;};
    public Toolkit             getToolkit(){return null;};
    public Graphics            getGraphics(){return null;};
    public FontMetrics         getFontMetrics(Font font){return null;};
    public void                dispose(){};
    public void                setForeground(Color c){};
    public void                setBackground(Color c){};
    public void                setFont(Font f){};
    public void                updateCursorImmediately(){};
    public boolean             requestFocus(Component lightweightChild,
                                     boolean temporary,
                                     boolean focusedWindowChangeAllowed,
                                     long time, CausedFocusEvent.Cause cause){
        return true;
    };
    public boolean             isFocusable(){return true;};
    public Image               createImage(ImageProducer producer){return null;};
    public Image               createImage(int width, int height){return null;};
    public VolatileImage       createVolatileImage(int width, int height){return null;};
    public boolean             prepareImage(Image img, int w, int h, ImageObserver o){return true;};
    public int                 checkImage(Image img, int w, int h, ImageObserver o){return 0;};
    public GraphicsConfiguration getGraphicsConfiguration(){return null;};
    public boolean     handlesWheelScrolling(){return true;};
    public void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException{};
    public Image getBackBuffer(){return null;};
    public void flip(int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction){};
    public void destroyBuffers(){};
    public void reparent(ContainerPeer newContainer){};
    public boolean isReparentSupported(){return true;};
    public void        layout(){};
     public    Rectangle getBounds(){return null;};
    public void applyShape(Region shape){};
    public Dimension           preferredSize(){return null;};
    public Dimension           minimumSize(){return null;};
    public void                show(){};
    public void                hide(){};
    public void                enable(){};
    public void                disable(){};
    public void                reshape(int x, int y, int width, int height){};
}

public class RenderingToCachedGraphicsTest extends Frame {
    private static volatile boolean failed = false;
    private static volatile CountDownLatch latch;
    private Graphics cachedGraphics;
    private Canvas renderCanvas;
    public RenderingToCachedGraphicsTest() {
        super("Test starts in 2 seconds");
        renderCanvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                if (getWidth() < 100 || getHeight() < 100) {
                    repaint();
                    return;
                }
                try { Thread.sleep(2000); } catch (InterruptedException ex) {}
                try {
                    runTest();
                } catch (Throwable t) {
                    failed = true;
                } finally {
                    latch.countDown();
                }
            }
            @Override
            public void update(Graphics g) {}
        };
        add("Center", renderCanvas);
    }
    private void runTest() {
        renderCanvas.createBufferStrategy(2);
        BufferStrategy bs = renderCanvas.getBufferStrategy();
        do {
            Graphics bsg = bs.getDrawGraphics();
            bsg.setColor(Color.blue);
            bsg.fillRect(0, 0,
                         renderCanvas.getWidth(), renderCanvas.getHeight());
        } while (bs.contentsLost() || bs.contentsRestored());
        cachedGraphics = renderCanvas.getGraphics();
        cachedGraphics.setColor(Color.red);
        cachedGraphics.fillRect(0, 0, getWidth(), getHeight());
        bs.dispose();
        bs = null;
        cachedGraphics.setColor(Color.green);
        cachedGraphics.fillRect(0, 0,
                                renderCanvas.getWidth(),
                                renderCanvas.getHeight());
        Toolkit.getDefaultToolkit().sync();
        cachedGraphics.fillRect(0, 0,
                                renderCanvas.getWidth(),
                                renderCanvas.getHeight());
        Toolkit.getDefaultToolkit().sync();
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
            failed = true;
            return;
        }
        Point p = renderCanvas.getLocationOnScreen();
        Rectangle r = new Rectangle(p.x, p.y,
                                    renderCanvas.getWidth(),
                                    renderCanvas.getHeight());
        BufferedImage bi = robot.createScreenCapture(r);
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                if (bi.getRGB(x, y) != Color.green.getRGB()) {
                    System.err.println("Colors mismatch!");
                    String name = "RenderingToCachedGraphicsTest.png";
                    try {
                        ImageIO.write(bi, "png", new File(name));
                        System.err.println("Dumped grabbed image to: "+name);
                    } catch (Exception e) {}
                    failed = true;
                    return;
                }
            }
        }
    }
    public static void main(String[] args) {
        int depth = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration().
                getColorModel().getPixelSize();
        if (depth < 16) {
            System.out.println("Test PASSED (depth < 16bit)");
            return;
        }
        latch = new CountDownLatch(1);
        RenderingToCachedGraphicsTest t1 = new RenderingToCachedGraphicsTest();
        t1.pack();
        t1.setSize(300, 300);
        t1.setVisible(true);
        try { latch.await(); } catch (InterruptedException ex) {}
        t1.dispose();
        if (failed) {
            throw new
                RuntimeException("Failed: rendering didn't show up");
        }
        System.out.println("Test PASSED");
    }
}

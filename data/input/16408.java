public class DrawImageBilinear extends Canvas {
    private static final int SIZE = 5;
    private static boolean done;
    private BufferedImage bimg1, bimg2;
    private VolatileImage vimg;
    private static volatile BufferedImage capture;
    private static void doCapture(Component test) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {}
        try {
            Robot robot = new Robot();
            Point pt1 = test.getLocationOnScreen();
            Rectangle rect =
                new Rectangle(pt1.x, pt1.y, test.getWidth(), test.getHeight());
            capture = robot.createScreenCapture(rect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void renderPattern(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(0, 0, SIZE, SIZE);
        g.dispose();
    }
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        if (bimg1 == null) {
            bimg1 = (BufferedImage)createImage(SIZE, SIZE);
            bimg1.setAccelerationPriority(0.0f);
            renderPattern(bimg1.createGraphics());
            bimg2 = (BufferedImage)createImage(SIZE, SIZE);
            renderPattern(bimg2.createGraphics());
            vimg = createVolatileImage(SIZE, SIZE);
            vimg.validate(getGraphicsConfiguration());
            renderPattern(vimg.createGraphics());
        }
        do {
            g2d.setColor(Color.white);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                 RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(bimg1, 10, 10, 40, 40, null);
            g2d.drawImage(bimg2, 80, 10, 40, 40, null);
            if (!skipOglTextureTest) {
                g2d.drawImage(bimg2, 80, 10, 40, 40, null);
            }
            if (vimg.validate(getGraphicsConfiguration()) != VolatileImage.IMAGE_OK) {
                renderPattern(vimg.createGraphics());
            }
            g2d.drawImage(vimg, 150, 10, 40, 40, null);
            Toolkit.getDefaultToolkit().sync();
        } while (vimg.contentsLost());
        synchronized (this) {
            if (!done) {
                doCapture(this);
                done = true;
            }
            notifyAll();
        }
    }
    public Dimension getPreferredSize() {
        return new Dimension(200, 100);
    }
    private static void testRegion(BufferedImage bi,
                                   Rectangle affectedRegion)
    {
        int x1 = affectedRegion.x;
        int y1 = affectedRegion.y;
        int x2 = x1 + affectedRegion.width;
        int y2 = y1 + affectedRegion.height;
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                int actual = bi.getRGB(x, y);
                if ((actual != 0xfffe0000) && (actual != 0xffff0000)) {
                    throw new RuntimeException("Test failed at x="+x+" y="+y+
                                               " (expected=0xffff0000"+
                                               " actual=0x"+
                                               Integer.toHexString(actual) +
                                               ")");
                }
            }
        }
    }
    private static boolean skipOglTextureTest = false;
    public static void main(String[] args) {
        boolean show = false;
        for (String arg : args) {
            if ("-show".equals(arg)) {
                show = true;
            }
        }
        String arch = System.getProperty("os.arch");
        boolean isOglEnabled = Boolean.getBoolean("sun.java2d.opengl");
        skipOglTextureTest = isOglEnabled && ("sparc".equals(arch));
        System.out.println("Skip OpenGL texture test: " + skipOglTextureTest);
        DrawImageBilinear test = new DrawImageBilinear();
        Frame frame = new Frame();
        frame.add(test);
        frame.pack();
        frame.setVisible(true);
        synchronized (test) {
            while (!done) {
                try {
                    test.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Failed: Interrupted");
                }
            }
        }
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        if (gc.getColorModel() instanceof IndexColorModel) {
            System.out.println("IndexColorModel detected: " +
                               "test considered PASSED");
            frame.dispose();
            return;
        }
        if (!show) {
            frame.dispose();
        }
        if (capture == null) {
            throw new RuntimeException("Failed: capture is null");
        }
        int pixel = capture.getRGB(5, 5);
        if (pixel != 0xffffffff) {
            throw new RuntimeException("Failed: Incorrect color for " +
                                       "background");
        }
        testRegion(capture, new Rectangle(10, 10, 40, 40));
        testRegion(capture, new Rectangle(80, 10, 40, 40));
        testRegion(capture, new Rectangle(150, 10, 40, 40));
    }
}

public class OnScreenRenderingResizeTest {
    private static volatile boolean done = false;
    private static volatile boolean nocheck = false;
    private static final int FRAME_W = 256;
    private static final int FRAME_H = 256;
    private static final int IMAGE_W = 128;
    private static final int IMAGE_H = 128;
    private static long RUN_TIME = 1000*20;
    private static final Color renderColor = Color.green;
    private static final Color bgColor = Color.white;
    public static void main(String[] args) {
        for (String arg : args) {
            if ("-inf".equals(arg)) {
                System.err.println("Test will run indefinitely");
                RUN_TIME = Long.MAX_VALUE;
            } else  if ("-nocheck".equals(arg)) {
                System.err.println("Test will not check rendering results");
                nocheck = true;
            } else {
                System.err.println("Usage: OnScreenRenderingResizeTest [-inf][-nocheck]");
            }
        }
        BufferedImage output =
            new BufferedImage(IMAGE_W, IMAGE_H, BufferedImage.TYPE_INT_RGB);
        output.setAccelerationPriority(0.0f);
        Graphics g = output.getGraphics();
        g.setColor(renderColor);
        g.fillRect(0, 0, output.getWidth(), output.getHeight());
        final Frame frame = new Frame("OnScreenRenderingResizeTest") {
            public void paint(Graphics g) {}
            public void update(Graphics g) {}
        };
        frame.setBackground(bgColor);
        frame.pack();
        frame.setSize(FRAME_W, FRAME_H);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                done = true;
            }
        });
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    frame.setVisible(true);
                }
            });
            Thread.sleep(2000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        int maxW = gc.getBounds().width /2;
        int maxH = gc.getBounds().height/2;
        int minW = frame.getWidth();
        int minH = frame.getHeight();
        int incW = 10, incH = 10, cnt = 0;
        Robot robot = null;
        if (!nocheck && gc.getColorModel().getPixelSize() > 8) {
            try {
                robot = new Robot();
            } catch (AWTException ex) {
                System.err.println("Robot creation failed, continuing.");
            }
        } else {
            System.err.println("No screen rendering checks.");
        }
        VolatileImage vi = gc.createCompatibleVolatileImage(512, 512);
        vi.validate(gc);
        long timeStarted = System.currentTimeMillis();
        while (!done && (System.currentTimeMillis() - timeStarted) < RUN_TIME) {
            if (++cnt > 100) {
                int w = frame.getWidth() + incW;
                int h = frame.getHeight() + incH;
                if (w < minW || w > maxW ) {
                    incW = -incW;
                }
                if (h < minH || h > maxH ) {
                    incH = -incH;
                }
                frame.setSize(w, h);
                cnt = 0;
            }
            vi.validate(gc);
            Graphics2D vig = (Graphics2D)vi.getGraphics();
            vig.rotate(30.0f, vi.getWidth()/2, vi.getHeight()/2);
            vig.drawImage(output, 0, 0,
                          vi.getWidth(), vi.getHeight(), null);
            Insets in = frame.getInsets();
            frame.getGraphics().drawImage(output, in.left, in.top, null);
            if (cnt == 90 && robot != null) {
                Point p = frame.getLocationOnScreen();
                p.translate(in.left+10, in.top+10);
                BufferedImage bi =
                    robot.createScreenCapture(
                        new Rectangle(p.x, p.y, IMAGE_W/2, IMAGE_H/2));
                int accepted1[] = { Color.white.getRGB(), Color.green.getRGB()};
                checkBI(bi, accepted1);
                p = frame.getLocationOnScreen();
                p.translate(in.left, in.top+IMAGE_H+5);
                bi = robot.createScreenCapture(
                    new Rectangle(p.x, p.y,
                                  frame.getWidth()-in.left-in.right,
                                  frame.getHeight()-in.top-in.bottom-5-IMAGE_H));
                int accepted2[] = { Color.white.getRGB() };
                checkBI(bi, accepted1);
            }
            Thread.yield();
        }
        frame.dispose();
        System.out.println("Test Passed");
    }
    private static void checkBI(BufferedImage bi, int accepted[]) {
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int pix = bi.getRGB(x, y);
                boolean found = false;
                for (int acc : accepted) {
                    if (pix == acc) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    try {
                        String name = "OnScreenRenderingResizeTest.png";
                        ImageIO.write(bi, "png", new File(name));
                        System.out.println("Screen shot file: " + name);
                    } catch (IOException ex) {}
                    throw new
                        RuntimeException("Test failed at " + x + "-" + y +
                                         " rgb=0x" + Integer.toHexString(pix));
                }
            }
        }
    }
}

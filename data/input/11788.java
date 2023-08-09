public class OverriddenInsetsTest {
    public static final Insets INSETS1 = new Insets(25,25,0,0);
    public static final Insets INSETS2 = new Insets(100,100,0,0);
    static final CountDownLatch lock = new CountDownLatch(1);
    static boolean failed = false;
    public static void main(String[] args) {
        if (GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().
                    getColorModel().getPixelSize() < 16)
        {
            System.out.println("<16 bit mode detected, test passed");
        }
        final Frame f = new Frame("OverriddenInsetsTest");
        f.setSize(260,260);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                f.setVisible(false);
                System.exit(0);
            }
        });
        f.setBackground(Color.gray);
        Panel p1 = new Panel() {
            public Insets getInsets() {
                return INSETS1;
            }
        };
        p1.setLayout(null);
        p1.setSize(250, 250);
        Panel p = new Panel(){
            @Override
            public Insets getInsets() {
                return INSETS2;
            }
            public void paint(Graphics g) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {}
                g.setColor(Color.red);
                g.drawRect(0,0,getWidth()-1,getHeight()-1 );
                g.setColor(Color.blue);
                g.fillRect(0,0,getWidth()/2,getHeight()/2);
                Point p = getLocationOnScreen();
                try {
                    Robot r = new Robot();
                    BufferedImage bi =
                        r.createScreenCapture(new
                            Rectangle(p.x, p.y, getWidth()/2, getHeight()/2));
                    for (int y = 0; y < bi.getHeight(); y++) {
                        for (int x = 0; x < bi.getWidth(); x++) {
                            if (bi.getRGB(x, y) != Color.blue.getRGB()) {
                                failed = true;
                                System.err.printf("Test failed at %d %d c=%x\n",
                                                  x, y, bi.getRGB(x, y));
                                String name = "OverriddenInsetsTest_res.png";
                                try {
                                    ImageIO.write(bi, "png", new File(name));
                                    System.out.println("Dumped res to: "+name);
                                } catch (IOException e) {}
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    failed = true;
                } finally {
                    lock.countDown();
                }
            }
        };
        p.setSize(200, 200);
        p1.add(p);
        p.setLocation(50, 50);
        f.add(p1);
        f.setVisible(true);
        try {
            lock.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        if (args.length <= 0 || !"-show".equals(args[0])) {
            f.dispose();
        }
        if (failed) {
            throw new RuntimeException("Test FAILED.");
        }
        System.out.println("Test PASSED");
    }
}

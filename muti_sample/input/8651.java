public class AcceleratedScaleTest {
    private static final int IMAGE_SIZE = 200;
    private static VolatileImage destVI;
    private static void initVI(GraphicsConfiguration gc) {
        int res;
        if (destVI == null) {
            res = VolatileImage.IMAGE_INCOMPATIBLE;
        } else {
            res = destVI.validate(gc);
        }
        if (res == VolatileImage.IMAGE_INCOMPATIBLE) {
            if (destVI != null) destVI.flush();
            destVI = gc.createCompatibleVolatileImage(IMAGE_SIZE, IMAGE_SIZE);
            destVI.validate(gc);
            res = VolatileImage.IMAGE_RESTORED;
        }
        if (res == VolatileImage.IMAGE_RESTORED) {
            Graphics vig = destVI.getGraphics();
            vig.setColor(Color.red);
            vig.fillRect(0, 0, destVI.getWidth(), destVI.getHeight());
            vig.dispose();
        }
    }
    public static void main(String[] args) {
        Frame f = new Frame();
        f.pack();
        GraphicsConfiguration gc = f.getGraphicsConfiguration();
        if (gc.getColorModel().getPixelSize() < 16) {
            System.out.printf("Bit depth: %d . Test considered passed.",
                              gc.getColorModel().getPixelSize());
            f.dispose();
            return;
        }
        BufferedImage bi =
            new BufferedImage(IMAGE_SIZE/4, IMAGE_SIZE/4,
                              BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D)bi.getGraphics();
        g.setColor(Color.red);
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        BufferedImage snapshot;
        do {
            initVI(gc);
            g = (Graphics2D)destVI.getGraphics();
            for (int i = 0; i < 5; i++) {
                g.drawImage(bi, 0, 0, null);
            }
            g.setColor(Color.white);
            g.fillRect(0, 0, destVI.getWidth(), destVI.getHeight());
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                               RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(bi, 0, 0, destVI.getWidth(), destVI.getHeight(), null);
            g.fillRect(0, 0, destVI.getWidth(), destVI.getHeight());
            g.drawImage(bi, 0, 0, destVI.getWidth(), destVI.getHeight(), null);
            snapshot = destVI.getSnapshot();
        } while (destVI.contentsLost());
        f.dispose();
        int whitePixel = Color.white.getRGB();
        for (int y = 0; y < snapshot.getHeight(); y++) {
            for (int x = 0; x < snapshot.getWidth(); x++) {
                if (snapshot.getRGB(x, y) == whitePixel) {
                    System.out.printf("Found untouched pixel at %dx%d\n", x, y);
                    System.out.println("Dumping the dest. image to " +
                                       "AcceleratedScaleTest_dst.png");
                    try {
                        ImageIO.write(snapshot, "png",
                                      new File("AcceleratedScaleTest_dst.png"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    throw new RuntimeException("Test failed.");
                }
            }
        }
        System.out.println("Test Passed.");
    }
}

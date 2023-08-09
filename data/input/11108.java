public class MutableColorTest {
    static Image bmImage;
    static Image argbImage;
    static class EvilColor extends Color {
        Color colors[] = { Color.red, Color.green, Color.blue };
        int currentIndex = 0;
        EvilColor() {
            super(Color.red.getRGB());
        }
        @Override
        public int getRGB() {
            return colors[currentIndex].getRGB();
        }
        void nextColor() {
            currentIndex++;
        }
    }
    private static int testImage(Image im,
                                 boolean doClip, boolean doTx)
    {
        int w = im.getWidth(null);
        int h = im.getHeight(null);
        Graphics2D g = (Graphics2D)im.getGraphics();
        EvilColor evilColor = new EvilColor();
        g.setColor(evilColor);
        g.fillRect(0, 0, w, h);
        g.dispose();
        evilColor.nextColor();
        g = (Graphics2D)im.getGraphics();
        if (doTx) {
            g.rotate(Math.PI/2.0, w/2, h/2);
        }
        g.setColor(evilColor);
        g.fillRect(0, 0, w, h);
        if (doClip) {
            g.clip(new Ellipse2D.Float(0, 0, w, h));
        }
        g.fillRect(0, h/3, w, h/3);
        g.drawImage(bmImage, 0, 2*h/3, evilColor, null);
        g.drawImage(argbImage, 0, 2*h/3+h/3/2, evilColor, null);
        return evilColor.getRGB();
    }
    private static void testResult(final String desc,
                                   final BufferedImage snapshot,
                                   final int evilColor) {
        for (int y = 0; y < snapshot.getHeight(); y++) {
            for (int x = 0; x < snapshot.getWidth(); x++) {
                int snapRGB = snapshot.getRGB(x, y);
                if (!isSameColor(snapRGB, evilColor)) {
                    System.err.printf("Wrong RGB for %s at (%d,%d): 0x%x " +
                        "instead of 0x%x\n", desc, x, y, snapRGB, evilColor);
                    String fileName = "MutableColorTest_"+desc+".png";
                    try {
                        ImageIO.write(snapshot, "png", new File(fileName));
                        System.err.println("Dumped snapshot to "+fileName);
                    } catch (IOException ex) {}
                    throw new RuntimeException("Test FAILED.");
                }
            }
        }
    }
    public static void main(String[] args) {
        GraphicsConfiguration gc =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        bmImage = gc.createCompatibleImage(64, 64, Transparency.BITMASK);
        argbImage = gc.createCompatibleImage(64, 64, Transparency.TRANSLUCENT);
        if (gc.getColorModel().getPixelSize() > 8) {
            VolatileImage vi =
                gc.createCompatibleVolatileImage(64, 64, Transparency.OPAQUE);
            do {
                if (vi.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
                    vi = gc.createCompatibleVolatileImage(64, 64,
                                                          Transparency.OPAQUE);
                    vi.validate(gc);
                }
                int color = testImage(vi, false, false);
                testResult("vi_noclip_notx", vi.getSnapshot(), color);
                color = testImage(vi, true, true);
                testResult("vi_clip_tx", vi.getSnapshot(), color);
                color = testImage(vi, true, false);
                testResult("vi_clip_notx", vi.getSnapshot(), color);
                color = testImage(vi, false, true);
                testResult("vi_noclip_tx", vi.getSnapshot(), color);
            } while (vi.contentsLost());
        }
        BufferedImage bi = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        int color = testImage(bi, false, false);
        testResult("bi_noclip_notx", bi, color);
        color = testImage(bi, true, true);
        testResult("bi_clip_tx", bi, color);
        color = testImage(bi, true, false);
        testResult("bi_clip_notx", bi, color);
        color = testImage(bi, false, true);
        testResult("bi_noclip_tx", bi, color);
        System.err.println("Test passed.");
    }
     private static boolean isSameColor(int color1, int color2) {
        final int tolerance = 2;
        for (int i = 0; i < 32; i += 8) {
            int c1 = 0xff & (color1 >> i);
            int c2 = 0xff & (color2 >> i);
            if (Math.abs(c1 - c2) > tolerance) {
                return false;
            }
        }
        return true;
    }
}

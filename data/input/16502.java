public class MlibOpsTest {
    public static void main(String[] args) {
        System.out.println("AffineTransformOp:");
        BufferedImageOp op = getAffineTransformOp();
        doTest(op);
        System.out.println("ConvolveOp:");
        op = getConvolveOp();
        doTest(op);
        System.out.println("LookupOp:");
        op = getLookupOp();
        doTest(op);
    }
    public static void doTest(BufferedImageOp op) {
        BufferedImage src = createSrcImage();
        BufferedImage dst = createImage();
        BufferedImage ret = null;
        try {
            ret = ImagingLib.filter(op, src, dst);
        } catch (Exception e) {
            throw new RuntimeException("Test FAILED.", e);
        }
        if (ret == null) {
            throw new RuntimeException("Test FAILED: null output");
        }
        System.out.println("ret: " + ret);
        System.out.println("Test PASSED for " + op.getClass().getName());
    }
    private static BufferedImageOp getAffineTransformOp() {
        AffineTransform at = new AffineTransform();
       return new AffineTransformOp(at,
                                    AffineTransformOp.TYPE_BICUBIC);
    }
    private static BufferedImageOp getConvolveOp() {
        int kw = 3;
        int kh = 3;
        int size = kw * kh;
        float[] kdata = new float[size];
        Arrays.fill(kdata, 1.0f / size);
        Kernel k  = new Kernel(kw, kh, kdata);
        return new ConvolveOp(k);
    }
    private static BufferedImageOp getLookupOp() {
        byte[] inv = new byte[256];
        for (int i = 0; i < 256; i++) {
            inv[i] = (byte)(255 - i);
        }
        ByteLookupTable table = new ByteLookupTable(0, inv);
        return new LookupOp(table, null);
    }
    private static int w = 100;
    private static int h = 100;
    private static BufferedImage createImage() {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    }
    private static BufferedImage createSrcImage() {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        Color[] colors = { Color.red, Color.green, Color.blue };
        float[] dist = {0.0f, 0.5f, 1.0f };
        Point2D center = new Point2D.Float(0.5f * w, 0.5f * h);
        RadialGradientPaint p =
                new RadialGradientPaint(center, 0.5f * w, dist, colors);
        g.setPaint(p);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }
}

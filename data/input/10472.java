public class OpCompatibleImageTest {
    public static void main(String[] args) {
        OpCompatibleImageTest t = new OpCompatibleImageTest();
        t.doTest(BufferedImage.TYPE_3BYTE_BGR);
        t.doTest(BufferedImage.TYPE_4BYTE_ABGR);
        t.doTest(BufferedImage.TYPE_BYTE_GRAY);
        t.doTest(BufferedImage.TYPE_INT_ARGB);
        t.doTest(BufferedImage.TYPE_INT_BGR);
        t.doTest(BufferedImage.TYPE_INT_RGB);
        t.doTest(BufferedImage.TYPE_BYTE_INDEXED);
    }
    private BufferedImageOp op;
    public OpCompatibleImageTest() {
        final Kernel kernel = new Kernel(3, 3,
                new float[] {
            1f/9f, 1f/9f, 1f/9f,
            1f/9f, 1f/9f, 1f/9f,
            1f/9f, 1f/9f, 1f/9f});
        op = new ConvolveOp(kernel);
    }
    public void doTest(int type) {
        System.out.println("Test for type " + describeType(type));
        BufferedImage src = createTestImage(type);
        BufferedImage res = null;
        System.out.println("Testing null destination...");
        try {
            res = op.filter(src, null);
        } catch (ImagingOpException e) {
            throw new RuntimeException("Test FAILED!", e);
        }
        if (res == null ||
            ((src.getType() != BufferedImage.TYPE_BYTE_INDEXED) &&
             (res.getType() != src.getType())))
        {
            throw new RuntimeException("Test FAILED!");
        }
        System.out.println("Test PASSED.");
    }
    private BufferedImage createCompatible(ColorModel cm, int w, int h) {
        return new BufferedImage (cm,
                                  cm.createCompatibleWritableRaster(w, h),
                                  cm.isAlphaPremultiplied(), null);
    }
    private BufferedImage createTestImage(int type) {
        BufferedImage img = new BufferedImage(100, 100, type);
        Graphics g = img.createGraphics();
        g.setColor(Color.red);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        return img;
    }
    private static String describeType(int type) {
        switch(type) {
        case BufferedImage.TYPE_3BYTE_BGR:
            return "TYPE_3BYTE_BGR";
        case BufferedImage.TYPE_4BYTE_ABGR:
            return "TYPE_4BYTE_ABGR";
        case BufferedImage.TYPE_BYTE_GRAY:
            return "TYPE_BYTE_GRAY";
        case BufferedImage.TYPE_INT_ARGB:
            return "TYPE_INT_ARGB";
        case BufferedImage.TYPE_INT_BGR:
            return  "TYPE_INT_BGR";
        case BufferedImage.TYPE_INT_RGB:
            return "TYPE_INT_RGB";
        case BufferedImage.TYPE_BYTE_INDEXED:
            return "TYPE_BYTE_INDEXED";
        default:
            throw new RuntimeException("Test FAILED: unknown type " + type);
        }
    }
}

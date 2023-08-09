public class SingleArrayTest {
    public static void main(String[] args) {
        SingleArrayTest t = new SingleArrayTest();
        t.doTest(BufferedImage.TYPE_3BYTE_BGR);
        t.doTest(BufferedImage.TYPE_4BYTE_ABGR);
        t.doTest(BufferedImage.TYPE_INT_RGB);
        t.doTest(BufferedImage.TYPE_INT_ARGB);
        t.doTest(BufferedImage.TYPE_INT_BGR);
        t.doTest(BufferedImage.TYPE_BYTE_GRAY);
    }
    private LookupOp op;
    public SingleArrayTest() {
        byte[] array = new byte[256];
        for (int i = 0; i < 256; i++) {
            array[i] = (byte)i;
        }
        ByteLookupTable table = new ByteLookupTable(0, array);
        op = new LookupOp(table, null);
    }
    public void doTest(int bi_type) {
        System.out.println("Test for type: " + bi_type);
        BufferedImage src = new BufferedImage(2, 2, bi_type);
        BufferedImage dst = new BufferedImage(2, 2, bi_type);
        doTest(src.getData(), dst.getRaster());
        doTest(src, dst);
        System.out.println("Test passed.");
    }
    public void doTest(Raster src, WritableRaster dst) {
        System.out.println("Test for raster:" + src);
        try {
            dst = op.filter(src, dst);
        } catch (Exception e) {
            throw new RuntimeException("Test failed.", e);
        }
    }
    public void doTest(BufferedImage src, BufferedImage dst) {
        System.out.println("Test for image: " + src);
        try {
            dst = op.filter(src, dst);
        } catch (Exception e) {
            throw new RuntimeException("Test failed.", e);
        }
    }
}

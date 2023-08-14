public class EdgeNoOpCrash {
    private static final int w = 3000;
    private static final int h = 200;
    public static void main(String[] args) {
        crashTest();
    }
    private static void crashTest() {
        Raster src = createSrcRaster();
        WritableRaster dst = createDstRaster();
        ConvolveOp op = createConvolveOp(ConvolveOp.EDGE_NO_OP);
        try {
            op.filter(src, dst);
        } catch (ImagingOpException e) {
        }
        System.out.println("Test PASSED.");
    }
    private static Raster createSrcRaster() {
        WritableRaster r = Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT,
                w, h, 4, new Point(0, 0));
        return r;
    }
    private static WritableRaster createDstRaster() {
        WritableRaster r = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                w, h, 4, new Point(0, 0));
        return r;
    }
    private static ConvolveOp createConvolveOp(int edgeHint) {
        final int kw = 3;
        final int kh = 3;
        float[] kdata = new float[kw * kh];
        float v = 1f / kdata.length;
        Arrays.fill(kdata, v);
        Kernel k = new Kernel(kw, kh, kdata);
        ConvolveOp op = new ConvolveOp(k, edgeHint, null);
        return op;
    }
}

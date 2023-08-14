public class IncorrectSampleMaskTest {
    public static void main(String[] args) {
        int[] dataTypes = new int[] {
            DataBuffer.TYPE_BYTE,
            DataBuffer.TYPE_USHORT,
            DataBuffer.TYPE_INT };
        for (int type : dataTypes) {
            doTest(type);
        }
    }
    private static final int w = 100;
    private static final int h = 100;
    private static AffineTransform at =
        AffineTransform.getScaleInstance(0.5, 0.5);
    private static RasterOp op =
        new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    private static void doTest(int dataType) {
        int maxSize = DataBuffer.getDataTypeSize(dataType);
        System.out.println("Type size: " + maxSize);
        int theMask = (int)(1L << (maxSize + 2)) - 1;
        System.out.printf("theMask=%x\n", theMask);
        SinglePixelPackedSampleModel sm =
            new SinglePixelPackedSampleModel(dataType, w, h,
                                             new int[] { theMask });
        int[] sampleSize = sm.getSampleSize();
        for (int s : sampleSize) {
            if (s > maxSize) {
                throw new RuntimeException("Test failed: sample size is too big:" + s);
            }
        }
        System.out.println("Test medialib...");
        DataBuffer buf = createDataBuffer(dataType);
        WritableRaster wr = Raster.createWritableRaster(sm, buf, null);
        op.filter(wr, null);
        System.out.println("Test PASSED.");
    }
    private static DataBuffer createDataBuffer(int type) {
        switch (type) {
        case DataBuffer.TYPE_BYTE: {
            byte[] buf = new byte[w * h];
            return new DataBufferByte(buf, buf.length);
        }
        case DataBuffer.TYPE_USHORT: {
            short[] buf = new short[w * h];
            return new DataBufferUShort(buf, buf.length);
        }
        case DataBuffer.TYPE_INT: {
            int[] buf = new int[w * h];
            return new DataBufferInt(buf, buf.length);
        }
        default :
            throw new RuntimeException("Unsupported data type.");
        }
    }
}

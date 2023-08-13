public class GetSamplesTest {
    public static int width = 100;
    public static int height = 100;
    public static int dataType = DataBuffer.TYPE_BYTE;
    public static int numBands = 4;
    public static void main(String[] args) {
        Vector<Class<? extends SampleModel>> classes = new Vector<Class<? extends SampleModel>>();
        classes.add(ComponentSampleModel.class);
        classes.add(MultiPixelPackedSampleModel.class);
        classes.add(SinglePixelPackedSampleModel.class);
        classes.add(BandedSampleModel.class);
        classes.add(PixelInterleavedSampleModel.class);
        for (Class<? extends SampleModel> c : classes) {
            doTest(c);
        }
    }
    private static void doTest(Class<? extends SampleModel> c) {
        System.out.println("Test for: " + c.getName());
        SampleModel sm = createSampleModel(c);
        DataBuffer db = sm.createDataBuffer();
        int[] iArray = new int[ width * height + numBands];
        float[] fArray = new float[ width * height + numBands];
        double[] dArray = new double[ width * height + numBands];
        boolean iOk = false;
        boolean fOk = false;
        boolean dOk = false;
        try {
            sm.getSamples(Integer.MAX_VALUE, 0, 1, 1, 0, iArray, db);
            sm.setSamples(Integer.MAX_VALUE, 0, 1, 1, 0, iArray, db);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            iOk = true;
        }
        try {
            sm.getSamples(Integer.MAX_VALUE, 0, 1, 1, 0, fArray, db);
            sm.setSamples(Integer.MAX_VALUE, 0, 1, 1, 0, fArray, db);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            fOk = true;
        }
        try {
            sm.getSamples(0, Integer.MAX_VALUE, 1, 1, 0, dArray, db);
            sm.setSamples(0, Integer.MAX_VALUE, 1, 1, 0, dArray, db);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            dOk = true;
        }
        if (!iOk || !fOk || !dOk) {
            throw new RuntimeException("Test for " + c.getSimpleName() +
                    " failed: iOk=" + iOk + "; fOk=" + fOk + "; dOk=" + dOk);
        }
    }
    private static SampleModel createSampleModel(Class<? extends SampleModel> cls) {
        SampleModel res = null;
        if (cls == ComponentSampleModel.class) {
            res = new ComponentSampleModel(dataType, width, height, 4, width * 4, new int[] { 0, 1, 2, 3 } );
        } else if (cls == MultiPixelPackedSampleModel.class) {
            res = new MultiPixelPackedSampleModel(dataType, width, height, 4);
        } else if (cls == SinglePixelPackedSampleModel.class) {
            res = new SinglePixelPackedSampleModel(dataType, width, height,
                    new int[]{ 0xff000000, 0x00ff0000, 0x0000ff00, 0x000000ff });
        } else if (cls == BandedSampleModel.class) {
            res = new BandedSampleModel(dataType, width, height, numBands);
        } else if (cls == PixelInterleavedSampleModel.class) {
            res = new PixelInterleavedSampleModel(dataType, width, height, 4, width * 4, new int[] { 0, 1, 2, 3 });
        } else {
            throw new RuntimeException("Unknown class " + cls);
        }
        return res;
    }
}

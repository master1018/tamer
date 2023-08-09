public class SmileyParserUnitTests extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SmileyParser.init(getContext());
    }
    public void testSmileyParser() {
        SmileyParser parser = SmileyParser.getInstance();
        SpannableStringBuilder buf = new SpannableStringBuilder();
        buf.append(parser.addSmileySpans(":-):-:-("));
        ImageSpan[] spans = buf.getSpans(0, buf.length(), ImageSpan.class);
        assertTrue("Smiley (happy) bitmaps aren't equal",
                compareImageSpans(new ImageSpan(mContext,
                        Smileys.getSmileyResource(Smileys.HAPPY)), spans[0]));
        assertTrue("Smiley (sad) bitmaps aren't equal",
                compareImageSpans(new ImageSpan(mContext,
                        Smileys.getSmileyResource(Smileys.SAD)), spans[1]));
    }
    private boolean compareImageSpans(ImageSpan span1, ImageSpan span2) {
        BitmapDrawable bitmapDrawable1 = (BitmapDrawable)span1.getDrawable();
        BitmapDrawable bitmapDrawable2 = (BitmapDrawable)span2.getDrawable();
        Bitmap bitmap1 = bitmapDrawable1.getBitmap();
        Bitmap bitmap2 = bitmapDrawable2.getBitmap();
        int rowBytes1 = bitmap1.getRowBytes();
        int rowBytes2 = bitmap2.getRowBytes();
        if (rowBytes1 != rowBytes2) {
            return false;
        }
        int height1 = bitmap1.getHeight();
        int height2 = bitmap2.getHeight();
        if (height1 != height2) {
            return false;
        }
        int size = height1 * rowBytes1;
        int[] intArray1 = new int[size];
        int[] intArray2 = new int[size];
        IntBuffer intBuf1 = IntBuffer.wrap(intArray1);
        IntBuffer intBuf2 = IntBuffer.wrap(intArray2);
        bitmap1.copyPixelsToBuffer(intBuf1);
        bitmap2.copyPixelsToBuffer(intBuf2);
        for (int i = 0; i < size; i++) {
            if (intArray1[i] != intArray2[i]) {
                return false;
            }
        }
        return true;
    }
}

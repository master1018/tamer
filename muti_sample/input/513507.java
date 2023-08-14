public class WidgetTestUtils {
    public static void assertEquals(Bitmap b1, Bitmap b2) {
        if (b1 == b2) {
            return;
        }
        if (b1 == null || b2 == null) {
            Assert.fail("the bitmaps are not equal");
        }
        if (b1.getWidth() != b2.getWidth() || b1.getHeight() != b2.getHeight()) {
            Assert.fail("the bitmaps are not equal");
        }
        int w = b1.getWidth();
        int h = b1.getHeight();
        int s = w * h;
        int[] pixels1 = new int[s];
        int[] pixels2 = new int[s];
        b1.getPixels(pixels1, 0, w, 0, 0, w, h);
        b2.getPixels(pixels2, 0, w, 0, 0, w, h);
        for (int i = 0; i < s; i++) {
            if (pixels1[i] != pixels2[i]) {
                Assert.fail("the bitmaps are not equal");
            }
        }
    }
    public static final void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException {
        Assert.assertNotNull(parser);
        Assert.assertNotNull(firstElementName);
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
            ;
        }
        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName()
                    + ", expected " + firstElementName);
        }
    }
    public static void assertScaledPixels(int expected, int actual, Context context) {
        Assert.assertEquals(expected * context.getResources().getDisplayMetrics().density,
                actual, 3);
    }
    public static Bitmap getUnscaledBitmap(Resources resources, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }
}

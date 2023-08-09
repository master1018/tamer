public class TypefaceTest extends TestCase {
    private final Typeface[] mFaces = new Typeface[] {
        Typeface.create(Typeface.SANS_SERIF, 0),
        Typeface.create(Typeface.SANS_SERIF, 1),
        Typeface.create(Typeface.SERIF, 0),
        Typeface.create(Typeface.SERIF, 1),
        Typeface.create(Typeface.SERIF, 2),
        Typeface.create(Typeface.SERIF, 3),
        Typeface.create(Typeface.MONOSPACE, 0)
    };
    @SmallTest
    public void testBasic() throws Exception {
        assertTrue("basic", Typeface.DEFAULT != null);
        assertTrue("basic", Typeface.DEFAULT_BOLD != null);
        assertTrue("basic", Typeface.SANS_SERIF != null);
        assertTrue("basic", Typeface.SERIF != null);
        assertTrue("basic", Typeface.MONOSPACE != null);
    }
    @SmallTest
    public void testUnique() throws Exception {
        final int n = mFaces.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                assertTrue("unique", mFaces[i] != mFaces[j]);
            }
        }
    }
    @SmallTest
    public void testStyles() throws Exception {
        assertTrue("style", mFaces[0].getStyle() == Typeface.NORMAL);
        assertTrue("style", mFaces[1].getStyle() == Typeface.BOLD);
        assertTrue("style", mFaces[2].getStyle() == Typeface.NORMAL);
        assertTrue("style", mFaces[3].getStyle() == Typeface.BOLD);
        assertTrue("style", mFaces[4].getStyle() == Typeface.ITALIC);
        assertTrue("style", mFaces[5].getStyle() == Typeface.BOLD_ITALIC);
        assertTrue("style", mFaces[6].getStyle() == Typeface.NORMAL);
    }
    @MediumTest
    public void testUniformY() throws Exception {
        Paint p = new Paint();
        final int n = mFaces.length;
        for (int i = 1; i <= 36; i++) {
            p.setTextSize(i);
            float ascent = 0;
            float descent = 0;
            for (int j = 0; j < n; j++) {
                p.setTypeface(mFaces[j]);
                Paint.FontMetrics fm = p.getFontMetrics();
                if (j == 0) {
                    ascent = fm.ascent;
                    descent = fm.descent;
                } else {
                    assertTrue("fontMetrics", fm.ascent == ascent);
                    assertTrue("fontMetrics", fm.descent == descent);
                }
            }
        }
    }
}

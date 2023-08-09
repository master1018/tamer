@TestTargetClass(DynamicLayout.class)
public class DynamicLayoutTest extends AndroidTestCase {
    protected static final float SPACING_MULT_NO_SCALE = 1.0f;
    protected static final float SPACING_ADD_NO_SCALE = 0.0f;
    protected static final int DEFAULT_OUTER_WIDTH = 150;
    protected static final CharSequence SINGLELINE_CHAR_SEQUENCE = "......";
    protected static final String[] TEXT = {"CharSequence\n", "Char\tSequence\n", "CharSequence"};
    protected static final CharSequence MULTLINE_CHAR_SEQUENCE = TEXT[0] + TEXT[1] + TEXT[2];
    protected static final Layout.Alignment DEFAULT_ALIGN = Layout.Alignment.ALIGN_CENTER;
    protected TextPaint mDefaultPaint;
    private static final int LINE0 = 0;
    private static final int LINE0_TOP = 0;
    private static final int LINE1 = 1;
    private static final int LINE2 = 2;
    private static final int LINE3 = 3;
    private static final int ELLIPSIS_UNDEFINED = 0x80000000;
    private DynamicLayout mDynamicLayout;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDefaultPaint = new TextPaint();
        mDynamicLayout = new DynamicLayout(MULTLINE_CHAR_SEQUENCE,
                mDefaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                true);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DynamicLayout",
            args = {java.lang.CharSequence.class, java.lang.CharSequence.class,
                    android.text.TextPaint.class, int.class, android.text.Layout.Alignment.class,
                    float.class, float.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DynamicLayout",
            args = {java.lang.CharSequence.class, java.lang.CharSequence.class,
                    android.text.TextPaint.class, int.class, android.text.Layout.Alignment.class,
                    float.class, float.class, boolean.class,
                    android.text.TextUtils.TruncateAt.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DynamicLayout",
            args = {java.lang.CharSequence.class, android.text.TextPaint.class,
                    int.class, android.text.Layout.Alignment.class,
                    float.class, float.class, boolean.class}
        )
    })
    public void testConstructors() {
        new DynamicLayout(SINGLELINE_CHAR_SEQUENCE,
                MULTLINE_CHAR_SEQUENCE,
                mDefaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                true);
        new DynamicLayout(SINGLELINE_CHAR_SEQUENCE,
                MULTLINE_CHAR_SEQUENCE,
                mDefaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                true,
                TextUtils.TruncateAt.START,
                DEFAULT_OUTER_WIDTH);
        new DynamicLayout(MULTLINE_CHAR_SEQUENCE,
                mDefaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                true);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getEllipsisCount",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getEllipsisStart",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getEllipsizedWidth",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "DynamicLayout javadoc is incomplete.")
    public void testEllipsis() {
        final DynamicLayout dynamicLayout = new DynamicLayout(SINGLELINE_CHAR_SEQUENCE,
                MULTLINE_CHAR_SEQUENCE,
                mDefaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                true,
                TextUtils.TruncateAt.START,
                DEFAULT_OUTER_WIDTH);
        assertEquals(0, dynamicLayout.getEllipsisCount(LINE1));
        assertEquals(ELLIPSIS_UNDEFINED, dynamicLayout.getEllipsisStart(LINE1));
        assertEquals(DEFAULT_OUTER_WIDTH, dynamicLayout.getEllipsizedWidth());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTopPadding",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBottomPadding",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "DynamicLayout javadoc is incomplete.")
    public void testIncludePadding() {
        final FontMetricsInt fontMetricsInt = mDefaultPaint.getFontMetricsInt();
        DynamicLayout dynamicLayout = new DynamicLayout(SINGLELINE_CHAR_SEQUENCE,
                mDefaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                true);
        assertEquals(fontMetricsInt.top - fontMetricsInt.ascent, dynamicLayout.getTopPadding());
        assertEquals(fontMetricsInt.bottom - fontMetricsInt.descent,
                dynamicLayout.getBottomPadding());
        dynamicLayout = new DynamicLayout(SINGLELINE_CHAR_SEQUENCE,
                mDefaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACING_MULT_NO_SCALE,
                SPACING_ADD_NO_SCALE,
                false);
        assertEquals(0, dynamicLayout.getTopPadding());
        assertEquals(0, dynamicLayout.getBottomPadding());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineContainsTab",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineDescent",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineTop",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineDirections",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParagraphDirection",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineStart",
            args = {int.class}
        )
    })
    public void testLineLayout() {
        assertEquals(TEXT.length, mDynamicLayout.getLineCount());
        assertFalse(mDynamicLayout.getLineContainsTab(LINE0));
        assertTrue(mDynamicLayout.getLineContainsTab(LINE1));
        assertEquals(LINE0_TOP, mDynamicLayout.getLineTop(LINE0));
        assertEquals(mDynamicLayout.getLineBottom(LINE0), mDynamicLayout.getLineTop(LINE1));
        assertEquals(mDynamicLayout.getLineBottom(LINE1), mDynamicLayout.getLineTop(LINE2));
        assertEquals(mDynamicLayout.getLineBottom(LINE2), mDynamicLayout.getLineTop(LINE3));
        try {
            assertEquals(mDynamicLayout.getLineBottom(mDynamicLayout.getLineCount()),
                    mDynamicLayout.getLineTop(mDynamicLayout.getLineCount() + 1));
            fail("Test DynamicLayout fail, should throw IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
        }
        assertEquals(mDynamicLayout.getLineDescent(LINE0) - mDynamicLayout.getLineAscent(LINE0),
                mDynamicLayout.getLineBottom(LINE0));
        assertEquals(mDynamicLayout.getLineDescent(LINE1) - mDynamicLayout.getLineAscent(LINE1),
                mDynamicLayout.getLineBottom(LINE1) - mDynamicLayout.getLineBottom(LINE0));
        assertEquals(mDynamicLayout.getLineDescent(LINE2) - mDynamicLayout.getLineAscent(LINE2),
                mDynamicLayout.getLineBottom(LINE2) - mDynamicLayout.getLineBottom(LINE1));
        assertEquals(LINE0, mDynamicLayout.getLineForVertical(mDynamicLayout.getLineTop(LINE0)));
        assertNotNull(mDynamicLayout.getLineDirections(LINE0));
        assertEquals(Layout.DIR_LEFT_TO_RIGHT, mDynamicLayout.getParagraphDirection(LINE0));
        assertEquals(0, mDynamicLayout.getLineStart(LINE0));
        assertEquals(TEXT[0].length(), mDynamicLayout.getLineStart(LINE1));
        assertEquals(TEXT[0].length() + TEXT[1].length(), mDynamicLayout.getLineStart(LINE2));
    }
}

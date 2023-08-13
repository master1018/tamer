@TestTargetClass(android.graphics.drawable.ColorDrawable.class)
public class ColorDrawableTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Constructors",
            method = "ColorDrawable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Constructors",
            method = "ColorDrawable",
            args = {int.class}
        )
    })
    public void testConstructors() {
        new ColorDrawable();
        new ColorDrawable(0);
        new ColorDrawable(1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test draw(Canvas)",
        method = "draw",
        args = {android.graphics.Canvas.class}
    )
    @ToBeFixed(bug = "1400249", explanation = "It will be tested by functional test.")
    public void testDraw() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getAlpha() and setAlpha(int)",
            method = "getAlpha",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getAlpha() and setAlpha(int)",
            method = "setAlpha",
            args = {int.class}
        )
    })
    public void testAccessAlpha() {
        ColorDrawable colorDrawable = new ColorDrawable();
        assertEquals(0, colorDrawable.getAlpha());
        colorDrawable.setAlpha(128);
        assertEquals(0, colorDrawable.getAlpha());
        colorDrawable = new ColorDrawable(1 << 24);
        assertEquals(1, colorDrawable.getAlpha());
        colorDrawable.setAlpha(128);
        assertEquals(0, colorDrawable.getAlpha());
        colorDrawable.setAlpha(255);
        assertEquals(1, colorDrawable.getAlpha());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getChangingConfigurations()",
        method = "getChangingConfigurations",
        args = {}
    )
    public void testGetChangingConfigurations() {
        final ColorDrawable colorDrawable = new ColorDrawable();
        assertEquals(0, colorDrawable.getChangingConfigurations());
        colorDrawable.setChangingConfigurations(1);
        assertEquals(1, colorDrawable.getChangingConfigurations());
        colorDrawable.setChangingConfigurations(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, colorDrawable.getChangingConfigurations());
        colorDrawable.setChangingConfigurations(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, colorDrawable.getChangingConfigurations());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getConstantState()",
        method = "getConstantState",
        args = {}
    )
    public void testGetConstantState() {
        final ColorDrawable colorDrawable = new ColorDrawable();
        assertNotNull(colorDrawable.getConstantState());
        assertEquals(colorDrawable.getChangingConfigurations(),
                colorDrawable.getConstantState().getChangingConfigurations());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getOpacity()",
        method = "getOpacity",
        args = {}
    )
    public void testGetOpacity() {
        ColorDrawable colorDrawable = new ColorDrawable();
        assertEquals(PixelFormat.TRANSPARENT, colorDrawable.getOpacity());
        colorDrawable = new ColorDrawable(255 << 24);
        assertEquals(PixelFormat.OPAQUE, colorDrawable.getOpacity());
        colorDrawable = new ColorDrawable(1 << 24);
        assertEquals(PixelFormat.TRANSLUCENT, colorDrawable.getOpacity());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test inflate(Resources, XmlPullParser, AttributeSet)",
        method = "inflate",
        args = {android.content.res.Resources.class, org.xmlpull.v1.XmlPullParser.class, 
                android.util.AttributeSet.class}
    )
    public void testInflate() throws XmlPullParserException, IOException {
        int eventType = -1;
        final ColorDrawable colorDrawable = new ColorDrawable();
        final XmlPullParser parser = mContext.getResources().getXml(R.drawable.colordrawable_test);
        while (eventType != XmlResourceParser.START_TAG
                && eventType != XmlResourceParser.END_DOCUMENT) {
            try {
                eventType = parser.next();
            } catch (XmlPullParserException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
        if (eventType == XmlResourceParser.START_TAG) {
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            colorDrawable.inflate(mContext.getResources(), parser, attrs);
            assertEquals(2, colorDrawable.getAlpha());
        } else {
            fail("XML parser didn't find the start element of the specified xml file.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setColorFilter(ColorFilter)",
        method = "setColorFilter",
        args = {android.graphics.ColorFilter.class}
    )
    public void testSetColorFilter() {
        final ColorDrawable colorDrawable = new ColorDrawable();
        colorDrawable.setColorFilter(null);
    }
}

@TestTargetClass(android.graphics.drawable.PaintDrawable.class)
public class PaintDrawableTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PaintDrawable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PaintDrawable",
            args = {int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        new PaintDrawable();
        new PaintDrawable(0x0);
        new PaintDrawable(0xffffffff);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCornerRadius",
        args = {float.class}
    )
    public void testSetCornerRadius() {
        PaintDrawable paintDrawable;
        paintDrawable = getPaintDrawable(false);
        assertNull(paintDrawable.getShape());
        paintDrawable.setCornerRadius(1.5f);
        assertNotNull(paintDrawable.getShape());
        assertTrue(paintDrawable.getShape() instanceof RoundRectShape);
        paintDrawable = getPaintDrawable(true);
        assertNotNull(paintDrawable.getShape());
        paintDrawable.setCornerRadius(0);
        assertNull(paintDrawable.getShape());
        paintDrawable = getPaintDrawable(true);
        assertNotNull(paintDrawable.getShape());
        paintDrawable.setCornerRadius(-2.5f);
        assertNull(paintDrawable.getShape());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCornerRadii",
        args = {float[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testSetCornerRadii() {
        PaintDrawable paintDrawable;
        paintDrawable = getPaintDrawable(false);
        assertNull(paintDrawable.getShape());
        paintDrawable.setCornerRadii(null);
        assertNull(paintDrawable.getShape());
        paintDrawable = getPaintDrawable(true);
        assertNotNull(paintDrawable.getShape());
        paintDrawable.setCornerRadii(null);
        assertNull(paintDrawable.getShape());
        float[] radii = {
                4.5f, 6.0f, 4.5f, 6.0f, 4.5f, 6.0f, 4.5f, 6.0f
        };
        float[] fakeRadii = new float[7];
        try {
            paintDrawable.setCornerRadii(fakeRadii);
            fail("setCornerRadii should throw a ArrayIndexOutOfBoundsException if array is"
                    + " shorter than 8.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        assertNull(paintDrawable.getShape());
        paintDrawable.setCornerRadii(radii);
        assertNotNull(paintDrawable.getShape());
        assertTrue(paintDrawable.getShape() instanceof RoundRectShape);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflateTag",
        args = {java.lang.String.class, android.content.res.Resources.class,
                org.xmlpull.v1.XmlPullParser.class, android.util.AttributeSet.class}
    )
    public void testInflateTag() throws XmlPullParserException, IOException {
        XmlResourceParser parser = getParser();
        AttributeSet attr = getAtrributeSet(parser);
        assertNotNull(attr);
        gotoTag(parser, "padding");
        Rect padding = new Rect(0, 0, 10, 10);
        MyPaintDrawable paintDrawable = new MyPaintDrawable();
        paintDrawable.setPadding(padding);
        assertTrue(paintDrawable.getPadding(padding));
        assertTrue(paintDrawable.inflateTag("padding", getContext().getResources(), parser, attr));
        assertFalse(paintDrawable.getPadding(padding));
        parser = getParser();
        attr = getAtrributeSet(parser);
        assertNotNull(attr);
        assertFalse(new MyPaintDrawable().inflateTag("", getContext().getResources(), parser,
                attr));
        try {
            new MyPaintDrawable().inflateTag(null, getContext().getResources(), parser, attr);
            fail("Normally the function would throw a NullPointerException here.");
        } catch (NullPointerException e) {
        }
        try {
            gotoTag(parser, "padding");
            new MyPaintDrawable().inflateTag("padding", null, parser, attr);
            fail("Normally the function would throw a NullPointerException here.");
        } catch (NullPointerException e) {
        }
        parser = getParser();
        attr = getAtrributeSet(parser);
        assertNotNull(attr);
        gotoTag(parser, "padding");
        paintDrawable = new MyPaintDrawable();
        assertTrue(paintDrawable.inflateTag("padding", getContext().getResources(), null, attr));
        try {
            new MyPaintDrawable().inflateTag("padding", getContext().getResources(), parser, null);
            fail("Normally the function would throw a NullPointerException here.");
        } catch (NullPointerException e) {
        }
        assertNull(paintDrawable.getShape());
        parser = getParser();
        attr = getAtrributeSet(parser);
        assertNotNull(attr);
        gotoTag(parser, "corners");
        assertTrue(paintDrawable.inflateTag("corners", getContext().getResources(), parser, attr));
        assertNotNull(paintDrawable.getShape());
    }
    private XmlResourceParser getParser() {
        return getContext().getResources().getXml(R.drawable.paintdrawable_attr);
    }
    private AttributeSet getAtrributeSet(XmlResourceParser parser) throws XmlPullParserException,
            IOException {
        int type;
        type = parser.next();
        while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            type = parser.next();
        }
        if (type != XmlPullParser.START_TAG) {
            return null;
        }
        return Xml.asAttributeSet(parser);
    }
    private void gotoTag(XmlResourceParser parser, String tagName) throws XmlPullParserException,
            IOException {
        if (parser == null) {
            return;
        }
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            String name = parser.getName();
            if (name.equals(tagName)) {
                break;
            }
            parser.nextText();
        }
    }
    private static class MyPaintDrawable extends PaintDrawable {
        @Override
        protected boolean inflateTag(String name, Resources r, XmlPullParser parser,
                AttributeSet attrs) {
            return super.inflateTag(name, r, parser, attrs);
        }
    }
    private PaintDrawable getPaintDrawable(boolean hasShape) {
        PaintDrawable paintDrawable = new PaintDrawable();
        if (hasShape) {
            paintDrawable.setCornerRadius(1.5f);
        }
        return paintDrawable;
    }
}

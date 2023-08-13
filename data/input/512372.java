@TestTargetClass(ImageButton.class)
public class ImageButtonTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ImageButton",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ImageButton",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "ImageButton",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "javadoc does not declare the corner case " +
            "behavior when context is null")
    public void testConstructor() {
        XmlPullParser parser = getContext().getResources().getXml(R.layout.imagebutton_test);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        assertNotNull(attrs);
        new ImageButton(getContext());
        new ImageButton(getContext(), attrs);
        new ImageButton(getContext(), attrs, 0);
        try {
            new ImageButton(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new ImageButton(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new ImageButton(null, null, -1);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Test {@link ImageButton#onSetAlpha(int)}.",
            method = "onSetAlpha",
            args = {int.class}
        )
    })
    public void testOnSetAlpha() {
    }
}

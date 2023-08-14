@TestTargetClass(Button.class)
public class ButtonTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Button}",
            method = "Button",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Button}",
            method = "Button",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link Button}",
            method = "Button",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug="1417734", explanation="should add @throws clause into javadoc of " +
            "Button's constructors when the input AttributeSet or Context is null")
    public void testConstructor() {
        XmlPullParser parser = mContext.getResources().getXml(R.layout.togglebutton_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new Button(mContext, attrs, 0);
        new Button(mContext, attrs);
        new Button(mContext);
        try {
            new Button(null, null, -1);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new Button(null, null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new Button(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
}

@TestTargetClass(CheckBox.class)
public class CheckBoxTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "CheckBox",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "CheckBox",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "CheckBox",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug="1695243", explanation="should add @throws clause into javadoc of " +
            "CheckBox's constructors when the input AttributeSet or Context is null")
    public void testConstructor() {
        XmlPullParser parser = mContext.getResources().getXml(R.layout.checkbox_layout);
        AttributeSet mAttrSet = Xml.asAttributeSet(parser);
        new CheckBox(mContext, mAttrSet, 0);
        new CheckBox(mContext, mAttrSet);
        new CheckBox(mContext);
        try {
            new CheckBox(null, null, -1);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new CheckBox(null, null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new CheckBox(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
}

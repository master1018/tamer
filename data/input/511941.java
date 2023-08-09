@TestTargetClass(RadioButton.class)
public class RadioButtonTest extends InstrumentationTestCase {
    private Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "RadioButton",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "RadioButton",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "RadioButton",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "RadioButton#RadioButton(Context), "
            + "RadioButton#RadioButton(Context, AttributeSet) "
            + "and RadioButton#RadioButton(Context, AttributeSet, int) "
            + "when param Context is null")
    public void testConstructor() {
        AttributeSet attrs = mContext.getResources().getLayout(R.layout.radiogroup_1);
        assertNotNull(attrs);
        new RadioButton(mContext);
        try {
            new RadioButton(null);
            fail("The constructor should throw NullPointerException when param Context is null.");
        } catch (NullPointerException e) {
        }
        new RadioButton(mContext, attrs);
        try {
            new RadioButton(null, attrs);
            fail("The constructor should throw NullPointerException when param Context is null.");
        } catch (NullPointerException e) {
        }
        new RadioButton(mContext, null);
        new RadioButton(mContext, attrs, 0);
        try {
            new RadioButton(null, attrs, 0);
            fail("The constructor should throw NullPointerException when param Context is null.");
        } catch (NullPointerException e) {
        }
        new RadioButton(mContext, null, 0);
        new RadioButton(mContext, attrs, Integer.MAX_VALUE);
        new RadioButton(mContext, attrs, Integer.MIN_VALUE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link RadioButton#toggle()}",
        method = "toggle",
        args = {}
    )
    public void testToggle() {
        RadioButton button = new RadioButton(mContext);
        assertFalse(button.isChecked());
        button.toggle();
        assertTrue(button.isChecked());
        button.toggle();
        assertTrue(button.isChecked());
    }
}

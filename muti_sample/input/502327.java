@TestTargetClass(TextSwitcher.class)
public class TextSwitcherTest extends InstrumentationTestCase {
    private Context mContext;
    private static final int PARAMS_WIDTH = 200;
    private static final int PARAMS_HEIGHT = 300;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TextSwitcher",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TextSwitcher",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        new TextSwitcher(mContext);
        new TextSwitcher(mContext, null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setText",
        args = {java.lang.CharSequence.class}
    )
    public void testSetText() {
        final String viewText1 = "Text 1";
        final String viewText2 = "Text 2";
        final String changedText = "Changed";
        TextSwitcher textSwitcher = new TextSwitcher(mContext);
        TextView tv1 = new TextView(mContext);
        TextView tv2 = new TextView(mContext);
        tv1.setText(viewText1);
        tv2.setText(viewText2);
        textSwitcher.addView(tv1, 0, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
        textSwitcher.addView(tv2, 1, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
        TextView tvChild1 = (TextView) textSwitcher.getChildAt(0);
        TextView tvChild2 = (TextView) textSwitcher.getChildAt(1);
        assertEquals(viewText1, (tvChild1.getText().toString()));
        assertEquals(viewText2, (tvChild2.getText().toString()));
        assertSame(tv1, textSwitcher.getCurrentView());
        textSwitcher.setText(changedText);
        assertEquals(viewText1, (tvChild1.getText().toString()));
        assertEquals(changedText, (tvChild2.getText().toString()));
        assertSame(tv2, textSwitcher.getCurrentView());
        textSwitcher.setText(changedText);
        assertEquals(changedText, (tvChild1.getText().toString()));
        assertEquals(changedText, (tvChild2.getText().toString()));
        assertSame(tv1, textSwitcher.getCurrentView());
        textSwitcher.setText(null);
        assertEquals(changedText, (tvChild1.getText().toString()));
        assertEquals("", (tvChild2.getText().toString()));
        assertSame(tv2, textSwitcher.getCurrentView());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCurrentText",
        args = {java.lang.CharSequence.class}
    )
    public void testSetCurrentText() {
        final String viewText1 = "Text 1";
        final String viewText2 = "Text 2";
        final String changedText1 = "Changed 1";
        final String changedText2 = "Changed 2";
        TextSwitcher textSwitcher = new TextSwitcher(mContext);
        TextView tv1 = new TextView(mContext);
        TextView tv2 = new TextView(mContext);
        tv1.setText(viewText1);
        tv2.setText(viewText2);
        textSwitcher.addView(tv1, 0, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
        textSwitcher.addView(tv2, 1, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
        TextView tvChild1 = (TextView) textSwitcher.getChildAt(0);
        TextView tvChild2 = (TextView) textSwitcher.getChildAt(1);
        assertEquals(viewText1, (tvChild1.getText().toString()));
        assertEquals(viewText2, (tvChild2.getText().toString()));
        assertSame(tv1, textSwitcher.getCurrentView());
        textSwitcher.setCurrentText(changedText1);
        assertEquals(changedText1, (tvChild1.getText().toString()));
        assertEquals(viewText2, (tvChild2.getText().toString()));
        assertSame(tv1, textSwitcher.getCurrentView());
        textSwitcher.setCurrentText(changedText2);
        assertEquals(changedText2, (tvChild1.getText().toString()));
        assertEquals(viewText2, (tvChild2.getText().toString()));
        assertSame(tv1, textSwitcher.getCurrentView());
        textSwitcher.setCurrentText(null);
        assertEquals("", (tvChild1.getText().toString()));
        assertEquals(viewText2, (tvChild2.getText().toString()));
        assertSame(tv1, textSwitcher.getCurrentView());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addView",
        args = {android.view.View.class, int.class, android.view.ViewGroup.LayoutParams.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for addView() is incomplete." +
            "1. not clear what is supposed to happen if the LayoutParams is null.")
    public void testAddView() {
        TextSwitcher textSwitcher = new TextSwitcher(mContext);
        TextView tv1 = new TextView(mContext);
        TextView tv2 = new TextView(mContext);
        textSwitcher.addView(tv1, 0, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
        assertSame(tv1, textSwitcher.getChildAt(0));
        assertEquals(1, textSwitcher.getChildCount());
        try {
            textSwitcher.addView(tv1, 0, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        try {
            textSwitcher.addView(tv2, Integer.MAX_VALUE,
                    new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        textSwitcher.addView(tv2, 1,
                new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
        assertSame(tv2, textSwitcher.getChildAt(1));
        assertEquals(2, textSwitcher.getChildCount());
        TextView tv3 = new TextView(mContext);
        try {
            textSwitcher.addView(tv3, 2, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        textSwitcher = new TextSwitcher(mContext);
        ListView lv = new ListView(mContext);
        try {
            textSwitcher.addView(lv, 0, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            textSwitcher.addView(null, 0, new ViewGroup.LayoutParams(PARAMS_WIDTH, PARAMS_HEIGHT));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            textSwitcher.addView(tv3, 0, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
}

@TestTargetClass(CharacterPickerDialog.class)
public class CharacterPickerDialogTest extends
        ActivityInstrumentationTestCase2<StubActivity> {
    private Activity mActivity;
    public CharacterPickerDialogTest() {
        super("com.android.cts.stub", StubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "CharacterPickerDialog",
        args = {Context.class, View.class, Editable.class, String.class, boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add @throw in the javadoc.")
    public void testConstructor() {
        final CharSequence str = "123456";
        final Editable content = Editable.Factory.getInstance().newEditable(str);
        final View view = new TextView(mActivity);
        new CharacterPickerDialog(view.getContext(), view, content, "\u00A1", false);
        try {
            new CharacterPickerDialog(null, view, content, "\u00A1", false);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onCreate",
        args = {Bundle.class}
    )
    public void testOnCreate() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "only position is read in this method",
        method = "onItemClick",
        args = {AdapterView.class, View.class, int.class, long.class}
    )
    public void testOnItemClick() {
        final Gallery parent = new Gallery(mActivity);
        final CharSequence str = "123456";
        Editable text = Editable.Factory.getInstance().newEditable(str);
        final View view = new TextView(mActivity);
        CharacterPickerDialog replacePickerDialog =
                new CharacterPickerDialog(view.getContext(), view, text, "abc", false);
        replacePickerDialog.show();
        Selection.setSelection(text, 0, 0);
        assertEquals(str, text.toString());
        assertTrue(replacePickerDialog.isShowing());
        replacePickerDialog.onItemClick(parent, view, 0, 0);
        assertEquals("a123456", text.toString());
        assertFalse(replacePickerDialog.isShowing());
        replacePickerDialog.show();
        Selection.setSelection(text, 2, 2);
        assertTrue(replacePickerDialog.isShowing());
        replacePickerDialog.onItemClick(parent, view, 2, 0);
        assertEquals("ac23456", text.toString());
        assertFalse(replacePickerDialog.isShowing());
        text = Editable.Factory.getInstance().newEditable(str);
        CharacterPickerDialog insertPickerDialog =
            new CharacterPickerDialog(view.getContext(), view, text, "abc", true);
        Selection.setSelection(text, 2, 2);
        assertEquals(str, text.toString());
        insertPickerDialog.show();
        assertTrue(insertPickerDialog.isShowing());
        insertPickerDialog.onItemClick(parent, view, 2, 0);
        assertEquals("12c3456", text.toString());
        assertFalse(insertPickerDialog.isShowing());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onClick",
        args = {View.class}
    )
    public void testOnClick() {
        final CharSequence str = "123456";
        final Editable content = Editable.Factory.getInstance().newEditable(str);
        final View view = new TextView(mActivity);
        CharacterPickerDialog characterPickerDialog =
                new CharacterPickerDialog(view.getContext(), view, content, "\u00A1", false);
        characterPickerDialog.show();
        assertTrue(characterPickerDialog.isShowing());
        characterPickerDialog.onClick(view);
    }
}

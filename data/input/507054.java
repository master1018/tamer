@TestTargetClass(SingleLineTransformationMethod.class)
public class SingleLineTransformationMethodTest
        extends ActivityInstrumentationTestCase2<StubActivity> {
    public SingleLineTransformationMethodTest() {
        super("com.android.cts.stub", StubActivity.class);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SingleLineTransformationMethod",
        args = {}
    )
    public void testConstructor() {
        new SingleLineTransformationMethod();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        SingleLineTransformationMethod method0 = SingleLineTransformationMethod.getInstance();
        assertNotNull(method0);
        SingleLineTransformationMethod method1 = SingleLineTransformationMethod.getInstance();
        assertSame(method0, method1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getReplacement",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOriginal",
            args = {}
        )
    })
    public void testGetReplacement() {
        MySingleLineTranformationMethod method = new MySingleLineTranformationMethod();
        TextMethodUtils.assertEquals(new char[] { ' ', '\uFEFF' }, method.getReplacement());
        TextMethodUtils.assertEquals(new char[] { '\n', '\r' }, method.getOriginal());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "getTransformation",
        args = {CharSequence.class, View.class}
    )
    public void testGetTransformation() {
        SingleLineTransformationMethod method = SingleLineTransformationMethod.getInstance();
        CharSequence result = method.getTransformation("hello\nworld\r", null);
        assertEquals("hello world\uFEFF", result.toString());
        EditText editText = new EditText(getActivity());
        editText.setText("hello\nworld\r");
    }
    private static class MySingleLineTranformationMethod extends SingleLineTransformationMethod {
        @Override
        protected char[] getOriginal() {
            return super.getOriginal();
        }
        @Override
        protected char[] getReplacement() {
            return super.getReplacement();
        }
    }
}

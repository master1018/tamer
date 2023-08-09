@TestTargetClass(Editable.Factory.class)
public class Editable_FactoryTest extends AndroidTestCase {
    Factory mFactory;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test newEditable(CharSequence source)",
        method = "newEditable",
        args = {java.lang.CharSequence.class}
    )
    public void testNewEditable() {
        CharSequence source = "abc";
        Editable expected = new SpannableStringBuilder(source);
        mFactory = new Editable.Factory();
        Editable actual = mFactory.newEditable(source);
        assertEquals(expected.toString(), actual.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getInstance()",
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        mFactory = Factory.getInstance();
        assertTrue(mFactory instanceof Editable.Factory);
    }
}

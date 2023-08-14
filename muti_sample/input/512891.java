@TestTargetClass(FormattableFlags.class) 
public class FormattableFlagsTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies fields.",
        method = "!Constants",
        args = {}
    )
    public void test_ConstantFieldValues() {
        assertEquals(1, FormattableFlags.LEFT_JUSTIFY);
        assertEquals(2, FormattableFlags.UPPERCASE);
        assertEquals(4, FormattableFlags.ALTERNATE);
    }
}

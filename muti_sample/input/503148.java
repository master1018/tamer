@TestTargetClass(String.class) 
public class Tests extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check NullPointerException.",
        method = "contains",
        args = {java.lang.CharSequence.class}
    )
    public void test_contains() {
        assertTrue("aabc".contains("abc"));
        assertTrue("abcd".contains("abc"));
        assertFalse("abcd".contains("cba"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies positive functionality.",
        method = "charAt",
        args = {int.class}
    )
    public void test_charAt() {
        assertTrue("abcd".charAt(0) == 'a');
        assertTrue("abcd".charAt(3) == 'd');
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check specific cases.",
        method = "startsWith",
        args = {java.lang.String.class}
    )
    public void test_StartsWith() {
        assertTrue("abcd".startsWith("abc"));
        assertFalse("abcd".startsWith("aabc"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't check specific cases.",
        method = "endsWith",
        args = {java.lang.String.class}
    )
    public void test_EndsWith() {
        assertTrue("abcd".endsWith("bcd"));
        assertFalse("abcd".endsWith("bcde"));
    }
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Verifies nothing.",
        method = "!Constants",
        args = {}
    )
    public void test_CASE_INSENSITIVE_ORDER() {
        String  s1 = "ABCDEFG";
        String  s2 = "abcdefg";
        assertTrue(String.CASE_INSENSITIVE_ORDER.compare(s1, s2) == 0);
    }
}

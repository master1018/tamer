@TestTargetClass(RetentionPolicy.class)
public class RetentionPolicyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("nls")
    public void test_valueOfLjava_lang_String() throws Exception {
        assertSame(RetentionPolicy.CLASS, RetentionPolicy
                .valueOf("CLASS"));
        assertSame(RetentionPolicy.RUNTIME, RetentionPolicy
                .valueOf("RUNTIME"));
        assertSame(RetentionPolicy.SOURCE, RetentionPolicy
                .valueOf("SOURCE"));
        try {
            RetentionPolicy.valueOf("OTHER");
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    @SuppressWarnings("nls")
    public void test_values() throws Exception {
        RetentionPolicy[] values = RetentionPolicy.values();
        assertTrue(values.length > 1);
        Arrays.sort(values);
        assertTrue(Arrays.binarySearch(values, RetentionPolicy.RUNTIME) >= 0);
    }
}

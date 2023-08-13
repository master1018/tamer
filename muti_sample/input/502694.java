@TestTargetClass(ElementType.class)
public class ElementTypeTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("nls")
    public void test_valueOfLjava_lang_String() throws Exception {
        assertSame(ElementType.ANNOTATION_TYPE, ElementType
                .valueOf("ANNOTATION_TYPE"));
        assertSame(ElementType.CONSTRUCTOR, ElementType.valueOf("CONSTRUCTOR"));
        assertSame(ElementType.FIELD, ElementType.valueOf("FIELD"));
        assertSame(ElementType.LOCAL_VARIABLE, ElementType
                .valueOf("LOCAL_VARIABLE"));
        assertSame(ElementType.METHOD, ElementType.valueOf("METHOD"));
        assertSame(ElementType.PACKAGE, ElementType.valueOf("PACKAGE"));
        assertSame(ElementType.PARAMETER, ElementType.valueOf("PARAMETER"));
        assertSame(ElementType.TYPE, ElementType.valueOf("TYPE"));
        try {
            ElementType.valueOf("OTHER");
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
        ElementType[] values = ElementType.values();
        assertTrue(values.length > 1);
        Arrays.sort(values);
        assertTrue(Arrays.binarySearch(values, ElementType.METHOD) >= 0);
    }
}

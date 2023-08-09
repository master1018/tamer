@TestTargetClass(Attributes.Name.class)
public class AttributesNameTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Name",
        args = {java.lang.String.class}
    )
    public void test_AttributesName_Constructor() {
        try {
            new Attributes.Name(
                    "01234567890123456789012345678901234567890123456789012345678901234567890");
            fail("Assert 0: should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            new Attributes.Name((String) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
        }
        assertNotNull(new Attributes.Name("Attr"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        Attributes.Name attr1 = new Attributes.Name("Attr");
        Attributes.Name attr2 = new Attributes.Name("Attr");
        assertTrue(attr1.equals(attr2));
        attr2 = new Attributes.Name("Attr1");
        assertFalse(attr1.equals(attr2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        Attributes.Name attr1 = new Attributes.Name("Attr1");
        Attributes.Name attr2 = new Attributes.Name("Attr2");
        assertNotSame(attr1.hashCode(), attr2.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        String str1 = "Attr1";
        String str2 = "Attr2";
        Attributes.Name attr1 = new Attributes.Name(str1);
        Attributes.Name attr2 = new Attributes.Name("Attr2");
        assertTrue(attr1.toString().equals(str1));
        assertTrue(attr2.toString().equals(str2));
        assertFalse(attr2.toString().equals(str1));
    }
}

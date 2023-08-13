@TestTargetClass(AttributeListImpl.class)
public class AttributeListImplTest extends TestCase {
    private AttributeListImpl empty = new AttributeListImpl();
    private AttributeListImpl multi = new AttributeListImpl();
    @Override
    public void setUp() {
        multi.addAttribute("foo", "string", "abc");
        multi.addAttribute("bar", "string", "xyz");
        multi.addAttribute("answer", "int", "42");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AttributeListImpl",
        args = { }
    )
    public void testAttributeListImpl() {
        assertEquals(0, empty.getLength());
        assertEquals(3, multi.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AttributeListImpl",
        args = { AttributeList.class }
    )
    public void testAttributeListImplAttributeList() {
        AttributeListImpl ai = new AttributeListImpl(empty);
        assertEquals(0, ai.getLength());
        ai = new AttributeListImpl(multi);
        assertEquals(3, ai.getLength());
        try {
            ai = new AttributeListImpl(null);
            assertEquals(0, ai.getLength());
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAttributeList",
        args = { AttributeList.class }
    )
    public void testSetAttributeList() {
        AttributeListImpl attrs = new AttributeListImpl();
        attrs.addAttribute("doe", "boolean", "false");
        attrs.setAttributeList(empty);
        assertEquals(0, attrs.getLength());
        attrs.setAttributeList(multi);
        assertEquals(multi.getLength(), attrs.getLength());
        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getName(i), attrs.getName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
        }
        try {
            attrs.setAttributeList(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertEquals(3, attrs.getLength());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addAttribute",
        args = { String.class, String.class, String.class }
    )
    public void testAddAttribute() {
        multi.addAttribute("doe", "boolean", "false");
        assertEquals("doe", multi.getName(3));
        assertEquals("boolean", multi.getType(3));
        assertEquals("false", multi.getValue(3));
        multi.addAttribute("doe", "boolean", "false");
        assertEquals("doe", multi.getName(4));
        assertEquals("boolean", multi.getType(4));
        assertEquals("false", multi.getValue(4));
        multi.addAttribute(null, null, null);
        assertEquals(null, multi.getName(5));
        assertEquals(null, multi.getType(5));
        assertEquals(null, multi.getValue(5));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeAttribute",
        args = { String.class }
    )
    public void testRemoveAttribute() {
        multi.removeAttribute("foo");
        assertEquals("bar", multi.getName(0));
        assertEquals("string", multi.getType(0));
        assertEquals("xyz", multi.getValue(0));
        multi.removeAttribute("john");
        assertEquals(2, multi.getLength());
        multi.removeAttribute(null);
        assertEquals(2, multi.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clear",
        args = { }
    )
    public void testClear() {
        assertEquals(3, multi.getLength());
        multi.clear();
        assertEquals(0, multi.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLength",
        args = { }
    )
    public void testGetLength() {
        AttributeListImpl ai = new AttributeListImpl(empty);
        assertEquals(0, ai.getLength());
        ai = new AttributeListImpl(multi);
        assertEquals(3, ai.getLength());
        for (int i = 2; i >= 0; i--) {
            ai.removeAttribute(ai.getName(i));
            assertEquals(i, ai.getLength());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getName",
        args = { int.class }
    )
    public void testGetName() {
        assertEquals("foo", multi.getName(0));
        assertEquals("bar", multi.getName(1));
        assertEquals("answer", multi.getName(2));
        assertEquals(null, multi.getName(-1));
        assertEquals(null, multi.getName(3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getType",
        args = { int.class }
    )
    public void testGetTypeInt() {
        assertEquals("string", multi.getType(0));
        assertEquals("string", multi.getType(1));
        assertEquals("int", multi.getType(2));
        assertEquals(null, multi.getType(-1));
        assertEquals(null, multi.getType(3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getValue",
        args = { int.class }
    )
    public void testGetValueInt() {
        assertEquals("abc", multi.getValue(0));
        assertEquals("xyz", multi.getValue(1));
        assertEquals("42", multi.getValue(2));
        assertEquals(null, multi.getValue(-1));
        assertEquals(null, multi.getValue(5));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getType",
        args = { String.class }
    )
    public void testGetTypeString() {
        assertEquals("string", multi.getType("foo"));
        assertEquals("string", multi.getType("bar"));
        assertEquals("int", multi.getType("answer"));
        assertEquals(null, multi.getType("john"));
        assertEquals(null, multi.getType(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getValue",
        args = { String.class }
    )
    public void testGetValueString() {
        assertEquals("abc", multi.getValue("foo"));
        assertEquals("xyz", multi.getValue("bar"));
        assertEquals("42", multi.getValue("answer"));
        assertEquals(null, multi.getValue("john"));
        assertEquals(null, multi.getValue(null));
    }
}

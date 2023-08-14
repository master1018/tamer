@TestTargetClass(AttributesImpl.class)
public class AttributesImplTest extends TestCase {
    private AttributesImpl empty = new AttributesImpl();
    private AttributesImpl multi = new AttributesImpl();
    @Override
    public void setUp() {
        multi.addAttribute("http:
                "string", "abc");
        multi.addAttribute("http:
                "string", "xyz");
        multi.addAttribute("http:
                "int", "42");
        multi.addAttribute("", "gabbaHey", "", "string", "1-2-3-4");
        multi.addAttribute("", "", "gabba:hey", "string", "1-2-3-4");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AttributesImpl",
        args = { }
    )
    public void testAttributesImpl() {
        assertEquals(0, empty.getLength());
        assertEquals(5, multi.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AttributesImpl",
        args = { Attributes.class }
    )
    public void testAttributesImplAttributes() {
        AttributesImpl ai = new AttributesImpl(empty);
        assertEquals(0, ai.getLength());
        ai = new AttributesImpl(multi);
        assertEquals(5, ai.getLength());
        try {
            ai = new AttributesImpl(null);
            assertEquals(0, ai.getLength());
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLength",
        args = { }
    )
    public void testGetLength() {
        AttributesImpl ai = new AttributesImpl(empty);
        assertEquals(0, ai.getLength());
        ai = new AttributesImpl(multi);
        assertEquals(5, ai.getLength());
        for (int i = 4; i >= 0; i--) {
            ai.removeAttribute(i);
            assertEquals(i, ai.getLength());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getURI",
        args = { int.class }
    )
    public void testGetURI() {
        assertEquals("http:
        assertEquals("http:
        assertEquals("http:
        assertEquals("", multi.getURI(3));
        assertEquals("", multi.getURI(4));
        assertEquals(null, multi.getURI(-1));
        assertEquals(null, multi.getURI(5));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLocalName",
        args = { int.class }
    )
    public void testGetLocalName() {
        assertEquals("foo", multi.getLocalName(0));
        assertEquals("bar", multi.getLocalName(1));
        assertEquals("answer", multi.getLocalName(2));
        assertEquals("gabbaHey", multi.getLocalName(3));
        assertEquals("", multi.getLocalName(4));
        assertEquals(null, multi.getLocalName(-1));
        assertEquals(null, multi.getLocalName(5));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getQName",
        args = { int.class }
    )
    public void testGetQName() {
        assertEquals("ns1:foo", multi.getQName(0));
        assertEquals("ns1:bar", multi.getQName(1));
        assertEquals("ns2:answer", multi.getQName(2));
        assertEquals("", multi.getQName(3));
        assertEquals("gabba:hey", multi.getQName(4));
        assertEquals(null, multi.getQName(-1));
        assertEquals(null, multi.getQName(5));
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
        assertEquals("string", multi.getType(3));
        assertEquals("string", multi.getType(4));
        assertEquals(null, multi.getType(-1));
        assertEquals(null, multi.getType(5));
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
        assertEquals("1-2-3-4", multi.getValue(3));
        assertEquals("1-2-3-4", multi.getValue(4));
        assertEquals(null, multi.getValue(-1));
        assertEquals(null, multi.getValue(5));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIndex",
        args = { String.class, String.class }
    )
    public void testGetIndexStringString() {
        assertEquals(0, multi.getIndex("http:
        assertEquals(1, multi.getIndex("http:
        assertEquals(2, multi.getIndex("http:
        assertEquals(-1, multi.getIndex("john", "doe"));
        assertEquals(-1, multi.getIndex("http:
        assertEquals(-1, multi.getIndex(null, "foo"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIndex",
        args = { String.class }
    )
    public void testGetIndexString() {
        assertEquals(0, multi.getIndex("ns1:foo"));
        assertEquals(1, multi.getIndex("ns1:bar"));
        assertEquals(2, multi.getIndex("ns2:answer"));
        assertEquals(4, multi.getIndex("gabba:hey"));
        assertEquals(-1, multi.getIndex("john:doe"));
        assertEquals(-1, multi.getIndex(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getType",
        args = { String.class, String.class }
    )
    public void testGetTypeStringString() {
        assertEquals("string", multi.getType("http:
        assertEquals("string", multi.getType("http:
        assertEquals("int", multi.getType("http:
        assertEquals(null, multi.getType("john", "doe"));
        assertEquals(null, multi.getType("http:
        assertEquals(null, multi.getType(null, "foo"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getType",
        args = { String.class }
    )
    public void testGetTypeString() {
        assertEquals("string", multi.getType("ns1:foo"));
        assertEquals("string", multi.getType("ns1:bar"));
        assertEquals("int", multi.getType("ns2:answer"));
        assertEquals("string", multi.getType("gabba:hey"));
        assertEquals(null, multi.getType("john:doe"));
        assertEquals(null, multi.getType(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getValue",
        args = { String.class, String.class }
    )
    public void testGetValueStringString() {
        assertEquals("abc", multi.getValue("http:
        assertEquals("xyz", multi.getValue("http:
        assertEquals("42", multi.getValue("http:
        assertEquals(null, multi.getValue("john", "doe"));
        assertEquals(null, multi.getValue("http:
        assertEquals(null, multi.getValue(null, "foo"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getValue",
        args = { String.class }
    )
    public void testGetValueString() {
        assertEquals("abc", multi.getValue("ns1:foo"));
        assertEquals("xyz", multi.getValue("ns1:bar"));
        assertEquals("42", multi.getValue("ns2:answer"));
        assertEquals("1-2-3-4", multi.getValue("gabba:hey"));
        assertEquals(null, multi.getValue("john:doe"));
        assertEquals(null, multi.getValue(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clear",
        args = { }
    )
    public void testClear() {
        assertEquals(5, multi.getLength());
        multi.clear();
        assertEquals(0, multi.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAttributes",
        args = { Attributes.class }
    )
    public void testSetAttributes() {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("http:
                "boolean", "false");
        attrs.setAttributes(empty);
        assertEquals(0, attrs.getLength());
        attrs.setAttributes(multi);
        assertEquals(multi.getLength(), attrs.getLength());
        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
        }
        try {
            attrs.setAttributes(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            assertEquals(0, attrs.getLength());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addAttribute",
        args = { String.class, String.class, String.class, String.class,
                 String.class }
    )
    public void testAddAttribute() {
        multi.addAttribute("http:
                "boolean", "false");
        assertEquals("http:
        assertEquals("doe", multi.getLocalName(5));
        assertEquals("john:doe", multi.getQName(5));
        assertEquals("boolean", multi.getType(5));
        assertEquals("false", multi.getValue(5));
        multi.addAttribute("http:
                "boolean", "false");
        assertEquals("http:
        assertEquals("doe", multi.getLocalName(6));
        assertEquals("john:doe", multi.getQName(6));
        assertEquals("boolean", multi.getType(6));
        assertEquals("false", multi.getValue(6));
        multi.addAttribute(null, null, null, null, null);
        assertEquals(null, multi.getURI(7));
        assertEquals(null, multi.getLocalName(7));
        assertEquals(null, multi.getQName(7));
        assertEquals(null, multi.getType(7));
        assertEquals(null, multi.getValue(7));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAttribute",
        args = { int.class, String.class, String.class, String.class,
                 String.class, String.class }
    )
    public void testSetAttribute() {
        multi.setAttribute(0, "http:
                "boolean", "false");
        assertEquals("http:
        assertEquals("doe", multi.getLocalName(0));
        assertEquals("john:doe", multi.getQName(0));
        assertEquals("boolean", multi.getType(0));
        assertEquals("false", multi.getValue(0));
        multi.setAttribute(1, null, null, null, null, null);
        assertEquals(null, multi.getURI(1));
        assertEquals(null, multi.getLocalName(1));
        assertEquals(null, multi.getQName(1));
        assertEquals(null, multi.getType(1));
        assertEquals(null, multi.getValue(1));
        try {
            multi.setAttribute(-1, "http:
                    "boolean", "false");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.setAttribute(5, "http:
                    "boolean", "false");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeAttribute",
        args = { int.class }
    )
    public void testRemoveAttribute() {
        multi.removeAttribute(0);
        assertEquals("http:
        assertEquals("bar", multi.getLocalName(0));
        assertEquals("ns1:bar", multi.getQName(0));
        assertEquals("string", multi.getType(0));
        assertEquals("xyz", multi.getValue(0));
        try {
            multi.removeAttribute(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.removeAttribute(4);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setURI",
        args = { int.class, String.class }
    )
    public void testSetURI() {
        multi.setURI(0, "http:
        assertEquals("http:
        multi.setURI(1, null);
        assertEquals(null, multi.getURI(1));
        try {
            multi.setURI(-1, "http:
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.setURI(5, "http:
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setLocalName",
        args = { int.class, String.class }
    )
    public void testSetLocalName() {
        multi.setLocalName(0, "john");
        assertEquals("john", multi.getLocalName(0));
        multi.setLocalName(1, null);
        assertEquals(null, multi.getLocalName(1));
        try {
            multi.setLocalName(-1, "john");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.setLocalName(5, "john");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setQName",
        args = { int.class, String.class }
    )
    public void testSetQName() {
        multi.setQName(0, "john:doe");
        assertEquals("john:doe", multi.getQName(0));
        multi.setQName(1, null);
        assertEquals(null, multi.getQName(1));
        try {
            multi.setQName(-1, "john:doe");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.setQName(5, "john:doe");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setType",
        args = { int.class, String.class }
    )
    public void testSetType() {
        multi.setType(0, "float");
        assertEquals("float", multi.getType(0));
        multi.setType(1, null);
        assertEquals(null, multi.getType(1));
        try {
            multi.setType(-1, "float");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.setType(5, "float");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setValue",
        args = { int.class, String.class }
    )
    public void testSetValue() {
        multi.setValue(0, "too much");
        assertEquals("too much", multi.getValue(0));
        multi.setValue(1, null);
        assertEquals(null, multi.getValue(1));
        try {
            multi.setValue(-1, "too much");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.setValue(5, "too much");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
}

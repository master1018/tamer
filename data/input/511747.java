@TestTargetClass(Attributes2Impl.class)
public class Attributes2ImplTest extends TestCase {
    private Attributes2Impl empty = new Attributes2Impl();
    private Attributes2Impl multi = new Attributes2Impl();
    private Attributes2Impl cdata = new Attributes2Impl();
    @Override
    public void setUp() {
        multi.addAttribute("http:
                "string", "abc");
        multi.addAttribute("http:
                "string", "xyz");
        multi.addAttribute("http:
                "int", "42");
        multi.addAttribute("http:
                "string", "gabba");
        multi.setDeclared(0, false);
        multi.setSpecified(0, false);
        multi.setDeclared(1, true);
        multi.setSpecified(1, false);
        multi.setDeclared(2, false);
        multi.setSpecified(2, true);
        multi.setDeclared(3, true);
        multi.setSpecified(3, true);
        cdata.addAttribute("http:
                "CDATA", "hey");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAttributes",
        args = { Attributes.class }
    )
    public void testSetAttributes() {
        Attributes2Impl attrs = new Attributes2Impl();
        attrs.addAttribute("", "", "john", "string", "doe");
        attrs.setAttributes(empty);
        assertEquals(0, attrs.getLength());
        attrs.setAttributes(multi);
        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
            assertEquals(multi.isDeclared(i), attrs.isDeclared(i));
            assertEquals(multi.isSpecified(i), attrs.isSpecified(i));
        }
        attrs.setAttributes(empty);
        assertEquals(0, attrs.getLength());
        attrs.setAttributes(new AttributesImpl(multi));
        assertEquals(multi.getLength(), attrs.getLength());
        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
            assertEquals(true, attrs.isDeclared(i));
            assertEquals(true, attrs.isSpecified(i));
        }
        attrs.setAttributes(new AttributesImpl(cdata));
        assertEquals(1, attrs.getLength());
        assertEquals(false, attrs.isDeclared(0));
        assertEquals(true, attrs.isSpecified(0));
        try {
            attrs.setAttributes(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addAttribute",
        args = { String.class, String.class, String.class, String.class,
                 String.class }
    )
    public void testAddAttribute() {
        Attributes2Impl attrs = new Attributes2Impl();
        attrs.addAttribute("http:
                "string", "abc");
        assertEquals(1, attrs.getLength());
        assertEquals("http:
        assertEquals("doe", attrs.getLocalName(0));
        assertEquals("john:doe", attrs.getQName(0));
        assertEquals("string", attrs.getType(0));
        assertEquals("abc", attrs.getValue(0));
        assertEquals(true, attrs.isDeclared(0));
        assertEquals(true, attrs.isSpecified(0));
        attrs.addAttribute("http:
                "CDATA", "abc");
        assertEquals(2, attrs.getLength());
        assertEquals("http:
        assertEquals("doe", attrs.getLocalName(1));
        assertEquals("jane:doe", attrs.getQName(1));
        assertEquals("CDATA", attrs.getType(1));
        assertEquals("abc", attrs.getValue(1));
        assertEquals(false, attrs.isDeclared(1));
        assertEquals(true, attrs.isSpecified(1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeAttribute",
        args = { int.class }
    )
    public void testRemoveAttribute() {
        Attributes2Impl attrs = new Attributes2Impl(multi);
        attrs.removeAttribute(1);
        assertEquals(3, attrs.getLength());
        assertEquals(multi.getURI(0), attrs.getURI(0));
        assertEquals(multi.getLocalName(0), attrs.getLocalName(0));
        assertEquals(multi.getQName(0), attrs.getQName(0));
        assertEquals(multi.getType(0), attrs.getType(0));
        assertEquals(multi.getValue(0), attrs.getValue(0));
        assertEquals(multi.isDeclared(0), attrs.isDeclared(0));
        assertEquals(multi.isSpecified(0), attrs.isSpecified(0));
        assertEquals(multi.getURI(2), attrs.getURI(1));
        assertEquals(multi.getLocalName(2), attrs.getLocalName(1));
        assertEquals(multi.getQName(2), attrs.getQName(1));
        assertEquals(multi.getType(2), attrs.getType(1));
        assertEquals(multi.getValue(2), attrs.getValue(1));
        assertEquals(multi.isDeclared(2), attrs.isDeclared(1));
        assertEquals(multi.isSpecified(2), attrs.isSpecified(1));
        try {
            attrs.removeAttribute(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            attrs.removeAttribute(3);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Attributes2Impl",
        args = {  }
    )
    public void testAttributes2Impl() {
        assertEquals(0, empty.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Attributes2Impl",
        args = { Attributes.class }
    )
    public void testAttributes2ImplAttributes() {
        Attributes2Impl attrs = new Attributes2Impl(multi);
        assertEquals(multi.getLength(), attrs.getLength());
        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
            assertEquals(multi.isDeclared(i), attrs.isDeclared(i));
            assertEquals(multi.isSpecified(i), attrs.isSpecified(i));
        }
        attrs = new Attributes2Impl(empty);
        assertEquals(0, attrs.getLength());
        attrs = new Attributes2Impl(new AttributesImpl(multi));
        assertEquals(multi.getLength(), attrs.getLength());
        for (int i = 0; i < multi.getLength(); i++) {
            assertEquals(multi.getURI(i), attrs.getURI(i));
            assertEquals(multi.getLocalName(i), attrs.getLocalName(i));
            assertEquals(multi.getQName(i), attrs.getQName(i));
            assertEquals(multi.getType(i), attrs.getType(i));
            assertEquals(multi.getValue(i), attrs.getValue(i));
            assertEquals(true, attrs.isDeclared(i));
            assertEquals(true, attrs.isSpecified(i));
        }
        attrs = new Attributes2Impl(new AttributesImpl(cdata));
        assertEquals(1, attrs.getLength());
        assertEquals(false, attrs.isDeclared(0));
        assertEquals(true, attrs.isSpecified(0));
        try {
            attrs = new Attributes2Impl(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isDeclared",
        args = { int.class }
    )
    public void testIsDeclaredInt() {
        assertEquals(false, multi.isDeclared(0));
        assertEquals(true, multi.isDeclared(1));
        try {
            multi.isDeclared(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.isDeclared(4);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isDeclared",
        args = { String.class, String.class }
    )
    public void testIsDeclaredStringString() {
        assertEquals(false, multi.isDeclared("http:
        assertEquals(true, multi.isDeclared("http:
        try {
            assertFalse(multi.isDeclared("not", "found"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isDeclared",
        args = { String.class }
    )
    public void testIsDeclaredString() {
        assertEquals(false, multi.isDeclared("ns1:foo"));
        assertEquals(true, multi.isDeclared("ns1:bar"));
        try {
            assertFalse(multi.isDeclared("notfound"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isSpecified",
        args = { int.class }
    )
    public void testIsSpecifiedInt() {
        assertEquals(false, multi.isSpecified(1));
        assertEquals(true, multi.isSpecified(2));
        try {
            multi.isSpecified(-1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.isSpecified(4);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isSpecified",
        args = { String.class, String.class }
    )
    public void testIsSpecifiedStringString() {
        assertEquals(false, multi.isSpecified("http:
        assertEquals(true, multi.isSpecified("http:
        try {
            assertFalse(multi.isSpecified("not", "found"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isSpecified",
        args = { String.class }
    )
    public void testIsSpecifiedString() {
        assertEquals(false, multi.isSpecified("ns1:bar"));
        assertEquals(true, multi.isSpecified("ns2:answer"));
        try {
            assertFalse(multi.isSpecified("notfound"));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDeclared",
        args = { int.class, boolean.class }
    )
    public void testSetDeclared() {
        multi.setSpecified(0, false);
        assertEquals(false, multi.isSpecified(0));
        multi.setSpecified(0, true);
        assertEquals(true, multi.isSpecified(0));
        multi.setSpecified(0, false);
        assertEquals(false, multi.isSpecified(0));
        try {
            multi.setSpecified(-1, true);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.setSpecified(5, true);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setSpecified",
        args = { int.class, boolean.class }
    )
    public void testSetSpecified() {
        multi.setSpecified(0, false);
        assertEquals(false, multi.isSpecified(0));
        multi.setSpecified(0, true);
        assertEquals(true, multi.isSpecified(0));
        multi.setSpecified(0, false);
        assertEquals(false, multi.isSpecified(0));
        try {
            multi.setSpecified(-1, true);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            multi.setSpecified(5, true);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
}

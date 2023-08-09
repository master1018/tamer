@TestTargetClass(SAXException.class)
public class SAXExceptionTest extends TestCase {
    public static final String ERR = "Houston, we have a problem";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXException",
        args = { }
    )
    public void testSAXParseException() {
        SAXException e = new SAXException();
        assertNull(e.getMessage());
        assertNull(e.getException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXException",
        args = { String.class, Exception.class }
    )
    public void testSAXException_String_Exception() {
        Exception c = new Exception();
        SAXException e = new SAXException(ERR, c);
        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());
        e = new SAXException(null, c);
        assertNull(e.getMessage());
        assertEquals(c, e.getException());
        e = new SAXParseException(ERR, null);
        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXException",
        args = { String.class }
    )
    public void testSAXException_String() {
        SAXException e = new SAXException(ERR);
        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
        e = new SAXException((String)null);
        assertNull(e.getMessage());
        assertNull(e.getException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXException",
        args = { Exception.class }
    )
    public void testSAXException_Exception() {
        Exception c = new Exception();
        SAXException e = new SAXException(c);
        assertNull(e.getMessage());
        assertEquals(c, e.getException());
        e = new SAXException((Exception)null);
        assertNull(e.getMessage());
        assertNull(e.getException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = { }
    )
    public void testToString() {
        SAXException e = new SAXException(ERR);
        String s = e.toString();
        assertTrue(s.contains(ERR));
        e = new SAXException();
        s = e.toString();
        assertFalse(s.contains(ERR));
   }
}
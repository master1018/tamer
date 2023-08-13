@TestTargetClass(SAXParseException.class)
public class SAXParseExceptionTest extends TestCase {
    public static final String ERR = "Houston, we have a problem";
    public static final String SYS = "mySystemID";
    public static final String PUB = "myPublicID";
    public static final int ROW = 1;
    public static final int COL = 2;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SAXParseException",
            args = { String.class, Locator.class, Exception.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMessage",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getException",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPublicId",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemId",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineNumber",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColumnNumber",
            args = { }
        )
    })
    public void testSAXParseException_String_Locator_Exception() {
        LocatorImpl l = new LocatorImpl();
        l.setPublicId(PUB);
        l.setSystemId(SYS);
        l.setLineNumber(ROW);
        l.setColumnNumber(COL);
        Exception c = new Exception();
        SAXParseException e = new SAXParseException(ERR, l, c);
        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
        e = new SAXParseException(null, l, c);
        assertNull(e.getMessage());
        assertEquals(c, e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
        e = new SAXParseException(ERR, null, c);
        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());
        assertNull(e.getPublicId());
        assertNull(e.getSystemId());
        assertEquals(-1, e.getLineNumber());
        assertEquals(-1, e.getColumnNumber());
        e = new SAXParseException(ERR, l, null);
        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXParseException",
        args = { String.class, Locator.class }
    )
    public void testSAXParseException_String_Locator() {
        LocatorImpl l = new LocatorImpl();
        l.setPublicId(PUB);
        l.setSystemId(SYS);
        l.setLineNumber(ROW);
        l.setColumnNumber(COL);
        SAXParseException e = new SAXParseException(ERR, l);
        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
        e = new SAXParseException(null, l);
        assertNull(e.getMessage());
        assertNull(e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
        e = new SAXParseException(ERR, null);
        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
        assertNull(e.getPublicId());
        assertNull(e.getSystemId());
        assertEquals(-1, e.getLineNumber());
        assertEquals(-1, e.getColumnNumber());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXParseException",
        args = { String.class, String.class, String.class, int.class, int.class,
                 Exception.class }
    )
    public void testSAXParseException_String_String_String_int_int_Exception() {
        Exception c = new Exception();
        SAXParseException e = new SAXParseException(ERR, PUB, SYS, ROW, COL, c);
        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
        e = new SAXParseException(null, PUB, SYS, ROW, COL, c);
        assertNull(e.getMessage());
        assertEquals(c, e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
        e = new SAXParseException(ERR, null, null, -1, -1, c);
        assertEquals(ERR, e.getMessage());
        assertEquals(c, e.getException());
        assertNull(e.getPublicId());
        assertNull(e.getSystemId());
        assertEquals(-1, e.getLineNumber());
        assertEquals(-1, e.getColumnNumber());
        e = new SAXParseException(ERR, PUB, SYS, ROW, COL, null);
        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SAXParseException",
        args = { String.class, String.class, String.class, int.class,
                 int.class }
        )
    public void testSAXParseException_String_String_String_int_int() {
        SAXParseException e = new SAXParseException(ERR, PUB, SYS, ROW, COL);
        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
        e = new SAXParseException(null, PUB, SYS, ROW, COL);
        assertNull(e.getMessage());
        assertNull(e.getException());
        assertEquals(PUB, e.getPublicId());
        assertEquals(SYS, e.getSystemId());
        assertEquals(ROW, e.getLineNumber());
        assertEquals(COL, e.getColumnNumber());
        e = new SAXParseException(ERR, null, null, -1, -1);
        assertEquals(ERR, e.getMessage());
        assertNull(e.getException());
        assertNull(e.getPublicId());
        assertNull(e.getSystemId());
        assertEquals(-1, e.getLineNumber());
        assertEquals(-1, e.getColumnNumber());
    }
}
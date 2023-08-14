@TestTargetClass(Locator2Impl.class)
public class Locator2ImplTest extends TestCase {
    public static final String SYS = "mySystemID";
    public static final String PUB = "myPublicID";
    public static final int ROW = 1;
    public static final int COL = 2;
    public static final String ENC = "Klingon";
    public static final String XML = "1.0";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Locator2Impl",
        args = { }
    )
    public void testLocatorImpl() {
        Locator2Impl l = new Locator2Impl();
        assertEquals(null, l.getPublicId());
        assertEquals(null, l.getSystemId());
        assertEquals(0, l.getLineNumber());
        assertEquals(0, l.getColumnNumber());
        assertEquals(null, l.getEncoding());
        assertEquals(null, l.getXMLVersion());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Locator2Impl",
        args = { Locator.class }
    )
    public void testLocatorImplLocator() {
        Locator2Impl inner = new Locator2Impl();
        inner.setPublicId(PUB);
        inner.setSystemId(SYS);
        inner.setLineNumber(ROW);
        inner.setColumnNumber(COL);
        inner.setEncoding(ENC);
        inner.setXMLVersion(XML);
        Locator2Impl outer = new Locator2Impl(inner);
        assertEquals(PUB, outer.getPublicId());
        assertEquals(SYS, outer.getSystemId());
        assertEquals(ROW, outer.getLineNumber());
        assertEquals(COL, outer.getColumnNumber());
        assertEquals(ENC, outer.getEncoding());
        assertEquals(XML, outer.getXMLVersion());
        outer = new Locator2Impl(new LocatorImpl(inner));
        assertEquals(PUB, outer.getPublicId());
        assertEquals(SYS, outer.getSystemId());
        assertEquals(ROW, outer.getLineNumber());
        assertEquals(COL, outer.getColumnNumber());
        assertEquals(null, outer.getEncoding());
        assertEquals(null, outer.getXMLVersion());
        try {
            outer = new Locator2Impl(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setXMLVersion",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getXMLVersion",
            args = { }
        )
    })
    public void testSetXMLVersionGetXMLVersion() {
        Locator2Impl l = new Locator2Impl();
        l.setXMLVersion(XML);
        assertEquals(XML, l.getXMLVersion());
        l.setXMLVersion(null);
        assertEquals(null, l.getXMLVersion());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEncoding",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEncoding",
            args = { }
        )
    })
    public void testSetEncodingGetEncoding() {
        Locator2Impl l = new Locator2Impl();
        l.setEncoding(ENC);
        assertEquals(ENC, l.getEncoding());
        l.setEncoding(null);
        assertEquals(null, l.getEncoding());
    }
}

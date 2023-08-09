@TestTargetClass(LocatorImpl.class)
public class LocatorImplTest extends TestCase {
    public static final String SYS = "mySystemID";
    public static final String PUB = "myPublicID";
    public static final int ROW = 1;
    public static final int COL = 2;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "LocatorImpl",
        args = { }
    )
    public void testLocatorImpl() {
        LocatorImpl l = new LocatorImpl();
        assertEquals(null, l.getPublicId());
        assertEquals(null, l.getSystemId());
        assertEquals(0, l.getLineNumber());
        assertEquals(0, l.getColumnNumber());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "LocatorImpl",
        args = { Locator.class }
    )
    public void testLocatorImplLocator() {
        LocatorImpl inner = new LocatorImpl();
        inner.setPublicId(PUB);
        inner.setSystemId(SYS);
        inner.setLineNumber(ROW);
        inner.setColumnNumber(COL);
        LocatorImpl outer = new LocatorImpl(inner);
        assertEquals(PUB, outer.getPublicId());
        assertEquals(SYS, outer.getSystemId());
        assertEquals(ROW, outer.getLineNumber());
        assertEquals(COL, outer.getColumnNumber());
        try {
            outer = new LocatorImpl(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPublicId",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPublicId",
            args = { }
        )
    })
    public void testSetPublicIdGetPublicId() {
        LocatorImpl l = new LocatorImpl();
        l.setPublicId(PUB);
        assertEquals(PUB, l.getPublicId());
        l.setPublicId(null);
        assertEquals(null, l.getPublicId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSystemId",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemId",
            args = { }
        )
    })
    public void testSetSystemIdGetSystemId() {
        LocatorImpl l = new LocatorImpl();
        l.setSystemId(SYS);
        assertEquals(SYS, l.getSystemId());
        l.setSystemId(null);
        assertEquals(null, l.getSystemId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLineNumber",
            args = { int.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLineNumber",
            args = { }
        )
    })
    public void testSetLineNumberGetLineNumber() {
        LocatorImpl l = new LocatorImpl();
        l.setLineNumber(ROW);
        assertEquals(ROW, l.getLineNumber());
        l.setLineNumber(0);
        assertEquals(0, l.getLineNumber());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setColumnNumber",
            args = { int.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColumnNumber",
            args = { }
        )
    })
    public void testSetColumnNumberGetColumnNumber() {
        LocatorImpl l = new LocatorImpl();
        l.setColumnNumber(COL);
        assertEquals(COL, l.getColumnNumber());
        l.setColumnNumber(0);
        assertEquals(0, l.getColumnNumber());
    }
}

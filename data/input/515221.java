@TestTargetClass(CodingErrorAction.class)
public class CodingErrorActionTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verify constant",
        method = "!Constants",
        args = {}
    )
    public void testIGNORE() {
        assertNotNull(CodingErrorAction.IGNORE);
        assertNotNull(CodingErrorAction.REPLACE);
        assertNotNull(CodingErrorAction.REPORT);
        assertNotSame(CodingErrorAction.IGNORE, CodingErrorAction.REPLACE);
        assertNotSame(CodingErrorAction.IGNORE, CodingErrorAction.REPORT);
        assertNotSame(CodingErrorAction.REPLACE, CodingErrorAction.REPORT);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verify constant",
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertTrue(CodingErrorAction.IGNORE.toString().indexOf("IGNORE") != -1);
        assertTrue(CodingErrorAction.REPLACE.toString().indexOf("REPLACE") != -1);
        assertTrue(CodingErrorAction.REPORT.toString().indexOf("REPORT") != -1);
    }
}

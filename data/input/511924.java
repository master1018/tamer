@TestTargetClass(PSource.class)
public class PSourceTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Nested class.",
        clazz = PSource.PSpecified.class,
        method = "PSpecified",
        args = { byte[].class }
    )
    public void testPSpecified() {
        try {
            new PSource.PSpecified(null);
            fail("NullPointerException should be thrown in the case of "
                    + "null p array.");
        } catch (NullPointerException e) {
        }
        assertEquals("The PSource.PSpecified DEFAULT value should be byte[0]",
                0, PSource.PSpecified.DEFAULT.getValue().length);
        byte[] p = new byte[] {1, 2, 3, 4, 5};
        PSource.PSpecified ps = new PSource.PSpecified(p);
        p[0]++;
        assertFalse("The change of p specified in the constructor "
                + "should not cause the change of internal array.", p[0] == ps
                .getValue()[0]);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = PSource.PSpecified.class,
            method = "PSpecified",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            clazz = PSource.PSpecified.class,
            method = "getValue",
            args = {}
        )
    })
    public void testGetValue() {
        byte[] p = new byte[] {1, 2, 3, 4, 5};
        PSource.PSpecified ps = new PSource.PSpecified(p);
        byte[] result = ps.getValue();
        if (!Arrays.equals(p, result)) {
            fail("The returned array does not equal to the specified "
                    + "in the constructor.");
        }
        result[0]++;
        assertFalse("The change of returned by getValue() array "
                + "should not cause the change of internal array.",
                result[0] == ps.getValue()[0]);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks NullPointerException",
        method = "PSource",
        args = {java.lang.String.class}
    )
    public void testPSource() {
        try {
            new PSource(null) {};
            fail("NullPointerException should be thrown in the case of "
                    + "null pSrcName.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getAlgorithm",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "PSource",
            args = {java.lang.String.class}
        )
    })
    public void testGetAlgorithm() {
        String pSrcName = "pSrcName";
        PSource ps = new PSource(pSrcName) {};
        assertTrue("The returned value is not equal to the value specified "
                + "in constructor", pSrcName.equals(ps.getAlgorithm()));
    }
    public static Test suite() {
        return new TestSuite(PSourceTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

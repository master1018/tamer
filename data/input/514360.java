@TestTargetClass(PBEParameterSpec.class)
public class PBEParameterSpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PBEParameterSpec",
        args = {byte[].class, int.class}
    )
    public void testPBEParameterSpec() {
        byte[] salt = {1, 2, 3, 4, 5};
        int iterationCount = 10;
        try {
            new PBEParameterSpec(null, iterationCount);
            fail("A NullPointerException should be was thrown "
                    + "in the case of null salt.");
        } catch (NullPointerException e) {
        }
        PBEParameterSpec pbeps = new PBEParameterSpec(salt, iterationCount);
        salt[0] ++;
        assertFalse("The change of salt specified in the constructor "
                    + "should not cause the change of internal array.",
                    salt[0] == pbeps.getSalt()[0]);
   }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSalt",
        args = {}
    )
    public void testGetSalt() {
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        PBEParameterSpec pbeps = new PBEParameterSpec(salt, iterationCount);
        byte[] result = pbeps.getSalt();
        if (! Arrays.equals(salt, result)) {
            fail("The returned salt is not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getSalt() method salt"
                    + "should not cause the change of internal array.",
                    result[0] == pbeps.getSalt()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIterationCount",
        args = {}
    )
    public void testGetIterationCount() {
        byte[] salt = new byte[] {1, 2, 3, 4, 5};
        int iterationCount = 10;
        PBEParameterSpec pbeps = new PBEParameterSpec(salt, iterationCount);
        assertTrue("The returned iterationCount is not equal to the specified "
                + "in the constructor.",
                pbeps.getIterationCount() == iterationCount);
    }
    public static Test suite() {
        return new TestSuite(PBEParameterSpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

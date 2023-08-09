@TestTargetClass(CertPath.class)
public class CertPathCertPathRepTest extends TestCase {
    private static final byte[] testEncoding = new byte[] { (byte) 1, (byte) 2,
            (byte) 3, (byte) 4, (byte) 5 };
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertPath.CertPathRep.CertPathRep",
        args = { String.class, byte[].class}
    )
    public final void testCertPathCertPathRep() {
        MyCertPath cp = new MyCertPath(testEncoding);
        MyCertPathRep rep = cp.new MyCertPathRep("MyEncoding", testEncoding);
        assertEquals(testEncoding, rep.getData());
        assertEquals("MyEncoding", rep.getType());
        try {
            cp.new MyCertPathRep(null, null);
        } catch (Exception e) {
            fail("Unexpected exeption " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks ObjectStreamException",
        method = "CertPath.CertPathRep.readResolve",
        args = {}
    )
    public final void testReadResolve() {
        MyCertPath cp = new MyCertPath(testEncoding);
        MyCertPathRep rep = cp.new MyCertPathRep("MyEncoding", testEncoding);
        try {
            Object obj = rep.readResolve();
            fail("ObjectStreamException was not thrown.");
        } catch (ObjectStreamException e) {
        }
        rep = cp.new MyCertPathRep("MyEncoding", new byte[] {(byte) 1, (byte) 2, (byte) 3 });
        try {
            rep.readResolve();
            fail("ObjectStreamException expected");
        } catch (ObjectStreamException e) {
            System.out.println(e);
        }
    }
}

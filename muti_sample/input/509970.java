@TestTargetClass(CertPath.class)
public class CertPathTest extends TestCase {
    private static final byte[] testEncoding = new byte[] {
            (byte)1, (byte)2, (byte)3, (byte)4, (byte)5
    };
    private static final byte[] testEncoding1 = new byte[] {
        (byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertPath",
        args = {java.lang.String.class}
    )
    public final void testCertPath() {
        try {
            CertPath cp1 = new MyCertPath(testEncoding);
            assertEquals("MyEncoding", cp1.getType());
            assertTrue(Arrays.equals(testEncoding, cp1.getEncoded()));
        } catch (CertificateEncodingException e) {
            fail("Unexpected CertificateEncodingException " + e.getMessage());
        }
        try {
            CertPath cp1 = new MyCertPath(null);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCode() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);
        CertPath cp3 = new MyCertPath(testEncoding1);
        assertTrue(cp1.hashCode() == cp2.hashCode());
        assertTrue(cp1.hashCode() != cp3.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public final void testHashCodeEqualsObject() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);
        assertTrue((cp1.hashCode() == cp2.hashCode()) && cp1.equals(cp2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getType",
        args = {}
    )
    public final void testGetType() {
        assertEquals("MyEncoding", new MyCertPath(testEncoding).getType());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that object equals to itself.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject01() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertTrue(cp1.equals(cp1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that CertPath object equals to other CertPath with the same state.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject02() {
        CertPath cp1 = new MyCertPath(testEncoding);
        CertPath cp2 = new MyCertPath(testEncoding);
        assertTrue(cp1.equals(cp2) && cp2.equals(cp1));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject03() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertFalse(cp1.equals(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies non equal objects.",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject04() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertFalse(cp1.equals("MyEncoding"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() {
        CertPath cp1 = new MyCertPath(testEncoding);
        assertNotNull(cp1.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Abstract method.",
        method = "getCertificates",
        args = {}
    )
    public final void testGetCertificates() {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getCertificates();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Abstract method.",
        method = "getEncoded",
        args = {}
    )
    public final void testGetEncoded() throws CertificateEncodingException {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncoded();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Abstract method.",
        method = "getEncoded",
        args = {java.lang.String.class}
    )
    public final void testGetEncodedString() throws CertificateEncodingException {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncoded("MyEncoding");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Abstract method.",
        method = "getEncodings",
        args = {}
    )
    public final void testGetEncodings() {
        CertPath cp1 = new MyCertPath(testEncoding);
        cp1.getEncodings();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ObjectStreamException.",
        method = "writeReplace",
        args = {}
    )
    public final void testWriteReplace() {
        try {
            MyCertPath cp1 = new MyCertPath(testEncoding);
            Object obj = cp1.writeReplace();
            assertTrue(obj.toString().contains(
                    "java.security.cert.CertPath$CertPathRep"));
        } catch (ObjectStreamException e) {
            fail("Unexpected ObjectStreamException " + e.getMessage());
        }
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "verifies ObjectStreamException.",
            method = "writeReplace",
            args = {}
        )
    public final void testWriteReplace_ObjectStreamException() {
        try {
            MyFailingCertPath cp = new MyFailingCertPath(testEncoding);
            Object obj = cp.writeReplace();
            fail("expected ObjectStreamException");
        } catch (ObjectStreamException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility. And tests default constructor",
            method = "!SerializationSelf",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "writeReplace",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "CertPath.CertPathRep.readResolve",
            args = {}
        )
    })
    @KnownFailure(value="expired certificate bug 2322662")
    public void testSerializationSelf() throws Exception {
        TestUtils.initCertPathSSCertChain();
        CertPath certPath = TestUtils.buildCertPathSSCertChain();
        SerializationTest.verifySelf(certPath);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationGolden",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "writeReplace",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "CertPath.CertPathRep.readResolve",
            args = {}
        )
    })
    @KnownFailure(value="expired certificate bug 2322662")
    public void testSerializationCompatibility() throws Exception {
        TestUtils.initCertPathSSCertChain();
        CertPath certPath = TestUtils.buildCertPathSSCertChain();
        SerializationTest.verifyGolden(this, certPath);
    }
}

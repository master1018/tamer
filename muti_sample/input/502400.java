@TestTargetClass(CollectionCertStoreParameters.class)
public class CollectionCertStoreParametersTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CollectionCertStoreParameters",
        args = {}
    )
    public final void testCollectionCertStoreParameters01() {
        CertStoreParameters cp = new CollectionCertStoreParameters();
        assertTrue("isCollectionCertStoreParameters",
                cp instanceof CollectionCertStoreParameters);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CollectionCertStoreParameters",
        args = {}
    )
    @SuppressWarnings("unchecked")
    public final void testCollectionCertStoreParameters02() {
        CollectionCertStoreParameters cp = new CollectionCertStoreParameters();
        Collection c = cp.getCollection();
        assertTrue("isEmpty", c.isEmpty());
        try {
            c.add(new Object());
            fail("empty collection must be immutable");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CollectionCertStoreParameters",
        args = {java.util.Collection.class}
    )
    public final void testCollectionCertStoreParametersCollection01() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        certificates.add(new MyCertificate("TEST", new byte[] {}));
        new CollectionCertStoreParameters(certificates);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CollectionCertStoreParameters",
        args = {java.util.Collection.class}
    )
    public final void testCollectionCertStoreParametersCollection02() {
        Vector<String> certificates = new Vector<String>();
        certificates.add(new String("Not a Certificate"));
        new CollectionCertStoreParameters(certificates);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CollectionCertStoreParameters",
        args = {java.util.Collection.class}
    )
    public final void testCollectionCertStoreParametersCollection03() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        CollectionCertStoreParameters cp =
            new CollectionCertStoreParameters(certificates);
        assertTrue("isRefUsed_1", certificates == cp.getCollection());
        assertTrue("isEmpty", cp.getCollection().isEmpty());
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)1}));
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)2}));
        assertTrue("isRefUsed_2", certificates.equals(cp.getCollection()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "CollectionCertStoreParameters",
        args = {java.util.Collection.class}
    )
    public final void testCollectionCertStoreParametersCollection04() {
        try {
            new CollectionCertStoreParameters(null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public final void testClone01() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)4}));
        CollectionCertStoreParameters cp1 =
            new CollectionCertStoreParameters(certificates);
        CollectionCertStoreParameters cp2 =
            (CollectionCertStoreParameters)cp1.clone();
        assertTrue(cp1 != cp2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public final void testClone02() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)4}));
        CollectionCertStoreParameters cp1 =
            new CollectionCertStoreParameters(certificates);
        CollectionCertStoreParameters cp2 =
            (CollectionCertStoreParameters)cp1.clone();
        assertTrue(cp1.getCollection() == cp2.getCollection());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public final void testClone03() {
        CollectionCertStoreParameters cp1 =
            new CollectionCertStoreParameters();
        CollectionCertStoreParameters cp2 =
            (CollectionCertStoreParameters)cp1.clone();
        CollectionCertStoreParameters cp3 =
            (CollectionCertStoreParameters)cp2.clone();
        assertTrue(cp1.getCollection() == cp2.getCollection() &&
                   cp3.getCollection() == cp2.getCollection());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString01() {
        CollectionCertStoreParameters cp =
            new CollectionCertStoreParameters();
        String s = cp.toString();
        assertNotNull(s);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString02() {
        Vector<Certificate> certificates = new Vector<Certificate>();
        certificates.add(new MyCertificate("TEST", new byte[] {(byte)4}));
        CollectionCertStoreParameters cp =
            new CollectionCertStoreParameters(certificates);
        assertNotNull(cp.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getCollection",
        args = {}
    )
    public final void testGetCollection01() {
        CollectionCertStoreParameters cp = new CollectionCertStoreParameters();
        assertNotNull(cp.getCollection());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getCollection",
        args = {}
    )
    public final void testGetCollection02() {
        Vector certificates = new Vector();
        CollectionCertStoreParameters cp =
            new CollectionCertStoreParameters(certificates);
        assertNotNull(cp.getCollection());
    }
}

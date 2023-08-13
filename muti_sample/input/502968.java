@TestTargetClass(CRL.class)
public class CRLTest extends TestCase {
    public static final String[] validValues = { "X.509", "x.509" };
    private final static String[] invalidValues = SpiEngUtils.invalidValues;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CRL",
        args = {java.lang.String.class}
    )
    public final void testConstructor() {
        for (int i = 0; i< validValues.length; i++) {
            CRL crl = new MyCRL(validValues[i]);
            assertEquals(validValues[i], crl.getType());
        }
        for (int i = 0; i< invalidValues.length; i++) {
            CRL crl = new MyCRL(invalidValues[i]);
            assertEquals(invalidValues[i], crl.getType());
        }
        try {
            CRL crl = new MyCRL(null);
        } catch (Exception e) {
            fail("Unexpected exception for NULL parameter");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getType",
        args = {}
    )
    public final void testGetType01() {
        CRL crl = new MyCRL("TEST_TYPE");
        assertEquals("TEST_TYPE", crl.getType());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "getType",
        args = {}
    )
    public final void testGetType02() {
        CRL crl = new MyCRL(null);
        assertNull(crl.getType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() {
        CRL crl = new MyCRL("TEST_TYPE");
        crl.toString();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isRevoked",
        args = {java.security.cert.Certificate.class}
    )
    public final void testIsRevoked() {
        CRL crl = new MyCRL("TEST_TYPE");
        crl.isRevoked(null);
    }
    class MyCRL extends CRL {
        protected MyCRL(String type) {
            super(type);
        }
        @Override
        public boolean isRevoked(Certificate cert) {
            return false;
        }
        @Override
        public String toString() {
            return null;
        }
    }
}

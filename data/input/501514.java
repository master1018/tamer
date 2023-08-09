        value = Certificate.class,
        untestedMethods = {
            @TestTargetNew(
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "not specific enough for black-box testing",
                    method = "toString",
                    args = {}
                  )
        }
)
public class CertificateTest extends TestCase {
    private class TBTCert extends Certificate {
        public byte[] getEncoded() throws CertificateEncodingException {
            return null;
        }
        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }
        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }
        public String toString() {
            return "TBTCert";
        }
        public PublicKey getPublicKey() {
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Certificate",
        args = {}
    )
    public final void testCertificate() {
        TBTCert tbt_cert = new TBTCert();
        assertNull("Public key should be null", tbt_cert.getPublicKey());
        assertEquals("Wrong string representation for Certificate", "TBTCert", tbt_cert.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        TBTCert tbt_cert = new TBTCert() {
            public byte[] getEncoded() {
                return new byte[] { 1, 2, 3 };
            }
        };
        TBTCert tbt_cert_1 = new TBTCert() {
            public byte[] getEncoded() {
                return new byte[] { 1, 2, 3 };
            }
        };
        TBTCert tbt_cert_2 = new TBTCert() {
            public byte[] getEncoded() {
                return new byte[] { 1, 2, 3 };
            }
        };
        TBTCert tbt_cert_3 = new TBTCert() {
            public byte[] getEncoded() {
                return new byte[] { 3, 2, 1 };
            }
        };
        assertTrue("The equivalence relation should be reflexive.", tbt_cert
                .equals(tbt_cert));
        assertEquals(
                "The Certificates with equal encoded form should be equal",
                tbt_cert, tbt_cert_1);
        assertTrue("The equivalence relation should be symmetric.", tbt_cert_1
                .equals(tbt_cert));
        assertEquals(
                "The Certificates with equal encoded form should be equal",
                tbt_cert_1, tbt_cert_2);
        assertTrue("The equivalence relation should be transitive.", tbt_cert
                .equals(tbt_cert_2));
        assertFalse("Should not be equal to null object.", tbt_cert
                .equals(null));
        assertFalse("The Certificates with differing encoded form "
                + "should not be equal", tbt_cert.equals(tbt_cert_3));
        assertFalse("The Certificates should not be equals to the object "
                + "which is not an instance of Certificate", tbt_cert
                .equals(new Object()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        TBTCert tbt_cert = new TBTCert() {
            public byte[] getEncoded() {
                return new byte[] { 1, 2, 3 };
            }
        };
        TBTCert tbt_cert_1 = new TBTCert() {
            public byte[] getEncoded() {
                return new byte[] { 1, 2, 3 };
            }
        };
        assertTrue("Equal objects should have the same hash codes.", tbt_cert
                .hashCode() == tbt_cert_1.hashCode());
    }
    public static Test suite() {
        return new TestSuite(CertificateTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

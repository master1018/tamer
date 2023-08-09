@TestTargetClass(TrustAnchor.class)
public class TrustAnchorTest extends TestCase {
    private static final String keyAlg = "DSA";
    private static final String validCaNameRfc2253 =
        "CN=Test CA,"+
        "OU=Testing Division,"+
        "O=Test It All,"+
        "L=Test Town,"+
        "ST=Testifornia,"+
        "C=Testland";
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "TrustAnchor",
        args = {java.lang.String.class, java.security.PublicKey.class, byte[].class}
    )
    public final void testTrustAnchorStringPublicKeybyteArray01()
            throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        new TrustAnchor(validCaNameRfc2253, pk, getFullEncoding());
        new TrustAnchor(validCaNameRfc2253, pk, getEncodingPSOnly());        
        new TrustAnchor(validCaNameRfc2253, pk, getEncodingESOnly());        
        new TrustAnchor(validCaNameRfc2253, pk, getEncodingNoMinMax());        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "TrustAnchor",
        args = {java.lang.String.class, java.security.PublicKey.class, byte[].class}
    )
    public final void testTrustAnchorStringPublicKeybyteArray02()
            throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        new TrustAnchor(validCaNameRfc2253, pk, null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "TrustAnchor",
        args = {java.lang.String.class, java.security.PublicKey.class, byte[].class}
    )
    public final void testTrustAnchorStringPublicKeybyteArray03()
            throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        byte[] nc = getEncodingPSOnly();
        byte[] ncCopy = nc.clone();
        TrustAnchor ta = new TrustAnchor(validCaNameRfc2253, pk, ncCopy);
        ncCopy[0]=(byte)0;
        assertTrue(Arrays.equals(nc, ta.getNameConstraints()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exceptions.",
        method = "TrustAnchor",
        args = {java.lang.String.class, java.security.PublicKey.class, byte[].class}
    )
    public final void testTrustAnchorStringPublicKeybyteArray04()
            throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        try {
            new TrustAnchor((String)null, pk, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
        try {
            new TrustAnchor(validCaNameRfc2253, null, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
        try {
            new TrustAnchor((String)null, null, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
        try {
            new TrustAnchor("", pk, getEncodingPSOnly());
            fail("IllegalArgumentException has not been thrown");
        } catch (IllegalArgumentException ok) {
        }
        try {
            new TrustAnchor("AID.11.12=A", pk, getEncodingPSOnly());
            fail("IllegalArgumentException has not been thrown");
        } catch (IllegalArgumentException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "TrustAnchor",
        args = {javax.security.auth.x500.X500Principal.class, java.security.PublicKey.class, byte[].class}
    )
    public final void testTrustAnchorX500PrincipalPublicKeybyteArray01()
            throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        new TrustAnchor(x500p, pk, getFullEncoding());
        new TrustAnchor(x500p, pk, getEncodingPSOnly());        
        new TrustAnchor(x500p, pk, getEncodingESOnly());        
        new TrustAnchor(x500p, pk, getEncodingNoMinMax());        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "TrustAnchor",
        args = {javax.security.auth.x500.X500Principal.class, java.security.PublicKey.class, byte[].class}
    )
    public final void testTrustAnchorX500PrincipalPublicKeybyteArray02()
            throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        new TrustAnchor(x500p, pk, null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies TrustAnchor with copied byte array.",
        method = "TrustAnchor",
        args = {javax.security.auth.x500.X500Principal.class, java.security.PublicKey.class, byte[].class}
    )
    public final void testTrustAnchorX500PrincipalPublicKeybyteArray03()
            throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        byte[] nc = getEncodingPSOnly();
        byte[] ncCopy = nc.clone();
        TrustAnchor ta = new TrustAnchor(new X500Principal(validCaNameRfc2253),
                pk, ncCopy);
        ncCopy[0]=(byte)0;
        assertTrue(Arrays.equals(nc, ta.getNameConstraints()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "TrustAnchor",
        args = {javax.security.auth.x500.X500Principal.class, java.security.PublicKey.class, byte[].class}
    )
    public final void testTrustAnchorX500PrincipalPublicKeybyteArray04()
            throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        try {
            new TrustAnchor((X500Principal)null,
                    pk, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
        try {
            new TrustAnchor(x500p, null, getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
        try {
            new TrustAnchor((X500Principal)null, null,
                    getEncodingPSOnly());
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "TrustAnchor",
        args = {java.security.cert.X509Certificate.class, byte[].class}
    )
    public final void testTrustAnchorX509CertificatebyteArray01()
            throws CertificateException {
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
        TrustAnchor ta1 = new TrustAnchor(pemCert, getFullEncoding());
        assertNull(ta1.getCA());
        assertNull(ta1.getCAName());
        assertNull(ta1.getCAPublicKey());
        assertTrue(Arrays.equals(getFullEncoding(), ta1.getNameConstraints()));
        assertEquals(pemCert, ta1.getTrustedCert());
        TrustAnchor ta2 = new TrustAnchor(pemCert, getEncodingPSOnly());
        assertNull(ta2.getCA());
        assertNull(ta2.getCAName());
        assertNull(ta2.getCAPublicKey());
        assertTrue(Arrays.equals(getEncodingPSOnly(), ta2.getNameConstraints()));
        assertEquals(pemCert, ta2.getTrustedCert());
        TrustAnchor ta3 = new TrustAnchor(pemCert, getEncodingESOnly());
        assertNull(ta3.getCA());
        assertNull(ta3.getCAName());
        assertNull(ta3.getCAPublicKey());
        assertTrue(Arrays.equals(getEncodingESOnly(), ta3.getNameConstraints()));
        assertEquals(pemCert, ta3.getTrustedCert());
        TrustAnchor ta4 = new TrustAnchor(pemCert, getEncodingNoMinMax());
        assertNull(ta4.getCA());
        assertNull(ta4.getCAName());
        assertNull(ta4.getCAPublicKey());
        assertTrue(Arrays.equals(getEncodingNoMinMax(), ta4
                .getNameConstraints()));
        assertEquals(pemCert, ta4.getTrustedCert());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "TrustAnchor",
        args = {java.security.cert.X509Certificate.class, byte[].class}
    )
    public final void testTrustAnchorX509CertificatebyteArray02()
            throws Exception {
        try {
            new TrustAnchor(null, getFullEncoding());
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with null as nameConstraints parameter.",
        method = "TrustAnchor",
        args = {java.security.cert.X509Certificate.class, byte[].class}
    )
    public final void testTrustAnchorX509CertificatebyteArray03()
            throws Exception {
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
        try {
            new TrustAnchor(pemCert, null);
        } catch (Exception e) {
            fail("Unexpected exeption " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "TrustAnchor",
        args = {java.security.cert.X509Certificate.class, byte[].class}
    )
    public final void testTrustAnchorX509CertificatebyteArray04()
            throws Exception {
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
        try {
            new TrustAnchor(pemCert,
                    new byte[] { (byte) 1, (byte) 2, (byte) 3 });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "TrustAnchor",
        args = {java.security.cert.X509Certificate.class, byte[].class}
    )
    public final void testTrustAnchorX509CertificatebyteArray05()
            throws Exception {
        try {
            new TrustAnchor(null, null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCAPublicKey",
        args = {}
    )
    public final void testGetCAPublicKey01() throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        TrustAnchor ta =
            new TrustAnchor(validCaNameRfc2253, pk, null);
        assertEquals("equals1", pk, ta.getCAPublicKey());
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        ta = new TrustAnchor(x500p, pk, null);
        assertEquals("equals2", pk, ta.getCAPublicKey());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCAName",
        args = {}
    )
    public final void testGetCAName01() throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        TrustAnchor ta =
            new TrustAnchor(validCaNameRfc2253, pk, null);
        assertEquals("equals1", validCaNameRfc2253, ta.getCAName());
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        ta = new TrustAnchor(x500p, pk, null);
        assertEquals("equals2", validCaNameRfc2253, ta.getCAName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTrustedCert",
        args = {}
    )
    public final void testGetTrustedCer02() throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        TrustAnchor ta =
            new TrustAnchor(validCaNameRfc2253, pk, null);
        assertNull("null1", ta.getTrustedCert());
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        ta = new TrustAnchor(x500p, pk, null);
        assertNull("null2", ta.getTrustedCert());
        X509Certificate cert = new TestCertUtils.TestX509Certificate(x500p, x500p);
        TrustAnchor ta2 = new TrustAnchor(cert, null);
        assertSame(cert, ta2.getTrustedCert());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getNameConstraints",
        args = {}
    )
    public final void testGetNameConstraints01() throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        TrustAnchor ta1 = new TrustAnchor(validCaNameRfc2253, pk,
                getFullEncoding());
        assertTrue(Arrays.equals(getFullEncoding(), ta1.getNameConstraints()));
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        TrustAnchor ta2 = new TrustAnchor(x500p, pk, getEncodingNoMinMax());
        assertTrue(Arrays.equals(getEncodingNoMinMax(), ta2
                .getNameConstraints()));
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
        TrustAnchor ta3 = new TrustAnchor(pemCert, getEncodingPSOnly());
        assertTrue(Arrays.equals(getEncodingPSOnly(), ta3.getNameConstraints()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getNameConstraints returns null.",
        method = "getNameConstraints",
        args = {}
    )
    public final void testGetNameConstraints02() throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        TrustAnchor ta1 = new TrustAnchor(validCaNameRfc2253, pk, null);
        assertNull(ta1.getNameConstraints());
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        TrustAnchor ta2 = new TrustAnchor(x500p, pk, null);
        assertNull(ta2.getNameConstraints());
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
        TrustAnchor ta3 = new TrustAnchor(pemCert, null);
        assertNull(ta3.getNameConstraints());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        TrustAnchor ta1 = new TrustAnchor(validCaNameRfc2253, pk,
                getFullEncoding());
        assertNotNull(ta1.toString());
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        TrustAnchor ta2 = new TrustAnchor(x500p, pk, getEncodingNoMinMax());
        assertNotNull(ta2.toString());
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
        TrustAnchor ta3 = new TrustAnchor(pemCert, getEncodingPSOnly());
        assertNotNull(ta3.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCA",
        args = {}
    )
    public final void testGetCA01() throws Exception {
        PublicKey pk = new TestKeyPair(keyAlg).getPublic();
        TrustAnchor ta =
            new TrustAnchor(validCaNameRfc2253, pk, null);
        X500Principal ca = ta.getCA();
        assertEquals("equals1", validCaNameRfc2253, ca.getName());
        X500Principal x500p = new X500Principal(validCaNameRfc2253);
        ta = new TrustAnchor(x500p, pk, null);
        assertEquals("equals2", x500p, ta.getCA());
    }
    private static final byte[] getFullEncoding() {
        return new byte[] {
                (byte)0x30,(byte)0x81,(byte)0x8c,(byte)0xa0,
                (byte)0x44,(byte)0x30,(byte)0x16,(byte)0x86,
                (byte)0x0e,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x66,(byte)0x6f,(byte)0x6f,(byte)0x2e,
                (byte)0x63,(byte)0x6f,(byte)0x6d,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0x30,(byte)0x16,(byte)0x86,
                (byte)0x0e,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x62,(byte)0x61,(byte)0x72,(byte)0x2e,
                (byte)0x63,(byte)0x6f,(byte)0x6d,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0x30,(byte)0x12,(byte)0x86,
                (byte)0x0a,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x6d,(byte)0x75,(byte)0x75,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0xa1,(byte)0x44,(byte)0x30,
                (byte)0x16,(byte)0x86,(byte)0x0e,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x66,(byte)0x6f,
                (byte)0x6f,(byte)0x2e,(byte)0x63,(byte)0x6f,
                (byte)0x6d,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01,(byte)0x30,
                (byte)0x16,(byte)0x86,(byte)0x0e,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x62,(byte)0x61,
                (byte)0x72,(byte)0x2e,(byte)0x63,(byte)0x6f,
                (byte)0x6d,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01,(byte)0x30,
                (byte)0x12,(byte)0x86,(byte)0x0a,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x6d,(byte)0x75,
                (byte)0x75,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01
        };
    }
    private static final byte[] getEncodingPSOnly() {
        return new byte[] {
                (byte)0x30,(byte)0x46,(byte)0xa0,(byte)0x44,
                (byte)0x30,(byte)0x16,(byte)0x86,(byte)0x0e,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x66,
                (byte)0x6f,(byte)0x6f,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
                (byte)0x30,(byte)0x16,(byte)0x86,(byte)0x0e,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x62,
                (byte)0x61,(byte)0x72,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
                (byte)0x30,(byte)0x12,(byte)0x86,(byte)0x0a,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x6d,
                (byte)0x75,(byte)0x75,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
        };
    }
    private static final byte[] getEncodingESOnly() {
        return new byte[] {
                (byte)0x30,(byte)0x46,(byte)0xa1,(byte)0x44,
                (byte)0x30,(byte)0x16,(byte)0x86,(byte)0x0e,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70, 
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x66, 
                (byte)0x6f,(byte)0x6f,(byte)0x2e,(byte)0x63, 
                (byte)0x6f,(byte)0x6d,(byte)0x80,(byte)0x01, 
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
                (byte)0x30,(byte)0x16,(byte)0x86,(byte)0x0e,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x62,
                (byte)0x61,(byte)0x72,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
                (byte)0x30,(byte)0x12,(byte)0x86,(byte)0x0a,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x6d,
                (byte)0x75,(byte)0x75,(byte)0x80,(byte)0x01,
                (byte)0x00,(byte)0x81,(byte)0x01,(byte)0x01,
        };
    }
    private static final byte[] getEncodingNoMinMax() {
        return new byte[] {
                (byte)0x30,(byte)0x68,(byte)0xa0,(byte)0x32,
                (byte)0x30,(byte)0x10,(byte)0x86,(byte)0x0e,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x66,
                (byte)0x6f,(byte)0x6f,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x30,(byte)0x10,
                (byte)0x86,(byte)0x0e,(byte)0x66,(byte)0x69,
                (byte)0x6c,(byte)0x65,(byte)0x3a,(byte)0x2f,
                (byte)0x2f,(byte)0x62,(byte)0x61,(byte)0x72,
                (byte)0x2e,(byte)0x63,(byte)0x6f,(byte)0x6d,
                (byte)0x30,(byte)0x0c,(byte)0x86,(byte)0x0a,
                (byte)0x66,(byte)0x69,(byte)0x6c,(byte)0x65,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x6d,
                (byte)0x75,(byte)0x75,(byte)0xa1,(byte)0x32,
                (byte)0x30,(byte)0x10,(byte)0x86,(byte)0x0e,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x66,
                (byte)0x6f,(byte)0x6f,(byte)0x2e,(byte)0x63,
                (byte)0x6f,(byte)0x6d,(byte)0x30,(byte)0x10,
                (byte)0x86,(byte)0x0e,(byte)0x68,(byte)0x74,
                (byte)0x74,(byte)0x70,(byte)0x3a,(byte)0x2f,
                (byte)0x2f,(byte)0x62,(byte)0x61,(byte)0x72,
                (byte)0x2e,(byte)0x63,(byte)0x6f,(byte)0x6d,
                (byte)0x30,(byte)0x0c,(byte)0x86,(byte)0x0a,
                (byte)0x68,(byte)0x74,(byte)0x74,(byte)0x70,
                (byte)0x3a,(byte)0x2f,(byte)0x2f,(byte)0x6d,
                (byte)0x75,(byte)0x75,
        };
    }
}

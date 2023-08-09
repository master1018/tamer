@TestTargetClass(PKIXBuilderParameters.class)
public class PKIXBuilderParametersTest extends TestCase {
    String certificate = "-----BEGIN CERTIFICATE-----\n"
            + "MIICZTCCAdICBQL3AAC2MA0GCSqGSIb3DQEBAgUAMF8xCzAJBgNVBAYTAlVTMSAw\n"
            + "HgYDVQQKExdSU0EgRGF0YSBTZWN1cml0eSwgSW5jLjEuMCwGA1UECxMlU2VjdXJl\n"
            + "IFNlcnZlciBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw05NzAyMjAwMDAwMDBa\n"
            + "Fw05ODAyMjAyMzU5NTlaMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv\n"
            + "cm5pYTESMBAGA1UEBxMJUGFsbyBBbHRvMR8wHQYDVQQKExZTdW4gTWljcm9zeXN0\n"
            + "ZW1zLCBJbmMuMSEwHwYDVQQLExhUZXN0IGFuZCBFdmFsdWF0aW9uIE9ubHkxGjAY\n"
            + "BgNVBAMTEWFyZ29uLmVuZy5zdW4uY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB\n"
            + "iQKBgQCofmdY+PiUWN01FOzEewf+GaG+lFf132UpzATmYJkA4AEA/juW7jSi+LJk\n"
            + "wJKi5GO4RyZoyimAL/5yIWDV6l1KlvxyKslr0REhMBaD/3Z3EsLTTEf5gVrQS6sT\n"
            + "WMoSZAyzB39kFfsB6oUXNtV8+UKKxSxKbxvhQn267PeCz5VX2QIDAQABMA0GCSqG\n"
            + "SIb3DQEBAgUAA34AXl3at6luiV/7I9MN5CXYoPJYI8Bcdc1hBagJvTMcmlqL2uOZ\n"
            + "H9T5hNMEL9Tk6aI7yZPXcw/xI2K6pOR/FrMp0UwJmdxX7ljV6ZtUZf7pY492UqwC\n"
            + "1777XQ9UEZyrKJvF5ntleeO0ayBqLGVKCWzWZX9YsXCpv47FNLZbupE=\n"
            + "-----END CERTIFICATE-----\n";
    String certificate2 = "-----BEGIN CERTIFICATE-----\n"
            + "MIICZzCCAdCgAwIBAgIBGzANBgkqhkiG9w0BAQUFADBhMQswCQYDVQQGEwJVUzEY\n"
            + "MBYGA1UEChMPVS5TLiBHb3Zlcm5tZW50MQwwCgYDVQQLEwNEb0QxDDAKBgNVBAsT\n"
            + "A1BLSTEcMBoGA1UEAxMTRG9EIFBLSSBNZWQgUm9vdCBDQTAeFw05ODA4MDMyMjAy\n"
            + "MjlaFw0wODA4MDQyMjAyMjlaMGExCzAJBgNVBAYTAlVTMRgwFgYDVQQKEw9VLlMu\n"
            + "IEdvdmVybm1lbnQxDDAKBgNVBAsTA0RvRDEMMAoGA1UECxMDUEtJMRwwGgYDVQQD\n"
            + "ExNEb0QgUEtJIE1lZCBSb290IENBMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB\n"
            + "gQDbrM/J9FrJSX+zxFUbsI9Vw5QbguVBIa95rwW/0M8+sM0r5gd+DY6iubm6wnXk\n"
            + "CSvbfQlFEDSKr4WYeeGp+d9WlDnQdtDFLdA45tCi5SHjnW+hGAmZnld0rz6wQekF\n"
            + "5xQaa5A6wjhMlLOjbh27zyscrorMJ1O5FBOWnEHcRv6xqQIDAQABoy8wLTAdBgNV\n"
            + "HQ4EFgQUVrmYR6m9701cHQ3r5kXyG7zsCN0wDAYDVR0TBAUwAwEB/zANBgkqhkiG\n"
            + "9w0BAQUFAAOBgQDVX1Y0YqC7vekeZjVxtyuC8Mnxbrz6D109AX07LEIRzNYzwZ0w\n"
            + "MTImSp9sEzWW+3FueBIU7AxGys2O7X0qmN3zgszPfSiocBuQuXIYQctJhKjF5KVc\n"
            + "VGQRYYlt+myhl2vy6yPzEVCjiKwMEb1Spu0irCf+lFW2hsdjvmSQMtZvOw==\n"
            + "-----END CERTIFICATE-----\n";
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "PKIXBuilderParameters",
        args = {java.util.Set.class, java.security.cert.CertSelector.class}
    )
    public final void testPKIXBuilderParametersSetCertSelector01()
        throws InvalidAlgorithmParameterException {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p =
            new PKIXBuilderParameters(taSet, new X509CertSelector());
        assertTrue("instanceOf", p instanceof PKIXBuilderParameters);
        assertNotNull("certSelector", p.getTargetCertConstraints());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a CertSelector parameter.",
        method = "PKIXBuilderParameters",
        args = {java.util.Set.class, java.security.cert.CertSelector.class}
    )
    public final void testPKIXBuilderParametersSetCertSelector02()
        throws InvalidAlgorithmParameterException {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor set)");
        }
        PKIXParameters p = new PKIXBuilderParameters(taSet, null);
        assertTrue("instanceOf", p instanceof PKIXBuilderParameters);
        assertNull("certSelector", p.getTargetCertConstraints());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a CertSelector parameter.",
        method = "PKIXBuilderParameters",
        args = {java.util.Set.class, java.security.cert.CertSelector.class}
    )
    @SuppressWarnings("unchecked")
    public final void testPKIXBuilderParametersSetCertSelector03()
        throws InvalidAlgorithmParameterException {
        Set<TrustAnchor> taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor set)");
        }
        HashSet<TrustAnchor> originalSet = (HashSet<TrustAnchor>) taSet;
        HashSet<TrustAnchor> originalSetCopy = (HashSet<TrustAnchor>) originalSet
                .clone();
        PKIXBuilderParameters pp =
            new PKIXBuilderParameters(originalSetCopy, null);
        originalSetCopy.clear();
        Set returnedSet = pp.getTrustAnchors();
        assertEquals(originalSet, returnedSet);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as parameters.",
        method = "PKIXBuilderParameters",
        args = {java.util.Set.class, java.security.cert.CertSelector.class}
    )
    public final void testPKIXBuilderParametersSetCertSelector04() throws Exception {
        try {
            new PKIXBuilderParameters((Set<TrustAnchor>) null, null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "PKIXBuilderParameters",
        args = {java.util.Set.class, java.security.cert.CertSelector.class}
    )
    public final void testPKIXBuilderParametersSetCertSelector05() {
        try {
            new PKIXBuilderParameters(new HashSet<TrustAnchor>(), null);
            fail("InvalidAlgorithmParameterException expected");
        } catch (InvalidAlgorithmParameterException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies ClassCastException.",
        method = "PKIXBuilderParameters",
        args = {java.util.Set.class, java.security.cert.CertSelector.class}
    )
    @SuppressWarnings("unchecked")
    public final void testPKIXBuilderParametersSetCertSelector06()
            throws Exception {
        Set taSet = TestUtils.getTrustAnchorSet();
        if (taSet == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor set)");
        }
        assertTrue(taSet.add(new Object()));
        try {
            new PKIXBuilderParameters(taSet, null);
            fail("ClassCastException expected");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Veirifies null as a KeyStore parameter.",
        method = "PKIXBuilderParameters",
        args = {java.security.KeyStore.class, java.security.cert.CertSelector.class}
    )
    public final void testPKIXBuilderParametersKeyStoreCertSelector01()
            throws Exception {
        try {
            new PKIXBuilderParameters((KeyStore) null, new X509CertSelector());
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Veirifies null as a CertSelector parameter.",
        method = "PKIXBuilderParameters",
        args = {java.security.KeyStore.class, java.security.cert.CertSelector.class}
    )
    public final void testPKIXBuilderParametersKeyStoreCertSelector02()
            throws Exception {
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            new PKIXBuilderParameters(keyTest, null);
            fail("KeyStoreException expected");
        } catch (KeyStoreException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as parameters.",
        method = "PKIXBuilderParameters",
        args = {java.security.KeyStore.class, java.security.cert.CertSelector.class}
    )
    public final void testPKIXBuilderParametersKeyStoreCertSelector03()
            throws Exception {
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        keyTest.load(null, null);
        try {
            new PKIXBuilderParameters(keyTest, new X509CertSelector());
            fail("InvalidAlgorithmParameterException expected");
        } catch (InvalidAlgorithmParameterException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "PKIXBuilderParameters",
        args = {java.security.KeyStore.class, java.security.cert.CertSelector.class}
    )
    public final void testPKIXBuilderParametersKeyStoreCertSelector04()
            throws Exception {
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        keyTest.load(null, null);
        ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
                .getBytes());
        ByteArrayInputStream certArray2 = new ByteArrayInputStream(certificate2
                .getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setCertificateEntry("alias2", cert[0]);
        keyTest.setCertificateEntry("alias3", cert[1]);
        try {
            PKIXBuilderParameters p = new PKIXBuilderParameters(keyTest,
                    new X509CertSelector());
            assertEquals(3, p.getTrustAnchors().size());
            assertEquals(5, p.getMaxPathLength());
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxPathLength",
        args = {}
    )
    public final void testGetMaxPathLength() throws Exception {
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        keyTest.load(null, null);
        ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
                .getBytes());
        ByteArrayInputStream certArray2 = new ByteArrayInputStream(certificate2
                .getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setCertificateEntry("alias2", cert[0]);
        keyTest.setCertificateEntry("alias3", cert[1]);
        PKIXBuilderParameters p = new PKIXBuilderParameters(keyTest,
                new X509CertSelector());
        assertEquals(5, p.getMaxPathLength());
        p.setMaxPathLength(10);
        assertEquals(10, p.getMaxPathLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMaxPathLength",
        args = {int.class}
    )
    public final void testSetMaxPathLength() throws Exception {
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        keyTest.load(null, null);
        ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
                .getBytes());
        ByteArrayInputStream certArray2 = new ByteArrayInputStream(certificate2
                .getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setCertificateEntry("alias2", cert[0]);
        keyTest.setCertificateEntry("alias3", cert[1]);
        PKIXBuilderParameters p = new PKIXBuilderParameters(keyTest,
                new X509CertSelector());
        assertEquals(5, p.getMaxPathLength());
        p.setMaxPathLength(10);
        assertEquals(10, p.getMaxPathLength());
        p.setMaxPathLength(0);
        assertEquals(0, p.getMaxPathLength());
        p.setMaxPathLength(-1);
        assertEquals(-1, p.getMaxPathLength());
        int[] maxPathLength = {-2, -10, Integer.MIN_VALUE};
        for (int i = 0; i < maxPathLength.length; i++) {
            try {
                p.setMaxPathLength(maxPathLength[i]);
                fail("InvalidParameterException expected ");
            } catch (InvalidParameterException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString() throws Exception {
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        keyTest.load(null, null);
        ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
                .getBytes());
        ByteArrayInputStream certArray2 = new ByteArrayInputStream(certificate2
                .getBytes());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setCertificateEntry("alias2", cert[0]);
        keyTest.setCertificateEntry("alias3", cert[1]);
        PKIXBuilderParameters p = new PKIXBuilderParameters(keyTest,
                new X509CertSelector());
        assertNotNull(p.toString());
    }
}

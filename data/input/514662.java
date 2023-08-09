@TestTargetClass(KeyStore.class)
public class KeyStore2Test extends junit.framework.TestCase {
    static PrivateKey privateKey;
    static {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator
                    .getInstance("DSA");
            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(1024, secureRandom);
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            privateKey = keyPair.getPrivate();
        } catch (Exception e) {
            fail("initialization failed: " + e);
        }
    }
    final char[] pssWord = { 'a', 'b', 'c' };
    final byte[] testEncoding = new byte[] { (byte) 1, (byte) 2, (byte) 3,
            (byte) 4, (byte) 5 };
    private Provider support_TestProvider;
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
    ByteArrayInputStream certArray = new ByteArrayInputStream(certificate
            .getBytes());
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
    ByteArrayInputStream certArray2 = new ByteArrayInputStream(certificate2
            .getBytes());
    String certificate3 = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDXDCCAsWgAwIBAgIBSjANBgkqhkiG9w0BAQUFADBWMQswCQYDVQQGEwJVUzEY\n"
            + "MBYGA1UEChMPVS5TLiBHb3Zlcm5tZW50MQwwCgYDVQQLEwNEb0QxDDAKBgNVBAsT\n"
            + "A1BLSTERMA8GA1UEAxMITWVkIENBLTEwHhcNOTgwODAyMTgwMjQwWhcNMDEwODAy\n"
            + "MTgwMjQwWjB0MQswCQYDVQQGEwJVUzEYMBYGA1UEChMPVS5TLiBHb3Zlcm5tZW50\n"
            + "MQwwCgYDVQQLEwNEb0QxDDAKBgNVBAsTA1BLSTENMAsGA1UECxMEVVNBRjEgMB4G\n"
            + "A1UEAxMXR3VtYnkuSm9zZXBoLjAwMDAwMDUwNDQwgZ8wDQYJKoZIhvcNAQEBBQAD\n"
            + "gY0AMIGJAoGBALT/R7bPqs1c1YqXAg5HNpZLgW2HuAc7RCaP06cE4R44GBLw/fQc\n"
            + "VRNLn5pgbTXsDnjiZVd8qEgYqjKFQka4/tNhaF7No2tBZB+oYL/eP0IWtP+h/W6D\n"
            + "KR5+UvIIdgmx7k3t9jp2Q51JpHhhKEb9WN54trCO9Yu7PYU+LI85jEIBAgMBAAGj\n"
            + "ggEaMIIBFjAWBgNVHSAEDzANMAsGCWCGSAFlAgELAzAfBgNVHSMEGDAWgBQzOhTo\n"
            + "CWdhiGUkIOx5cELXppMe9jAdBgNVHQ4EFgQUkLBJl+ayKgzOp/wwBX9M1lSkCg4w\n"
            + "DgYDVR0PAQH/BAQDAgbAMAwGA1UdEwEB/wQCMAAwgZ0GA1UdHwSBlTCBkjCBj6CB\n"
            + "jKCBiYaBhmxkYXA6Ly9kcy0xLmNoYW1iLmRpc2EubWlsL2NuJTNkTWVkJTIwQ0El\n"
            + "MmQxJTJjb3UlM2RQS0klMmNvdSUzZERvRCUyY28lM2RVLlMuJTIwR292ZXJubWVu\n"
            + "dCUyY2MlM2RVUz9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0JTNiYmluYXJ5MA0G\n"
            + "CSqGSIb3DQEBBQUAA4GBAFjapuDHMvIdUeYRyEYdShBR1JZC20tJ3MQnyBQveddz\n"
            + "LGFDGpIkRAQU7T/5/ne8lMexyxViC21xOlK9LdbJCbVyywvb9uEm/1je9wieQQtr\n"
            + "kjykuB+WB6qTCIslAO/eUmgzfzIENvnH8O+fH7QTr2PdkFkiPIqBJYHvw7F3XDqy\n"
            + "-----END CERTIFICATE-----\n";
    ByteArrayInputStream certArray3 = new ByteArrayInputStream(certificate3
            .getBytes());
    private byte[] creatCertificate() throws Exception {
        ByteArrayOutputStream out = null;
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        keyTest.load(null, null);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setKeyEntry("alias2", privateKey, pssWord, cert);
        keyTest.setCertificateEntry("alias3", cert[1]);
        out = new ByteArrayOutputStream();
        keyTest.store(out, pssWord);
        out.close();
        return out.toByteArray();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "aliases",
        args = {}
    )
    public void test_aliases() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.aliases();
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setCertificateEntry("alias2", cert[0]);
        keyTest.setCertificateEntry("alias3", cert[0]);
        Enumeration<String> aliase = keyTest.aliases();
        Set<String> alia = new HashSet<String>();
        int i = 0;
        while (aliase.hasMoreElements()) {
            alia.add(aliase.nextElement());
            i++;
        }
        assertTrue("the alias names were returned wrong", i == 3
                && alia.contains("alias1") && alia.contains("alias2")
                && alia.contains("alias3"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsAlias",
        args = {java.lang.String.class}
    )
    public void test_containsAliasLjava_lang_String() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.containsAlias("alias1");
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setCertificateEntry("alias2", cert[0]);
        assertTrue("alias1 does not exist", keyTest.containsAlias("alias1"));
        assertTrue("alias2 does not exist", keyTest.containsAlias("alias2"));
        assertFalse("alias3 exists", keyTest.containsAlias("alias3"));
        try {
            keyTest.containsAlias(null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificate",
        args = {java.lang.String.class}
    )
    public void test_getCertificateLjava_lang_String() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.getCertificate("anAlias");
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        PublicKey pub = cert[0].getPublicKey();
        keyTest.setCertificateEntry("alias1", cert[0]);
        java.security.cert.Certificate certRes = keyTest
                .getCertificate("alias1");
        assertTrue("the public key of the certificate from getCertificate() "
                + "did not equal the original certificate", certRes
                .getPublicKey() == pub);
        keyTest.setCertificateEntry("alias2", cert[0]);
        java.security.cert.Certificate cert2 = keyTest.getCertificate("alias2");
        assertTrue("the certificate for alias2 is supposed to exist",
                cert2 != null && cert2.equals(cert[0]));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "KeyStoreException checking missed",
        method = "getCertificateAlias",
        args = {java.security.cert.Certificate.class}
    )
    public void test_getCertificateAliasLjava_security_cert_Certificate()
            throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        keyTest.load(null, null);
        keyTest.setCertificateEntry("alias1", cert[1]);
        String alias = keyTest.getCertificateAlias(cert[1]);
        assertTrue("certificate entry - the alias returned for this "
                + "certificate was wrong", alias.equals("alias1"));
        keyTest.setKeyEntry("alias2", privateKey, pssWord, cert);
        alias = keyTest.getCertificateAlias(cert[0]);
        assertTrue("key entry - the alias returned for this "
                + "certificate was wrong", alias.equals("alias2"));
        X509Certificate cert2 = (X509Certificate) cf
                .generateCertificate(certArray3);
        String aliasNull = keyTest.getCertificateAlias(cert2);
        assertNull("the alias returned for the nonexist certificate "
                + "was NOT null", aliasNull);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificateChain",
        args = {java.lang.String.class}
    )
    public void test_getCertificateChainLjava_lang_String() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.getCertificateChain("anAlias");
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setKeyEntry("alias2", privateKey, pssWord, cert);
        java.security.cert.Certificate[] certRes = keyTest
                .getCertificateChain("alias2");
        assertTrue("there are more than two certificate returned "
                + "from getCertificateChain", certRes.length == 2);
        assertTrue("the certificates returned from getCertificateChain "
                + "is not correct", cert[0].getPublicKey() == certRes[0]
                .getPublicKey()
                && cert[1].getPublicKey() == certRes[1].getPublicKey());
        java.security.cert.Certificate[] certResNull = keyTest
                .getCertificateChain("alias1");
        assertNull("the certificate chain returned from "
                + "getCertificateChain is NOT null", certResNull);
        try {
            keyTest.getCertificateChain(null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "KeyStoreException checking missed",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() throws Exception {
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        assertTrue("the method getInstance did not obtain "
                + "the correct type", keyTest.getType().equals(
                KeyStore.getDefaultType()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "KeyStoreException, NoSuchAlgorithmException checking missed",
        method = "getKey",
        args = {java.lang.String.class, char[].class}
    )
    public void test_getKeyLjava_lang_String$C() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        keyTest.load(null, null);
        keyTest.setKeyEntry("alias2", privateKey, pssWord, cert);
        PrivateKey returnedKey = (PrivateKey) keyTest.getKey("alias2", pssWord);
        byte[] retB = returnedKey.getEncoded();
        byte[] priB = privateKey.getEncoded();
        boolean equality = Arrays.equals(retB, priB);
        equality &= returnedKey.getAlgorithm()
                .equals(privateKey.getAlgorithm());
        equality &= returnedKey.getFormat().equals(privateKey.getFormat());
        assertTrue("the private key returned from getKey for a "
                + "key entry did not equal the original key", equality);
        try {
            keyTest.getKey("alias2", "wrong".toCharArray());
            fail("Should have thrown UnrecoverableKeyException");
        } catch (UnrecoverableKeyException e) {
        }
        keyTest.setCertificateEntry("alias1", cert[1]);
        assertNull("the private key returned from getKey for "
                + "a certificate entry is not null", keyTest.getKey("alias1",
                pssWord));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isCertificateEntry",
        args = {java.lang.String.class}
    )
    public void test_isCertificateEntryLjava_lang_String() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.isCertificateEntry("alias");
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setKeyEntry("alias2", privateKey, pssWord, cert);
        assertTrue("isCertificateEntry method returns false for a certificate",
                keyTest.isCertificateEntry("alias1"));
        assertFalse(
                "isCertificateEntry method returns true for noncertificate",
                keyTest.isCertificateEntry("alias2"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isKeyEntry",
        args = {java.lang.String.class}
    )
    public void test_isKeyEntryLjava_lang_String() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.isKeyEntry("alias");
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setKeyEntry("alias2", privateKey, pssWord, cert);
        assertTrue("isKeyEntry method returns false for a certificate", keyTest
                .isKeyEntry("alias2"));
        assertFalse("isKeyEntry method returns true for noncertificate",
                keyTest.isKeyEntry("alias1"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IOException, NoSuchAlgorithmException, CertificateException checking missed",
        method = "load",
        args = {java.io.InputStream.class, char[].class}
    )
    public void test_loadLjava_io_InputStream$C() throws Exception {
        byte[] keyStore = creatCertificate();
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream in = new ByteArrayInputStream(keyStore);
        keyTest.load(in, pssWord);
        in.close();
        assertTrue("alias1 is not a certificate", keyTest
                .isCertificateEntry("alias1"));
        assertTrue("alias2 is not a keyEntry", keyTest.isKeyEntry("alias2"));
        assertTrue("alias3 is not a certificate", keyTest
                .isCertificateEntry("alias3"));
        keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        in = new ByteArrayInputStream(keyStore);
        keyTest.load(in, null);
        in.close();
        assertTrue("alias1 is not a certificate", keyTest
                .isCertificateEntry("alias1"));
        assertTrue("alias2 is not a keyEntry", keyTest.isKeyEntry("alias2"));
        assertTrue("alias3 is not a certificate", keyTest
                .isCertificateEntry("alias3"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalArgumentException, IOException, NoSuchAlgorithmException, CertificateException and non null parameter checking missed",
        method = "load",
        args = {java.security.KeyStore.LoadStoreParameter.class}
    )
    public void test_loadLjava_security_KeyStoreLoadStoreParameter() {
        try {
            KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
            keyTest.load(null);
        } catch (Exception e ) {
            fail("Unexpected Exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCertificateEntry",
        args = {java.lang.String.class, java.security.cert.Certificate.class}
    )
    public void test_setCertificateEntryLjava_lang_StringLjava_security_cert_Certificate()
            throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf
                .generateCertificate(certArray);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.setCertificateEntry("alias", cert);
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        PublicKey pub = cert.getPublicKey();
        keyTest.setCertificateEntry("alias1", cert);
        assertTrue(
                "the entry specified by the alias alias1 is not a certificate",
                keyTest.isCertificateEntry("alias1"));
        java.security.cert.Certificate resultCert = keyTest
                .getCertificate("alias1");
        assertTrue(
                "the public key of the certificate from getCertificate() did not equal the original certificate",
                resultCert.getPublicKey() == pub);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setKeyEntry",
        args = {java.lang.String.class, java.security.Key.class, char[].class, java.security.cert.Certificate[].class}
    )
    public void test_setKeyEntryLjava_lang_StringLjava_security_Key$C$Ljava_security_cert_Certificate()
            throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.setKeyEntry("alias3", privateKey, pssWord, cert);
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        keyTest.setKeyEntry("alias3", privateKey, pssWord, cert);
        assertTrue("the entry specified by the alias alias3 is not a keyEntry",
                keyTest.isKeyEntry("alias3"));
        try {
            keyTest.setKeyEntry("alias4", privateKey, pssWord, new Certificate[] {});
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert[] = new X509Certificate[2];
        cert[0] = (X509Certificate) cf.generateCertificate(certArray);
        cert[1] = (X509Certificate) cf.generateCertificate(certArray2);
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.size();
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, null);
        keyTest.setCertificateEntry("alias1", cert[0]);
        keyTest.setKeyEntry("alias2", privateKey, pssWord, cert);
        keyTest.setCertificateEntry("alias3", cert[1]);
        assertEquals("the size of the keyStore is not 3", 3, keyTest.size());
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "deleteEntry",
            args = {java.lang.String.class}
    )
    @AndroidOnly("Spec says: throws KeyStoreException ... if the entry can not be deleted. RI goes against its own spec.")
    public void test_deleteEmptyEntry() {
        try {
            KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
            keyTest.load(null, null);
            keyTest.deleteEntry("");
            fail("Should throw KeyStoreException");
        } catch (KeyStoreException e) {
        } catch (Exception e) {
            fail("Unexpected Exception " + e);
        }
        try {
            KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
            keyTest.load(null, null);
            keyTest.deleteEntry("entry");
            fail("Should throw KeyStoreException");
        } catch (KeyStoreException e) {
        } catch (Exception e) {
            fail("Unexpected Exception " + e);
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "deleteEntry",
        args = {java.lang.String.class}
    )
    public void test_deleteEntry() {
        try {
            KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
            keyTest.load(null, null);
            keyTest.deleteEntry(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        } catch (Exception e) {
            fail("Unexpected Exception " + e);
        }
        try {
            KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
            keyTest.load(null, "password".toCharArray());
            KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(
                    pssWord);
            Certificate[] chain = { new MyCertificate("DSA", testEncoding),
                    new MyCertificate("DSA", testEncoding) };
            KeyStore.PrivateKeyEntry pkEntry = new KeyStore.PrivateKeyEntry(
                    privateKey, chain);
            keyTest.setEntry("symKey", pkEntry, pp);
            keyTest.deleteEntry("symKey");
        } catch (KeyStoreException e) {
            fail("Unexpected KeyStoreException " + e.getMessage());
        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            fail("Unexpected NoSuchAlgorithmException " + e.getMessage());
        } catch (CertificateException e) {
            fail("Unexpected CertificateException " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected Exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCreationDate",
        args = {java.lang.String.class}
    )
    public void test_getCreationDate() throws Exception {
        String type = "DSA";
        KeyStore keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyTest.getCreationDate("anAlias");
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyTest.load(null, pssWord);
        assertNull(keyTest.getCreationDate(""));
        try {
            keyTest.getCreationDate(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        Certificate[] chain = { new MyCertificate(type, testEncoding),
                new MyCertificate(type, testEncoding) };
        PrivateKey privateKey1 = KeyFactory.getInstance(type).generatePrivate(
                new DSAPrivateKeySpec(new BigInteger("0"), new BigInteger("0"),
                        new BigInteger("0"), new BigInteger("0")));
        KeyStore.PasswordProtection pp = new KeyStore.PasswordProtection(
                pssWord);
        KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry(privateKey,
                chain);
        KeyStore.PrivateKeyEntry pke1 = new KeyStore.PrivateKeyEntry(
                privateKey1, chain);
        keyTest.setEntry("alias1", pke, pp);
        keyTest.setEntry("alias2", pke1, pp);
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int dayExpected = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int monthExpected = Calendar.getInstance().get(Calendar.MONTH);
        int yearExpected = Calendar.getInstance().get(Calendar.YEAR);
        int hourExpected = Calendar.getInstance().get(Calendar.HOUR);
        int minuteExpected = Calendar.getInstance().get(Calendar.MINUTE);
        Calendar.getInstance().setTimeInMillis(
                keyTest.getCreationDate("alias1").getTime());
        int dayActual1 = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int monthActual1 = Calendar.getInstance().get(Calendar.MONTH);
        int yearActual1 = Calendar.getInstance().get(Calendar.YEAR);
        int hourActual1 = Calendar.getInstance().get(Calendar.HOUR);
        int minuteActual1 = Calendar.getInstance().get(Calendar.MINUTE);
        assertEquals(dayExpected, dayActual1);
        assertEquals(monthExpected, monthActual1);
        assertEquals(yearExpected, yearActual1);
        assertEquals(hourExpected, hourActual1);
        assertEquals(minuteExpected, minuteActual1);
        Calendar.getInstance().setTimeInMillis(
                keyTest.getCreationDate("alias2").getTime());
        int dayActual2 = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int monthActual2 = Calendar.getInstance().get(Calendar.MONTH);
        int yearActual2 = Calendar.getInstance().get(Calendar.YEAR);
        int hourActual2 = Calendar.getInstance().get(Calendar.HOUR);
        int minuteActual2 = Calendar.getInstance().get(Calendar.MINUTE);
        assertEquals(dayExpected, dayActual2);
        assertEquals(monthExpected, monthActual2);
        assertEquals(yearExpected, yearActual2);
        assertEquals(hourExpected, hourActual2);
        assertEquals(minuteExpected, minuteActual2);
        try {
            keyTest.getCreationDate(null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NoSuchAlgorithmException, UnrecoverableEntryException checking missed",
        method = "getEntry",
        args = {java.lang.String.class, java.security.KeyStore.ProtectionParameter.class}
    )
    public void test_getEntry() {
        String type = "DSA";
        KeyStore keyTest = null;
        KeyStore.PasswordProtection pp = null;
            try {
                keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            try {
                keyTest.getEntry("anAlias", new KeyStore.PasswordProtection(new char[] {}));
                fail("expected KeyStoreException");
            } catch (KeyStoreException e) {
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            }
            try {
                keyTest.load(null, pssWord);
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (CertificateException e) {
                fail("unexpected exception: " + e);
            } catch (IOException e) {
                fail("unexpected exception: " + e);
            }
            try {
                keyTest.getEntry(null, new KeyStore.PasswordProtection(new char[] {}));
                fail("expected NullPointerException");
            } catch (NullPointerException e) {
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            try {
                keyTest.getEntry("anAlias", null);
            } catch (NullPointerException e) {
                fail("unexpected exception: " + e);
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            try {
                keyTest.getEntry(null, null);
                fail("expected NullPointerException");
            } catch (NullPointerException e) {
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            try {
                assertNull(keyTest.getEntry("alias", pp));
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            Certificate[] chain = { new MyCertificate(type, testEncoding),
                    new MyCertificate(type, testEncoding) };
            DSAPrivateKey privateKey1 = null;
            try {
                privateKey1 = (DSAPrivateKey) KeyFactory.getInstance(type)
                        .generatePrivate(
                                new DSAPrivateKeySpec(new BigInteger("1"),
                                        new BigInteger("2"), new BigInteger("3"),
                                        new BigInteger("4")));
            } catch (InvalidKeySpecException e) {
                fail("unexpected exception: " + e);
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            }
            pp = new KeyStore.PasswordProtection(pssWord);
            try {
                assertNull(keyTest.getEntry("alias", pp));
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            KeyStore.PrivateKeyEntry pke1 = new KeyStore.PrivateKeyEntry(
                    privateKey, chain);
            KeyStore.PrivateKeyEntry pke2 = new KeyStore.PrivateKeyEntry(
                    privateKey1, chain);
            try {
                keyTest.setEntry("alias1", pke1, pp);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            try {
                keyTest.setEntry("alias2", pke2, pp);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            try {
                assertNull(keyTest.getEntry("alias", pp));
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            KeyStore.PrivateKeyEntry pkeActual1 = null;
            try {
                pkeActual1 = (KeyStore.PrivateKeyEntry) keyTest
                        .getEntry("alias1", pp);
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            KeyStore.PrivateKeyEntry pkeActual2 = null;
            try {
                pkeActual2 = (KeyStore.PrivateKeyEntry) keyTest
                        .getEntry("alias2", pp);
            } catch (NoSuchAlgorithmException e) {
                fail("unexpected exception: " + e);
            } catch (UnrecoverableEntryException e) {
                fail("unexpected exception: " + e);
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            assertTrue(Arrays.equals(chain, pkeActual1.getCertificateChain()));
            assertEquals(privateKey, pkeActual1.getPrivateKey());
            assertEquals(new MyCertificate(type, testEncoding), pkeActual1
                    .getCertificate());
            try {
                assertTrue(keyTest.entryInstanceOf("alias1",
                        KeyStore.PrivateKeyEntry.class));
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
            assertTrue(Arrays.equals(chain, pkeActual2.getCertificateChain()));
            DSAPrivateKey entryPrivateKey = (DSAPrivateKey) pkeActual2.getPrivateKey();
            assertEquals(privateKey1.getX(), entryPrivateKey.getX());
            assertEquals(privateKey1.getParams().getG(), entryPrivateKey.getParams().getG());
            assertEquals(privateKey1.getParams().getP(), entryPrivateKey.getParams().getP());
            assertEquals(privateKey1.getParams().getQ(), entryPrivateKey.getParams().getQ());
            assertEquals(new MyCertificate(type, testEncoding), pkeActual2
                    .getCertificate());
            try {
                assertTrue(keyTest.entryInstanceOf("alias2",
                        KeyStore.PrivateKeyEntry.class));
            } catch (KeyStoreException e) {
                fail("unexpected exception: " + e);
            }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setEntry",
        args = {java.lang.String.class, java.security.KeyStore.Entry.class, java.security.KeyStore.ProtectionParameter.class}
    )
    public void test_setEntry() {
        String type = "DSA";
        KeyStore keyTest = null;
        KeyStore.PasswordProtection pp = null;
        try {
            keyTest = KeyStore.getInstance(KeyStore.getDefaultType());
            keyTest.load(null, pssWord);
            Certificate[] chain = { new MyCertificate(type, testEncoding),
                    new MyCertificate(type, testEncoding) };
            DSAPrivateKey privateKey1 = (DSAPrivateKey) KeyFactory.getInstance(type)
                    .generatePrivate(
                            new DSAPrivateKeySpec(new BigInteger("1"),
                                    new BigInteger("2"), new BigInteger("3"),
                                    new BigInteger("4")));
            pp = new KeyStore.PasswordProtection(pssWord);
            KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry(
                    privateKey, chain);
            KeyStore.PrivateKeyEntry pke1 = new KeyStore.PrivateKeyEntry(
                    privateKey1, chain);
            try {
                keyTest.setEntry("alias", pke, null);
                fail("Exception expected");
            } catch (Exception e) {
            }
            keyTest.setEntry("alias", pke, pp);
            KeyStore.PrivateKeyEntry pkeActual = (KeyStore.PrivateKeyEntry) keyTest
                    .getEntry("alias", pp);
            assertTrue(Arrays.equals(chain, pkeActual.getCertificateChain()));
            assertEquals(privateKey, pkeActual.getPrivateKey());
            assertEquals(new MyCertificate(type, testEncoding), pkeActual
                    .getCertificate());
            assertTrue(keyTest.entryInstanceOf("alias",
                    KeyStore.PrivateKeyEntry.class));
            keyTest.setEntry("alias", pke1, pp);
            pkeActual = (KeyStore.PrivateKeyEntry) keyTest
                    .getEntry("alias", pp);
            assertTrue(Arrays.equals(chain, pkeActual.getCertificateChain()));
            DSAPrivateKey actualPrivateKey = (DSAPrivateKey) pkeActual.getPrivateKey();
            assertEquals(privateKey1.getX(), actualPrivateKey.getX());
            assertEquals(privateKey1.getParams().getG(), actualPrivateKey.getParams().getG());
            assertEquals(privateKey1.getParams().getP(), actualPrivateKey.getParams().getP());
            assertEquals(privateKey1.getParams().getQ(), actualPrivateKey.getParams().getQ());
            assertEquals(new MyCertificate(type, testEncoding), pkeActual
                    .getCertificate());
            assertTrue(keyTest.entryInstanceOf("alias",
                    KeyStore.PrivateKeyEntry.class));
            keyTest.setEntry("alias2", pke1, pp);
            pkeActual = (KeyStore.PrivateKeyEntry) keyTest.getEntry("alias2",
                    pp);
            assertTrue(Arrays.equals(chain, pkeActual.getCertificateChain()));
            actualPrivateKey = (DSAPrivateKey) pkeActual.getPrivateKey();
            assertEquals(privateKey1.getX(), actualPrivateKey.getX());
            assertEquals(privateKey1.getParams().getG(), actualPrivateKey.getParams().getG());
            assertEquals(privateKey1.getParams().getP(), actualPrivateKey.getParams().getP());
            assertEquals(privateKey1.getParams().getQ(), actualPrivateKey.getParams().getQ());
            assertEquals(new MyCertificate(type, testEncoding), pkeActual
                    .getCertificate());
            assertTrue(keyTest.entryInstanceOf("alias2",
                    KeyStore.PrivateKeyEntry.class));
        } catch (Exception e) {
            fail("Unexpected Exception " + e);
        }
        try {
            keyTest.setEntry(null, null, null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "entryInstanceOf",
        args = {java.lang.String.class, java.lang.Class.class}
    )
    public void test_entryInstanceOf() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyStore.entryInstanceOf("anAlias", KeyStore.SecretKeyEntry.class);
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyStore.load(null, "pwd".toCharArray());
        String alias = "alias";
        Certificate[] chain = { new MyCertificate("DSA", testEncoding),
                new MyCertificate("DSA", testEncoding) };
        keyStore.setKeyEntry(alias, privateKey, "pwd".toCharArray(), chain);
        assertTrue(keyStore.entryInstanceOf(alias,
                KeyStore.PrivateKeyEntry.class));
        assertFalse(keyStore.entryInstanceOf(alias,
                KeyStore.SecretKeyEntry.class));
        assertFalse(keyStore.entryInstanceOf(alias,
                KeyStore.TrustedCertificateEntry.class));
        try {
            keyStore.entryInstanceOf(null, KeyStore.SecretKeyEntry.class);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            keyStore.entryInstanceOf("anAlias", null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalArgumentException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException checking missed",
        method = "store",
        args = {java.security.KeyStore.LoadStoreParameter.class}
    )
    public void test_store_java_securityKeyStore_LoadStoreParameter()
            throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, "pwd".toCharArray());
        try {
            keyStore.store(null);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IOException, NoSuchAlgorithmException, CertificateException checking missed",
        method = "store",
        args = {java.io.OutputStream.class, char[].class}
    )
    public void test_store_java_io_OutputStream_char() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            keyStore.store(new ByteArrayOutputStream(), "pwd".toCharArray());
            fail("expected KeyStoreException");
        } catch (KeyStoreException e) {
        }
        keyStore.load(null, "pwd".toCharArray());
        try {
            keyStore.store(null, "pwd".toCharArray());
            fail("NullPointerException or IOException expected");
        } catch (NullPointerException e) {
        } catch (IOException e) {
        } catch (Exception e) {
            fail("Unexpected Exception " + e);
        }
    }
    protected void setUp() throws Exception {
        super.setUp();
        support_TestProvider = new Support_TestProvider();
        Security.addProvider(support_TestProvider);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(support_TestProvider.getName());
    }
    @SuppressWarnings("unused")
    class MyCertificate extends Certificate {
        private final byte[] encoding;
        public MyCertificate(String type, byte[] encoding) {
            super(type);
            this.encoding = encoding;
        }
        public byte[] getEncoded() throws CertificateEncodingException {
            return encoding.clone();
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
            return "[My test Certificate, type: " + getType() + "]";
        }
        public PublicKey getPublicKey() {
            return new PublicKey() {
                public String getAlgorithm() {
                    return "DSA";
                }
                public byte[] getEncoded() {
                    return new byte[] { (byte) 1, (byte) 2, (byte) 3 };
                }
                public String getFormat() {
                    return "TEST_FORMAT";
                }
            };
        }
    }
}

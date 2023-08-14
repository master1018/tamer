@TestTargetClass(X509CertSelector.class)
public class X509CertSelectorTest extends TestCase {
    byte[][] constraintBytes = new byte[][] {
            {
                    48, 34, -96, 15, 48, 13, -127, 8, 56, 50, 50, 46, 78,
                    97, 109, 101, -128, 1, 0, -95, 15, 48, 13, -127, 8, 56,
                    50, 50, 46, 78, 97, 109, 101, -128, 1, 0},
            {
                    48, 42, -96, 19, 48, 17, -127, 12, 114, 102, 99, 64,
                    56, 50, 50, 46, 78, 97, 109, 101, -128, 1, 0, -95, 19,
                    48, 17, -127, 12, 114, 102, 99, 64, 56, 50, 50, 46, 78,
                    97, 109, 101, -128, 1, 0},
            {
                    48, 34, -96, 15, 48, 13, -126, 8, 78, 97, 109, 101, 46,
                    111, 114, 103, -128, 1, 0, -95, 15, 48, 13, -126, 8,
                    78, 97, 109, 101, 46, 111, 114, 103, -128, 1, 0},
            {
                    48, 42, -96, 19, 48, 17, -126, 12, 100, 78, 83, 46, 78,
                    97, 109, 101, 46, 111, 114, 103, -128, 1, 0, -95, 19,
                    48, 17, -126, 12, 100, 78, 83, 46, 78, 97, 109, 101,
                    46, 111, 114, 103, -128, 1, 0},
            {
                    48, 54, -96, 25, 48, 23, -122, 18, 104, 116, 116, 112,
                    58, 47, 47, 82, 101, 115, 111, 117, 114, 99, 101, 46,
                    73, 100, -128, 1, 0, -95, 25, 48, 23, -122, 18, 104,
                    116, 116, 112, 58, 47, 47, 82, 101, 115, 111, 117, 114,
                    99, 101, 46, 73, 100, -128, 1, 0},
            {
                    48, 70, -96, 33, 48, 31, -122, 26, 104, 116, 116, 112,
                    58, 47, 47, 117, 110, 105, 102, 111, 114, 109, 46, 82,
                    101, 115, 111, 117, 114, 99, 101, 46, 73, 100, -128, 1,
                    0, -95, 33, 48, 31, -122, 26, 104, 116, 116, 112, 58,
                    47, 47, 117, 110, 105, 102, 111, 114, 109, 46, 82, 101,
                    115, 111, 117, 114, 99, 101, 46, 73, 100, -128, 1, 0},
            {
                    48, 26, -96, 11, 48, 9, -121, 4, 1, 1, 1, 1, -128, 1,
                    0, -95, 11, 48, 9, -121, 4, 1, 1, 1, 1, -128, 1, 0},
            {
                    48, 50, -96, 23, 48, 21, -121, 16, 1, 1, 1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1, 1, 1, 1, 1, -128, 1, 0, -95, 23, 48, 21,
                    -121, 16, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                    1, -128, 1, 0}};
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IOException checking missed",
        method = "addSubjectAlternativeName",
        args = {int.class, byte[].class}
    )
    public void test_addSubjectAlternativeNameLintLbyte_array() throws IOException {
        int[] types = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
        for (int i = 0; i < types.length; i++) {
            try {
                new X509CertSelector().addSubjectAlternativeName(types[i],
                        (byte[]) null);
                fail("No expected NullPointerException for type: " + i);
            } catch (NullPointerException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies IOException.",
        method = "addSubjectAlternativeName",
        args = {int.class, java.lang.String.class}
    )
    public void test_addSubjectAlternativeNameLintLjava_lang_String() {
        int[] types = { 0, 2, 3, 4, 5, 6, 7, 8 };
        for (int i = 0; i < types.length; i++) {
            try {
                new X509CertSelector().addSubjectAlternativeName(types[i],
                        "0xDFRF");
                fail("IOException expected");
            } catch (IOException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies NullPointerException.",
        method = "addPathToName",
        args = {int.class, byte[].class}
    )
    public void test_addPathToNameLintLbyte_array() throws IOException {
        int[] types = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
        for (int i = 0; i < types.length; i++) {
            try {
                new X509CertSelector().addPathToName(types[i], (byte[]) null);
                fail("No expected NullPointerException for type: " + i);
            } catch (NullPointerException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies IOException.",
        method = "addPathToName",
        args = {int.class, java.lang.String.class}
    )
    public void test_addPathToNameLintLjava_lang_String() {
        for (int type = 0; type <= 8; type++) {
            try {
                new X509CertSelector().addPathToName(type, (String) null);
                fail("IOException expected!");
            } catch (IOException ioe) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X509CertSelector",
        args = {}
    )
    public void test_X509CertSelector() {
        X509CertSelector selector = null;
        try {
            selector = new X509CertSelector();
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        assertEquals(-1, selector.getBasicConstraints());
        assertTrue(selector.getMatchAllSubjectAltNames());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() throws Exception {
        X509CertSelector selector = new X509CertSelector();
        X509CertSelector selector1 = (X509CertSelector) selector.clone();
        assertEquals(selector.getMatchAllSubjectAltNames(), selector1
                .getMatchAllSubjectAltNames());
        assertEquals(selector.getAuthorityKeyIdentifier(), selector1
                .getAuthorityKeyIdentifier());
        assertEquals(selector.getBasicConstraints(), selector1
                .getBasicConstraints());
        assertEquals(selector.getCertificate(), selector1.getCertificate());
        assertEquals(selector.getCertificateValid(), selector1
                .getCertificateValid());
        assertEquals(selector.getExtendedKeyUsage(), selector1
                .getExtendedKeyUsage());
        assertEquals(selector.getIssuer(), selector1.getIssuer());
        assertEquals(selector.getIssuerAsBytes(), selector1.getIssuerAsBytes());
        assertEquals(selector.getIssuerAsString(), selector1
                .getIssuerAsString());
        assertEquals(selector.getKeyUsage(), selector1.getKeyUsage());
        assertEquals(selector.getNameConstraints(), selector1
                .getNameConstraints());
        assertEquals(selector.getPathToNames(), selector1.getPathToNames());
        assertEquals(selector.getPolicy(), selector1.getPolicy());
        assertEquals(selector.getPrivateKeyValid(), selector1
                .getPrivateKeyValid());
        assertEquals(selector.getSerialNumber(), selector1.getSerialNumber());
        assertEquals(selector.getSubject(), selector1.getSubject());
        assertEquals(selector.getSubjectAlternativeNames(), selector1
                .getSubjectAlternativeNames());
        assertEquals(selector.getSubjectAsBytes(), selector1
                .getSubjectAsBytes());
        assertEquals(selector.getSubjectAsString(), selector1
                .getSubjectAsString());
        assertEquals(selector.getSubjectKeyIdentifier(), selector1
                .getSubjectKeyIdentifier());
        assertEquals(selector.getSubjectPublicKey(), selector1
                .getSubjectPublicKey());
        assertEquals(selector.getSubjectPublicKeyAlgID(), selector1
                .getSubjectPublicKeyAlgID());
        selector = null;
        try {
            selector.clone();
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAuthorityKeyIdentifier",
        args = {}
    )
    public void test_getAuthorityKeyIdentifier() {
        byte[] akid1 = new byte[] { 4, 5, 1, 2, 3, 4, 5 }; 
        byte[] akid2 = new byte[] { 4, 5, 5, 4, 3, 2, 1 }; 
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector
                .getAuthorityKeyIdentifier());
        selector.setAuthorityKeyIdentifier(akid1);
        assertTrue("The returned keyID should be equal to specified", Arrays
                .equals(akid1, selector.getAuthorityKeyIdentifier()));
        assertTrue("The returned keyID should be equal to specified", Arrays
                .equals(akid1, selector.getAuthorityKeyIdentifier()));
        assertFalse("The returned keyID should differ", Arrays.equals(akid2,
                selector.getAuthorityKeyIdentifier()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getBasicConstraints",
        args = {}
    )
    public void test_getBasicConstraints() {
        X509CertSelector selector = new X509CertSelector();
        int[] validValues = { 2, 1, 0, 1, 2, 3, 10, 20 };
        for (int i = 0; i < validValues.length; i++) {
            selector.setBasicConstraints(validValues[i]);
            assertEquals(validValues[i], selector.getBasicConstraints());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificate",
        args = {}
    )
    public void test_getCertificate() throws CertificateException {
        X509CertSelector selector = new X509CertSelector();
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate cert1 = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
        X509Certificate cert2 = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v1()));
        selector.setCertificate(cert1);
        assertEquals(cert1, selector.getCertificate());
        selector.setCertificate(cert2);
        assertEquals(cert2, selector.getCertificate());
        selector.setCertificate(null);
        assertNull(selector.getCertificate());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificateValid",
        args = {}
    )
    public void test_getCertificateValid() {
        Date date1 = new Date(100);
        Date date2 = new Date(200);
        Date date3 = Calendar.getInstance().getTime();
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector
                .getCertificateValid());
        selector.setCertificateValid(date1);
        assertTrue("The returned date should be equal to specified", date1
                .equals(selector.getCertificateValid()));
        selector.getCertificateValid().setTime(200);
        assertTrue("The returned date should be equal to specified", date1
                .equals(selector.getCertificateValid()));
        assertFalse("The returned date should differ", date2.equals(selector
                .getCertificateValid()));
        selector.setCertificateValid(date3);
        assertTrue("The returned date should be equal to specified", date3
                .equals(selector.getCertificateValid()));
        selector.setCertificateValid(null);
        assertNull(selector.getCertificateValid());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getExtendedKeyUsage",
        args = {}
    )
    public void test_getExtendedKeyUsage() {
        HashSet<String> ku = new HashSet<String>(Arrays
                .asList(new String[] { "1.3.6.1.5.5.7.3.1",
                        "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3",
                        "1.3.6.1.5.5.7.3.4", "1.3.6.1.5.5.7.3.8",
                        "1.3.6.1.5.5.7.3.9", "1.3.6.1.5.5.7.3.5",
                        "1.3.6.1.5.5.7.3.6", "1.3.6.1.5.5.7.3.7" }));
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector
                .getExtendedKeyUsage());
        try {
            selector.setExtendedKeyUsage(ku);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue(
                "The returned extendedKeyUsage should be equal to specified",
                ku.equals(selector.getExtendedKeyUsage()));
        try {
            selector.getExtendedKeyUsage().add("KRIBLEGRABLI");
            fail("The returned Set should be immutable.");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIssuer",
        args = {}
    )
    public void test_getIssuer() {
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector.getIssuer());
        selector.setIssuer(iss1);
        assertEquals("The returned issuer should be equal to specified", iss1,
                selector.getIssuer());
        assertFalse("The returned issuer should differ", iss2.equals(selector
                .getIssuer()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIssuerAsBytes",
        args = {}
    )
    public void test_getIssuerAsBytes() {
        byte[] name1 = new byte[]
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] name2 = new byte[]
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        X500Principal iss1 = new X500Principal(name1);
        X500Principal iss2 = new X500Principal(name2);
        X509CertSelector selector = new X509CertSelector();
        try {
            assertNull("Selector should return null", selector
                    .getIssuerAsBytes());
            selector.setIssuer(iss1);
            assertTrue("The returned issuer should be equal to specified",
                    Arrays.equals(name1, selector.getIssuerAsBytes()));
            assertFalse("The returned issuer should differ", name2
                    .equals(selector.getIssuerAsBytes()));
            selector.setIssuer(iss2);
            assertTrue("The returned issuer should be equal to specified",
                    Arrays.equals(name2, selector.getIssuerAsBytes()));
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIssuerAsString",
        args = {}
    )
    public void test_getIssuerAsString() {
        String name1 = "O=First Org.";
        String name2 = "O=Second Org.";
        X500Principal iss1 = new X500Principal(name1);
        X500Principal iss2 = new X500Principal(name2);
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector.getIssuerAsString());
        selector.setIssuer(iss1);
        assertEquals("The returned issuer should be equal to specified", name1,
                selector.getIssuerAsString());
        assertFalse("The returned issuer should differ", name2.equals(selector
                .getIssuerAsString()));
        selector.setIssuer(iss2);
        assertEquals("The returned issuer should be equal to specified", name2,
                selector.getIssuerAsString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKeyUsage",
        args = {}
    )
    public void test_getKeyUsage() {
        boolean[] ku = new boolean[] { true, false, true, false, true, false,
                true, false, true };
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector.getKeyUsage());
        selector.setKeyUsage(ku);
        assertTrue("The returned date should be equal to specified", Arrays
                .equals(ku, selector.getKeyUsage()));
        boolean[] result = selector.getKeyUsage();
        result[0] = !result[0];
        assertTrue("The returned keyUsage should be equal to specified", Arrays
                .equals(ku, selector.getKeyUsage()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMatchAllSubjectAltNames",
        args = {}
    )
    public void test_getMatchAllSubjectAltNames() {
        X509CertSelector selector = new X509CertSelector();
        assertTrue("The matchAllNames initially should be true", selector
                .getMatchAllSubjectAltNames());
        selector.setMatchAllSubjectAltNames(false);
        assertFalse("The value should be false", selector
                .getMatchAllSubjectAltNames());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getNameConstraints",
        args = {}
    )
    public void test_getNameConstraints() throws IOException {
        X509CertSelector selector = new X509CertSelector();
        for (int i = 0; i < constraintBytes.length; i++) {
            selector.setNameConstraints(constraintBytes[i]);
            assertTrue(Arrays.equals(constraintBytes[i], selector
                    .getNameConstraints()));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPathToNames",
        args = {}
    )
    public void test_getPathToNames() {
        try {
            GeneralName san0 = new GeneralName(new OtherName("1.2.3.4.5",
                    new byte[] { 1, 2, 0, 1 }));
            GeneralName san1 = new GeneralName(1, "rfc@822.Name");
            GeneralName san2 = new GeneralName(2, "dNSName");
            GeneralName san3 = new GeneralName(new ORAddress());
            GeneralName san4 = new GeneralName(new Name("O=Organization"));
            GeneralName san6 = new GeneralName(6, "http:
            GeneralName san7 = new GeneralName(7, "1.1.1.1");
            GeneralName san8 = new GeneralName(8, "1.2.3.4444.55555");
            GeneralNames sans1 = new GeneralNames();
            sans1.addName(san0);
            sans1.addName(san1);
            sans1.addName(san2);
            sans1.addName(san3);
            sans1.addName(san4);
            sans1.addName(san6);
            sans1.addName(san7);
            sans1.addName(san8);
            GeneralNames sans2 = new GeneralNames();
            sans2.addName(san0);
            TestCert cert1 = new TestCert(sans1);
            TestCert cert2 = new TestCert(sans2);
            X509CertSelector selector = new X509CertSelector();
            selector.setMatchAllSubjectAltNames(true);
            selector.setPathToNames(null);
            assertTrue("Any certificate should match in the case of null "
                    + "subjectAlternativeNames criteria.", selector
                    .match(cert1)
                    && selector.match(cert2));
            Collection<List<?>> sans = sans1.getPairsList();
            selector.setPathToNames(sans);
            Collection<List<?>> col = selector.getPathToNames();
            Iterator<List<?>> i = col.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof List)) {
                    fail("expected a List");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPolicy",
        args = {}
    )
    public void test_getPolicy() throws IOException {
        String[] policies1 = new String[] { "1.3.6.1.5.5.7.3.1",
                "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3", "1.3.6.1.5.5.7.3.4",
                "1.3.6.1.5.5.7.3.8", "1.3.6.1.5.5.7.3.9", "1.3.6.1.5.5.7.3.5",
                "1.3.6.1.5.5.7.3.6", "1.3.6.1.5.5.7.3.7" };
        String[] policies2 = new String[] { "1.3.6.7.3.1" };
        HashSet<String> p1 = new HashSet<String>(Arrays.asList(policies1));
        HashSet<String> p2 = new HashSet<String>(Arrays.asList(policies2));
        X509CertSelector selector = new X509CertSelector();
        selector.setPolicy(null);
        assertNull(selector.getPolicy());
        selector.setPolicy(p1);
        assertEquals("The returned date should be equal to specified", p1, selector.getPolicy());
        selector.setPolicy(p2);
        assertEquals("The returned date should be equal to specified", p2, selector.getPolicy());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrivateKeyValid",
        args = {}
    )
    public void test_getPrivateKeyValid() {
        Date date1 = new Date(100);
        Date date2 = new Date(200);
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector.getPrivateKeyValid());
        selector.setPrivateKeyValid(date1);
        assertTrue("The returned date should be equal to specified", date1
                .equals(selector.getPrivateKeyValid()));
        selector.getPrivateKeyValid().setTime(200);
        assertTrue("The returned date should be equal to specified", date1
                .equals(selector.getPrivateKeyValid()));
        assertFalse("The returned date should differ", date2.equals(selector
                .getPrivateKeyValid()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSerialNumber",
        args = {}
    )
    public void test_getSerialNumber() {
        BigInteger ser1 = new BigInteger("10000");
        BigInteger ser2 = new BigInteger("10001");
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector.getSerialNumber());
        selector.setSerialNumber(ser1);
        assertEquals("The returned serial number should be equal to specified",
                ser1, selector.getSerialNumber());
        assertFalse("The returned serial number should differ", ser2
                .equals(selector.getSerialNumber()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubject",
        args = {}
    )
    public void test_getSubject() {
        X500Principal sub1 = new X500Principal("O=First Org.");
        X500Principal sub2 = new X500Principal("O=Second Org.");
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector.getSubject());
        selector.setSubject(sub1);
        assertEquals("The returned subject should be equal to specified", sub1,
                selector.getSubject());
        assertFalse("The returned subject should differ", sub2.equals(selector
                .getSubject()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectAlternativeNames",
        args = {}
    )
    public void test_getSubjectAlternativeNames() {
        try {
            GeneralName san1 = new GeneralName(1, "rfc@822.Name");
            GeneralName san2 = new GeneralName(2, "dNSName");
            GeneralNames sans = new GeneralNames();
            sans.addName(san1);
            sans.addName(san2);
            TestCert cert_1 = new TestCert(sans);
            X509CertSelector selector = new X509CertSelector();
            assertNull("Selector should return null", selector
                    .getSubjectAlternativeNames());
            selector.setSubjectAlternativeNames(sans.getPairsList());
            assertTrue("The certificate should match the selection criteria.",
                    selector.match(cert_1));
            selector.getSubjectAlternativeNames().clear();
            assertTrue("The modification of initialization object "
                    + "should not affect the modification "
                    + "of internal object.", selector.match(cert_1));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectAsBytes",
        args = {}
    )
    public void test_getSubjectAsBytes() {
        byte[] name1 = new byte[]
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] name2 = new byte[]
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        X500Principal sub1 = new X500Principal(name1);
        X500Principal sub2 = new X500Principal(name2);
        X509CertSelector selector = new X509CertSelector();
        try {
            assertNull("Selector should return null", selector
                    .getSubjectAsBytes());
            selector.setSubject(sub1);
            assertTrue("The returned issuer should be equal to specified",
                    Arrays.equals(name1, selector.getSubjectAsBytes()));
            assertFalse("The returned issuer should differ", name2
                    .equals(selector.getSubjectAsBytes()));
            selector.setSubject(sub2);
            assertTrue("The returned issuer should be equal to specified",
                    Arrays.equals(name2, selector.getSubjectAsBytes()));
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectAsString",
        args = {}
    )
    public void test_getSubjectAsString() {
        String name1 = "O=First Org.";
        String name2 = "O=Second Org.";
        X500Principal sub1 = new X500Principal(name1);
        X500Principal sub2 = new X500Principal(name2);
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector.getSubjectAsString());
        selector.setSubject(sub1);
        assertEquals("The returned subject should be equal to specified",
                name1, selector.getSubjectAsString());
        assertFalse("The returned subject should differ", name2.equals(selector
                .getSubjectAsString()));
        selector.setSubject(sub2);
        assertEquals("The returned subject should be equal to specified",
                name2, selector.getSubjectAsString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectKeyIdentifier",
        args = {}
    )
    public void test_getSubjectKeyIdentifier() {
        byte[] skid1 = new byte[] { 1, 2, 3, 4, 5 }; 
        byte[] skid2 = new byte[] { 4, 5, 5, 4, 3, 2, 1 }; 
        X509CertSelector selector = new X509CertSelector();
        assertNull("Selector should return null", selector
                .getSubjectKeyIdentifier());
        selector.setSubjectKeyIdentifier(skid1);
        assertTrue("The returned keyID should be equal to specified", Arrays
                .equals(skid1, selector.getSubjectKeyIdentifier()));
        selector.getSubjectKeyIdentifier()[0]++;
        assertTrue("The returned keyID should be equal to specified", Arrays
                .equals(skid1, selector.getSubjectKeyIdentifier()));
        assertFalse("The returned keyID should differ", Arrays.equals(skid2,
                selector.getSubjectKeyIdentifier()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectPublicKey",
        args = {}
    )
    public void test_getSubjectPublicKey() throws Exception {
        byte[] enc = { 0x30, 0x0E, 
                0x30, 0x07, 
                0x06, 0x02, 0x03, 0x05,
                0x01, 0x01, 0x07, 
                0x03, 0x03, 0x01, 0x01, 0x06, 
        };
        X509CertSelector selector = new X509CertSelector();
        selector.setSubjectPublicKey(enc);
        PublicKey key = selector.getSubjectPublicKey();
        assertEquals("0.3.5", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());
        assertTrue(Arrays.equals(enc, key.getEncoded()));
        assertNotNull(key.toString());
        key = new MyPublicKey();
        selector.setSubjectPublicKey(key);
        PublicKey keyActual = selector.getSubjectPublicKey();
        assertEquals(key, keyActual);
        assertEquals(key.getAlgorithm(), keyActual.getAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectPublicKeyAlgID",
        args = {}
    )
    public void test_getSubjectPublicKeyAlgID() {
        X509CertSelector selector = new X509CertSelector();
        String[] validOIDs = { "0.0.20", "1.25.0", "2.0.39", "0.2.10", "1.35.15",
                "2.17.89" };
        assertNull("Selector should return null", selector
                .getSubjectPublicKeyAlgID());
        for (int i = 0; i < validOIDs.length; i++) {
            try {
                selector.setSubjectPublicKeyAlgID(validOIDs[i]);
                assertEquals(validOIDs[i], selector.getSubjectPublicKeyAlgID());
            } catch (IOException e) {
                System.out.println("t = " + e.getMessage());
            }
        }
        String pkaid1 = "1.2.840.113549.1.1.1"; 
        String pkaid2 = "1.2.840.113549.1.1.4"; 
        try {
            selector.setSubjectPublicKeyAlgID(pkaid1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The returned oid should be equal to specified", pkaid1
                .equals(selector.getSubjectPublicKeyAlgID()));
        assertFalse("The returned oid should differ", pkaid2.equals(selector
                .getSubjectPublicKeyAlgID()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "match",
        args = {java.security.cert.Certificate.class}
    )
    public void test_matchLjava_security_cert_Certificate()
            throws CertificateException {
        X509CertSelector selector = new X509CertSelector();
        assertFalse(selector.match(null));
        CertificateFactory certFact = CertificateFactory.getInstance("X509");
        X509Certificate cert1 = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
        X509Certificate cert2 = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v1()));
        selector.setCertificate(cert1);
        assertTrue(selector.match(cert1));
        assertFalse(selector.match(cert2));
        selector.setCertificate(cert2);
        assertFalse(selector.match(cert1));
        assertTrue(selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setAuthorityKeyIdentifier",
        args = {byte[].class}
    )
    public void test_setAuthorityKeyIdentifierLB$() throws CertificateException {
        X509CertSelector selector = new X509CertSelector();
        byte[] akid1 = new byte[] { 1, 2, 3, 4, 5 }; 
        byte[] akid2 = new byte[] { 5, 4, 3, 2, 1 }; 
        TestCert cert1 = new TestCert(akid1);
        TestCert cert2 = new TestCert(akid2);
        selector.setAuthorityKeyIdentifier(null);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
        assertNull(selector.getAuthorityKeyIdentifier());
        selector.setAuthorityKeyIdentifier(akid1);
        assertTrue("The certificate should not match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setAuthorityKeyIdentifier(akid2);
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert1));
        assertTrue("The certificate should not match the selection criteria.",
                selector.match(cert2));
        akid2[0]++;
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setBasicConstraints",
        args = {int.class}
    )
    public void test_setBasicConstraintsLint() {
        X509CertSelector selector = new X509CertSelector();
        int[] invalidValues = { -3, -4, -5, 1000000000 };
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                selector.setBasicConstraints(-3);
                fail("IllegalArgumentException expected");
            } catch (IllegalArgumentException e) {
            }
        }
        int[] validValues = { -2, -1, 0, 1, 2, 3, 10, 20 };
        for (int i = 0; i < validValues.length; i++) {
            selector.setBasicConstraints(validValues[i]);
            assertEquals(validValues[i], selector.getBasicConstraints());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCertificate",
        args = {java.security.cert.X509Certificate.class}
    )
    public void test_setCertificateLjava_security_cert_X509Certificate()
            throws CertificateException {
        TestCert cert1 = new TestCert("same certificate");
        TestCert cert2 = new TestCert("other certificate");
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(null);
        assertTrue("Any certificates should match in the case of null "
                + "certificateEquals criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setCertificate(cert1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setCertificate(cert2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
        selector.setCertificate(null);
        assertNull(selector.getCertificate());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCertificateValid",
        args = {java.util.Date.class}
    )
    public void test_setCertificateValidLjava_util_Date()
            throws CertificateException {
        X509CertSelector selector = new X509CertSelector();
        Date date1 = new Date(100);
        Date date2 = new Date(200);
        TestCert cert1 = new TestCert(date1);
        TestCert cert2 = new TestCert(date2);
        selector.setCertificateValid(null);
        assertNull(selector.getCertificateValid());
        selector.setCertificateValid(date1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setCertificateValid(date2);
        date2.setTime(300);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setExtendedKeyUsage",
        args = {java.util.Set.class}
    )
    public void test_setExtendedKeyUsageLjava_util_Set()
            throws CertificateException {
        HashSet<String> ku1 = new HashSet<String>(Arrays
                .asList(new String[] { "1.3.6.1.5.5.7.3.1",
                        "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3",
                        "1.3.6.1.5.5.7.3.4", "1.3.6.1.5.5.7.3.8",
                        "1.3.6.1.5.5.7.3.9", "1.3.6.1.5.5.7.3.5",
                        "1.3.6.1.5.5.7.3.6", "1.3.6.1.5.5.7.3.7" }));
        HashSet<String> ku2 = new HashSet<String>(Arrays.asList(new String[] {
                "1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3",
                "1.3.6.1.5.5.7.3.4", "1.3.6.1.5.5.7.3.8", "1.3.6.1.5.5.7.3.9",
                "1.3.6.1.5.5.7.3.5", "1.3.6.1.5.5.7.3.6" }));
        TestCert cert1 = new TestCert(ku1);
        TestCert cert2 = new TestCert(ku2);
        X509CertSelector selector = new X509CertSelector();
        try {
            selector.setExtendedKeyUsage(null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificate should match in the case of null "
                + "extendedKeyUsage criteria.", selector.match(cert1)
                && selector.match(cert2));
        try {
            selector.setExtendedKeyUsage(ku1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertEquals(ku1, selector.getExtendedKeyUsage());
        try {
            selector.setExtendedKeyUsage(ku2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertEquals(ku2, selector.getExtendedKeyUsage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIssuer",
        args = {byte[].class}
    )
    public void test_setIssuerLB$() throws CertificateException {
        byte[] name1 = new byte[]
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] name2 = new byte[]
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        X500Principal iss1 = new X500Principal(name1);
        X500Principal iss2 = new X500Principal(name2);
        TestCert cert1 = new TestCert(iss1);
        TestCert cert2 = new TestCert(iss2);
        X509CertSelector selector = new X509CertSelector();
        try {
            selector.setIssuer((byte[]) null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificates should match "
                + "in the case of null issuer criteria.", selector.match(cert1)
                && selector.match(cert2));
        try {
            selector.setIssuer(name1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setIssuer(name2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIssuer",
        args = {java.lang.String.class}
    )
    public void test_setIssuerLjava_lang_String() throws CertificateException {
        String name1 = "O=First Org.";
        String name2 = "O=Second Org.";
        X500Principal iss1 = new X500Principal(name1);
        X500Principal iss2 = new X500Principal(name2);
        TestCert cert1 = new TestCert(iss1);
        TestCert cert2 = new TestCert(iss2);
        X509CertSelector selector = new X509CertSelector();
        try {
            selector.setIssuer((String) null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificates should match "
                + "in the case of null issuer criteria.", selector.match(cert1)
                && selector.match(cert2));
        try {
            selector.setIssuer(name1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setIssuer(name2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIssuer",
        args = {javax.security.auth.x500.X500Principal.class}
    )
    public void test_setIssuerLjavax_security_auth_x500_X500Principal()
            throws CertificateException {
        X500Principal iss1 = new X500Principal("O=First Org.");
        X500Principal iss2 = new X500Principal("O=Second Org.");
        TestCert cert1 = new TestCert(iss1);
        TestCert cert2 = new TestCert(iss2);
        X509CertSelector selector = new X509CertSelector();
        selector.setIssuer((X500Principal) null);
        assertTrue("Any certificates should match "
                + "in the case of null issuer criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setIssuer(iss1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setIssuer(iss2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setKeyUsage",
        args = {boolean[].class}
    )
    public void test_setKeyUsageZ() throws CertificateException {
        boolean[] ku1 = new boolean[] { true, true, true, true, true, true,
                true, true, true };
        boolean[] ku2 = new boolean[] { true, true, true, true, true, true,
                true, true, false };
        TestCert cert1 = new TestCert(ku1);
        TestCert cert2 = new TestCert(ku2);
        TestCert cert3 = new TestCert((boolean[]) null);
        X509CertSelector selector = new X509CertSelector();
        selector.setKeyUsage(null);
        assertTrue("Any certificate should match in the case of null "
                + "keyUsage criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setKeyUsage(ku1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        assertTrue("The certificate which does not have a keyUsage extension "
                + "implicitly allows all keyUsage values.", selector
                .match(cert3));
        selector.setKeyUsage(ku2);
        ku2[0] = !ku2[0];
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMatchAllSubjectAltNames",
        args = {boolean.class}
    )
    public void test_setMatchAllSubjectAltNamesZ() {
        TestCert cert = new TestCert();
        X509CertSelector selector = new X509CertSelector();
        assertTrue(selector.match(cert));
        assertFalse(selector.match(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setNameConstraints",
        args = {byte[].class}
    )
    public void test_setNameConstraintsLB$() throws IOException {
        X509CertSelector selector = new X509CertSelector();
        for (int i = 0; i < constraintBytes.length; i++) {
            selector.setNameConstraints(constraintBytes[i]);
            assertTrue(Arrays.equals(constraintBytes[i], selector
                    .getNameConstraints()));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPathToNames",
        args = {java.util.Collection.class}
    )
    public void test_setPathToNamesLjava_util_Collection() {
        try {
            GeneralName san0 = new GeneralName(new OtherName("1.2.3.4.5",
                    new byte[] { 1, 2, 0, 1 }));
            GeneralName san1 = new GeneralName(1, "rfc@822.Name");
            GeneralName san2 = new GeneralName(2, "dNSName");
            GeneralName san3 = new GeneralName(new ORAddress());
            GeneralName san4 = new GeneralName(new Name("O=Organization"));
            GeneralName san6 = new GeneralName(6, "http:
            GeneralName san7 = new GeneralName(7, "1.1.1.1");
            GeneralName san8 = new GeneralName(8, "1.2.3.4444.55555");
            GeneralNames sans1 = new GeneralNames();
            sans1.addName(san0);
            sans1.addName(san1);
            sans1.addName(san2);
            sans1.addName(san3);
            sans1.addName(san4);
            sans1.addName(san6);
            sans1.addName(san7);
            sans1.addName(san8);
            GeneralNames sans2 = new GeneralNames();
            sans2.addName(san0);
            TestCert cert1 = new TestCert(sans1);
            TestCert cert2 = new TestCert(sans2);
            X509CertSelector selector = new X509CertSelector();
            selector.setMatchAllSubjectAltNames(true);
            selector.setPathToNames(null);
            assertTrue("Any certificate should match in the case of null "
                    + "subjectAlternativeNames criteria.", selector
                    .match(cert1)
                    && selector.match(cert2));
            Collection<List<?>> sans = sans1.getPairsList();
            selector.setPathToNames(sans);
            Collection<List<?>> col = selector.getPathToNames();
            Iterator<List<?>> i = col.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof List)) {
                    fail("expected a List");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPolicy",
        args = {java.util.Set.class}
    )
    public void test_setPolicyLjava_util_Set() throws IOException {
        String[] policies1 = new String[] { "1.3.6.1.5.5.7.3.1",
                "1.3.6.1.5.5.7.3.2", "1.3.6.1.5.5.7.3.3", "1.3.6.1.5.5.7.3.4",
                "1.3.6.1.5.5.7.3.8", "1.3.6.1.5.5.7.3.9", "1.3.6.1.5.5.7.3.5",
                "1.3.6.1.5.5.7.3.6", "1.3.6.1.5.5.7.3.7" };
        String[] policies2 = new String[] { "1.3.6.7.3.1" };
        HashSet<String> p1 = new HashSet<String>(Arrays.asList(policies1));
        HashSet<String> p2 = new HashSet<String>(Arrays.asList(policies2));
        X509CertSelector selector = new X509CertSelector();
        TestCert cert1 = new TestCert(policies1);
        TestCert cert2 = new TestCert(policies2);
        selector.setPolicy(null);
        assertTrue("Any certificate should match in the case of null "
                + "privateKeyValid criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setPolicy(p1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setPolicy(p2);
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert1));
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setPrivateKeyValid",
        args = {java.util.Date.class}
    )
    public void test_setPrivateKeyValidLjava_util_Date()
            throws CertificateException {
        Date date1 = new Date(100000000);
        Date date2 = new Date(200000000);
        Date date3 = new Date(300000000);
        Date date4 = new Date(150000000);
        Date date5 = new Date(250000000);
        TestCert cert1 = new TestCert(date1, date2);
        TestCert cert2 = new TestCert(date2, date3);
        X509CertSelector selector = new X509CertSelector();
        selector.setPrivateKeyValid(null);
        assertTrue("Any certificate should match in the case of null "
                + "privateKeyValid criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setPrivateKeyValid(date4);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setPrivateKeyValid(date5);
        date5.setTime(date4.getTime());
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSerialNumber",
        args = {java.math.BigInteger.class}
    )
    public void test_setSerialNumberLjava_math_BigInteger()
            throws CertificateException {
        BigInteger ser1 = new BigInteger("10000");
        BigInteger ser2 = new BigInteger("10001");
        TestCert cert1 = new TestCert(ser1);
        TestCert cert2 = new TestCert(ser2);
        X509CertSelector selector = new X509CertSelector();
        selector.setSerialNumber(null);
        assertTrue("Any certificate should match in the case of null "
                + "serialNumber criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setSerialNumber(ser1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setSerialNumber(ser2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubject",
        args = {byte[].class}
    )
    public void test_setSubjectLB$() throws CertificateException {
        byte[] name1 = new byte[]
        { 48, 21, 49, 19, 48, 17, 6, 3, 85, 4, 10, 19, 10, 70, 105, 114, 115,
                116, 32, 79, 114, 103, 46 };
        byte[] name2 = new byte[]
        { 48, 22, 49, 20, 48, 18, 6, 3, 85, 4, 10, 19, 11, 83, 101, 99, 111,
                110, 100, 32, 79, 114, 103, 46 };
        X500Principal sub1 = new X500Principal(name1);
        X500Principal sub2 = new X500Principal(name2);
        TestCert cert1 = new TestCert(sub1);
        TestCert cert2 = new TestCert(sub2);
        X509CertSelector selector = new X509CertSelector();
        try {
            selector.setSubject((byte[]) null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificates should match "
                + "in the case of null issuer criteria.", selector.match(cert1)
                && selector.match(cert2));
        try {
            selector.setSubject(name1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setSubject(name2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubject",
        args = {java.lang.String.class}
    )
    public void test_setSubjectLjava_lang_String() throws CertificateException {
        String name1 = "O=First Org.";
        String name2 = "O=Second Org.";
        X500Principal sub1 = new X500Principal(name1);
        X500Principal sub2 = new X500Principal(name2);
        TestCert cert1 = new TestCert(sub1);
        TestCert cert2 = new TestCert(sub2);
        X509CertSelector selector = new X509CertSelector();
        try {
            selector.setSubject((String) null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificates should match "
                + "in the case of null subject criteria.", selector
                .match(cert1)
                && selector.match(cert2));
        try {
            selector.setSubject(name1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setSubject(name2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubject",
        args = {javax.security.auth.x500.X500Principal.class}
    )
    public void test_setSubjectLjavax_security_auth_x500_X500Principal()
            throws CertificateException {
        X500Principal sub1 = new X500Principal("O=First Org.");
        X500Principal sub2 = new X500Principal("O=Second Org.");
        TestCert cert1 = new TestCert(sub1);
        TestCert cert2 = new TestCert(sub2);
        X509CertSelector selector = new X509CertSelector();
        selector.setSubject((X500Principal) null);
        assertTrue("Any certificates should match "
                + "in the case of null subjcet criteria.", selector
                .match(cert1)
                && selector.match(cert2));
        selector.setSubject(sub1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setSubject(sub2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectAlternativeNames",
        args = {java.util.Collection.class}
    )
    public void test_setSubjectAlternativeNamesLjava_util_Collection() {
        try {
            GeneralName san0 = new GeneralName(new OtherName("1.2.3.4.5",
                    new byte[] { 1, 2, 0, 1 }));
            GeneralName san1 = new GeneralName(1, "rfc@822.Name");
            GeneralName san2 = new GeneralName(2, "dNSName");
            GeneralName san3 = new GeneralName(new ORAddress());
            GeneralName san4 = new GeneralName(new Name("O=Organization"));
            GeneralName san6 = new GeneralName(6, "http:
            GeneralName san7 = new GeneralName(7, "1.1.1.1");
            GeneralName san8 = new GeneralName(8, "1.2.3.4444.55555");
            GeneralNames sans1 = new GeneralNames();
            sans1.addName(san0);
            sans1.addName(san1);
            sans1.addName(san2);
            sans1.addName(san3);
            sans1.addName(san4);
            sans1.addName(san6);
            sans1.addName(san7);
            sans1.addName(san8);
            GeneralNames sans2 = new GeneralNames();
            sans2.addName(san0);
            TestCert cert1 = new TestCert(sans1);
            TestCert cert2 = new TestCert(sans2);
            X509CertSelector selector = new X509CertSelector();
            selector.setMatchAllSubjectAltNames(true);
            selector.setSubjectAlternativeNames(null);
            assertTrue("Any certificate should match in the case of null "
                    + "subjectAlternativeNames criteria.", selector
                    .match(cert1)
                    && selector.match(cert2));
            Collection<List<?>> sans = sans1.getPairsList();
            selector.setSubjectAlternativeNames(sans);
            Collection<List<?>> col = selector.getSubjectAlternativeNames();
            Iterator<List<?>> i = col.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                if (!(o instanceof List)) {
                    fail("expected a List");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected IOException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectKeyIdentifier",
        args = {byte[].class}
    )
    public void test_setSubjectKeyIdentifierLB$() throws CertificateException {
        byte[] skid1 = new byte[] { 1, 2, 3, 4, 5 }; 
        byte[] skid2 = new byte[] { 5, 4, 3, 2, 1 }; 
        TestCert cert1 = new TestCert(skid1);
        TestCert cert2 = new TestCert(skid2);
        X509CertSelector selector = new X509CertSelector();
        selector.setSubjectKeyIdentifier(null);
        assertTrue("Any certificate should match in the case of null "
                + "serialNumber criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setSubjectKeyIdentifier(skid1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setSubjectKeyIdentifier(skid2);
        skid2[0]++;
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectPublicKey",
        args = {byte[].class}
    )
    public void test_setSubjectPublicKeyLB$() throws Exception {
        byte[] enc = { 0x30, 0x0E, 
                0x30, 0x07, 
                0x06, 0x02, 0x03, 0x05,
                0x01, 0x01, 0x07, 
                0x03, 0x03, 0x01, 0x01, 0x06, 
        };
        X509CertSelector selector = new X509CertSelector();
        selector.setSubjectPublicKey(enc);
        PublicKey key = selector.getSubjectPublicKey();
        assertEquals("0.3.5", key.getAlgorithm());
        assertEquals("X.509", key.getFormat());
        assertTrue(Arrays.equals(enc, key.getEncoded()));
        assertNotNull(key.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectPublicKey",
        args = {java.security.PublicKey.class}
    )
    public void test_setSubjectPublicKeyLjava_security_PublicKey()
            throws CertificateException {
        PublicKey pkey1 = null;
        PublicKey pkey2 = null;
        try {
            pkey1 = new TestKeyPair("RSA").getPublic();
            pkey2 = new TestKeyPair("DSA").getPublic();
        } catch (Exception e) {
            fail("Unexpected Exception was thrown: " + e.getMessage());
        }
        TestCert cert1 = new TestCert(pkey1);
        TestCert cert2 = new TestCert(pkey2);
        X509CertSelector selector = new X509CertSelector();
        selector.setSubjectPublicKey((PublicKey) null);
        assertTrue("Any certificate should match in the case of null "
                + "subjectPublicKey criteria.", selector.match(cert1)
                && selector.match(cert2));
        selector.setSubjectPublicKey(pkey1);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        selector.setSubjectPublicKey(pkey2);
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSubjectPublicKeyAlgID",
        args = {java.lang.String.class}
    )
    public void test_setSubjectPublicKeyAlgIDLjava_lang_String()
            throws CertificateException {
        X509CertSelector selector = new X509CertSelector();
        String pkaid1 = "1.2.840.113549.1.1.1"; 
        String pkaid2 = "1.2.840.10040.4.1"; 
        PublicKey pkey1;
        PublicKey pkey2;
        try {
            pkey1 = new TestKeyPair("RSA").getPublic();
            pkey2 = new TestKeyPair("DSA").getPublic();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception was thrown: " + e.getMessage());
            return;
        }
        TestCert cert1 = new TestCert(pkey1);
        TestCert cert2 = new TestCert(pkey2);
        try {
            selector.setSubjectPublicKeyAlgID(null);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("Any certificate should match in the case of null "
                + "subjectPublicKeyAlgID criteria.", selector.match(cert1)
                && selector.match(cert2));
        String[] validOIDs = { "0.0.20", "1.25.0", "2.0.39", "0.2.10", "1.35.15",
                "2.17.89", "2.5.29.16", "2.5.29.17", "2.5.29.30", "2.5.29.32",
                "2.5.29.37" };
        for (int i = 0; i < validOIDs.length; i++) {
            try {
                selector.setSubjectPublicKeyAlgID(validOIDs[i]);
                assertEquals(validOIDs[i], selector.getSubjectPublicKeyAlgID());
            } catch (IOException e) {
                fail("Unexpected exception " + e.getMessage());
            }
        }
        String[] invalidOIDs = { "0.20", "1.25", "2.39", "3.10"};
        for (int i = 0; i < invalidOIDs.length; i++) {
            try {
                selector.setSubjectPublicKeyAlgID(invalidOIDs[i]);
                fail("IOException wasn't thrown for " + invalidOIDs[i]);
            } catch (IOException e) {
            }
        }
        try {
            selector.setSubjectPublicKeyAlgID(pkaid1);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert1));
        assertFalse("The certificate should not match the selection criteria.",
                selector.match(cert2));
        try {
            selector.setSubjectPublicKeyAlgID(pkaid2);
        } catch (IOException e) {
            fail("Unexpected IOException was thrown.");
        }
        assertTrue("The certificate should match the selection criteria.",
                selector.match(cert2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        X509CertSelector selector = new X509CertSelector();
        assertNotNull(selector.toString());
    }
    public class MyPublicKey implements PublicKey {
        private static final long serialVersionUID = 2899528375354645752L;
        public MyPublicKey() {
            super();
        }
        public String getAlgorithm() {
            return "PublicKey";
        }
        public String getFormat() {
            return "Format";
        }
        public byte[] getEncoded() {
            return new byte[0];
        }
        public long getSerVerUID() {
            return serialVersionUID;
        }
    }
    private class TestCert extends X509Certificate {
        private static final long serialVersionUID = 176676115254260405L;
        protected String equalCriteria = null; 
        protected BigInteger serialNumber = null;
        protected X500Principal issuer = null;
        protected X500Principal subject = null;
        protected byte[] keyIdentifier = null;
        protected Date date = null;
        protected Date notBefore = null;
        protected Date notAfter = null;
        protected PublicKey key = null;
        protected boolean[] keyUsage = null;
        protected List<String> extKeyUsage = null;
        protected int pathLen = 1;
        protected GeneralNames sans = null;
        protected byte[] encoding = null;
        protected String[] policies = null;
        protected Collection<List<?>> collection = null;
        protected NameConstraints nameConstraints = null;
        public TestCert() {
        }
        public TestCert(GeneralNames sans) {
            setSubjectAlternativeNames(sans);
        }
        public TestCert(NameConstraints nameConstraints) {
            this.nameConstraints = nameConstraints;
        }
        public TestCert(Collection<List<?>> collection) {
            setCollection(collection);
        }
        public TestCert(String equalCriteria) {
            setEqualCriteria(equalCriteria);
        }
        public TestCert(String[] policies) {
            setPolicies(policies);
        }
        public TestCert(BigInteger serial) {
            setSerialNumber(serial);
        }
        public TestCert(X500Principal principal) {
            setIssuer(principal);
            setSubject(principal);
        }
        public TestCert(byte[] array) {
            setKeyIdentifier(array);
        }
        public TestCert(Date date) {
            setDate(date);
        }
        public TestCert(Date notBefore, Date notAfter) {
            setPeriod(notBefore, notAfter);
        }
        public TestCert(PublicKey key) {
            setPublicKey(key);
        }
        public TestCert(boolean[] keyUsage) {
            setKeyUsage(keyUsage);
        }
        public TestCert(Set<String> extKeyUsage) {
            setExtendedKeyUsage(extKeyUsage);
        }
        public TestCert(int pathLen) {
            this.pathLen = pathLen;
        }
        public void setSubjectAlternativeNames(GeneralNames sans) {
            this.sans = sans;
        }
        public void setCollection(Collection<List<?>> collection) {
            this.collection = collection;
        }
        public void setPolicies(String[] policies) {
            this.policies = policies;
        }
        public void setExtendedKeyUsage(Set<String> extKeyUsage) {
            this.extKeyUsage = (extKeyUsage == null) ? null : new ArrayList<String>(
                    extKeyUsage);
        }
        public void setKeyUsage(boolean[] keyUsage) {
            this.keyUsage = (keyUsage == null) ? null : (boolean[]) keyUsage
                    .clone();
        }
        public void setPublicKey(PublicKey key) {
            this.key = key;
        }
        public void setPeriod(Date notBefore, Date notAfter) {
            this.notBefore = notBefore;
            this.notAfter = notAfter;
        }
        public void setSerialNumber(BigInteger serial) {
            this.serialNumber = serial;
        }
        public void setEqualCriteria(String equalCriteria) {
            this.equalCriteria = equalCriteria;
        }
        public void setIssuer(X500Principal issuer) {
            this.issuer = issuer;
        }
        public void setSubject(X500Principal subject) {
            this.subject = subject;
        }
        public void setKeyIdentifier(byte[] subjectKeyID) {
            this.keyIdentifier = (byte[]) subjectKeyID.clone();
        }
        public void setDate(Date date) {
            this.date = new Date(date.getTime());
        }
        public void setEncoding(byte[] encoding) {
            this.encoding = encoding;
        }
        public boolean equals(Object cert) {
            if (cert == null) {
                return false;
            }
            if ((equalCriteria == null)
                    || (((TestCert) cert).equalCriteria == null)) {
                return false;
            } else {
                return equalCriteria.equals(((TestCert) cert).equalCriteria);
            }
        }
        public String toString() {
            if (equalCriteria != null) {
                return equalCriteria;
            }
            return "";
        }
        public void checkValidity() throws CertificateExpiredException,
                CertificateNotYetValidException {
        }
        public void checkValidity(Date date)
                throws CertificateExpiredException,
                CertificateNotYetValidException {
            if (this.date == null) {
                throw new CertificateExpiredException();
            }
            int result = this.date.compareTo(date);
            if (result > 0) {
                throw new CertificateExpiredException();
            }
            if (result < 0) {
                throw new CertificateNotYetValidException();
            }
        }
        public int getVersion() {
            return 3;
        }
        public BigInteger getSerialNumber() {
            return (serialNumber == null) ? new BigInteger("1111")
                    : serialNumber;
        }
        public Principal getIssuerDN() {
            return issuer;
        }
        public X500Principal getIssuerX500Principal() {
            return issuer;
        }
        public Principal getSubjectDN() {
            return subject;
        }
        public X500Principal getSubjectX500Principal() {
            return subject;
        }
        public Date getNotBefore() {
            return null;
        }
        public Date getNotAfter() {
            return null;
        }
        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return null;
        }
        public byte[] getSignature() {
            return null;
        }
        public String getSigAlgName() {
            return null;
        }
        public String getSigAlgOID() {
            return null;
        }
        public byte[] getSigAlgParams() {
            return null;
        }
        public boolean[] getIssuerUniqueID() {
            return null;
        }
        public boolean[] getSubjectUniqueID() {
            return null;
        }
        public boolean[] getKeyUsage() {
            return keyUsage;
        }
        public List<String> getExtendedKeyUsage()
                throws CertificateParsingException {
            return extKeyUsage;
        }
        public int getBasicConstraints() {
            return pathLen;
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
        public PublicKey getPublicKey() {
            return key;
        }
        public byte[] getEncoded() throws CertificateEncodingException {
            return encoding;
        }
        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }
        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }
        public byte[] getExtensionValue(String oid) {
            if (("2.5.29.14".equals(oid)) || ("2.5.29.35".equals(oid))) {
                return ASN1OctetString.getInstance().encode(keyIdentifier);
            }
            if ("2.5.29.16".equals(oid)) {
                PrivateKeyUsagePeriod pkup = new PrivateKeyUsagePeriod(
                        notBefore, notAfter);
                byte[] encoded = pkup.getEncoded();
                return ASN1OctetString.getInstance().encode(encoded);
            }
            if (("2.5.29.37".equals(oid)) && (extKeyUsage != null)) {
                ASN1Oid[] oa = new ASN1Oid[extKeyUsage.size()];
                String[] val = new String[extKeyUsage.size()];
                Iterator it = extKeyUsage.iterator();
                int id = 0;
                while (it.hasNext()) {
                    oa[id] = ASN1Oid.getInstanceForString();
                    val[id++] = (String) it.next();
                }
                return ASN1OctetString.getInstance().encode(
                        new ASN1Sequence(oa).encode(val));
            }
            if ("2.5.29.19".equals(oid)) {
                return ASN1OctetString.getInstance().encode(
                        new ASN1Sequence(new ASN1Type[] {
                                ASN1Boolean.getInstance(),
                                ASN1Integer.getInstance() })
                                .encode(new Object[] {
                                        new Boolean(pathLen != 1),
                                        BigInteger.valueOf(pathLen)
                                                .toByteArray() }));
            }
            if ("2.5.29.17".equals(oid) && (sans != null)) {
                if (sans.getNames() == null) {
                    return null;
                }
                return ASN1OctetString.getInstance().encode(
                        GeneralNames.ASN1.encode(sans));
            }
            if ("2.5.29.32".equals(oid) && (policies != null)
                    && (policies.length > 0)) {
                CertificatePolicies certificatePolicies = new CertificatePolicies();
                for (int i = 0; i < policies.length; i++) {
                    PolicyInformation policyInformation = new PolicyInformation(
                            policies[i]);
                    certificatePolicies.addPolicyInformation(policyInformation);
                }
                return ASN1OctetString.getInstance().encode(
                        certificatePolicies.getEncoded());
            }
            if ("2.5.29.30".equals(oid) && (nameConstraints != null)) { 
                return ASN1OctetString.getInstance().encode(
                        nameConstraints.getEncoded());
            }
            return null;
        }
        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }
    }
    public X509Certificate rootCertificate;
    public X509Certificate endCertificate;
    public MyCRL crl;
    private X509CertSelector theCertSelector;
    private CertPathBuilder builder;
    private void setupEnvironment() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream bi = new ByteArrayInputStream(TestUtils.rootCert.getBytes());
        rootCertificate = (X509Certificate) cf.generateCertificate(bi);
        bi = new ByteArrayInputStream(TestUtils.endCert.getBytes());
        endCertificate = (X509Certificate) cf.generateCertificate(bi);
        BigInteger revokedSerialNumber = BigInteger.valueOf(1);
        crl = new MyCRL("X.509");
        List<Object> list = new ArrayList<Object>();
        list.add(rootCertificate);
        list.add(endCertificate);
        theCertSelector = new X509CertSelector();
        theCertSelector.setCertificate(endCertificate);
        theCertSelector.setIssuer(endCertificate.getIssuerX500Principal()
                .getEncoded());
        builder = CertPathBuilder.getInstance("PKIX");
    }
    private CertPath buildCertPath() throws InvalidAlgorithmParameterException {
        PKIXCertPathBuilderResult result = null;
        PKIXBuilderParameters buildParams = new PKIXBuilderParameters(
                Collections.singleton(new TrustAnchor(rootCertificate, null)),
                theCertSelector);
        try {
        result = (PKIXCertPathBuilderResult) builder
        .build(buildParams);
        } catch(CertPathBuilderException e) {
            return null;
        }
        return result.getCertPath();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies Exception",
        method = "addPathToName",
        args = {int.class, byte[].class}
    )
    public void test_addPathToNameLintLbyte_array2() throws Exception {
        TestUtils.initCertPathSSCertChain();
        setupEnvironment();
        byte[] bytes, bytesName;
        bytes = new byte[] {-127, 8, 56, 50, 50, 46, 78, 97, 109, 101};
        bytesName = new byte[] {22, 8, 56, 50, 50, 46, 78, 97, 109, 101};
        bytes[bytes.length-3] = (byte) 200;
        try {
            theCertSelector.addPathToName(1, bytes);
        } catch (IOException e) {
        }
        theCertSelector.setPathToNames(null);
        theCertSelector.addPathToName(1, bytesName);
        assertNotNull(theCertSelector.getPathToNames());
        CertPath p = buildCertPath();
        assertNull(p);
        theCertSelector.setPathToNames(null);
        theCertSelector.addPathToName(4, TestUtils.rootCertificateSS.getIssuerX500Principal().getEncoded());
        assertNotNull(theCertSelector.getPathToNames());
        p = TestUtils.buildCertPathSSCertChain();
        assertNotNull(p);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies IOException.",
        method = "addPathToName",
        args = {int.class, java.lang.String.class}
    )
    public void test_addPathToNameLintLjava_lang_String2() throws Exception {
        setupEnvironment();
        byte[] bytes, bytesName;
        bytes = new byte[] {-127, 8, 56, 50, 50, 46, 78, 97, 109, 101};
        bytesName = new byte[] {22, 8, 56, 50, 50, 46, 78, 97, 109, 101};
        assertNotNull(bytes);
        byte[] b = new byte[bytes.length];
        b = bytes;
        b[bytes.length-3] = (byte) 200;
        try {
        theCertSelector.addPathToName(1, new String(b));
        } catch (IOException e) {
        }
        theCertSelector.setPathToNames(null);
        theCertSelector.addPathToName(1, new String(bytesName));
        assertNotNull(theCertSelector.getPathToNames());
        CertPath p = buildCertPath();
        assertNull(p);
        theCertSelector.setPathToNames(null);
        theCertSelector.addPathToName(1, rootCertificate.getIssuerX500Principal().getName());
        assertNotNull(theCertSelector.getPathToNames());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "IOException checking missed",
        method = "addSubjectAlternativeName",
        args = {int.class, byte[].class}
    )
    public void test_addSubjectAlternativeNameLintLbyte_array2()
            throws Exception {
        GeneralName san0 = new GeneralName(new OtherName("1.2.3.4.5",
                new byte[] {1, 2, 0, 1}));
        GeneralName san1 = new GeneralName(1, "rfc@822.Name");
        GeneralName san2 = new GeneralName(2, "dNSName");
        GeneralNames sans1 = new GeneralNames();
        sans1.addName(san0);
        sans1.addName(san1);
        sans1.addName(san2);
        X509CertSelector selector = new X509CertSelector();
        selector.addSubjectAlternativeName(0, san0.getEncodedName());
        selector.addSubjectAlternativeName(1, san1.getEncodedName());
        selector.addSubjectAlternativeName(2, san2.getEncodedName());
        GeneralNames sans2 = new GeneralNames();
        sans2.addName(san0);
        TestCert cert1 = new TestCert(sans1);
        TestCert cert2 = new TestCert(sans2);
        assertTrue(selector.match(cert1));
        assertFalse(selector.match(cert2));
        selector.setSubjectAlternativeNames(null);
        GeneralName name = new GeneralName(new Name("O=Android"));
        try {
            selector.addSubjectAlternativeName(0, name.getEncodedName());
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addSubjectAlternativeName",
        args = {int.class, java.lang.String.class}
    )
    public void test_addSubjectAlternativeNameLintLjava_lang_String2() throws Exception{
        GeneralName san6 = new GeneralName(6, "http:
        GeneralName san2 = new GeneralName(2, "dNSName");
        GeneralNames sans1 = new GeneralNames();
        sans1.addName(san6);
        sans1.addName(san2);
        X509CertSelector selector = new X509CertSelector();
        selector.addSubjectAlternativeName(6, "http:
        selector.addSubjectAlternativeName(2, "dNSName");
        GeneralNames sans2 = new GeneralNames();
        sans2.addName(san2);
        TestCert cert1 = new TestCert(sans1);
        TestCert cert2 = new TestCert(sans2);
        assertTrue(selector.match(cert1));
        assertFalse(selector.match(cert2));
        selector.setSubjectAlternativeNames(null);
        GeneralName name = new GeneralName(new Name("O=Android"));
        try {
            selector.addSubjectAlternativeName(0, (name.toString()));
        } catch (IOException e) {
        }
    }
}

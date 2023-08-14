@TestTargetClass(EncryptedPrivateKeyInfo.class)
public class EncryptedPrivateKeyInfoTest extends TestCase {
    private static final Provider[] provider = Security.getProviders();
    private static final String[][] algName = {
            { "AES", null},
            { "Blowfish", null },
            { "DES", null },
            { "TripleDES", null },
            { "PBEWithMD5AndTripleDES", null },
            { "PBEWithMD5AndDES", "PBEWithMD5AndDES/CBC/PKCS5Padding", "PBEWithMD5AndDES"},
            { "PBEWithMD5AndDES", null, "PBEWithMD5AndDES"}, { "PBEWithHmacSHA1AndDESede", null },
            { "DiffieHellman", null }, 
            { "DSA", null }, 
            { "RC2", null },
            { "RC4", null },
            { "RC5", null },
    };
    @Override protected void setUp() throws Exception {
        super.setUp();
        TestEnvironment.reset();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgName",
        args = {}
    )
    public void test_getAlgName () {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            try {
                TestDataGenerator g = new TestDataGenerator(algName[i][0],
                        algName[i][1], privateKeyInfoDamaged, null);
                EncryptedPrivateKeyInfo epki;
                if (g.ap() == null) {
                    epki = new EncryptedPrivateKeyInfo(algName[i][0], g.ct());
                } else {
                    epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                }
                if (algName[i].length == 3) {
                    assertEquals(algName[i][2], epki.getAlgName());
                }
                performed = true;
            } catch (TestDataGenerator.AllowedFailure allowedFailure) {
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {byte[].class}
    )
    public final void testEncryptedPrivateKeyInfobyteArray1() throws Exception {
        new EncryptedPrivateKeyInfo(EncryptedPrivateKeyInfoData
                .getValidEncryptedPrivateKeyInfoEncoding("DH"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {byte[].class}
    )
    public final void testEncryptedPrivateKeyInfobyteArray2()
            throws IOException {
        try {
            new EncryptedPrivateKeyInfo(null);
            fail(getName() + ": NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {byte[].class}
    )
    public final void testEncryptedPrivateKeyInfobyteArray3() {
        try {
            new EncryptedPrivateKeyInfo(new byte[0]);
            fail(getName() + ": IOException has not been thrown");
        } catch (IOException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {byte[].class}
    )
    public final void testEncryptedPrivateKeyInfobyteArray4() {
        try {
            new EncryptedPrivateKeyInfo(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9,
                    10 });
            fail(getName() + ": IOException has not been thrown");
        } catch (IOException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {byte[].class}
    )
    public final void testEncryptedPrivateKeyInfobyteArray5() throws Exception {
        byte[] enc = null;
        try {
            enc = EncryptedPrivateKeyInfoData
                    .getValidEncryptedPrivateKeyInfoEncoding("DSA");
            enc[9] = (byte) 6;
            new EncryptedPrivateKeyInfo(enc);
            fail(getName() + "(1): IOException has not been thrown");
        } catch (IOException ok) {
        }
        try {
            enc = EncryptedPrivateKeyInfoData
                    .getValidEncryptedPrivateKeyInfoEncoding("DSA");
            enc[307] = (byte) 6;
            new EncryptedPrivateKeyInfo(enc);
            fail(getName() + "(2): IOException has not been thrown");
        } catch (IOException ok) {
        }
        try {
            enc = EncryptedPrivateKeyInfoData
                    .getValidEncryptedPrivateKeyInfoEncoding("DSA");
            enc[310] = (byte) 1;
            new EncryptedPrivateKeyInfo(enc);
            fail(getName() + "(3): IOException has not been thrown");
        } catch (IOException ok) {
        }
        try {
            enc = EncryptedPrivateKeyInfoData
                    .getValidEncryptedPrivateKeyInfoEncoding("DSA");
            enc[17] = (byte) 0x29;
            EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(enc);
            if (epki.getAlgParameters() == null) {
            } else {
                fail(getName() + "(4): IOException has not been thrown");
            }
        } catch (IOException ok) {
        }
        try {
            enc = EncryptedPrivateKeyInfoData
                    .getValidEncryptedPrivateKeyInfoEncoding("DSA");
            enc[20] = (byte) 0x1d;
            new EncryptedPrivateKeyInfo(enc);
            fail(getName() + "(5): IOException has not been thrown");
        } catch (IOException ok) {
        }
        try {
            enc = EncryptedPrivateKeyInfoData
                    .getValidEncryptedPrivateKeyInfoEncoding("DSA");
            enc[20] = (byte) 0x1f;
            new EncryptedPrivateKeyInfo(enc);
            fail(getName() + "(6): IOException has not been thrown");
        } catch (IOException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {byte[].class}
    )
    public final void testEncryptedPrivateKeyInfobyteArray6() throws Exception {
        byte[] encoded = EncryptedPrivateKeyInfoData
                .getValidEncryptedPrivateKeyInfoEncoding("DSA");
        byte[] encodedCopy = encoded.clone();
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(encodedCopy);
        encodedCopy[9] = (byte) 6;
        assertTrue(Arrays.equals(encoded, epki.getEncoded()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(String, byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.lang.String.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoStringbyteArray1() {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                performed = true;
            } catch (NoSuchAlgorithmException allowed) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(String, byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.lang.String.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoStringbyteArray2() {
        try {
            new EncryptedPrivateKeyInfo("bla-bla",
                    EncryptedPrivateKeyInfoData.encryptedData);
            fail(getName() + ": NoSuchAlgorithmException has not been thrown");
        } catch (NoSuchAlgorithmException ok) {
        }
        try {
            new EncryptedPrivateKeyInfo("",
                    EncryptedPrivateKeyInfoData.encryptedData);
            fail(getName() + ": NoSuchAlgorithmException has not been thrown");
        } catch (NoSuchAlgorithmException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(String, byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.lang.String.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoStringbyteArray3()
            throws NoSuchAlgorithmException {
        try {
            new EncryptedPrivateKeyInfo((String) null,
                    EncryptedPrivateKeyInfoData.encryptedData);
            fail(getName() + ": NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
        try {
            new EncryptedPrivateKeyInfo("DSA", null);
            fail(getName() + ": NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(String, byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.lang.String.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoStringbyteArray4()
            throws Exception {
        try {
            new EncryptedPrivateKeyInfo("DSA", new byte[] {});
            fail(getName() + ": IllegalArgumentException has not been thrown");
        } catch (IllegalArgumentException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(String, byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.lang.String.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoStringbyteArray5()
            throws Exception {
        byte[] encryptedDataCopy = EncryptedPrivateKeyInfoData.encryptedData
                .clone();
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo("DSA",
                encryptedDataCopy);
        encryptedDataCopy[0] = (byte) 6;
        assertTrue(Arrays.equals(EncryptedPrivateKeyInfoData.encryptedData,
                epki.getEncryptedData()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for EncryptedPrivateKeyInfo(String, byte[]) constructor.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.lang.String.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoStringbyteArray6() {
        try {
            new EncryptedPrivateKeyInfo("0", new byte[] {});
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
        }    
    }
    class Mock_AlgorithmParameters extends AlgorithmParameters {
        protected Mock_AlgorithmParameters(AlgorithmParametersSpi paramSpi, Provider provider, String algorithm) {
            super(paramSpi, provider, algorithm);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality checked. NoSuchAlgorithmException should be tested for complete tests subset.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.security.AlgorithmParameters.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoAlgorithmParametersbyteArray1()
            throws IOException, NoSuchAlgorithmException {
        AlgorithmParameters ap = null;
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                ap = AlgorithmParameters
                        .getInstance(EncryptedPrivateKeyInfoData.algName0[i][0]);
                ap.init(EncryptedPrivateKeyInfoData.getParametersEncoding(
                        EncryptedPrivateKeyInfoData.algName0[i][0]));
                new EncryptedPrivateKeyInfo(ap,
                        EncryptedPrivateKeyInfoData.encryptedData);
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
        ap = new Mock_AlgorithmParameters(null, null, "Wrong alg name");
        try {
            new EncryptedPrivateKeyInfo(ap,
                EncryptedPrivateKeyInfoData.encryptedData);
            fail("NoSuchAlgorithmException expected");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "NullPointerException checked.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.security.AlgorithmParameters.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoAlgorithmParametersbyteArray2()
            throws NoSuchAlgorithmException, IOException {
        try {
            new EncryptedPrivateKeyInfo((AlgorithmParameters) null,
                    EncryptedPrivateKeyInfoData.encryptedData);
            fail(getName() + ": NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
        try {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("DSA");
            ap.init(EncryptedPrivateKeyInfoData.getParametersEncoding("DSA"));
            new EncryptedPrivateKeyInfo(ap, null);
            fail(getName() + ": NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IllegalArgumentException checked.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.security.AlgorithmParameters.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoAlgorithmParametersbyteArray3()
            throws Exception {
        try {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("DSA");
            ap.init(EncryptedPrivateKeyInfoData.getParametersEncoding("DSA"));
            new EncryptedPrivateKeyInfo(ap, new byte[] {});
            fail(getName() + ": IllegalArgumentException has not been thrown");
        } catch (IllegalArgumentException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality checked.",
        method = "EncryptedPrivateKeyInfo",
        args = {java.security.AlgorithmParameters.class, byte[].class}
    )
    public final void testEncryptedPrivateKeyInfoAlgorithmParametersbyteArray4()
            throws Exception {
        AlgorithmParameters ap = AlgorithmParameters.getInstance("DSA");
        ap.init(EncryptedPrivateKeyInfoData.getParametersEncoding("DSA"));
        byte[] encryptedDataCopy = EncryptedPrivateKeyInfoData.encryptedData.clone();
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(ap,
                encryptedDataCopy);
        encryptedDataCopy[0] = (byte) 6;
        assertTrue(Arrays.equals(EncryptedPrivateKeyInfoData.encryptedData,
                epki.getEncryptedData()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getAlgParameters method.",
        method = "getAlgParameters",
        args = {}
    )
    public final void testGetAlgParameters01() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData
                                .getValidEncryptedPrivateKeyInfoEncoding(
                                        EncryptedPrivateKeyInfoData.algName0[i][0]));
                AlgorithmParameters apar = epki.getAlgParameters();
                if (apar == null) {
                    continue;
                }
                assertTrue(Arrays
                        .equals(
                                EncryptedPrivateKeyInfoData
                                        .getParametersEncoding(EncryptedPrivateKeyInfoData.algName0[i][0]),
                                apar.getEncoded()));
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getAlgParameters method.",
        method = "getAlgParameters",
        args = {}
    )
    public final void testGetAlgParameters01_01() throws Exception {
        byte[] validEncodingWithUnknownAlgOID = EncryptedPrivateKeyInfoData
                .getValidEncryptedPrivateKeyInfoEncoding("DH");
        validEncodingWithUnknownAlgOID[18] = 0;
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                validEncodingWithUnknownAlgOID);
        assertNull(epki.getAlgParameters());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getAlgParameters method.",
        method = "getAlgParameters",
        args = {}
    )
    public final void testGetAlgParameters02() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData
                                .getValidEncryptedPrivateKeyInfoEncoding(
                                        EncryptedPrivateKeyInfoData.algName0[i][0],
                                        false));
                assertNull(epki.getAlgParameters());
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getAlgParameters method.",
        method = "getAlgParameters",
        args = {}
    )
    public final void testGetAlgParameters03() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                assertNull(epki.getAlgParameters());
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getAlgParameters method.",
        method = "getAlgParameters",
        args = {}
    )
    public final void testGetAlgParameters04() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                AlgorithmParameters ap = AlgorithmParameters
                        .getInstance(EncryptedPrivateKeyInfoData.algName0[i][0]);
                ap
                        .init(EncryptedPrivateKeyInfoData
                                .getParametersEncoding(
                                        EncryptedPrivateKeyInfoData.algName0[i][0]));
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(ap,
                        EncryptedPrivateKeyInfoData.encryptedData);
                assertSame(ap, epki.getAlgParameters());
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getEncryptedData method.",
        method = "getEncryptedData",
        args = {}
    )
    public final void testGetEncryptedData01() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData
                                .getValidEncryptedPrivateKeyInfoEncoding(
                                        EncryptedPrivateKeyInfoData.algName0[i][0]));
                assertTrue(Arrays.equals(
                        EncryptedPrivateKeyInfoData.encryptedData, epki
                                .getEncryptedData()));
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getEncryptedData method.",
        method = "getEncryptedData",
        args = {}
    )
    public final void testGetEncryptedData02() {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                assertTrue(Arrays.equals(
                        EncryptedPrivateKeyInfoData.encryptedData, epki
                                .getEncryptedData()));
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getEncryptedData method.",
        method = "getEncryptedData",
        args = {}
    )
    public final void testGetEncryptedData03() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                AlgorithmParameters ap = AlgorithmParameters
                        .getInstance(EncryptedPrivateKeyInfoData.algName0[i][0]);
                ap.init(EncryptedPrivateKeyInfoData.getParametersEncoding(
                        EncryptedPrivateKeyInfoData.algName0[i][0]));
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(ap,
                        EncryptedPrivateKeyInfoData.encryptedData);
                assertTrue(Arrays.equals(
                        EncryptedPrivateKeyInfoData.encryptedData, epki
                                .getEncryptedData()));
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getEncryptedData method.",
        method = "getEncryptedData",
        args = {}
    )
    public final void testGetEncryptedData04() {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                byte[] ecd1 = epki.getEncryptedData();
                byte[] ecd2 = epki.getEncryptedData();
                assertNotSame(EncryptedPrivateKeyInfoData.encryptedData, ecd1);
                assertNotSame(EncryptedPrivateKeyInfoData.encryptedData, ecd2);
                assertNotSame(ecd1, ecd2);
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check IOException",
        method = "getEncoded",
        args = {}
    )
    public final void testGetEncoded01() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                byte[] enc = EncryptedPrivateKeyInfoData
                        .getValidEncryptedPrivateKeyInfoEncoding(
                                EncryptedPrivateKeyInfoData.algName0[i][0]);
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(enc);
                assertTrue(Arrays.equals(enc, epki.getEncoded()));
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check IOException",
        method = "getEncoded",
        args = {}
    )
    public final void testGetEncoded02() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                byte[] refEnc = EncryptedPrivateKeyInfoData
                        .getValidEncryptedPrivateKeyInfoEncoding(
                                EncryptedPrivateKeyInfoData.algName0[i][0],
                                false);
                byte[] actEnc = epki.getEncoded();
                assertTrue(Arrays.equals(refEnc, actEnc));
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check IOException",
        method = "getEncoded",
        args = {}
    )
    public final void testGetEncoded03() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                AlgorithmParameters ap = AlgorithmParameters
                        .getInstance(EncryptedPrivateKeyInfoData.algName0[i][0]);
                ap.init(EncryptedPrivateKeyInfoData.getParametersEncoding(
                        EncryptedPrivateKeyInfoData.algName0[i][0]));
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(ap,
                        EncryptedPrivateKeyInfoData.encryptedData);
                assertTrue(Arrays.equals(
                        EncryptedPrivateKeyInfoData
                            .getValidEncryptedPrivateKeyInfoEncoding(
                                EncryptedPrivateKeyInfoData.algName0[i][0]),
                                epki.getEncoded()));
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check IOException",
        method = "getEncoded",
        args = {}
    )
    public final void testGetEncoded04() throws IOException {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                byte[] ec1 = epki.getEncoded();
                byte[] ec2 = epki.getEncoded();
                byte[] ec3 = epki.getEncoded();
                assertNotSame(ec1, ec2);
                assertNotSame(ec2, ec3);
                assertNotSame(ec1, ec3);
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getKeySpec method.",
        method = "getKeySpec",
        args = {javax.crypto.Cipher.class}
    )
    public final void testGetKeySpecCipher01() {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                try {
                    epki.getKeySpec((Cipher) null);
                    fail(getName() + "NullPointerException has not been thrown");
                } catch (NullPointerException ok) {
                } catch (InvalidKeySpecException e) {
                    fail(getName() + "Unexpected exception: " + e);
                }
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getKeySpec method.",
        method = "getKeySpec",
        args = {javax.crypto.Cipher.class}
    )
    public final void test_ROUNDTRIP_GetKeySpecCipher01() {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            try {
                TestDataGenerator g = new TestDataGenerator(algName[i][0],
                        algName[i][1], privateKeyInfo, null);
                EncryptedPrivateKeyInfo epki;
                if (g.ap() == null) {
                    epki = new EncryptedPrivateKeyInfo(algName[i][0], g.ct());
                } else {
                    epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                }
                try {
                    PKCS8EncodedKeySpec eks = epki.getKeySpec(g.c());
                    if (!Arrays.equals(privateKeyInfo, eks.getEncoded())) {
                        fail(algName[i][0] + " != " + algName[i][1]);
                    }
                } catch (InvalidKeySpecException e) {
                    fail(algName[i][0] + ", " + algName[i][1] + e + "\n");
                }
                performed = true;
            } catch (TestDataGenerator.AllowedFailure allowedFailure) {
            } catch (NoSuchAlgorithmException allowed) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getKeySpec method.",
        method = "getKeySpec",
        args = {javax.crypto.Cipher.class}
    )
    public final void test_ROUNDTRIP_GetKeySpecCipher02() {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            try {
                TestDataGenerator g = new TestDataGenerator(algName[i][0],
                        algName[i][1], privateKeyInfoDamaged, null);
                EncryptedPrivateKeyInfo epki;
                if (g.ap() == null) {
                    epki = new EncryptedPrivateKeyInfo(algName[i][0], g.ct());
                } else {
                    epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                }
                try {
                    epki.getKeySpec(g.c());
                    fail(algName[i][0] + ", " + algName[i][1]);
                } catch (InvalidKeySpecException ok) {
                }
                performed = true;
            } catch (TestDataGenerator.AllowedFailure allowedFailure) {
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check NoSuchAlgorithmException",
        method = "getKeySpec",
        args = {java.security.Key.class}
    )
    public final void testGetKeySpecKey01() {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                try {
                    epki.getKeySpec((Key) null);
                    fail(getName() + "NullPointerException has not been thrown");
                } catch (NullPointerException ok) {
                } catch (InvalidKeyException e) {
                    fail(getName() + "Unexpected exception: " + e);
                }
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check NoSuchAlgorithmException",
        method = "getKeySpec",
        args = {java.security.Key.class}
    )
    public final void test_ROUNDTRIP_GetKeySpecKey01() {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            try {
                TestDataGenerator g = new TestDataGenerator(algName[i][0],
                        algName[i][1], privateKeyInfo, null);
                EncryptedPrivateKeyInfo epki;
                if (g.ap() == null) {
                    epki = new EncryptedPrivateKeyInfo(algName[i][0], g.ct());
                } else {
                    epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                }
                try {
                    PKCS8EncodedKeySpec eks = epki
                            .getKeySpec(g.pubK() == null ? g.k() : g.pubK());
                    if (!Arrays.equals(privateKeyInfo, eks.getEncoded())) {
                        fail(algName[i][0] + " != " + algName[i][1]);
                    }
                } catch (InvalidKeyException e) {
                    fail(algName[i][0] + ", " + algName[i][1] + ": " + e);
                }
                performed = true;
            } catch (TestDataGenerator.AllowedFailure allowedFailure) {
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check NoSuchAlgorithmException",
        method = "getKeySpec",
        args = {java.security.Key.class}
    )
    public final void test_ROUNDTRIP_GetKeySpecKey02() {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            try {
                TestDataGenerator g = new TestDataGenerator(algName[i][0],
                        algName[i][1], privateKeyInfoDamaged, null);
                EncryptedPrivateKeyInfo epki;
                if (g.ap() == null) {
                    epki = new EncryptedPrivateKeyInfo(algName[i][0], g.ct());
                } else {
                    epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                }
                try {
                    epki.getKeySpec(g.pubK() == null ? g.k() : g.pubK());
                    fail(algName[i][0] + ", " + algName[i][1]);
                } catch (InvalidKeyException e) {
                }
                performed = true;
            } catch (TestDataGenerator.AllowedFailure allowedFailure) {
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "NoSuchAlgorithmException can not be checking",
        method = "getKeySpec",
        args = {java.security.Key.class, java.lang.String.class}
    )
    public final void testGetKeySpecKeyString01() throws Exception {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                try {
                    epki.getKeySpec((Key) null, "SomeProviderName");
                    fail(getName() + "NullPointerException has not been thrown");
                } catch (NullPointerException ok) {
                }
                try {
                    epki.getKeySpec(new Key() {
                        public String getAlgorithm() {
                            return "alg";
                        }
                        public String getFormat() {
                            return "fmt";
                        }
                        public byte[] getEncoded() {
                            return new byte[] {};
                        }
                    }, "StrangeProviderName");
                    fail(getName() + "NoSuchProviderException has not been thrown");
                } catch (NoSuchProviderException ok) {
                }
                try {
                    epki.getKeySpec(new Key() {
                        public String getAlgorithm() {
                            return "alg";
                        }
                        public String getFormat() {
                            return "fmt";
                        }
                        public byte[] getEncoded() {
                            return new byte[] {};
                        }
                    }, (String) null);
                    fail(getName() + "NullPointerException has not been thrown");
                } catch (NullPointerException ok) {
                }
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "In subset missed NoSuchProviderException & NoSuchAlgorithmException checking",
        method = "getKeySpec",
        args = {java.security.Key.class, java.lang.String.class}
    )
    public final void test_ROUNDTRIP_GetKeySpecKeyString01() throws Exception {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            for (int l = 0; l < provider.length; l++) {
                if (provider[l] == null) {
                    continue;
                }
                TestDataGenerator g;
                try {
                    g = new TestDataGenerator(algName[i][0], algName[i][1],
                            privateKeyInfo, provider[l]);
                } catch (TestDataGenerator.AllowedFailure allowedFailure) {
                    continue;
                }
                try {
                    EncryptedPrivateKeyInfo epki;
                    if (g.ap() == null) {
                        epki = new EncryptedPrivateKeyInfo(algName[i][0], g
                                .ct());
                    } else {
                        epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                    }
                    try {
                        PKCS8EncodedKeySpec eks = epki.getKeySpec(
                                g.pubK() == null ? g.k() : g.pubK(),
                                provider[l].getName());
                        if (!Arrays.equals(privateKeyInfo, eks.getEncoded())) {
                            fail(algName[i][0] + " != " + algName[i][1]);
                        }
                    } catch (InvalidKeyException e) {
                        fail(algName[i][0] + ", " + algName[i][1] + ": " + e);
                    }
                    performed = true;
                } catch (NoSuchAlgorithmException allowedFailure) {
                }
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "In subset missed NoSuchProviderException & NoSuchAlgorithmException checking",
        method = "getKeySpec",
        args = {java.security.Key.class, java.lang.String.class}
    )
    public final void test_ROUNDTRIP_GetKeySpecKeyString02() throws Exception {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            for (int l = 0; l < provider.length; l++) {
                if (provider[l] == null) {
                    continue;
                }
                TestDataGenerator g;
                try {
                    g = new TestDataGenerator(algName[i][0], algName[i][1],
                            privateKeyInfoDamaged, provider[l]);
                } catch (TestDataGenerator.AllowedFailure allowedFailure) {
                    continue;
                }
                try {
                    EncryptedPrivateKeyInfo epki;
                    if (g.ap() == null) {
                        epki = new EncryptedPrivateKeyInfo(algName[i][0], g
                                .ct());
                    } else {
                        epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                    }
                    try {
                        epki.getKeySpec(g.pubK() == null ? g.k() : g.pubK(),
                                provider[l].getName());
                        fail(algName[i][0] + ", " + algName[i][1]);
                    } catch (InvalidKeyException e) {
                    }
                    performed = true;
                } catch (NoSuchAlgorithmException allowedFailure) {
                }
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check NoSuchAlgorithmException",
        method = "getKeySpec",
        args = {java.security.Key.class, java.security.Provider.class}
    )
    public final void testGetKeySpecKeyProvider01() throws Exception {
        boolean performed = false;
        for (int i = 0; i < EncryptedPrivateKeyInfoData.algName0.length; i++) {
            try {
                EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(
                        EncryptedPrivateKeyInfoData.algName0[i][0],
                        EncryptedPrivateKeyInfoData.encryptedData);
                try {
                    epki.getKeySpec((Key) null, (Provider) null);
                    fail(getName() + "NullPointerException has not been thrown");
                } catch (NullPointerException ok) {
                }
                try {
                    epki.getKeySpec(new Key() {
                        public String getAlgorithm() {
                            return "alg";
                        }
                        public String getFormat() {
                            return "fmt";
                        }
                        public byte[] getEncoded() {
                            return new byte[] {};
                        }
                    }, (Provider) null);
                    fail(getName() + "NullPointerException has not been thrown");
                } catch (NullPointerException ok) {
                }
                performed = true;
            } catch (NoSuchAlgorithmException allowedFailure) {
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check NoSuchAlgorithmException",
        method = "getKeySpec",
        args = {java.security.Key.class, java.security.Provider.class}
    )
    public final void test_ROUNDTRIP_GetKeySpecKeyProvider01() {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            for (int l = 0; l < provider.length; l++) {
                if (provider[l] == null) {
                    continue;
                }
                TestDataGenerator g;
                try {
                    g = new TestDataGenerator(algName[i][0], algName[i][1],
                            privateKeyInfo, provider[l]);
                } catch (TestDataGenerator.AllowedFailure allowedFailure) {
                    continue;
                }
                try {
                    EncryptedPrivateKeyInfo epki;
                    if (g.ap() == null) {
                        epki = new EncryptedPrivateKeyInfo(algName[i][0], g
                                .ct());
                    } else {
                        epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                    }
                    try {
                        PKCS8EncodedKeySpec eks = epki.getKeySpec(
                                g.pubK() == null ? g.k() : g.pubK(),
                                provider[l]);
                        if (!Arrays.equals(privateKeyInfo, eks.getEncoded())) {
                            fail(algName[i][0] + " != " + algName[i][1]);
                        }
                    } catch (InvalidKeyException e) {
                        fail(algName[i][0] + ", " + algName[i][1] + ": " + e);
                    }
                    performed = true;
                } catch (NoSuchAlgorithmException allowedFailure) {
                }
            }
        }
        assertTrue("Test not performed", performed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Can not check NoSuchAlgorithmException",
        method = "getKeySpec",
        args = {java.security.Key.class, java.security.Provider.class}
    )
    public final void test_ROUNDTRIP_GetKeySpecKeyProvider02() {
        boolean performed = false;
        for (int i = 0; i < algName.length; i++) {
            for (int l = 0; l < provider.length; l++) {
                if (provider[l] == null) {
                    continue;
                }
                TestDataGenerator g;
                try {
                    g = new TestDataGenerator(algName[i][0], algName[i][1],
                            privateKeyInfoDamaged, provider[l]);
                } catch (TestDataGenerator.AllowedFailure allowedFailure) {
                    continue;
                }
                try {
                    EncryptedPrivateKeyInfo epki;
                    if (g.ap() == null) {
                        epki = new EncryptedPrivateKeyInfo(algName[i][0], g
                                .ct());
                    } else {
                        epki = new EncryptedPrivateKeyInfo(g.ap(), g.ct());
                    }
                    try {
                        epki.getKeySpec(g.pubK() == null ? g.k() : g.pubK(),
                                provider[l]);
                        fail(algName[i][0] + ", " + algName[i][1]);
                    } catch (InvalidKeyException e) {
                    }
                    performed = true;
                } catch (NoSuchAlgorithmException allowedFailure) {
                }
            }
        }
        assertTrue("Test not performed", performed);
    }
    public static class TestDataGenerator {
        public static class AllowedFailure extends Exception {
            AllowedFailure(String msg) {
                super(msg);
            }
        }
        private Cipher c = null;
        private Key k = null, pubK = null;
        private AlgorithmParameters ap = null;
        byte[] ct;
        public TestDataGenerator(String algName, String transformation,
                byte[] privateKeyInfo, Provider provider) throws AllowedFailure {
            try {
                c = (provider == null) ? Cipher
                        .getInstance(transformation != null ? transformation
                                : algName) : Cipher.getInstance(
                        transformation != null ? transformation : algName,
                        provider);
            } catch (NoSuchAlgorithmException e) {
                throw new AllowedFailure(e.getMessage());
            } catch (NoSuchPaddingException e) {
                throw new AllowedFailure(e.getMessage());
            }
            try {
                KeyGenerator kg = (provider == null) ? KeyGenerator
                        .getInstance(algName) : KeyGenerator.getInstance(
                        algName, provider);
                k = kg.generateKey();
            } catch (NoSuchAlgorithmException e) {
            }
            if (k == null) {
                try {
                    KeyPairGenerator kpg = (provider == null) ? KeyPairGenerator
                            .getInstance(algName)
                            : KeyPairGenerator.getInstance(algName, provider);
                    KeyPair kp = kpg.genKeyPair();
                    k = kp.getPrivate();
                    pubK = kp.getPublic();
                } catch (NoSuchAlgorithmException e) {
                }
            }
            PBEParameterSpec pbeParamSpec = null;
            if (k == null) {
                try {
                    pbeParamSpec = new PBEParameterSpec(new byte[] { 1, 2, 3,
                            4, 5, 6, 7, 8 }, 10);
                    SecretKeyFactory skf = (provider == null) ? SecretKeyFactory
                            .getInstance(algName)
                            : SecretKeyFactory.getInstance(algName, provider);
                    PBEKeySpec ks = new PBEKeySpec("12345678".toCharArray());
                    try {
                        k = skf.generateSecret(ks);
                    } catch (InvalidKeySpecException e) {
                        throw new AllowedFailure(e.getMessage());
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new AllowedFailure(e.getMessage());
                }
            }
            try {
                if (pbeParamSpec == null) {
                    c.init(Cipher.ENCRYPT_MODE, k);
                } else {
                    c.init(Cipher.ENCRYPT_MODE, k, pbeParamSpec);
                }
            } catch (InvalidKeyException e) {
                throw new AllowedFailure(e.getMessage());
            } catch (SecurityException e) {
                throw new AllowedFailure(e.getMessage());
            } catch (InvalidAlgorithmParameterException e) {
                throw new AllowedFailure(e.getMessage());
            }
            ap = c.getParameters();
            try {
                ct = c.doFinal(privateKeyInfo);
            } catch (IllegalStateException e) {
                throw new AllowedFailure(e.getMessage());
            } catch (IllegalBlockSizeException e) {
                throw new AllowedFailure(e.getMessage());
            } catch (BadPaddingException e) {
                throw new AllowedFailure(e.getMessage());
            } catch (RuntimeException e) {
                throw new AllowedFailure(e.getMessage());
            }
            try {
                if (pbeParamSpec != null) {
                    try {
                        ap = (provider == null) ? AlgorithmParameters
                                .getInstance(algName) : AlgorithmParameters
                                .getInstance(algName, provider);
                        ap.init(pbeParamSpec);
                        pbeParamSpec = null;
                    } catch (NoSuchAlgorithmException e) {
                        throw new AllowedFailure(e.getMessage());
                    } catch (InvalidParameterSpecException e) {
                        throw new AllowedFailure(e.getMessage());
                    }
                }
                if (ap == null) {
                    c.init(Cipher.DECRYPT_MODE, pubK == null ? k : pubK);
                } else {
                    c.init(Cipher.DECRYPT_MODE, pubK == null ? k : pubK, ap);
                }
            } catch (InvalidKeyException e) {
                throw new AllowedFailure(e.getMessage());
            } catch (SecurityException e) {
                throw new AllowedFailure(e.getMessage());
            } catch (InvalidAlgorithmParameterException e) {
                throw new AllowedFailure(e.getMessage());
            }
        }
        public Key k() {
            return k;
        }
        public Key pubK() {
            return pubK;
        }
        public Cipher c() {
            return c;
        }
        public byte[] ct() {
            return ct;
        }
        public AlgorithmParameters ap() {
            return ap;
        }
    }
    private static final byte[] privateKeyInfo = { (byte) 0x30, (byte) 0x82,
            (byte) 0x02, (byte) 0x77, (byte) 0x02, (byte) 0x01, (byte) 0x00,
            (byte) 0x30, (byte) 0x0d, (byte) 0x06, (byte) 0x09, (byte) 0x2a,
            (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xf7, (byte) 0x0d,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x05, (byte) 0x00,
            (byte) 0x04, (byte) 0x82, (byte) 0x02, (byte) 0x61, (byte) 0x30,
            (byte) 0x82, (byte) 0x02, (byte) 0x5d, (byte) 0x02, (byte) 0x01,
            (byte) 0x00, (byte) 0x02, (byte) 0x81, (byte) 0x81, (byte) 0x00,
            (byte) 0xb2, (byte) 0x4a, (byte) 0x9b, (byte) 0x5b, (byte) 0xba,
            (byte) 0x01, (byte) 0xc0, (byte) 0xcd, (byte) 0x65, (byte) 0x09,
            (byte) 0x63, (byte) 0x70, (byte) 0x0b, (byte) 0x5a, (byte) 0x1b,
            (byte) 0x92, (byte) 0x08, (byte) 0xf8, (byte) 0x55, (byte) 0x5e,
            (byte) 0x7c, (byte) 0x1b, (byte) 0x50, (byte) 0x17, (byte) 0xec,
            (byte) 0x44, (byte) 0x4c, (byte) 0x58, (byte) 0x42, (byte) 0x2b,
            (byte) 0x41, (byte) 0x09, (byte) 0x59, (byte) 0xf2, (byte) 0xe1,
            (byte) 0x5d, (byte) 0x43, (byte) 0x71, (byte) 0x4d, (byte) 0x92,
            (byte) 0x03, (byte) 0x1d, (byte) 0xb6, (byte) 0x6c, (byte) 0x7f,
            (byte) 0x5d, (byte) 0x48, (byte) 0xcd, (byte) 0x17, (byte) 0xec,
            (byte) 0xd7, (byte) 0x4c, (byte) 0x39, (byte) 0xb1, (byte) 0x7b,
            (byte) 0xe2, (byte) 0xbf, (byte) 0x96, (byte) 0x77, (byte) 0xbe,
            (byte) 0xd0, (byte) 0xa0, (byte) 0xf0, (byte) 0x2d, (byte) 0x6b,
            (byte) 0x24, (byte) 0xaa, (byte) 0x14, (byte) 0xba, (byte) 0x82,
            (byte) 0x79, (byte) 0x10, (byte) 0x9b, (byte) 0x16, (byte) 0x68,
            (byte) 0x47, (byte) 0x81, (byte) 0x54, (byte) 0xa2, (byte) 0xfa,
            (byte) 0x91, (byte) 0x9e, (byte) 0x0a, (byte) 0x2a, (byte) 0x53,
            (byte) 0xa6, (byte) 0xe7, (byte) 0x9e, (byte) 0x7d, (byte) 0x29,
            (byte) 0x33, (byte) 0xd8, (byte) 0x05, (byte) 0xfc, (byte) 0x02,
            (byte) 0x3f, (byte) 0xbd, (byte) 0xc7, (byte) 0x6e, (byte) 0xed,
            (byte) 0xaa, (byte) 0x30, (byte) 0x6c, (byte) 0x5f, (byte) 0x52,
            (byte) 0xed, (byte) 0x35, (byte) 0x65, (byte) 0x4b, (byte) 0x0e,
            (byte) 0xc8, (byte) 0xa7, (byte) 0x12, (byte) 0x10, (byte) 0x56,
            (byte) 0x37, (byte) 0xaf, (byte) 0x11, (byte) 0xfa, (byte) 0x21,
            (byte) 0x0e, (byte) 0x99, (byte) 0xff, (byte) 0xfa, (byte) 0x8c,
            (byte) 0x65, (byte) 0x8e, (byte) 0x6d, (byte) 0x02, (byte) 0x03,
            (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x81,
            (byte) 0x80, (byte) 0x78, (byte) 0x41, (byte) 0x72, (byte) 0x40,
            (byte) 0x90, (byte) 0x59, (byte) 0x96, (byte) 0x5d, (byte) 0xf3,
            (byte) 0x84, (byte) 0x3d, (byte) 0x99, (byte) 0xd9, (byte) 0x4e,
            (byte) 0x51, (byte) 0xc2, (byte) 0x52, (byte) 0x62, (byte) 0x8d,
            (byte) 0xd2, (byte) 0x49, (byte) 0x0b, (byte) 0x73, (byte) 0x1e,
            (byte) 0x6f, (byte) 0xb2, (byte) 0x31, (byte) 0x7c, (byte) 0x66,
            (byte) 0x45, (byte) 0x1e, (byte) 0x7c, (byte) 0xdc, (byte) 0x3a,
            (byte) 0xc2, (byte) 0x5f, (byte) 0x51, (byte) 0x9a, (byte) 0x1e,
            (byte) 0xa4, (byte) 0x19, (byte) 0x8d, (byte) 0xf4, (byte) 0xf9,
            (byte) 0x81, (byte) 0x7e, (byte) 0xbe, (byte) 0x17, (byte) 0xf7,
            (byte) 0xc7, (byte) 0x3c, (byte) 0x00, (byte) 0xa1, (byte) 0xf9,
            (byte) 0x60, (byte) 0x82, (byte) 0x34, (byte) 0x8f, (byte) 0x9c,
            (byte) 0xfd, (byte) 0x0b, (byte) 0x63, (byte) 0x42, (byte) 0x1b,
            (byte) 0x7f, (byte) 0x45, (byte) 0xf1, (byte) 0x31, (byte) 0xc3,
            (byte) 0x63, (byte) 0x47, (byte) 0x5c, (byte) 0xc1, (byte) 0xb2,
            (byte) 0x5f, (byte) 0x57, (byte) 0xee, (byte) 0x02, (byte) 0x9f,
            (byte) 0x5e, (byte) 0x08, (byte) 0x48, (byte) 0xba, (byte) 0x74,
            (byte) 0xba, (byte) 0x81, (byte) 0xb7, (byte) 0x30, (byte) 0xac,
            (byte) 0x4c, (byte) 0x01, (byte) 0x35, (byte) 0xce, (byte) 0x46,
            (byte) 0x47, (byte) 0x8c, (byte) 0xe4, (byte) 0x62, (byte) 0x36,
            (byte) 0x1a, (byte) 0x65, (byte) 0x0e, (byte) 0x33, (byte) 0x56,
            (byte) 0xf9, (byte) 0xb7, (byte) 0xa0, (byte) 0xc4, (byte) 0xb6,
            (byte) 0x82, (byte) 0x55, (byte) 0x7d, (byte) 0x36, (byte) 0x55,
            (byte) 0xc0, (byte) 0x52, (byte) 0x5e, (byte) 0x35, (byte) 0x54,
            (byte) 0xbd, (byte) 0x97, (byte) 0x01, (byte) 0x00, (byte) 0xbf,
            (byte) 0x10, (byte) 0xdc, (byte) 0x1b, (byte) 0x51, (byte) 0x02,
            (byte) 0x41, (byte) 0x00, (byte) 0xe7, (byte) 0x68, (byte) 0x03,
            (byte) 0x3e, (byte) 0x21, (byte) 0x64, (byte) 0x68, (byte) 0x24,
            (byte) 0x7b, (byte) 0xd0, (byte) 0x31, (byte) 0xa0, (byte) 0xa2,
            (byte) 0xd9, (byte) 0x87, (byte) 0x6d, (byte) 0x79, (byte) 0x81,
            (byte) 0x8f, (byte) 0x8f, (byte) 0x2d, (byte) 0x7a, (byte) 0x95,
            (byte) 0x2e, (byte) 0x55, (byte) 0x9f, (byte) 0xd7, (byte) 0x86,
            (byte) 0x29, (byte) 0x93, (byte) 0xbd, (byte) 0x04, (byte) 0x7e,
            (byte) 0x4f, (byte) 0xdb, (byte) 0x56, (byte) 0xf1, (byte) 0x75,
            (byte) 0xd0, (byte) 0x4b, (byte) 0x00, (byte) 0x3a, (byte) 0xe0,
            (byte) 0x26, (byte) 0xf6, (byte) 0xab, (byte) 0x9e, (byte) 0x0b,
            (byte) 0x2a, (byte) 0xf4, (byte) 0xa8, (byte) 0xd7, (byte) 0xff,
            (byte) 0xbe, (byte) 0x01, (byte) 0xeb, (byte) 0x9b, (byte) 0x81,
            (byte) 0xc7, (byte) 0x5f, (byte) 0x02, (byte) 0x73, (byte) 0xe1,
            (byte) 0x2b, (byte) 0x02, (byte) 0x41, (byte) 0x00, (byte) 0xc5,
            (byte) 0x3d, (byte) 0x78, (byte) 0xab, (byte) 0xe6, (byte) 0xab,
            (byte) 0x3e, (byte) 0x29, (byte) 0xfd, (byte) 0x98, (byte) 0xd0,
            (byte) 0xa4, (byte) 0x3e, (byte) 0x58, (byte) 0xee, (byte) 0x48,
            (byte) 0x45, (byte) 0xa3, (byte) 0x66, (byte) 0xac, (byte) 0xe9,
            (byte) 0x4d, (byte) 0xbd, (byte) 0x60, (byte) 0xea, (byte) 0x24,
            (byte) 0xff, (byte) 0xed, (byte) 0x0c, (byte) 0x67, (byte) 0xc5,
            (byte) 0xfd, (byte) 0x36, (byte) 0x28, (byte) 0xea, (byte) 0x74,
            (byte) 0x88, (byte) 0xd1, (byte) 0xd1, (byte) 0xad, (byte) 0x58,
            (byte) 0xd7, (byte) 0xf0, (byte) 0x67, (byte) 0x20, (byte) 0xc1,
            (byte) 0xe3, (byte) 0xb3, (byte) 0xdb, (byte) 0x52, (byte) 0xad,
            (byte) 0xf3, (byte) 0xc4, (byte) 0x21, (byte) 0xd8, (byte) 0x8c,
            (byte) 0x4c, (byte) 0x41, (byte) 0x27, (byte) 0xdb, (byte) 0xd0,
            (byte) 0x35, (byte) 0x92, (byte) 0xc7, (byte) 0x02, (byte) 0x41,
            (byte) 0x00, (byte) 0xe0, (byte) 0x99, (byte) 0x42, (byte) 0xb4,
            (byte) 0x76, (byte) 0x02, (byte) 0x97, (byte) 0x55, (byte) 0xf9,
            (byte) 0xda, (byte) 0x3b, (byte) 0xa0, (byte) 0xd7, (byte) 0x0e,
            (byte) 0xdc, (byte) 0xf4, (byte) 0x33, (byte) 0x7f, (byte) 0xbd,
            (byte) 0xcf, (byte) 0xd0, (byte) 0xeb, (byte) 0x6e, (byte) 0x89,
            (byte) 0xf7, (byte) 0x4f, (byte) 0x5a, (byte) 0x07, (byte) 0x7c,
            (byte) 0xa9, (byte) 0x49, (byte) 0x47, (byte) 0x68, (byte) 0x35,
            (byte) 0xa8, (byte) 0x05, (byte) 0x3d, (byte) 0xfd, (byte) 0x04,
            (byte) 0x7b, (byte) 0x17, (byte) 0x31, (byte) 0x0d, (byte) 0xc8,
            (byte) 0xa3, (byte) 0x98, (byte) 0x34, (byte) 0xa0, (byte) 0x50,
            (byte) 0x44, (byte) 0x00, (byte) 0xf1, (byte) 0x0c, (byte) 0xe6,
            (byte) 0xe5, (byte) 0xc4, (byte) 0x41, (byte) 0x3d, (byte) 0xf8,
            (byte) 0x3d, (byte) 0x4e, (byte) 0x0b, (byte) 0x1c, (byte) 0xdb,
            (byte) 0x02, (byte) 0x41, (byte) 0x00, (byte) 0x82, (byte) 0x9b,
            (byte) 0x8a, (byte) 0xfd, (byte) 0xa1, (byte) 0x98, (byte) 0x41,
            (byte) 0x68, (byte) 0xc2, (byte) 0xd1, (byte) 0xdf, (byte) 0x4e,
            (byte) 0xf3, (byte) 0x2e, (byte) 0x26, (byte) 0x53, (byte) 0x5b,
            (byte) 0x31, (byte) 0xb1, (byte) 0x7a, (byte) 0xcc, (byte) 0x5e,
            (byte) 0xbb, (byte) 0x09, (byte) 0xa2, (byte) 0xe2, (byte) 0x6f,
            (byte) 0x4a, (byte) 0x04, (byte) 0x0d, (byte) 0xef, (byte) 0x90,
            (byte) 0x15, (byte) 0xbe, (byte) 0x10, (byte) 0x4a, (byte) 0xac,
            (byte) 0x92, (byte) 0xeb, (byte) 0xda, (byte) 0x72, (byte) 0xdb,
            (byte) 0x43, (byte) 0x08, (byte) 0xb7, (byte) 0x2b, (byte) 0x4c,
            (byte) 0xe1, (byte) 0xbb, (byte) 0x58, (byte) 0xcb, (byte) 0x71,
            (byte) 0x80, (byte) 0xad, (byte) 0xbc, (byte) 0xdc, (byte) 0x62,
            (byte) 0x5e, (byte) 0x3e, (byte) 0xcb, (byte) 0x92, (byte) 0xda,
            (byte) 0xf6, (byte) 0xdf, (byte) 0x02, (byte) 0x40, (byte) 0x4d,
            (byte) 0x81, (byte) 0x90, (byte) 0xc5, (byte) 0x77, (byte) 0x30,
            (byte) 0xb7, (byte) 0x29, (byte) 0x00, (byte) 0xa8, (byte) 0xf1,
            (byte) 0xb4, (byte) 0xae, (byte) 0x52, (byte) 0x63, (byte) 0x00,
            (byte) 0xb2, (byte) 0x2d, (byte) 0x3e, (byte) 0x7d, (byte) 0xd6,
            (byte) 0x4d, (byte) 0xf9, (byte) 0x8a, (byte) 0xc1, (byte) 0xb1,
            (byte) 0x98, (byte) 0x89, (byte) 0x52, (byte) 0x40, (byte) 0x14,
            (byte) 0x1b, (byte) 0x0e, (byte) 0x61, (byte) 0x8f, (byte) 0xf4,
            (byte) 0xbe, (byte) 0x59, (byte) 0x79, (byte) 0x79, (byte) 0x95,
            (byte) 0x19, (byte) 0x5c, (byte) 0x51, (byte) 0x08, (byte) 0x66,
            (byte) 0xc1, (byte) 0x42, (byte) 0x30, (byte) 0xb3, (byte) 0x7a,
            (byte) 0x86, (byte) 0x9f, (byte) 0x3e, (byte) 0xf5, (byte) 0x19,
            (byte) 0xa3, (byte) 0xae, (byte) 0x64, (byte) 0x69, (byte) 0x14,
            (byte) 0x07, (byte) 0x50, (byte) 0x97, };
    private static final byte[] privateKeyInfoDamaged = { (byte) 0x30,
            (byte) 0x82, (byte) 0x02, (byte) 0x77, (byte) 0x02, (byte) 0x01,
            (byte) 0x00, (byte) 0x30, (byte) 0x0d, (byte) 0x06, (byte) 0x09,
            (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xf7,
            (byte) 0x0d, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x05,
            (byte) 0x00, (byte) 0x04, 
            (byte) 0x82, (byte) 0x02, (byte) 0x62, 
            (byte) 0x30, (byte) 0x82, (byte) 0x02, (byte) 0x5d, (byte) 0x02,
            (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x81, (byte) 0x81,
            (byte) 0x00, (byte) 0xb2, (byte) 0x4a, (byte) 0x9b, (byte) 0x5b,
            (byte) 0xba, (byte) 0x01, (byte) 0xc0, (byte) 0xcd, (byte) 0x65,
            (byte) 0x09, (byte) 0x63, (byte) 0x70, (byte) 0x0b, (byte) 0x5a,
            (byte) 0x1b, (byte) 0x92, (byte) 0x08, (byte) 0xf8, (byte) 0x55,
            (byte) 0x5e, (byte) 0x7c, (byte) 0x1b, (byte) 0x50, (byte) 0x17,
            (byte) 0xec, (byte) 0x44, (byte) 0x4c, (byte) 0x58, (byte) 0x42,
            (byte) 0x2b, (byte) 0x41, (byte) 0x09, (byte) 0x59, (byte) 0xf2,
            (byte) 0xe1, (byte) 0x5d, (byte) 0x43, (byte) 0x71, (byte) 0x4d,
            (byte) 0x92, (byte) 0x03, (byte) 0x1d, (byte) 0xb6, (byte) 0x6c,
            (byte) 0x7f, (byte) 0x5d, (byte) 0x48, (byte) 0xcd, (byte) 0x17,
            (byte) 0xec, (byte) 0xd7, (byte) 0x4c, (byte) 0x39, (byte) 0xb1,
            (byte) 0x7b, (byte) 0xe2, (byte) 0xbf, (byte) 0x96, (byte) 0x77,
            (byte) 0xbe, (byte) 0xd0, (byte) 0xa0, (byte) 0xf0, (byte) 0x2d,
            (byte) 0x6b, (byte) 0x24, (byte) 0xaa, (byte) 0x14, (byte) 0xba,
            (byte) 0x82, (byte) 0x79, (byte) 0x10, (byte) 0x9b, (byte) 0x16,
            (byte) 0x68, (byte) 0x47, (byte) 0x81, (byte) 0x54, (byte) 0xa2,
            (byte) 0xfa, (byte) 0x91, (byte) 0x9e, (byte) 0x0a, (byte) 0x2a,
            (byte) 0x53, (byte) 0xa6, (byte) 0xe7, (byte) 0x9e, (byte) 0x7d,
            (byte) 0x29, (byte) 0x33, (byte) 0xd8, (byte) 0x05, (byte) 0xfc,
            (byte) 0x02, (byte) 0x3f, (byte) 0xbd, (byte) 0xc7, (byte) 0x6e,
            (byte) 0xed, (byte) 0xaa, (byte) 0x30, (byte) 0x6c, (byte) 0x5f,
            (byte) 0x52, (byte) 0xed, (byte) 0x35, (byte) 0x65, (byte) 0x4b,
            (byte) 0x0e, (byte) 0xc8, (byte) 0xa7, (byte) 0x12, (byte) 0x10,
            (byte) 0x56, (byte) 0x37, (byte) 0xaf, (byte) 0x11, (byte) 0xfa,
            (byte) 0x21, (byte) 0x0e, (byte) 0x99, (byte) 0xff, (byte) 0xfa,
            (byte) 0x8c, (byte) 0x65, (byte) 0x8e, (byte) 0x6d, (byte) 0x02,
            (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02,
            (byte) 0x81, (byte) 0x80, (byte) 0x78, (byte) 0x41, (byte) 0x72,
            (byte) 0x40, (byte) 0x90, (byte) 0x59, (byte) 0x96, (byte) 0x5d,
            (byte) 0xf3, (byte) 0x84, (byte) 0x3d, (byte) 0x99, (byte) 0xd9,
            (byte) 0x4e, (byte) 0x51, (byte) 0xc2, (byte) 0x52, (byte) 0x62,
            (byte) 0x8d, (byte) 0xd2, (byte) 0x49, (byte) 0x0b, (byte) 0x73,
            (byte) 0x1e, (byte) 0x6f, (byte) 0xb2, (byte) 0x31, (byte) 0x7c,
            (byte) 0x66, (byte) 0x45, (byte) 0x1e, (byte) 0x7c, (byte) 0xdc,
            (byte) 0x3a, (byte) 0xc2, (byte) 0x5f, (byte) 0x51, (byte) 0x9a,
            (byte) 0x1e, (byte) 0xa4, (byte) 0x19, (byte) 0x8d, (byte) 0xf4,
            (byte) 0xf9, (byte) 0x81, (byte) 0x7e, (byte) 0xbe, (byte) 0x17,
            (byte) 0xf7, (byte) 0xc7, (byte) 0x3c, (byte) 0x00, (byte) 0xa1,
            (byte) 0xf9, (byte) 0x60, (byte) 0x82, (byte) 0x34, (byte) 0x8f,
            (byte) 0x9c, (byte) 0xfd, (byte) 0x0b, (byte) 0x63, (byte) 0x42,
            (byte) 0x1b, (byte) 0x7f, (byte) 0x45, (byte) 0xf1, (byte) 0x31,
            (byte) 0xc3, (byte) 0x63, (byte) 0x47, (byte) 0x5c, (byte) 0xc1,
            (byte) 0xb2, (byte) 0x5f, (byte) 0x57, (byte) 0xee, (byte) 0x02,
            (byte) 0x9f, (byte) 0x5e, (byte) 0x08, (byte) 0x48, (byte) 0xba,
            (byte) 0x74, (byte) 0xba, (byte) 0x81, (byte) 0xb7, (byte) 0x30,
            (byte) 0xac, (byte) 0x4c, (byte) 0x01, (byte) 0x35, (byte) 0xce,
            (byte) 0x46, (byte) 0x47, (byte) 0x8c, (byte) 0xe4, (byte) 0x62,
            (byte) 0x36, (byte) 0x1a, (byte) 0x65, (byte) 0x0e, (byte) 0x33,
            (byte) 0x56, (byte) 0xf9, (byte) 0xb7, (byte) 0xa0, (byte) 0xc4,
            (byte) 0xb6, (byte) 0x82, (byte) 0x55, (byte) 0x7d, (byte) 0x36,
            (byte) 0x55, (byte) 0xc0, (byte) 0x52, (byte) 0x5e, (byte) 0x35,
            (byte) 0x54, (byte) 0xbd, (byte) 0x97, (byte) 0x01, (byte) 0x00,
            (byte) 0xbf, (byte) 0x10, (byte) 0xdc, (byte) 0x1b, (byte) 0x51,
            (byte) 0x02, (byte) 0x41, (byte) 0x00, (byte) 0xe7, (byte) 0x68,
            (byte) 0x03, (byte) 0x3e, (byte) 0x21, (byte) 0x64, (byte) 0x68,
            (byte) 0x24, (byte) 0x7b, (byte) 0xd0, (byte) 0x31, (byte) 0xa0,
            (byte) 0xa2, (byte) 0xd9, (byte) 0x87, (byte) 0x6d, (byte) 0x79,
            (byte) 0x81, (byte) 0x8f, (byte) 0x8f, (byte) 0x2d, (byte) 0x7a,
            (byte) 0x95, (byte) 0x2e, (byte) 0x55, (byte) 0x9f, (byte) 0xd7,
            (byte) 0x86, (byte) 0x29, (byte) 0x93, (byte) 0xbd, (byte) 0x04,
            (byte) 0x7e, (byte) 0x4f, (byte) 0xdb, (byte) 0x56, (byte) 0xf1,
            (byte) 0x75, (byte) 0xd0, (byte) 0x4b, (byte) 0x00, (byte) 0x3a,
            (byte) 0xe0, (byte) 0x26, (byte) 0xf6, (byte) 0xab, (byte) 0x9e,
            (byte) 0x0b, (byte) 0x2a, (byte) 0xf4, (byte) 0xa8, (byte) 0xd7,
            (byte) 0xff, (byte) 0xbe, (byte) 0x01, (byte) 0xeb, (byte) 0x9b,
            (byte) 0x81, (byte) 0xc7, (byte) 0x5f, (byte) 0x02, (byte) 0x73,
            (byte) 0xe1, (byte) 0x2b, (byte) 0x02, (byte) 0x41, (byte) 0x00,
            (byte) 0xc5, (byte) 0x3d, (byte) 0x78, (byte) 0xab, (byte) 0xe6,
            (byte) 0xab, (byte) 0x3e, (byte) 0x29, (byte) 0xfd, 
            (byte) 0x98, (byte) 0xd0, (byte) 0xa4, (byte) 0x3e, (byte) 0x58,
            (byte) 0xee, (byte) 0x48, (byte) 0x45, (byte) 0xa3, (byte) 0x66,
            (byte) 0xac, (byte) 0xe9, (byte) 0x4d, (byte) 0xbd, (byte) 0x60,
            (byte) 0xea, (byte) 0x24, (byte) 0xff, (byte) 0xed, (byte) 0x0c,
            (byte) 0x67, (byte) 0xc5, (byte) 0xfd, (byte) 0x36, (byte) 0x28,
            (byte) 0xea, (byte) 0x74, (byte) 0x88, (byte) 0xd1, (byte) 0xd1,
            (byte) 0xad, (byte) 0x58, (byte) 0xd7, (byte) 0xf0, (byte) 0x67,
            (byte) 0x20, (byte) 0xc1, (byte) 0xe3, (byte) 0xb3, (byte) 0xdb,
            (byte) 0x52, (byte) 0xad, (byte) 0xf3, (byte) 0xc4, (byte) 0x21,
            (byte) 0xd8, (byte) 0x8c, (byte) 0x4c, (byte) 0x41, (byte) 0x27,
            (byte) 0xdb, (byte) 0xd0, (byte) 0x35, (byte) 0x92, (byte) 0xc7,
            (byte) 0x02, (byte) 0x41, (byte) 0x00, (byte) 0xe0, (byte) 0x99,
            (byte) 0x42, (byte) 0xb4, (byte) 0x76, (byte) 0x02, (byte) 0x97,
            (byte) 0x55, (byte) 0xf9, (byte) 0xda, (byte) 0x3b, (byte) 0xa0,
            (byte) 0xd7, (byte) 0x0e, (byte) 0xdc, (byte) 0xf4, (byte) 0x33,
            (byte) 0x7f, (byte) 0xbd, (byte) 0xcf, (byte) 0xd0, (byte) 0xeb,
            (byte) 0x6e, (byte) 0x89, (byte) 0xf7, (byte) 0x4f, (byte) 0x5a,
            (byte) 0x07, (byte) 0x7c, (byte) 0xa9, (byte) 0x49, (byte) 0x47,
            (byte) 0x68, (byte) 0x35, (byte) 0xa8, (byte) 0x05, (byte) 0x3d,
            (byte) 0xfd, (byte) 0x04, (byte) 0x7b, (byte) 0x17, (byte) 0x31,
            (byte) 0x0d, (byte) 0xc8, (byte) 0xa3, (byte) 0x98, (byte) 0x34,
            (byte) 0xa0, (byte) 0x50, (byte) 0x44, (byte) 0x00, (byte) 0xf1,
            (byte) 0x0c, (byte) 0xe6, (byte) 0xe5, (byte) 0xc4, (byte) 0x41,
            (byte) 0x3d, (byte) 0xf8, (byte) 0x3d, (byte) 0x4e, (byte) 0x0b, 
            (byte) 0x1c, (byte) 0xdb, (byte) 0x02, (byte) 0x41, (byte) 0x00,
            (byte) 0x82, (byte) 0x9b, (byte) 0x8a, (byte) 0xfd, (byte) 0xa1,
            (byte) 0x98, (byte) 0x41, (byte) 0x68, (byte) 0xc2, (byte) 0xd1,
            (byte) 0xdf, (byte) 0x4e, (byte) 0xf3, (byte) 0x2e, (byte) 0x26,
            (byte) 0x53, (byte) 0x5b, (byte) 0x31, (byte) 0xb1, (byte) 0x7a,
            (byte) 0xcc, (byte) 0x5e, (byte) 0xbb, (byte) 0x09, (byte) 0xa2,
            (byte) 0xe2, (byte) 0x6f, (byte) 0x4a, (byte) 0x04, (byte) 0x0d,
            (byte) 0xef, (byte) 0x90, (byte) 0x15, (byte) 0xbe, (byte) 0x10,
            (byte) 0x4a, (byte) 0xac, (byte) 0x92, (byte) 0xeb, (byte) 0xda,
            (byte) 0x72, (byte) 0xdb, (byte) 0x43, (byte) 0x08, (byte) 0xb7,
            (byte) 0x2b, (byte) 0x4c, (byte) 0xe1, (byte) 0xbb, (byte) 0x58,
            (byte) 0xcb, (byte) 0x71, (byte) 0x80, (byte) 0xad, (byte) 0xbc,
            (byte) 0xdc, (byte) 0x62, (byte) 0x5e, (byte) 0x3e, (byte) 0xcb,
            (byte) 0x92, (byte) 0xda, (byte) 0xf6, (byte) 0xdf, (byte) 0x02,
            (byte) 0x40, (byte) 0x4d, (byte) 0x81, (byte) 0x90, (byte) 0xc5,
            (byte) 0x77, (byte) 0x30, (byte) 0xb7, (byte) 0x29, (byte) 0x00,
            (byte) 0xa8, (byte) 0xf1, (byte) 0xb4, (byte) 0xae, (byte) 0x52,
            (byte) 0x63, (byte) 0x00, (byte) 0xb2, 
            (byte) 0x2d, (byte) 0x3e, (byte) 0x7d, (byte) 0xd6, (byte) 0x4d,
            (byte) 0xf9, (byte) 0x8a, (byte) 0xc1, (byte) 0xb1, (byte) 0x98,
            (byte) 0x89, (byte) 0x52, (byte) 0x40, (byte) 0x14, (byte) 0x1b,
            (byte) 0x0e, (byte) 0x61, (byte) 0x8f, (byte) 0xf4, (byte) 0xbe,
            (byte) 0x59, (byte) 0x79, (byte) 0x79, (byte) 0x95, (byte) 0x19,
            (byte) 0x5c, (byte) 0x51, (byte) 0x08, (byte) 0x66, (byte) 0xc1,
            (byte) 0x42, (byte) 0x30, (byte) 0xb3, (byte) 0x7a, (byte) 0x86,
            (byte) 0x9f, (byte) 0x3e, (byte) 0xf5, (byte) 0x19, (byte) 0xa3, 
            (byte) 0xae, (byte) 0x64, (byte) 0x69, (byte) 0x14, (byte) 0x07,
            (byte) 0x50, (byte) 0x97, };
}

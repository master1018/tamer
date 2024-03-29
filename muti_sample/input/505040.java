@TestTargetClass(KeyStore.Builder.class)
public class KeyStoreBuilderTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    private static char[] pass = { 's', 't', 'o', 'r', 'e', 'p', 'w', 'd' };
    private KeyStore.PasswordProtection protPass = new KeyStore.PasswordProtection(
            pass);
    private tmpCallbackHandler tmpCall = new tmpCallbackHandler();
    private KeyStore.CallbackHandlerProtection callbackHand = new KeyStore.CallbackHandlerProtection(
            tmpCall);
    private MyProtectionParameter myProtParam = new MyProtectionParameter(
            new byte[5]);
    public static String[] validValues = KeyStoreTestSupport.validValues;
    private static String defaultType = KeyStoreTestSupport.defaultType;
    private static Provider defaultProvider = null;
    static {
        defaultProvider = Security.getProviders(
                "KeyStore." + KeyStore.getDefaultType())[0];
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "Builder",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getKeyStore",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getProtectionParameter",
            args = {java.lang.String.class}
        )
    })
    public void testConstructor() {
        KeyStoreBuilder ksb;
        try {
            ksb = new KeyStoreBuilder();
            assertNotNull(ksb);
            ksb.getKeyStore();
            ksb.getProtectionParameter("test");
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newInstance",
        args = {java.security.KeyStore.class, java.security.KeyStore.ProtectionParameter.class}
    )
    public void testNewInstanceKeyStoreProtectionParameter()
            throws KeyStoreException, NoSuchAlgorithmException, IOException,
            CertificateException {
        try {
            KeyStore.Builder.newInstance(null, null);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        try {
            KeyStore.Builder.newInstance(null, protPass);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        KeyStore.Builder ksB;
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            KeyStore.Builder.newInstance(ks, null);
            fail("NullPointerException must be thrown when ProtectionParameter is null");
        } catch (NullPointerException e) {
        }
        KeyStore.PasswordProtection protPass1 = new KeyStore.PasswordProtection(
                pass);
        KeyStore.ProtectionParameter[] pp = { protPass, protPass1,
                callbackHand, myProtParam };
       for (int i = 0; i < pp.length; i++) {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            try {
                KeyStore.Builder.newInstance(ks, pp[i]);
                fail("IllegalArgumentException must be thrown because KeyStore was not initialized");
            } catch (IllegalArgumentException e) {
            }
            ks.load(null, pass);
            ksB = KeyStore.Builder.newInstance(ks, pp[i]);
            assertEquals("Incorrect KeyStore", ksB.getKeyStore().size(), 0);
            ksB = KeyStore.Builder.newInstance(ks, pp[i]);
            assertEquals("Incorrect KeyStore", ks, ksB.getKeyStore());
            try {
                ksB.getProtectionParameter(null);
                fail("NullPointerException must be thrown");
            } catch (NullPointerException e) {
            }
            try {
                assertEquals(ksB.getProtectionParameter("aaa"), pp[i]);
            } catch (KeyStoreException e) {
                fail("Unexpected: " + e.toString() + " was thrown");
            }
            try {
                assertEquals(ksB.getProtectionParameter("Bad alias"), pp[i]);
            } catch (KeyStoreException e) {
            }
            try {
                assertEquals(ksB.getProtectionParameter(""), pp[i]);
            } catch (KeyStoreException e) {
            }
            KeyStore.ProtectionParameter pPar = ksB
                    .getProtectionParameter("aaa");
            switch (i) {
            case 0:
                assertTrue(pPar instanceof KeyStore.PasswordProtection);
                break;
            case 1:
                assertTrue(pPar instanceof KeyStore.PasswordProtection);
                break;
            case 2:
                assertTrue(pPar instanceof KeyStore.CallbackHandlerProtection);
                break;
            case 3:
                assertTrue(pPar instanceof MyProtectionParameter);
                break;
            default:
                fail("Incorrect protection parameter");
            }
            assertEquals(pPar, pp[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newInstance",
        args = {java.lang.String.class, java.security.Provider.class, java.io.File.class, java.security.KeyStore.ProtectionParameter.class}
    )
    public void testNewInstanceStringProviderFileProtectionParameter()
            throws Exception {
        File fl = File.createTempFile("KSBuilder_ImplTest", "keystore");
        fl.deleteOnExit();
        KeyStore.Builder ksB;
        KeyStore.Builder ksB1;
        KeyStore ks = null;
        KeyStore ks1 = null;
        MyProtectionParameter myPP = new MyProtectionParameter(new byte[5]);
        try {
            KeyStore.Builder.newInstance(null, defaultProvider, fl, protPass);
            fail("NullPointerException must be thrown when type is null");
        } catch (NullPointerException e) {
        }
        try {
            KeyStore.Builder.newInstance(KeyStore.getDefaultType(), defaultProvider, null,
                    protPass);
            fail("NullPointerException must be thrown when file is null");
        } catch (NullPointerException e) {
        }
        try {
            KeyStore.Builder
                    .newInstance(KeyStore.getDefaultType(), defaultProvider, fl, null);
            fail("NullPointerException must be thrown when ProtectionParameter is null");
        } catch (NullPointerException e) {
        }
        try {
            KeyStore.Builder
                    .newInstance(KeyStore.getDefaultType(), defaultProvider, fl, myPP);
            fail("IllegalArgumentException must be thrown when ProtectionParameter is not correct");
        } catch (IllegalArgumentException e) {
        }
        try {
            KeyStore.Builder.newInstance(KeyStore.getDefaultType(), defaultProvider,
                    new File(fl.getAbsolutePath().concat("should_absent")),
                    protPass);
            fail("IllegalArgumentException must be thrown when file does not exist");
        } catch (IllegalArgumentException e) {
        }
        try {
            KeyStore.Builder.newInstance(KeyStore.getDefaultType(), defaultProvider, fl
                    .getParentFile(), protPass);
            fail("IllegalArgumentException must be thrown when file does not exist");
        } catch (IllegalArgumentException e) {
        }
        ksB = KeyStore.Builder.newInstance(KeyStore.getDefaultType(), defaultProvider, fl,
                protPass);
        try {
            ksB.getKeyStore();
            fail("KeyStoreException must be throw because file is empty");
        } catch (KeyStoreException e) {
        }
        fl = createKS();
        try {
            KeyStore.Builder.newInstance(KeyStore.getDefaultType(),
                    null, fl, myPP);
            fail("IllegalArgumentException must be "
                    + "thrown for incorrect ProtectionParameter");
        } catch (IllegalArgumentException e) {
        }
        try {
            KeyStore.Builder.newInstance(KeyStore.getDefaultType(),
                    defaultProvider, fl, myPP);
            fail("IllegalArgumentException must be "
                    + "thrown for incorrect ProtectionParameter");
        } catch (IllegalArgumentException e) {
        }
        ksB = KeyStore.Builder.newInstance(KeyStore.getDefaultType(),
                null, fl, protPass);
        ksB1 = KeyStore.Builder.newInstance(KeyStore.getDefaultType(),
                defaultProvider, fl, protPass);
        try {
            ks = ksB.getKeyStore();
        } catch (KeyStoreException e) {
            fail("Unexpected KeyException was thrown");
        }
        try {
            ks1 = ksB1.getKeyStore();
        } catch (KeyStoreException e) {
            fail("Unexpected KeyException was thrown: " + e.getMessage());
        }
        assertEquals("Incorrect KeyStore size", ks.size(), ks1.size());
        ;
        for (Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements(); ) {
            String aName = aliases.nextElement();
            try {
                assertEquals("Incorrect ProtectionParameter", ksB
                        .getProtectionParameter(aName), protPass);
            } catch (Exception e) {
                fail("Unexpected: " + e.toString()
                        + " was thrown for alias: " + aName);
            }
        }
        ksB.getKeyStore();
        try {
            assertEquals(ksB.getProtectionParameter("Bad alias"), protPass);
        } catch (KeyStoreException e) {
        }
        for (Enumeration<String> aliases = ks1.aliases(); aliases.hasMoreElements(); ) {
            String aName = aliases.nextElement();
            assertEquals("Incorrect ProtectionParameter", ksB1
                    .getProtectionParameter(aName), protPass);
        }
        try {
            assertEquals(ksB1.getProtectionParameter("Bad alias"), protPass);
        } catch (KeyStoreException e) {
        }
        ksB = KeyStore.Builder.newInstance(KeyStore.getDefaultType(),
                null, fl, callbackHand);
        ksB1 = KeyStore.Builder.newInstance(KeyStore.getDefaultType(),
                defaultProvider, fl, callbackHand);
        try {
            ks = ksB.getKeyStore();
            fail("KeyStoreException must be thrown for incorrect "
                    + "ProtectionParameter");
        } catch (KeyStoreException e) {
        }
        try {
            ks1 = ksB1.getKeyStore();
            fail("KeyStoreException must be thrown for incorrect "
                    + "ProtectionParameter");
        } catch (KeyStoreException e) {
        }
        assertEquals("Incorrect KeyStore size", ks.size(), ks1.size());
        for (Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements();) {
            String aName = aliases.nextElement();
            try {
                assertEquals("Incorrect ProtectionParameter", ksB
                        .getProtectionParameter(aName), callbackHand);
            } catch (Exception e) {
                fail("Unexpected: " + e.toString()
                        + " was thrown for alias: " + aName);
            }
        }
        for (Enumeration<String> iter = ks1.aliases(); iter.hasMoreElements();) {
            String aName = iter.nextElement();
            assertEquals("Incorrect ProtectionParameter", ksB1
                    .getProtectionParameter(aName), callbackHand);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newInstance",
        args = {java.lang.String.class, java.security.Provider.class, java.security.KeyStore.ProtectionParameter.class}
    )
    public void testNewInstanceStringProviderProtectionParameter()
            throws KeyStoreException {
        try {
            KeyStore.Builder.newInstance(null, defaultProvider, protPass);
            fail("NullPointerException must be thrown when type is null");
        } catch (NullPointerException e) {
        }
        try {
            KeyStore.Builder.newInstance(defaultType, defaultProvider, null);
            fail("NullPointerException must be thrown when ProtectionParameter is null");
        } catch (NullPointerException e) {
        }
        MyProtectionParameter myPP = new MyProtectionParameter(new byte[5]);
        KeyStore.ProtectionParameter[] pp = { protPass, myPP, callbackHand };
        KeyStore.Builder ksB, ksB1;
        KeyStore ks = null;
        for (int i = 0; i < pp.length; i++) {
            ksB = KeyStore.Builder.newInstance(defaultType, defaultProvider,
                    pp[i]);
            ksB1 = KeyStore.Builder.newInstance(defaultType, null, pp[i]);
            switch (i) {
            case 0:
                try {
                    ks = ksB.getKeyStore();
                    assertNotNull("KeyStore is null", ks);
                    try {
                        assertEquals(ksB.getProtectionParameter("Bad alias"),
                                pp[i]);
                    } catch (KeyStoreException e) {
                    }
                    ks = ksB1.getKeyStore();
                    assertNotNull("KeyStore is null", ks);
                    try {
                        assertEquals(ksB1.getProtectionParameter("Bad alias"),
                                pp[i]);
                    } catch (KeyStoreException e) {
                    }
                } catch (KeyStoreException e) {
                    try {
                        ks = ksB.getKeyStore();
                    } catch (KeyStoreException e1) {
                        assertEquals("Incorrect exception", e.getMessage(), e1
                                .getMessage());
                    }
                }
                break;
            case 1:
            case 2:
                Exception ex1 = null;
                Exception ex2 = null;
                try {
                    ks = ksB.getKeyStore();
                } catch (KeyStoreException e) {
                    ex1 = e;
                }
                try {
                    ks = ksB.getKeyStore();
                } catch (KeyStoreException e) {
                    ex2 = e;
                }
                assertEquals("Incorrect exception", ex1.getMessage(), ex2
                        .getMessage());
                try {
                    ksB.getProtectionParameter("aaa");
                    fail("IllegalStateException must be thrown because getKeyStore() was not invoked");
                } catch (IllegalStateException e) {
                }
                try {
                    ks = ksB1.getKeyStore();
                } catch (KeyStoreException e) {
                    ex1 = e;
                }
                try {
                    ks = ksB1.getKeyStore();
                } catch (KeyStoreException e) {
                    ex2 = e;
                }
                assertEquals("Incorrect exception", ex1.getMessage(), ex2
                        .getMessage());
                try {
                    ksB1.getProtectionParameter("aaa");
                    fail("IllegalStateException must be thrown because getKeyStore() was not invoked");
                } catch (IllegalStateException e) {
                }
                break;
            }
        }
    }
    class MyProtectionParameter implements KeyStore.ProtectionParameter {
        public MyProtectionParameter(byte[] param) {
            if (param == null) {
                throw new NullPointerException("param is null");
            }
        }
    }
    private File createKS() throws Exception {
        FileOutputStream fos = null;
        File ff = File.createTempFile("KSBuilder_ImplTest", "keystore");
        ff.deleteOnExit();
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            fos = new FileOutputStream(ff);
            ks.load(null, null);
            ks.store(fos, pass);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
        return ff;
    }
    class KeyStoreBuilder extends KeyStore.Builder {
        public KeyStoreBuilder() {
            super();
        }
        public KeyStore getKeyStore() {
            return null;
        }
        public KeyStore.ProtectionParameter getProtectionParameter(String alias) {
            return null;
        }
    }
}

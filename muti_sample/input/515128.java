@TestTargetClass(KeyFactory.class)
public class KeyFactory2Test extends junit.framework.TestCase {
    private static final String KEYFACTORY_ID = "KeyFactory.";
    private String[] keyfactAlgs = null;
    private String providerName = null;
    static class KeepAlive extends Thread {
        int sleepTime, iterations;
        public KeepAlive(int sleepTime, int iterations) {
            this.sleepTime = sleepTime;
            this.iterations = iterations;
        }
        public void run() {
            synchronized (this) {
                this.notify();
            }
            for (int i = 0; i < iterations; i++) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
    private KeepAlive createKeepAlive(String alg) {
        if (alg.equals("RSA")) {
            KeepAlive keepalive = new KeepAlive(240000, 8);
            synchronized (keepalive) {
                keepalive.start();
                try {
                    keepalive.wait();
                } catch (InterruptedException e) {
                }
            }
            return keepalive;
        }
        return null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyFactory",
        args = {java.security.KeyFactorySpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void test_constructor() {
        KeyFactorySpi kfs = new KeyFactorySpiStub();
        try {
            new KeyFactoryStub(null, null, null);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
        Provider[] providers = Security.getProviders("KeyFactory.DSA");
        if (providers != null) {
            for (int i = 0; i < providers.length; i++) {
                KeyFactoryStub kf = new KeyFactoryStub(kfs, providers[i],
                        "algorithm name");
                assertEquals("algorithm name", kf.getAlgorithm());
                assertEquals(providers[i], kf.getProvider());
            }
        } else {
            fail("No providers support KeyFactory.DSA");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "generatePrivate",
        args = {java.security.spec.KeySpec.class}
    )
    public void test_generatePrivateLjava_security_spec_KeySpec() {
        for (int i = 0; i < keyfactAlgs.length; i++) {
            try {
                KeyFactory fact = KeyFactory.getInstance(keyfactAlgs[i],
                        providerName);
                KeyPairGenerator keyGen = KeyPairGenerator
                        .getInstance(keyfactAlgs[i]);
                SecureRandom random = new SecureRandom(); 
                keyGen.initialize(1024, random);
                KeepAlive keepalive = createKeepAlive(keyfactAlgs[i]);
                KeyPair keys = keyGen.generateKeyPair();
                if (keepalive != null) {
                    keepalive.interrupt();
                }
                KeySpec privateKeySpec = fact.getKeySpec(keys.getPrivate(),
                        getPrivateKeySpecClass(keyfactAlgs[i]));
                PrivateKey privateKey = fact.generatePrivate(privateKeySpec);
                boolean samePrivate = Arrays.equals(keys.getPrivate()
                        .getEncoded(), privateKey.getEncoded());
                assertTrue(
                        "generatePrivate generated different key for algorithm "
                                + keyfactAlgs[i], samePrivate);
                fact.generatePrivate(new PKCS8EncodedKeySpec(keys.getPrivate()
                        .getEncoded()));
            } catch (InvalidKeySpecException e) {
                fail("invalid key spec for algorithm " + keyfactAlgs[i]);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + keyfactAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidKeySpecException checking missed",
        method = "generatePublic",
        args = {java.security.spec.KeySpec.class}
    )
    public void test_generatePublicLjava_security_spec_KeySpec() {
        for (int i = 0; i < keyfactAlgs.length; i++) {
            try {
                KeyFactory fact = KeyFactory.getInstance(keyfactAlgs[i],
                        providerName);
                KeyPairGenerator keyGen = KeyPairGenerator
                        .getInstance(keyfactAlgs[i]);
                SecureRandom random = new SecureRandom();
                keyGen.initialize(1024, random);
                KeepAlive keepalive = createKeepAlive(keyfactAlgs[i]);
                KeyPair keys = keyGen.generateKeyPair();
                if (keepalive != null) {
                    keepalive.interrupt();
                }
                KeySpec publicKeySpec = fact.getKeySpec(keys.getPublic(),
                        getPublicKeySpecClass(keyfactAlgs[i]));
                PublicKey publicKey = fact.generatePublic(publicKeySpec);
                boolean samePublic = Arrays.equals(keys.getPublic()
                        .getEncoded(), publicKey.getEncoded());
                assertTrue(
                        "generatePublic generated different key for algorithm "
                                + keyfactAlgs[i], samePublic);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + keyfactAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            } catch (InvalidKeySpecException e) {
                fail("invalid key spec for algorithm " + keyfactAlgs[i]);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() {
        for (int i = 0; i < keyfactAlgs.length; i++) {
            try {
                KeyFactory fact = KeyFactory.getInstance(keyfactAlgs[i],
                        providerName);
                assertTrue("getAlgorithm ok for algorithm " + keyfactAlgs[i],
                        fact.getAlgorithm().equals(keyfactAlgs[i]));
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + keyfactAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NoSuchAlgorithmException checking missed",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() {
        for (int i = 0; i < keyfactAlgs.length; i++) {
            try {
                assertNotNull(KeyFactory.getInstance(keyfactAlgs[i]));
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + keyfactAlgs[i]);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NoSuchAlgorithmException, NoSuchProviderException checking missed",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String() {
        try {
            Provider[] providers = Security.getProviders("KeyFactory.DSA");
            if (providers != null) {
                for (int i = 0; i < providers.length; i++) {
                    KeyFactory.getInstance("DSA", providers[i].getName());
                }
            } else {
                fail("No providers support KeyFactory.DSA");
            }
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find algorithm");
        } catch (NoSuchProviderException e) {
            fail("getInstance did not find the provider");
        }
        try {
            KeyFactory.getInstance("DSA", (String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, got " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NoSuchAlgorithmException checking missed",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider() {
        try {
            Provider[] providers = Security.getProviders("KeyFactory.DSA");
            if (providers != null) {
                for (int i = 0; i < providers.length; i++) {
                    KeyFactory.getInstance("DSA", providers[i]);
                }
            } else {
                fail("No providers support KeyFactory.DSA");
            }
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find algorithm");
        } catch (Exception e) {
            fail("unexpected exception " + e.getMessage());
        }
        try {
            KeyFactory.getInstance("DSA", (Provider) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            fail("Expected IllegalArgumentException, got " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidKeySpecException checking missed",
        method = "getKeySpec",
        args = {java.security.Key.class, java.lang.Class.class}
    )
    public void test_getKeySpecLjava_security_KeyLjava_lang_Class() {
        for (int i = 0; i < keyfactAlgs.length; i++) {
            try {
                KeyFactory fact = KeyFactory.getInstance(keyfactAlgs[i],
                        providerName);
                KeyPairGenerator keyGen = KeyPairGenerator
                        .getInstance(keyfactAlgs[i]);
                SecureRandom random = new SecureRandom();
                keyGen.initialize(1024, random);
                KeepAlive keepalive = createKeepAlive(keyfactAlgs[i]);
                KeyPair keys = keyGen.generateKeyPair();
                if (keepalive != null) {
                    keepalive.interrupt();
                }
                KeySpec privateKeySpec = fact.getKeySpec(keys.getPrivate(),
                        getPrivateKeySpecClass(keyfactAlgs[i]));
                KeySpec publicKeySpec = fact.getKeySpec(keys.getPublic(),
                        getPublicKeySpecClass(keyfactAlgs[i]));
                PrivateKey privateKey = fact.generatePrivate(privateKeySpec);
                PublicKey publicKey = fact.generatePublic(publicKeySpec);
                boolean samePublic = Arrays.equals(keys.getPublic()
                        .getEncoded(), publicKey.getEncoded());
                boolean samePrivate = Arrays.equals(keys.getPrivate()
                        .getEncoded(), privateKey.getEncoded());
                assertTrue(
                        "generatePrivate generated different key for algorithm "
                                + keyfactAlgs[i], samePrivate);
                assertTrue(
                        "generatePublic generated different key for algorithm "
                                + keyfactAlgs[i], samePublic);
                KeySpec encodedSpec = fact.getKeySpec(keys.getPublic(),
                        X509EncodedKeySpec.class);
                assertTrue("improper key spec for encoded public key",
                        encodedSpec.getClass().equals(X509EncodedKeySpec.class));
                encodedSpec = fact.getKeySpec(keys.getPrivate(),
                        PKCS8EncodedKeySpec.class);
                assertTrue("improper key spec for encoded private key",
                        encodedSpec.getClass()
                                .equals(PKCS8EncodedKeySpec.class));
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + keyfactAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            } catch (InvalidKeySpecException e) {
                fail("invalid key spec for algorithm " + keyfactAlgs[i]);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() {
        for (int i = 0; i < keyfactAlgs.length; i++) {
            try {
                KeyFactory fact = KeyFactory.getInstance(keyfactAlgs[i]);
                Provider p = fact.getProvider();
                assertNotNull("provider is null for algorithm "
                        + keyfactAlgs[i], p);
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + keyfactAlgs[i]);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidKeyException checking missed",
        method = "translateKey",
        args = {java.security.Key.class}
    )
    public void test_translateKeyLjava_security_Key() {
        for (int i = 0; i < keyfactAlgs.length; i++) {
            try {
                KeyFactory fact = KeyFactory.getInstance(keyfactAlgs[i],
                        providerName);
                KeyPairGenerator keyGen = KeyPairGenerator
                        .getInstance(keyfactAlgs[i]);
                SecureRandom random = new SecureRandom();
                keyGen.initialize(1024, random);
                KeepAlive keepalive = createKeepAlive(keyfactAlgs[i]);
                KeyPair keys = keyGen.generateKeyPair();
                if (keepalive != null) {
                    keepalive.interrupt();
                }
                fact.translateKey(keys.getPrivate());
            } catch (NoSuchAlgorithmException e) {
                fail("getInstance did not find algorithm " + keyfactAlgs[i]);
            } catch (NoSuchProviderException e) {
                fail("getInstance did not find provider " + providerName);
            } catch (InvalidKeyException e) {
                fail("generatePublic did not generate right spec for algorithm "
                        + keyfactAlgs[i]);
            }
        }
    }
    protected void setUp() {
        if (keyfactAlgs == null) {
            Provider[] providers = Security.getProviders();
            for (Provider provider : providers) {
                providerName = provider.getName();
                keyfactAlgs = getKeyFactoryAlgorithms(providerName);
                if (keyfactAlgs.length != 0) {
                    break;
                }
            }
        }
    }
    private String[] getKeyFactoryAlgorithms(String providerName) {
        Vector<String> algs = new Vector<String>();
        Provider provider = Security.getProvider(providerName);
        if (provider == null)
            return new String[0];
        Enumeration<Object> e = provider.keys();
        while (e.hasMoreElements()) {
            String algorithm = (String) e.nextElement();
            if (algorithm.startsWith(KEYFACTORY_ID) && !algorithm.contains(" ")) {
                algs.addElement(algorithm.substring(KEYFACTORY_ID.length()));
            }
        }
        return algs.toArray(new String[algs.size()]);
    }
    private Class<? extends KeySpec> getPrivateKeySpecClass(String algName) {
        if (algName.equals("RSA")) {
            return java.security.spec.RSAPrivateCrtKeySpec.class;
        }
        if (algName.equals("DSA")) {
            return java.security.spec.DSAPrivateKeySpec.class;
        }
        return null;
    }
    private Class<? extends KeySpec> getPublicKeySpecClass(String algName) {
        if (algName.equals("RSA")) {
            return java.security.spec.RSAPublicKeySpec.class;
        }
        if (algName.equals("DSA")) {
            return java.security.spec.DSAPublicKeySpec.class;
        }
        return null;
    }
    public class KeyFactoryStub extends KeyFactory {
        public KeyFactoryStub(KeyFactorySpi keyFacSpi, Provider provider,
                String algorithm) {
            super(keyFacSpi, provider, algorithm);
        }
    }
    public class KeyFactorySpiStub extends KeyFactorySpi {
        public KeyFactorySpiStub() {
            super();
        }
        public PrivateKey engineGeneratePrivate(KeySpec keySpec) {
            return null;
        }
        public PublicKey engineGeneratePublic(KeySpec keySpec) {
            return null;
        }
        public <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) {
            return null;
        }
        public Key engineTranslateKey(Key key) {
            return null;
        }
    }
}

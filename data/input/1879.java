public class Failover {
    public static void main(String[] args) throws Exception {
        Security.insertProviderAt(new ProviderFail(), 1);
        Security.addProvider(new ProviderPass());
        System.out.println(Arrays.asList(Security.getProviders()));
        KeyFactory kf;
        kf = KeyFactory.getInstance("FOO");
        kf.generatePublic(null);
        kf.generatePublic(null);
        kf.generatePrivate(null);
        kf = KeyFactory.getInstance("FOO");
        kf.generatePrivate(null);
        kf.getKeySpec(null, null);
        kf.translateKey(null);
        kf = KeyFactory.getInstance("FOO");
        kf.getKeySpec(null, null);
        kf.translateKey(null);
        kf = KeyFactory.getInstance("FOO");
        kf.translateKey(null);
        System.out.println("DSA tests...");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        kf = KeyFactory.getInstance("DSA");
        System.out.println(kf.translateKey(kp.getPrivate()));
        kf = KeyFactory.getInstance("DSA");
        KeySpec spec = kf.getKeySpec(kp.getPublic(), DSAPublicKeySpec.class);
        kf = KeyFactory.getInstance("DSA");
        System.out.println(kf.generatePublic(spec));
        kf = KeyFactory.getInstance("DSA");
        try {
            kf.generatePrivate(spec);
            throw new Exception("no exception");
        } catch (InvalidKeySpecException e) {
            System.out.println(e);
        }
    }
    private static class ProviderPass extends Provider {
        ProviderPass() {
            super("Pass", 1.0d, "Pass");
            put("KeyFactory.FOO" , "Failover$KeyFactoryPass");
        }
    }
    private static class ProviderFail extends Provider {
        ProviderFail() {
            super("Fail", 1.0d, "Fail");
            put("KeyFactory.FOO" , "Failover$KeyFactoryFail");
            put("KeyFactory.DSA" , "Failover$KeyFactoryFail");
        }
    }
    public static class KeyFactoryPass extends KeyFactorySpi {
        protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
            System.out.println("MyKeyFactoryPass.engineGeneratePublic()");
            return null;
        }
        protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
            System.out.println("MyKeyFactoryPass.engineGeneratePrivate()");
            return null;
        }
        protected <T extends KeySpec> T
            engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException
        {
            System.out.println("MyKeyFactoryPass.engineGetKeySpec()");
            return null;
        }
        protected Key engineTranslateKey(Key key) throws InvalidKeyException {
            System.out.println("MyKeyFactoryPass.engineTranslateKey()");
            return null;
        }
    }
    public static class KeyFactoryFail extends KeyFactorySpi {
        protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
            System.out.println("MyKeyFactoryFail.engineGeneratePublic()");
            throw new InvalidKeySpecException();
        }
        protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
            System.out.println("MyKeyFactoryFail.engineGeneratePrivate()");
            throw new InvalidKeySpecException();
        }
        protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
            System.out.println("MyKeyFactoryFail.engineGetKeySpec()");
            throw new InvalidKeySpecException();
        }
        protected Key engineTranslateKey(Key key) throws InvalidKeyException {
            System.out.println("MyKeyFactoryFail.engineTranslateKey()");
            throw new InvalidKeyException();
        }
    }
}

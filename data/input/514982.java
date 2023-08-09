public abstract class JDKKeyPairGenerator
    extends KeyPairGenerator
{
    public JDKKeyPairGenerator(
        String              algorithmName)
    {
        super(algorithmName);
    }
    public abstract void initialize(int strength, SecureRandom random);
    public abstract KeyPair generateKeyPair();
    public static class RSA
        extends JDKKeyPairGenerator
    {
        final static BigInteger defaultPublicExponent = BigInteger.valueOf(0x10001);
        final static int defaultTests = 8;
        RSAKeyGenerationParameters  param;
        RSAKeyPairGenerator         engine;
        public RSA()
        {
            super("RSA");
            engine = new RSAKeyPairGenerator();
            param = new RSAKeyGenerationParameters(defaultPublicExponent,
                            new SecureRandom(), 2048, defaultTests);
            engine.init(param);
        }
        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            param = new RSAKeyGenerationParameters(defaultPublicExponent,
                            random, strength, defaultTests);
            engine.init(param);
        }
        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(params instanceof RSAKeyGenParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("parameter object not a RSAKeyGenParameterSpec");
            }
            RSAKeyGenParameterSpec     rsaParams = (RSAKeyGenParameterSpec)params;
            param = new RSAKeyGenerationParameters(
                            rsaParams.getPublicExponent(),
                            random, rsaParams.getKeysize(), defaultTests);
            engine.init(param);
        }
        public KeyPair generateKeyPair()
        {
            AsymmetricCipherKeyPair     pair = engine.generateKeyPair();
            RSAKeyParameters            pub = (RSAKeyParameters)pair.getPublic();
            RSAPrivateCrtKeyParameters  priv = (RSAPrivateCrtKeyParameters)pair.getPrivate();
            return new KeyPair(new JCERSAPublicKey(pub),
                               new JCERSAPrivateCrtKey(priv));
        }
    }
    public static class DH
        extends JDKKeyPairGenerator
    {
        DHKeyGenerationParameters  param;
        DHBasicKeyPairGenerator    engine = new DHBasicKeyPairGenerator();
        int                        strength = 1024;
        int                        certainty = 20;
        SecureRandom               random = new SecureRandom();
        boolean                    initialised = false;
        public DH()
        {
            super("DH");
        }
        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            this.strength = strength;
            this.random = random;
        }
        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(params instanceof DHParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("parameter object not a DHParameterSpec");
            }
            DHParameterSpec     dhParams = (DHParameterSpec)params;
            param = new DHKeyGenerationParameters(random, new DHParameters(dhParams.getP(), dhParams.getG()));
            engine.init(param);
            initialised = true;
        }
        public KeyPair generateKeyPair()
        {
            if (!initialised)
            {
                DHParametersGenerator   pGen = new DHParametersGenerator();
                pGen.init(strength, certainty, random);
                param = new DHKeyGenerationParameters(random, pGen.generateParameters());
                engine.init(param);
                initialised = true;
            }
            AsymmetricCipherKeyPair   pair = engine.generateKeyPair();
            DHPublicKeyParameters     pub = (DHPublicKeyParameters)pair.getPublic();
            DHPrivateKeyParameters priv = (DHPrivateKeyParameters)pair.getPrivate();
            return new KeyPair(new JCEDHPublicKey(pub),
                               new JCEDHPrivateKey(priv));
        }
    }
    public static class DSA
        extends JDKKeyPairGenerator
    {
        DSAKeyGenerationParameters param;
        DSAKeyPairGenerator        engine = new DSAKeyPairGenerator();
        int                        strength = 1024;
        int                        certainty = 20;
        SecureRandom               random = new SecureRandom();
        boolean                    initialised = false;
        public DSA()
        {
            super("DSA");
        }
        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            this.strength = strength;
            this.random = random;
        }
        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(params instanceof DSAParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("parameter object not a DSAParameterSpec");
            }
            DSAParameterSpec     dsaParams = (DSAParameterSpec)params;
            param = new DSAKeyGenerationParameters(random, new DSAParameters(dsaParams.getP(), dsaParams.getQ(), dsaParams.getG()));
            engine.init(param);
            initialised = true;
        }
        public KeyPair generateKeyPair()
        {
            if (!initialised)
            {
                DSAParametersGenerator   pGen = new DSAParametersGenerator();
                pGen.init(strength, certainty, random);
                param = new DSAKeyGenerationParameters(random, pGen.generateParameters());
                engine.init(param);
                initialised = true;
            }
            AsymmetricCipherKeyPair   pair = engine.generateKeyPair();
            DSAPublicKeyParameters     pub = (DSAPublicKeyParameters)pair.getPublic();
            DSAPrivateKeyParameters priv = (DSAPrivateKeyParameters)pair.getPrivate();
            return new KeyPair(new JDKDSAPublicKey(pub),
                               new JDKDSAPrivateKey(priv));
        }
    }
}

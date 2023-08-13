public class JCEKeyGenerator
    extends KeyGeneratorSpi
{
    protected String                algName;
    protected int                   keySize;
    protected int                   defaultKeySize;
    protected CipherKeyGenerator    engine;
    protected boolean               uninitialised = true;
    protected JCEKeyGenerator(
        String              algName,
        int                 defaultKeySize,
        CipherKeyGenerator  engine)
    {
        this.algName = algName;
        this.keySize = this.defaultKeySize = defaultKeySize;
        this.engine = engine;
    }
    protected void engineInit(
        AlgorithmParameterSpec  params,
        SecureRandom            random)
    throws InvalidAlgorithmParameterException
    {
        throw new InvalidAlgorithmParameterException("Not Implemented");
    }
    protected void engineInit(
        SecureRandom    random)
    {
        if (random != null)
        {
            uninitialised = false;
            engine.init(new KeyGenerationParameters(random, defaultKeySize));
        }
    }
    protected void engineInit(
        int             keySize,
        SecureRandom    random)
    {
        uninitialised = false;
        try
        {
            engine.init(new KeyGenerationParameters(random, keySize));
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidParameterException(e.getMessage());
        }
    }
    protected SecretKey engineGenerateKey()
    {
        if (uninitialised)
        {
            engine.init(new KeyGenerationParameters(
                                    new SecureRandom(), defaultKeySize));
        }
        return (SecretKey)(new SecretKeySpec(engine.generateKey(), algName));
    }
    public static class DES
        extends JCEKeyGenerator
    {
        public DES()
        {
            super("DES", 64, new DESKeyGenerator());
        }
    }
    public static class DESede
        extends JCEKeyGenerator
    {
        private boolean     keySizeSet = false;
        public DESede()
        {
            super("DESede", 192, new DESedeKeyGenerator());
        }
        protected void engineInit(
            int             keySize,
            SecureRandom    random)
        {
            super.engineInit(keySize, random);
            keySizeSet = true;
        }
        protected SecretKey engineGenerateKey()
        {
            if (uninitialised)
            {
                engine.init(new KeyGenerationParameters(
                                        new SecureRandom(), defaultKeySize));
            }
            if (!keySizeSet)
            {
                byte[]     k = engine.generateKey();
                System.arraycopy(k, 0, k, 16, 8);
                return (SecretKey)(new SecretKeySpec(k, algName));
            }
            else
            {
                return (SecretKey)(new SecretKeySpec(engine.generateKey(), algName));
            }
        }
    }
    public static class DESede3
        extends JCEKeyGenerator
    {
        private boolean     keySizeSet = false;
        public DESede3()
        {
            super("DESede3", 192, new DESedeKeyGenerator());
        }
        protected void engineInit(
            int             keySize,
            SecureRandom    random)
        {
            super.engineInit(keySize, random);
            keySizeSet = true;
        }
        protected SecretKey engineGenerateKey()
        {
            if (uninitialised)
            {
                engine.init(new KeyGenerationParameters(
                                        new SecureRandom(), defaultKeySize));
            }
            return (SecretKey)(new SecretKeySpec(engine.generateKey(), algName));
        }
    }
    public static class Skipjack
        extends JCEKeyGenerator
    {
        public Skipjack()
        {
            super("SKIPJACK", 80, new CipherKeyGenerator());
        }
    }
    public static class Blowfish
        extends JCEKeyGenerator
    {
        public Blowfish()
        {
            super("Blowfish", 448, new CipherKeyGenerator());
        }
    }
    public static class Twofish
        extends JCEKeyGenerator
    {
        public Twofish()
        {
            super("Twofish", 256, new CipherKeyGenerator());
        }
    }
    public static class RC2
        extends JCEKeyGenerator
    {
        public RC2()
        {
            super("RC2", 128, new CipherKeyGenerator());
        }
    }
    public static class RC4
        extends JCEKeyGenerator
    {
        public RC4()
        {
            super("RC4", 128, new CipherKeyGenerator());
        }
    }
    public static class RC5
        extends JCEKeyGenerator
    {
        public RC5()
        {
            super("RC5", 128, new CipherKeyGenerator());
        }
    }
    public static class RC564
        extends JCEKeyGenerator
    {
        public RC564()
        {
            super("RC5-64", 256, new CipherKeyGenerator());
        }
    }
    public static class RC6
        extends JCEKeyGenerator
    {
        public RC6()
        {
            super("RC6", 256, new CipherKeyGenerator());
        }
    }
    public static class AES
        extends JCEKeyGenerator
    {
        public AES()
        {
            super("AES", 192, new CipherKeyGenerator());
        }
    }
    public static class AES128
        extends JCEKeyGenerator
    {
        public AES128()
        {
            super("AES", 128, new CipherKeyGenerator());
        }
    }
    public static class AES192
        extends JCEKeyGenerator
    {
        public AES192()
        {
            super("AES", 192, new CipherKeyGenerator());
        }
    }
    public static class AES256
        extends JCEKeyGenerator
    {
        public AES256()
        {
            super("AES", 256, new CipherKeyGenerator());
        }
    }
    public static class GOST28147
        extends JCEKeyGenerator
    {
        public GOST28147()
        {
            super("GOST28147", 256, new CipherKeyGenerator());
        }
    }
    public static class Rijndael
        extends JCEKeyGenerator
    {
        public Rijndael()
        {
            super("Rijndael", 192, new CipherKeyGenerator());
        }
    }
    public static class Serpent
        extends JCEKeyGenerator
    {
        public Serpent()
        {
            super("Serpent", 192, new CipherKeyGenerator());
        }
    }
    public static class Camellia
        extends JCEKeyGenerator
    {
        public Camellia()
        {
            super("Camellia", 256, new CipherKeyGenerator());
        }
    }
    public static class CAST5
        extends JCEKeyGenerator
    {
        public CAST5()
        {
            super("CAST5", 128, new CipherKeyGenerator());
        }
    }
    public static class CAST6
        extends JCEKeyGenerator
    {
        public CAST6()
        {
            super("CAST6", 256, new CipherKeyGenerator());
        }
    }
    public static class IDEA
        extends JCEKeyGenerator
    {
        public IDEA()
        {
            super("IDEA", 128, new CipherKeyGenerator());
        }
    }
    public static class MD4HMAC
        extends JCEKeyGenerator
    {
        public MD4HMAC()
        {
            super("HMACMD4", 128, new CipherKeyGenerator());
        }
    }
    public static class MD5HMAC
        extends JCEKeyGenerator
    {
        public MD5HMAC()
        {
            super("HMACMD5", 128, new CipherKeyGenerator());
        }
    }
    public static class RIPEMD128HMAC
        extends JCEKeyGenerator
    {
        public RIPEMD128HMAC()
        {
            super("HMACRIPEMD128", 128, new CipherKeyGenerator());
        }
    }
    public static class RIPEMD160HMAC
        extends JCEKeyGenerator
    {
        public RIPEMD160HMAC()
        {
            super("HMACRIPEMD160", 160, new CipherKeyGenerator());
        }
    }
    public static class HMACSHA1
        extends JCEKeyGenerator
    {
        public HMACSHA1()
        {
            super("HMACSHA1", 160, new CipherKeyGenerator());
        }
    }
    public static class HMACSHA224
        extends JCEKeyGenerator
    {
        public HMACSHA224()
        {
            super("HMACSHA224", 224, new CipherKeyGenerator());
        }
    }
    public static class HMACSHA256
        extends JCEKeyGenerator
    {
        public HMACSHA256()
        {
            super("HMACSHA256", 256, new CipherKeyGenerator());
        }
    }
    public static class HMACSHA384
        extends JCEKeyGenerator
    {
        public HMACSHA384()
        {
            super("HMACSHA384", 384, new CipherKeyGenerator());
        }
    }
    public static class HMACSHA512
        extends JCEKeyGenerator
    {
        public HMACSHA512()
        {
            super("HMACSHA512", 512, new CipherKeyGenerator());
        }
    }
    public static class HMACTIGER
        extends JCEKeyGenerator
    {
        public HMACTIGER()
        {
            super("HMACTIGER", 192, new CipherKeyGenerator());
        }
    }
}

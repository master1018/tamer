public class JCESecretKeyFactory
    extends SecretKeyFactorySpi
    implements PBE
{
    protected String                algName;
    protected DERObjectIdentifier   algOid;
    protected JCESecretKeyFactory(
        String               algName,
        DERObjectIdentifier  algOid)
    {
        this.algName = algName;
        this.algOid = algOid;
    }
    protected SecretKey engineGenerateSecret(
        KeySpec keySpec)
    throws InvalidKeySpecException
    {
        if (keySpec instanceof SecretKeySpec)
        {
            return (SecretKey)keySpec;
        }
        throw new InvalidKeySpecException("Invalid KeySpec");
    }
    protected KeySpec engineGetKeySpec(
        SecretKey key,
        Class keySpec)
    throws InvalidKeySpecException
    {
        if (keySpec == null)
        {
            throw new InvalidKeySpecException("keySpec parameter is null");
        }
        if (key == null)
        {
            throw new InvalidKeySpecException("key parameter is null");
        }
        if (SecretKeySpec.class.isAssignableFrom(keySpec))
        {
            return new SecretKeySpec(key.getEncoded(), algName);
        }
        try
        {
            Class[] parameters = { byte[].class };
            Constructor c = keySpec.getConstructor(parameters);
            Object[]    p = new Object[1];
            p[0] = key.getEncoded();
            return (KeySpec)c.newInstance(p);
        }
        catch (Exception e)
        {
            throw new InvalidKeySpecException(e.toString());
        }
    }
    protected SecretKey engineTranslateKey(
        SecretKey key)
    throws InvalidKeyException
    {
        if (key == null)
        {
            throw new InvalidKeyException("key parameter is null");
        }
        if (!key.getAlgorithm().equalsIgnoreCase(algName))
        {
            throw new InvalidKeyException("Key not of type " + algName + ".");
        }
        return new SecretKeySpec(key.getEncoded(), algName);
    }
    static public class PBEKeyFactory
        extends JCESecretKeyFactory
    {
        private boolean forCipher;
        private int     scheme;
        private int     digest;
        private int     keySize;
        private int     ivSize;
        public PBEKeyFactory(
            String              algorithm,
            DERObjectIdentifier oid,
            boolean             forCipher,
            int                 scheme,
            int                 digest,
            int                 keySize,
            int                 ivSize)
        {
            super(algorithm, oid);
            this.forCipher = forCipher;
            this.scheme = scheme;
            this.digest = digest;
            this.keySize = keySize;
            this.ivSize = ivSize;
        }
        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PBEKeySpec)
            {
                PBEKeySpec          pbeSpec = (PBEKeySpec)keySpec;
                CipherParameters    param;
                if (pbeSpec.getSalt() == null)
                {
                    return new JCEPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, null);
                }
                if (forCipher)
                {
                    param = Util.makePBEParameters(pbeSpec, scheme, digest, keySize, ivSize);
                }
                else
                {
                    param = Util.makePBEMacParameters(pbeSpec, scheme, digest, keySize);
                }
                return new JCEPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, param);
            }
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
    }
    static public class DESPBEKeyFactory
        extends JCESecretKeyFactory
    {
        private boolean forCipher;
        private int     scheme;
        private int     digest;
        private int     keySize;
        private int     ivSize;
        public DESPBEKeyFactory(
            String              algorithm,
            DERObjectIdentifier oid,
            boolean             forCipher,
            int                 scheme,
            int                 digest,
            int                 keySize,
            int                 ivSize)
        {
            super(algorithm, oid);
            this.forCipher = forCipher;
            this.scheme = scheme;
            this.digest = digest;
            this.keySize = keySize;
            this.ivSize = ivSize;
        }
        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof PBEKeySpec)
            {
                PBEKeySpec pbeSpec = (PBEKeySpec)keySpec;
                CipherParameters    param;
                if (pbeSpec.getSalt() == null)
                {
                    return new JCEPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, null);
                }
                if (forCipher)
                {
                    param = Util.makePBEParameters(pbeSpec, scheme, digest, keySize, ivSize);
                }
                else
                {
                    param = Util.makePBEMacParameters(pbeSpec, scheme, digest, keySize);
                }
                if (param instanceof ParametersWithIV)
                {
                    KeyParameter    kParam = (KeyParameter)((ParametersWithIV)param).getParameters();
                    DESParameters.setOddParity(kParam.getKey());
                }
                else
                {
                    KeyParameter    kParam = (KeyParameter)param;
                    DESParameters.setOddParity(kParam.getKey());
                }
                return new JCEPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, param);
            }
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
    }
    static public class DES
        extends JCESecretKeyFactory
    {
        public DES()
        {
            super("DES", null);
        }
        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof DESKeySpec)
            {
                DESKeySpec desKeySpec = (DESKeySpec)keySpec;
                return new SecretKeySpec(desKeySpec.getKey(), "DES");
            }
            return super.engineGenerateSecret(keySpec);
        }
    }
    static public class DESede
        extends JCESecretKeyFactory
    {
        public DESede()
        {
            super("DESede", null);
        }
        protected KeySpec engineGetKeySpec(
            SecretKey key,
            Class keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec == null)
            {
                throw new InvalidKeySpecException("keySpec parameter is null");
            }
            if (key == null)
            {
                throw new InvalidKeySpecException("key parameter is null");
            }
            if (SecretKeySpec.class.isAssignableFrom(keySpec))
            {
                return new SecretKeySpec(key.getEncoded(), algName);
            }
            else if (DESedeKeySpec.class.isAssignableFrom(keySpec))
            {
                byte[]  bytes = key.getEncoded();
                try
                {
                    if (bytes.length == 16)
                    {
                        byte[]  longKey = new byte[24];
                        System.arraycopy(bytes, 0, longKey, 0, 16);
                        System.arraycopy(bytes, 0, longKey, 16, 8);
                        return new DESedeKeySpec(longKey);
                    }
                    else
                    {
                        return new DESedeKeySpec(bytes);
                    }
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof DESedeKeySpec)
            {
                DESedeKeySpec desKeySpec = (DESedeKeySpec)keySpec;
                return new SecretKeySpec(desKeySpec.getKey(), "DESede");
            }
            return super.engineGenerateSecret(keySpec);
        }
    }
   static public class PBEWithMD5AndDES
       extends DESPBEKeyFactory
   {
       public PBEWithMD5AndDES()
       {
           super("PBEwithMD5andDES", null, true, PKCS5S1, MD5, 64, 64);
       }
   }
   static public class PBEWithMD5AndRC2
       extends PBEKeyFactory
   {
       public PBEWithMD5AndRC2()
       {
           super("PBEwithMD5andRC2", null, true, PKCS5S1, MD5, 64, 64);
       }
   }
   static public class PBEWithSHA1AndDES
       extends PBEKeyFactory
   {
       public PBEWithSHA1AndDES()
       {
           super("PBEwithSHA1andDES", null, true, PKCS5S1, SHA1, 64, 64);
       }
   }
   static public class PBEWithSHA1AndRC2
       extends PBEKeyFactory
   {
       public PBEWithSHA1AndRC2()
       {
           super("PBEwithSHA1andRC2", null, true, PKCS5S1, SHA1, 64, 64);
       }
   }
   static public class PBEWithSHAAndDES3Key
       extends PBEKeyFactory
   {
       public PBEWithSHAAndDES3Key()
       {
           super("PBEwithSHAandDES3Key-CBC", PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, true, PKCS12, SHA1, 192, 64);
       }
   }
   static public class PBEWithSHAAndDES2Key
       extends PBEKeyFactory
   {
       public PBEWithSHAAndDES2Key()
       {
           super("PBEwithSHAandDES2Key-CBC", PKCSObjectIdentifiers.pbeWithSHAAnd2_KeyTripleDES_CBC, true, PKCS12, SHA1, 128, 64);
       }
   }
   static public class PBEWithSHAAnd40BitRC2
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd40BitRC2()
       {
           super("PBEwithSHAand40BitRC2-CBC", PKCSObjectIdentifiers.pbewithSHAAnd40BitRC2_CBC, true, PKCS12, SHA1, 40, 64);
       }
   }
   public static class PBEWithSHA
       extends PBEKeyFactory
   {
       public PBEWithSHA()
       {
           super("PBEwithHmacSHA", null, false, PKCS12, SHA1, 160, 0);
       }
   }
   static public class PBEWithSHAAnd128BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd128BitAESBC()
       {
           super("PBEWithSHA1And128BitAES-CBC-BC", null, true, PKCS12, SHA1, 128, 128);
       }
   }
   static public class PBEWithSHAAnd192BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd192BitAESBC()
       {
           super("PBEWithSHA1And192BitAES-CBC-BC", null, true, PKCS12, SHA1, 192, 128);
       }
   }
   static public class PBEWithSHAAnd256BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd256BitAESBC()
       {
           super("PBEWithSHA1And256BitAES-CBC-BC", null, true, PKCS12, SHA1, 256, 128);
       }
   }
   static public class PBEWithSHA256And128BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHA256And128BitAESBC()
       {
           super("PBEWithSHA256And128BitAES-CBC-BC", null, true, PKCS12, SHA256, 128, 128);
       }
   }
   static public class PBEWithSHA256And192BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHA256And192BitAESBC()
       {
           super("PBEWithSHA256And192BitAES-CBC-BC", null, true, PKCS12, SHA256, 192, 128);
       }
   }
   static public class PBEWithSHA256And256BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHA256And256BitAESBC()
       {
           super("PBEWithSHA256And256BitAES-CBC-BC", null, true, PKCS12, SHA256, 256, 128);
       }
   }
   static public class PBEWithMD5And128BitAESCBCOpenSSL
       extends PBEKeyFactory
   {
       public PBEWithMD5And128BitAESCBCOpenSSL()
       {
           super("PBEWithMD5And128BitAES-CBC-OpenSSL", null, true, OPENSSL, MD5, 128, 128);
       }
   }
   static public class PBEWithMD5And192BitAESCBCOpenSSL
       extends PBEKeyFactory
   {
       public PBEWithMD5And192BitAESCBCOpenSSL()
       {
           super("PBEWithMD5And192BitAES-CBC-OpenSSL", null, true, OPENSSL, MD5, 192, 128);
       }
   }
   static public class PBEWithMD5And256BitAESCBCOpenSSL
       extends PBEKeyFactory
   {
       public PBEWithMD5And256BitAESCBCOpenSSL()
       {
           super("PBEWithMD5And256BitAES-CBC-OpenSSL", null, true, OPENSSL, MD5, 256, 128);
       }
   }
}

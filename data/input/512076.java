public abstract class JDKKeyFactory
    extends KeyFactorySpi
{
    protected boolean elGamalFactory = false;
    public JDKKeyFactory()
    {
    }
    protected KeySpec engineGetKeySpec(
        Key    key,
        Class    spec)
    throws InvalidKeySpecException
    {
       if (spec.isAssignableFrom(PKCS8EncodedKeySpec.class) && key.getFormat().equals("PKCS#8"))
       {
               return new PKCS8EncodedKeySpec(key.getEncoded());
       }
       else if (spec.isAssignableFrom(X509EncodedKeySpec.class) && key.getFormat().equals("X.509"))
       {
               return new X509EncodedKeySpec(key.getEncoded());
       }
       else if (spec.isAssignableFrom(RSAPublicKeySpec.class) && key instanceof RSAPublicKey)
       {
            RSAPublicKey    k = (RSAPublicKey)key;
            return new RSAPublicKeySpec(k.getModulus(), k.getPublicExponent());
       }
       else if (spec.isAssignableFrom(RSAPrivateKeySpec.class) && key instanceof RSAPrivateKey)
       {
            RSAPrivateKey    k = (RSAPrivateKey)key;
            return new RSAPrivateKeySpec(k.getModulus(), k.getPrivateExponent());
       }
       else if (spec.isAssignableFrom(RSAPrivateCrtKeySpec.class) && key instanceof RSAPrivateCrtKey)
       {
            RSAPrivateCrtKey    k = (RSAPrivateCrtKey)key;
            return new RSAPrivateCrtKeySpec(
                            k.getModulus(), k.getPublicExponent(),
                            k.getPrivateExponent(),
                            k.getPrimeP(), k.getPrimeQ(),
                            k.getPrimeExponentP(), k.getPrimeExponentQ(),
                            k.getCrtCoefficient());
       }
       else if (spec.isAssignableFrom(DHPrivateKeySpec.class) && key instanceof DHPrivateKey)
       {
           DHPrivateKey k = (DHPrivateKey)key;
           return new DHPrivateKeySpec(k.getX(), k.getParams().getP(), k.getParams().getG());
       }
       else if (spec.isAssignableFrom(DHPublicKeySpec.class) && key instanceof DHPublicKey)
       {
           DHPublicKey k = (DHPublicKey)key;
           return new DHPublicKeySpec(k.getY(), k.getParams().getP(), k.getParams().getG());
       }
        throw new RuntimeException("not implemented yet " + key + " " + spec);
    }
    protected Key engineTranslateKey(
        Key    key)
        throws InvalidKeyException
    {
        if (key instanceof RSAPublicKey)
        {
            return new JCERSAPublicKey((RSAPublicKey)key);
        }
        else if (key instanceof RSAPrivateCrtKey)
        {
            return new JCERSAPrivateCrtKey((RSAPrivateCrtKey)key);
        }
        else if (key instanceof RSAPrivateKey)
        {
            return new JCERSAPrivateKey((RSAPrivateKey)key);
        }
        else if (key instanceof DHPublicKey)
        {
                return new JCEDHPublicKey((DHPublicKey)key);
        }
        else if (key instanceof DHPrivateKey)
        {
                return new JCEDHPrivateKey((DHPrivateKey)key);
        }
        else if (key instanceof DSAPublicKey)
        {
            return new JDKDSAPublicKey((DSAPublicKey)key);
        }
        else if (key instanceof DSAPrivateKey)
        {
            return new JDKDSAPrivateKey((DSAPrivateKey)key);
        }
        throw new InvalidKeyException("key type unknown");
    }
    static PublicKey createPublicKeyFromDERStream(
        byte[]         in)
        throws IOException
    {
        return createPublicKeyFromPublicKeyInfo(
                new SubjectPublicKeyInfo((ASN1Sequence)(new ASN1InputStream(in).readObject())));
    }
    static PublicKey createPublicKeyFromPublicKeyInfo(
        SubjectPublicKeyInfo         info)
    {
        DERObjectIdentifier     algOid = info.getAlgorithmId().getObjectId();
        if (RSAUtil.isRsaOid(algOid))
        {
            return new JCERSAPublicKey(info);
        }
        else if (algOid.equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
            return new JCEDHPublicKey(info);
        }
        else if (algOid.equals(X9ObjectIdentifiers.dhpublicnumber))
        {
            return new JCEDHPublicKey(info);
        }
        else if (algOid.equals(X9ObjectIdentifiers.id_dsa))
        {
            return new JDKDSAPublicKey(info);
        }
        else if (algOid.equals(OIWObjectIdentifiers.dsaWithSHA1))
        {
            return new JDKDSAPublicKey(info);
        }
        else
        {
            throw new RuntimeException("algorithm identifier " + algOid + " in key not recognised");
        }
    }
    static PrivateKey createPrivateKeyFromDERStream(
        byte[]         in)
        throws IOException
    {
        return createPrivateKeyFromPrivateKeyInfo(
                new PrivateKeyInfo((ASN1Sequence)(new ASN1InputStream(in).readObject())));
    }
    static PrivateKey createPrivateKeyFromPrivateKeyInfo(
        PrivateKeyInfo      info)
    {
        DERObjectIdentifier     algOid = info.getAlgorithmId().getObjectId();
        if (RSAUtil.isRsaOid(algOid))
        {
              return new JCERSAPrivateCrtKey(info);
        }
        else if (algOid.equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
              return new JCEDHPrivateKey(info);
        }
        else if (algOid.equals(X9ObjectIdentifiers.id_dsa))
        {
              return new JDKDSAPrivateKey(info);
        }
        else
        {
            throw new RuntimeException("algorithm identifier " + algOid + " in key not recognised");
        }
    }
    public static class RSA
        extends JDKKeyFactory
    {
        public RSA()
        {
        }
        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    try
                    {
                        return new JCERSAPrivateCrtKey(
                            new RSAPrivateKeyStructure(
                                (ASN1Sequence)new ASN1InputStream(((PKCS8EncodedKeySpec)keySpec).getEncoded()).readObject()));
                    }
                    catch (Exception ex)
                    {
                        throw new InvalidKeySpecException(ex.toString());
                    }
                }
            }
            else if (keySpec instanceof RSAPrivateCrtKeySpec)
            {
                return new JCERSAPrivateCrtKey((RSAPrivateCrtKeySpec)keySpec);
            }
            else if (keySpec instanceof RSAPrivateKeySpec)
            {
                return new JCERSAPrivateKey((RSAPrivateKeySpec)keySpec);
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof RSAPublicKeySpec)
            {
                return new JCERSAPublicKey((RSAPublicKeySpec)keySpec);
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    public static class DH
        extends JDKKeyFactory
    {
        public DH()
        {
        }
        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof DHPrivateKeySpec)
            {
                return new JCEDHPrivateKey((DHPrivateKeySpec)keySpec);
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof DHPublicKeySpec)
            {
                return new JCEDHPublicKey((DHPublicKeySpec)keySpec);
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    public static class DSA
        extends JDKKeyFactory
    {
        public DSA()
        {
        }
        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof DSAPrivateKeySpec)
            {
                return new JDKDSAPrivateKey((DSAPrivateKeySpec)keySpec);
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            else if (keySpec instanceof DSAPublicKeySpec)
            {
                return new JDKDSAPublicKey((DSAPublicKeySpec)keySpec);
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    public static class GOST3410
        extends JDKKeyFactory
    {
        public GOST3410()
        {
        }
        protected PrivateKey engineGeneratePrivate(
                KeySpec    keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                            ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
        protected PublicKey engineGeneratePublic(
                KeySpec    keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                            ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    public static class ElGamal
        extends JDKKeyFactory
    {
        public ElGamal()
        {
            elGamalFactory = true;
        }
        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    public static class X509
        extends JDKKeyFactory
    {
        public X509()
        {
        }
        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    public static class EC
        extends JDKKeyFactory
    {
        String  algorithm;
        public EC()
        {
            this("EC");
        }
        public EC(
            String  algorithm)
        {
            this.algorithm = algorithm;
        }
        protected PrivateKey engineGeneratePrivate(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PKCS8EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPrivateKeyFromDERStream(
                                ((PKCS8EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
        protected PublicKey engineGeneratePublic(
            KeySpec    keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof X509EncodedKeySpec)
            {
                try
                {
                    return JDKKeyFactory.createPublicKeyFromDERStream(
                                ((X509EncodedKeySpec)keySpec).getEncoded());
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }
            throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
        }
    }
    public static class ECDSA
        extends EC
    {
        public ECDSA()
        {
            super("ECDSA");
        }
    }
    public static class ECGOST3410
        extends EC
    {
        public ECGOST3410()
        {
            super("ECGOST3410");
        }
    }
    public static class ECDH
        extends EC
    {
        public ECDH()
        {
            super("ECDH");
        }
    }
    public static class ECDHC
        extends EC
    {
        public ECDHC()
        {
            super("ECDHC");
        }
    }
}

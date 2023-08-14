public class JDKDigestSignature
    extends Signature implements PKCSObjectIdentifiers, X509ObjectIdentifiers
{
    private Digest                  digest;
    private AsymmetricBlockCipher   cipher;
    private AlgorithmIdentifier     algId;
    protected JDKDigestSignature(
        String                  name,
        DERObjectIdentifier     objId,
        Digest                  digest,
        AsymmetricBlockCipher   cipher)
    {
        super(name);
        this.digest = digest;
        this.cipher = cipher;
        this.algId = new AlgorithmIdentifier(objId);
    }
    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        if (!(publicKey instanceof RSAPublicKey))
        {
            throw new InvalidKeyException("Supplied key (" + getType(publicKey) + ") is not a RSAPublicKey instance");
        }
        CipherParameters    param = RSAUtil.generatePublicKeyParameter((RSAPublicKey)publicKey);
        digest.reset();
        cipher.init(false, param);
    }
    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        if (!(privateKey instanceof RSAPrivateKey))
        {
            throw new InvalidKeyException("Supplied key (" + getType(privateKey) + ") is not a RSAPrivateKey instance");
        }
        CipherParameters    param = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey);
        digest.reset();
        cipher.init(true, param);
    }
    private String getType(
        Object o)
    {
        if (o == null)
        {
            return null;
        }
        return o.getClass().getName();
    }
    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        digest.update(b);
    }
    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        digest.update(b, off, len);
    }
    protected byte[] engineSign()
        throws SignatureException
    {
        byte[]  hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        try
        {
            byte[]  bytes = derEncode(hash);
            return cipher.processBlock(bytes, 0, bytes.length);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new SignatureException("key too small for signature type");
        }
        catch (Exception e)
        {
            throw new SignatureException(e.toString());
        }
    }
    protected boolean engineVerify(
        byte[]  sigBytes) 
        throws SignatureException
    {
        byte[]  hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        byte[]      sig;
        byte[]      expected;
        try
        {
            sig = cipher.processBlock(sigBytes, 0, sigBytes.length);
            expected = derEncode(hash);
        }
        catch (Exception e)
        {
            return false;
        }
        if (sig.length == expected.length)
        {
            for (int i = 0; i < sig.length; i++)
            {
                if (sig[i] != expected[i])
                {
                    return false;
                }
            }
        }
        else if (expected.length == sig.length - 2)  
        {
            int sigOffset = sig.length - hash.length - 2;
            int expectedOffset = expected.length - hash.length - 2;
            sig[1] -= 2;      
            sig[3] -= 2;
            for (int i = 0; i < hash.length; i++)
            {
                if (sig[sigOffset + i] != expected[expectedOffset + i])  
                {
                    return false;
                }
            }
            for (int i = 0; i < expectedOffset; i++)
            {
                if (sig[i] != expected[i])  
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }
        return true;
    }
    protected void engineSetParameter(
        AlgorithmParameterSpec params)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }
    protected void engineSetParameter(
        String  param,
        Object  value)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }
    protected Object engineGetParameter(
        String      param)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }
    private byte[] derEncode(
        byte[]  hash)
        throws IOException
    {
        DigestInfo              dInfo = new DigestInfo(algId, hash);
        return dInfo.getEncoded(ASN1Encodable.DER);
    }
    static public class SHA1WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA1WithRSAEncryption()
        {
            super("SHA1withRSA", id_SHA1, new SHA1Digest(), new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class SHA224WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA224WithRSAEncryption()
        {
            super("SHA224withRSA", NISTObjectIdentifiers.id_sha224, new SHA224Digest(), new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class SHA256WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA256WithRSAEncryption()
        {
            super("SHA256withRSA", NISTObjectIdentifiers.id_sha256, new SHA256Digest(), new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class SHA384WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA384WithRSAEncryption()
        {
            super("SHA384withRSA", NISTObjectIdentifiers.id_sha384, new SHA384Digest(), new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class SHA512WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA512WithRSAEncryption()
        {
            super("SHA512withRSA", NISTObjectIdentifiers.id_sha512, new SHA512Digest(), new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class MD4WithRSAEncryption
        extends JDKDigestSignature
    {
        public MD4WithRSAEncryption()
        {
            super("MD4withRSA", md4, new MD4Digest(), new PKCS1Encoding(new RSAEngine()));
        }
    }
    static public class MD5WithRSAEncryption
        extends JDKDigestSignature
    {
        public MD5WithRSAEncryption()
        {
            super("MD5withRSA", md5, new MD5Digest(), new PKCS1Encoding(new RSAEngine()));
        }
    }
}

public class JDKISOSignature
    extends Signature
{
    private ISO9796d2Signer         signer;
    protected JDKISOSignature(
        String                  name,
        Digest                  digest,
        AsymmetricBlockCipher   cipher)
    {
        super(name);
        signer = new ISO9796d2Signer(cipher, digest, true);
    }
    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        CipherParameters    param = RSAUtil.generatePublicKeyParameter((RSAPublicKey)publicKey);
        signer.init(false, param);
    }
    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        CipherParameters    param = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey);
        signer.init(true, param);
    }
    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        signer.update(b);
    }
    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        signer.update(b, off, len);
    }
    protected byte[] engineSign()
        throws SignatureException
    {
        try
        {
            byte[]  sig = signer.generateSignature();
            return sig;
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
        boolean yes = signer.verifySignature(sigBytes);
        return yes;
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
    static public class SHA1WithRSAEncryption
        extends JDKISOSignature
    {
        public SHA1WithRSAEncryption()
        {
            super("SHA1withRSA/ISO9796-2", new SHA1Digest(), new RSAEngine());
        }
    }
    static public class MD5WithRSAEncryption
        extends JDKISOSignature
    {
        public MD5WithRSAEncryption()
        {
            super("MD5withRSA/ISO9796-2", new MD5Digest(), new RSAEngine());
        }
    }
}

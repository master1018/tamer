public class JDKDSASigner
    extends Signature implements PKCSObjectIdentifiers, X509ObjectIdentifiers
{
    private Digest                  digest;
    private DSA                     signer;
    private SecureRandom            random;
    protected JDKDSASigner(
        String                  name,
        Digest                  digest,
        DSA                     signer)
    {
        super(name);
        this.digest = digest;
        this.signer = signer;
    }
    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        CipherParameters    param = null;
        if (publicKey instanceof DSAKey)
        {
            param = DSAUtil.generatePublicKeyParameter(publicKey);
        }
        else
        {
            try
            {
                byte[]  bytes = publicKey.getEncoded();
                publicKey = JDKKeyFactory.createPublicKeyFromDERStream(bytes);
                if (publicKey instanceof DSAKey)
                {
                    param = DSAUtil.generatePublicKeyParameter(publicKey);
                }
                else
                {
                    throw new InvalidKeyException("can't recognise key type in DSA based signer");
                }
            }
            catch (Exception e)
            {
                throw new InvalidKeyException("can't recognise key type in DSA based signer");
            }
        }
        digest.reset();
        signer.init(false, param);
    }
    protected void engineInitSign(
        PrivateKey      privateKey,
        SecureRandom    random)
        throws InvalidKeyException
    {
        this.random = random;
        engineInitSign(privateKey);
    }
    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        CipherParameters    param = null;
            param = DSAUtil.generatePrivateKeyParameter(privateKey);
        digest.reset();
        if (random != null)
        {
            signer.init(true, new ParametersWithRandom(param, random));
        }
        else
        {
            signer.init(true, param);
        }
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
            BigInteger[]    sig = signer.generateSignature(hash);
            return derEncode(sig[0], sig[1]);
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
        BigInteger[]    sig;
        try
        {
            sig = derDecode(sigBytes);
        }
        catch (Exception e)
        {
            throw new SignatureException("error decoding signature bytes.");
        }
        return signer.verifySignature(hash, sig[0], sig[1]);
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
        BigInteger  r,
        BigInteger  s)
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        ASN1EncodableVector     v = new ASN1EncodableVector();
        v.add(new DERInteger(r));
        v.add(new DERInteger(s));
        dOut.writeObject(new DERSequence(v));
        return bOut.toByteArray();
    }
    private BigInteger[] derDecode(
        byte[]  encoding)
        throws IOException
    {
        ASN1InputStream         aIn = new ASN1InputStream(encoding);
        ASN1Sequence            s = (ASN1Sequence)aIn.readObject();
        BigInteger[]            sig = new BigInteger[2];
        sig[0] = ((DERInteger)s.getObjectAt(0)).getValue();
        sig[1] = ((DERInteger)s.getObjectAt(1)).getValue();
        return sig;
    }
    static public class stdDSA
        extends JDKDSASigner
    {
        public stdDSA()
        {
            super("SHA1withDSA", new SHA1Digest(), new DSASigner());
        }
    }
    static public class noneDSA
        extends JDKDSASigner
    {
        public noneDSA()
        {
            super("NONEwithDSA", new NullDigest(), new DSASigner());
        }
    }
    private static class NullDigest
        implements Digest
    {
        private ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        public String getAlgorithmName()
        {
            return "NULL";
        }
        public int getDigestSize()
        {
            return bOut.size();
        }
        public void update(byte in)
        {
            bOut.write(in);
        }
        public void update(byte[] in, int inOff, int len)
        {
            bOut.write(in, inOff, len);
        }
        public int doFinal(byte[] out, int outOff)
        {
            byte[] res = bOut.toByteArray();
            System.arraycopy(res, 0, out, outOff, res.length);
            return res.length;
        }
        public void reset()
        {
            bOut.reset();
        }
    }
}

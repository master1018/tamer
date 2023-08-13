public class JCERSAPublicKey
    implements RSAPublicKey
{
    private BigInteger modulus;
    private BigInteger publicExponent;
    JCERSAPublicKey(
        RSAKeyParameters key)
    {
        this.modulus = key.getModulus();
        this.publicExponent = key.getExponent();
    }
    JCERSAPublicKey(
        RSAPublicKeySpec spec)
    {
        this.modulus = spec.getModulus();
        this.publicExponent = spec.getPublicExponent();
    }
    JCERSAPublicKey(
        RSAPublicKey key)
    {
        this.modulus = key.getModulus();
        this.publicExponent = key.getPublicExponent();
    }
    JCERSAPublicKey(
        SubjectPublicKeyInfo    info)
    {
        try
        {
            RSAPublicKeyStructure   pubKey = new RSAPublicKeyStructure((ASN1Sequence)info.getPublicKey());
            this.modulus = pubKey.getModulus();
            this.publicExponent = pubKey.getPublicExponent();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("invalid info structure in RSA public key");
        }
    }
    public BigInteger getModulus()
    {
        return modulus;
    }
    public BigInteger getPublicExponent()
    {
        return publicExponent;
    }
    public String getAlgorithm()
    {
        return "RSA";
    }
    public String getFormat()
    {
        return "X.509";
    }
    public byte[] getEncoded()
    {
        SubjectPublicKeyInfo    info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.THE_ONE), new RSAPublicKeyStructure(getModulus(), getPublicExponent()).getDERObject());
        return info.getDEREncoded();
    }
    public boolean equals(Object o)
    {
        if (!(o instanceof RSAPublicKey))
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        RSAPublicKey key = (RSAPublicKey)o;
        return getModulus().equals(key.getModulus())
            && getPublicExponent().equals(key.getPublicExponent());
    }
    public String toString()
    {
        StringBuffer    buf = new StringBuffer();
        String          nl = System.getProperty("line.separator");
        buf.append("RSA Public Key").append(nl);
        buf.append("            modulus: ").append(this.getModulus().toString(16)).append(nl);
        buf.append("    public exponent: ").append(this.getPublicExponent().toString(16)).append(nl);
        return buf.toString();
    }
}

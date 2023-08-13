public class JDKDSAPrivateKey
    implements DSAPrivateKey, PKCS12BagAttributeCarrier
{
    BigInteger          x;
    DSAParams           dsaSpec;
    private Hashtable   pkcs12Attributes = new Hashtable();
    private Vector      pkcs12Ordering = new Vector();
    protected JDKDSAPrivateKey()
    {
    }
    JDKDSAPrivateKey(
        DSAPrivateKey    key)
    {
        this.x = key.getX();
        this.dsaSpec = key.getParams();
    }
    JDKDSAPrivateKey(
        DSAPrivateKeySpec    spec)
    {
        this.x = spec.getX();
        this.dsaSpec = new DSAParameterSpec(spec.getP(), spec.getQ(), spec.getG());
    }
    JDKDSAPrivateKey(
        PrivateKeyInfo  info)
    {
        DSAParameter    params = new DSAParameter((ASN1Sequence)info.getAlgorithmId().getParameters());
        DERInteger      derX = (DERInteger)info.getPrivateKey();
        this.x = derX.getValue();
        this.dsaSpec = new DSAParameterSpec(params.getP(), params.getQ(), params.getG());
    }
    JDKDSAPrivateKey(
        DSAPrivateKeyParameters  params)
    {
        this.x = params.getX();
        this.dsaSpec = new DSAParameterSpec(params.getParameters().getP(), params.getParameters().getQ(), params.getParameters().getG());
    }
    public String getAlgorithm()
    {
        return "DSA";
    }
    public String getFormat()
    {
        return "PKCS#8";
    }
    public byte[] getEncoded()
    {
        PrivateKeyInfo          info = new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(dsaSpec.getP(), dsaSpec.getQ(), dsaSpec.getG()).getDERObject()), new DERInteger(getX()));
        return info.getDEREncoded();
    }
    public DSAParams getParams()
    {
        return dsaSpec;
    }
    public BigInteger getX()
    {
        return x;
    }
    public boolean equals(
        Object o)
    {
        if (!(o instanceof DSAPrivateKey))
        {
            return false;
        }
        DSAPrivateKey other = (DSAPrivateKey)o;
        return this.getX().equals(other.getX()) 
            && this.getParams().getG().equals(other.getParams().getG()) 
            && this.getParams().getP().equals(other.getParams().getP()) 
            && this.getParams().getQ().equals(other.getParams().getQ());
    }
    public void setBagAttribute(
        DERObjectIdentifier oid,
        DEREncodable        attribute)
    {
        pkcs12Attributes.put(oid, attribute);
        pkcs12Ordering.addElement(oid);
    }
    public DEREncodable getBagAttribute(
        DERObjectIdentifier oid)
    {
        return (DEREncodable)pkcs12Attributes.get(oid);
    }
    public Enumeration getBagAttributeKeys()
    {
        return pkcs12Ordering.elements();
    }
}

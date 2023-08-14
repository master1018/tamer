class AlgIdDSA extends AlgorithmId implements DSAParams
{
    private static final long serialVersionUID = 3437177836797504046L;
    private BigInteger  p , q, g;
    public BigInteger   getP () { return p; }
    public BigInteger   getQ () { return q; }
    public BigInteger   getG () { return g; }
    public AlgIdDSA () {}
    AlgIdDSA (DerValue val) throws IOException
        { super(val.getOID()); }
    public AlgIdDSA (byte[] encodedAlg) throws IOException
        { super (new DerValue(encodedAlg).getOID()); }
    public AlgIdDSA (byte p [], byte q [], byte g [])
    throws IOException
    {
        this (new BigInteger (1, p),
            new BigInteger (1, q),
            new BigInteger (1, g));
    }
    public AlgIdDSA (BigInteger p, BigInteger q, BigInteger g)
    {
        super (DSA_oid);
        if (p != null || q != null || g != null) {
            if (p == null || q == null || g == null)
                throw new ProviderException("Invalid parameters for DSS/DSA" +
                                            " Algorithm ID");
            try {
                this.p = p;
                this.q = q;
                this.g = g;
                initializeParams ();
            } catch (IOException e) {
                throw new ProviderException ("Construct DSS/DSA Algorithm ID");
            }
        }
    }
    public String getName ()
        { return "DSA"; }
    private void initializeParams ()
    throws IOException
    {
        DerOutputStream out = new DerOutputStream ();
        out.putInteger(p);
        out.putInteger(q);
        out.putInteger(g);
        params = new DerValue (DerValue.tag_Sequence,out.toByteArray ());
    }
    protected void decodeParams ()
    throws IOException
    {
        if (params == null)
            throw new IOException("DSA alg params are null");
        if (params.tag != DerValue.tag_Sequence)
            throw new  IOException("DSA alg parsing error");
        params.data.reset ();
        this.p = params.data.getBigInteger();
        this.q = params.data.getBigInteger();
        this.g = params.data.getBigInteger();
        if (params.data.available () != 0)
            throw new IOException ("AlgIdDSA params, extra="+
                                   params.data.available ());
    }
    public String toString ()
        { return paramsToString (); }
    protected String paramsToString ()
    {
        if (params == null)
            return " null\n";
        else
            return
                "\n    p:\n" + Debug.toHexString(p) +
                "\n    q:\n" + Debug.toHexString(q) +
                "\n    g:\n" + Debug.toHexString(g) +
                "\n";
    }
}

public class X509V2CRLGenerator
{
    private SimpleDateFormat            dateF = new SimpleDateFormat("yyMMddHHmmss");
    private SimpleTimeZone              tz = new SimpleTimeZone(0, "Z");
    private V2TBSCertListGenerator      tbsGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private Hashtable                   extensions = null;
    private Vector                      extOrdering = null;
    public X509V2CRLGenerator()
    {
        dateF.setTimeZone(tz);
        tbsGen = new V2TBSCertListGenerator();
    }
    public void reset()
    {
        tbsGen = new V2TBSCertListGenerator();
    }
    public void setIssuerDN(
        X500Principal   issuer)
    {
        try
        {
            tbsGen.setIssuer(new X509Principal(issuer.getEncoded()));
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't process principal: " + e);
        }
    }
    public void setIssuerDN(
        X509Name   issuer)
    {
        tbsGen.setIssuer(issuer);
    }
    public void setThisUpdate(
        Date    date)
    {
        tbsGen.setThisUpdate(new Time(date));
    }
    public void setNextUpdate(
        Date    date)
    {
        tbsGen.setNextUpdate(new Time(date));
    }
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, int reason)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), reason);
    }
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, int reason, Date invalidityDate)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), reason, new DERGeneralizedTime(invalidityDate));
    }
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, X509Extensions extensions)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), extensions);
    }
    public void addCRL(X509CRL other)
        throws CRLException
    {
        Set revocations = other.getRevokedCertificates();
        Iterator it = revocations.iterator();
        while (it.hasNext())
        {
            X509CRLEntry entry = (X509CRLEntry)it.next();
            ASN1InputStream aIn = new ASN1InputStream(entry.getEncoded());
            try
            {
                tbsGen.addCRLEntry(ASN1Sequence.getInstance(aIn.readObject()));
            }
            catch (IOException e)
            {
                throw new CRLException("exception processing encoding of CRL: " + e.toString());
            }
        }
    }
    public void setSignatureAlgorithm(
        String  signatureAlgorithm)
    {
        this.signatureAlgorithm = signatureAlgorithm;
        try
        {
            sigOID = X509Util.getAlgorithmOID(signatureAlgorithm);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Unknown signature type requested");
        }
        sigAlgId = X509Util.getSigAlgID(sigOID);
        tbsGen.setSignature(sigAlgId);
    }
    public void addExtension(
        String          OID,
        boolean         critical,
        DEREncodable    value)
    {
        this.addExtension(new DERObjectIdentifier(OID), critical, value);
    }
    public void addExtension(
        DERObjectIdentifier OID,
        boolean             critical,
        DEREncodable        value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        try
        {
            dOut.writeObject(value);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("error encoding value: " + e);
        }
        this.addExtension(OID, critical, bOut.toByteArray());
    }
    public void addExtension(
        String          OID,
        boolean         critical,
        byte[]          value)
    {
        this.addExtension(new DERObjectIdentifier(OID), critical, value);
    }
    public void addExtension(
        DERObjectIdentifier OID,
        boolean             critical,
        byte[]              value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }
        extensions.put(OID, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(OID);
    }
    public X509CRL generateX509CRL(
        PrivateKey      key)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509CRL(key, "BC", null);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }
    public X509CRL generateX509CRL(
        PrivateKey      key,
        SecureRandom    random)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509CRL(key, "BC", random);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }
    public X509CRL generateX509CRL(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateX509CRL(key, provider, null);
    }
    public X509CRL generateX509CRL(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        Signature sig = null;
        try
        {
            sig = Signature.getInstance(sigOID.getId(), provider);
        }
        catch (NoSuchAlgorithmException ex)
        {
            try
            {
                sig = Signature.getInstance(signatureAlgorithm, provider);
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new SecurityException("exception creating signature: " + e.toString());
            }
        }
        if (random != null)
        {
            sig.initSign(key, random);
        }
        else
        {
            sig.initSign(key);
        }
        if (extensions != null)
        {
            tbsGen.setExtensions(new X509Extensions(extOrdering, extensions));
        }
        TBSCertList tbsCrl = tbsGen.generateTBSCertList();
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            dOut.writeObject(tbsCrl);
            sig.update(bOut.toByteArray());
        }
        catch (Exception e)
        {
            throw new SecurityException("exception encoding TBS cert - " + e);
        }
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(tbsCrl);
        v.add(sigAlgId);
        v.add(new DERBitString(sig.sign()));
        try
        {
            return new X509CRLObject(new CertificateList(new DERSequence(v)));
        }
        catch (CRLException e)
        {
            throw new SecurityException("exception creating CRL: " + e.getMessage());
        }
    }
    public Iterator getSignatureAlgNames()
    {
        return X509Util.getAlgNames();
    }
}

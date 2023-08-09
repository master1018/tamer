public class X509V2AttributeCertificateGenerator
{
    private V2AttributeCertificateInfoGenerator   acInfoGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private Hashtable                   extensions = null;
    private Vector                      extOrdering = null;
    public X509V2AttributeCertificateGenerator()
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
    }
    public void reset()
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
        extensions = null;
        extOrdering = null;
    }
    public void setHolder(
        AttributeCertificateHolder     holder)
    {
        acInfoGen.setHolder(holder.holder);
    }
    public void setIssuer(
        AttributeCertificateIssuer  issuer)
    {
        acInfoGen.setIssuer(AttCertIssuer.getInstance(issuer.form));
    }
    public void setSerialNumber(
        BigInteger      serialNumber)
    {
        acInfoGen.setSerialNumber(new DERInteger(serialNumber));
    }
    public void setNotBefore(
        Date    date)
    {
        acInfoGen.setStartDate(new DERGeneralizedTime(date));
    }
    public void setNotAfter(
        Date    date)
    {
        acInfoGen.setEndDate(new DERGeneralizedTime(date));
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
        sigAlgId = new AlgorithmIdentifier(this.sigOID, DERNull.THE_ONE);
        acInfoGen.setSignature(sigAlgId);
    }
    public void addAttribute(
        X509Attribute       attribute)
    {
        acInfoGen.addAttribute(Attribute.getInstance(attribute.toASN1Object()));
    }
    public void setIssuerUniqueId(
        boolean[] iui)
    {
        throw new RuntimeException("not implemented (yet)");
    }
    public void addExtension(
        String          OID,
        boolean         critical,
        ASN1Encodable   value)
        throws IOException
    {
        this.addExtension(OID, critical, value.getEncoded());
    }
    public void addExtension(
        String          OID,
        boolean         critical,
        byte[]          value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }
        DERObjectIdentifier oid = new DERObjectIdentifier(OID);
        extensions.put(oid, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(oid);
    }
    public X509AttributeCertificate generateCertificate(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateCertificate(key, provider, null);
    }
    public X509AttributeCertificate generateCertificate(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        Signature sig = null;
        if (sigOID == null)
        {
            throw new IllegalStateException("no signature algorithm specified");
        }
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
            acInfoGen.setExtensions(new X509Extensions(extOrdering, extensions));
        }
        AttributeCertificateInfo acInfo = acInfoGen.generateAttributeCertificateInfo();
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            dOut.writeObject(acInfo);
            sig.update(bOut.toByteArray());
        }
        catch (Exception e)
        {
            throw new SecurityException("exception encoding Attribute cert - " + e);
        }
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(acInfo);
        v.add(sigAlgId);
        v.add(new DERBitString(sig.sign()));
        try
        {
            return new X509V2AttributeCertificate(new AttributeCertificate(new DERSequence(v)));
        }
        catch (IOException e)
        {
            throw new RuntimeException("constructed invalid certificate!");
        }
    }
    public Iterator getSignatureAlgNames()
    {
        return X509Util.getAlgNames();
    }
}

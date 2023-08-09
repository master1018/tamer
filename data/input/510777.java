public class X509V3CertificateGenerator
{
    private V3TBSCertificateGenerator   tbsGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private Hashtable                   extensions = null;
    private Vector                      extOrdering = null;
    public X509V3CertificateGenerator()
    {
        tbsGen = new V3TBSCertificateGenerator();
    }
    public void reset()
    {
        tbsGen = new V3TBSCertificateGenerator();
        extensions = null;
        extOrdering = null;
    }
    public void setSerialNumber(
        BigInteger      serialNumber)
    {
        if (serialNumber.compareTo(BigInteger.ZERO) <= 0)
        {
            throw new IllegalArgumentException("serial number must be a positive integer");
        }
        tbsGen.setSerialNumber(new DERInteger(serialNumber));
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
    public void setNotBefore(
        Date    date)
    {
        tbsGen.setStartDate(new Time(date));
    }
    public void setNotAfter(
        Date    date)
    {
        tbsGen.setEndDate(new Time(date));
    }
    public void setSubjectDN(
        X500Principal   subject)
    {
        try
        {
            tbsGen.setSubject(new X509Principal(subject.getEncoded()));
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't process principal: " + e);
        }
    }
    public void setSubjectDN(
        X509Name   subject)
    {
        tbsGen.setSubject(subject);
    }
    public void setPublicKey(
        PublicKey       key)
    {
        try
        {
            tbsGen.setSubjectPublicKeyInfo(new SubjectPublicKeyInfo((ASN1Sequence)new ASN1InputStream(
                                new ByteArrayInputStream(key.getEncoded())).readObject()));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("unable to process key - " + e.toString());
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
            throw new IllegalArgumentException("Unknown signature type requested: " + signatureAlgorithm);
        }
        sigAlgId = X509Util.getSigAlgID(sigOID);
        tbsGen.setSignature(sigAlgId);
    }
    public void addExtension(
        String          oid,
        boolean         critical,
        DEREncodable    value)
    {
        this.addExtension(new DERObjectIdentifier(oid), critical, value);
    }
    public void addExtension(
        DERObjectIdentifier oid,
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
        this.addExtension(oid, critical, bOut.toByteArray());
    }
    public void addExtension(
        String          oid,
        boolean         critical,
        byte[]          value)
    {
        this.addExtension(new DERObjectIdentifier(oid), critical, value);
    }
    public void addExtension(
        DERObjectIdentifier oid,
        boolean             critical,
        byte[]              value)
    {
        if (extensions == null)
        {
            extensions = new Hashtable();
            extOrdering = new Vector();
        }
        extensions.put(oid, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(oid);
    }
    public void copyAndAddExtension(
        String          oid,
        boolean         critical,
        X509Certificate cert) 
        throws CertificateParsingException
    {
        byte[] extValue = cert.getExtensionValue(oid);
        if (extValue == null)
        {
            throw new CertificateParsingException("extension " + oid + " not present");
        }
        try
        {
            ASN1Encodable value = X509ExtensionUtil.fromExtensionValue(extValue);
            this.addExtension(oid, critical, value);
        }
        catch (IOException e)
        {
            throw new CertificateParsingException(e.toString());
        }
    }
    public void copyAndAddExtension(
        DERObjectIdentifier oid,
        boolean             critical,
        X509Certificate     cert)
        throws CertificateParsingException
    {
        this.copyAndAddExtension(oid.getId(), critical, cert);
    }
    public X509Certificate generateX509Certificate(
        PrivateKey      key)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509Certificate(key, "BC", null);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }
    public X509Certificate generateX509Certificate(
        PrivateKey      key,
        SecureRandom    random)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509Certificate(key, "BC", random);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }
    public X509Certificate generateX509Certificate(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateX509Certificate(key, provider, null);
    }
    public X509Certificate generateX509Certificate(
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
            tbsGen.setExtensions(new X509Extensions(extOrdering, extensions));
        }
        TBSCertificateStructure tbsCert = tbsGen.generateTBSCertificate();
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            dOut.writeObject(tbsCert);
            sig.update(bOut.toByteArray());
        }
        catch (Exception e)
        {
            throw new SecurityException("exception encoding TBS cert - " + e);
        }
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(tbsCert);
        v.add(sigAlgId);
        v.add(new DERBitString(sig.sign()));
        return new X509CertificateObject(new X509CertificateStructure(new DERSequence(v)));
    }
    public Iterator getSignatureAlgNames()
    {
        return X509Util.getAlgNames();
    }
}

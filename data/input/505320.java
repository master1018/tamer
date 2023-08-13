public class PKCS7SignedData
    implements PKCSObjectIdentifiers
{
    private int version, signerversion;
    private Set digestalgos;
    private Collection certs, crls;
    private X509Certificate signCert;
    private byte[] digest;
    private String digestAlgorithm, digestEncryptionAlgorithm;
    private Signature sig;
    private transient PrivateKey privKey;
    private final String ID_PKCS7_DATA = "1.2.840.113549.1.7.1";
    private final String ID_PKCS7_SIGNED_DATA = "1.2.840.113549.1.7.2";
    private final String ID_MD5 = "1.2.840.113549.2.5";
    private final String ID_MD2 = "1.2.840.113549.2.2";
    private final String ID_SHA1 = "1.3.14.3.2.26";
    private final String ID_RSA = "1.2.840.113549.1.1.1";
    private final String ID_DSA = "1.2.840.10040.4.1";
    public PKCS7SignedData(
        byte[]  in)
        throws SecurityException, CRLException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        this(in, "BC");
    }
    public PKCS7SignedData(
        byte[]  in,
        String  provider)
        throws SecurityException, CRLException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(in));
        DERObject pkcs;
        try
        {
            pkcs = din.readObject();
        }
        catch (IOException e)
        {
            throw new SecurityException("can't decode PKCS7SignedData object");
        }
        if (!(pkcs instanceof ASN1Sequence))
        {
            throw new SecurityException("Not a valid PKCS#7 object - not a sequence");
        }
        ContentInfo content = ContentInfo.getInstance(pkcs);
        if (!content.getContentType().equals(signedData))
        {
            throw new SecurityException("Not a valid PKCS#7 signed-data object - wrong header " + content.getContentType().getId());
        }
        SignedData  data = SignedData.getInstance(content.getContent());
        certs = new ArrayList();
        if (data.getCertificates() != null)
        {
            Enumeration ec = ASN1Set.getInstance(data.getCertificates()).getObjects();
            while (ec.hasMoreElements())
            {
                certs.add(new X509CertificateObject(X509CertificateStructure.getInstance(ec.nextElement())));
            }
        }
        crls = new ArrayList();
        if (data.getCRLs() != null)
        {
            Enumeration ec = ASN1Set.getInstance(data.getCRLs()).getObjects();
            while (ec.hasMoreElements())
            {
                crls.add(new X509CRLObject(CertificateList.getInstance(ec.nextElement())));
            }
        }
        version = data.getVersion().getValue().intValue();
        digestalgos = new HashSet();
        Enumeration e = data.getDigestAlgorithms().getObjects();
        while (e.hasMoreElements())
        {
            ASN1Sequence s = (ASN1Sequence)e.nextElement();
            DERObjectIdentifier o = (DERObjectIdentifier)s.getObjectAt(0);
            digestalgos.add(o.getId());
        }
        ASN1Set signerinfos = data.getSignerInfos();
        if (signerinfos.size() != 1)
        {
            throw new SecurityException("This PKCS#7 object has multiple SignerInfos - only one is supported at this time");
        }
        SignerInfo signerInfo = SignerInfo.getInstance(signerinfos.getObjectAt(0));
        signerversion = signerInfo.getVersion().getValue().intValue();
        IssuerAndSerialNumber isAnds = signerInfo.getIssuerAndSerialNumber();
        BigInteger      serialNumber = isAnds.getCertificateSerialNumber().getValue();
        X509Principal   issuer = new X509Principal(isAnds.getName());
        for (Iterator i = certs.iterator();i.hasNext();)
        {
            X509Certificate cert = (X509Certificate)i.next();
            if (serialNumber.equals(cert.getSerialNumber())
                    && issuer.equals(cert.getIssuerDN()))
            {
                signCert = cert;
                break;
            }
        }
        if (signCert == null)
        {
            throw new SecurityException("Can't find signing certificate with serial "+serialNumber.toString(16)); 
        }
        digestAlgorithm = signerInfo.getDigestAlgorithm().getObjectId().getId();
        digest = signerInfo.getEncryptedDigest().getOctets();
        digestEncryptionAlgorithm = signerInfo.getDigestEncryptionAlgorithm().getObjectId().getId();
        sig = Signature.getInstance(getDigestAlgorithm(), provider);
        sig.initVerify(signCert.getPublicKey());
    }
    public PKCS7SignedData(
        PrivateKey      privKey,
        Certificate[]   certChain,
        String          hashAlgorithm)
        throws SecurityException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        this(privKey, certChain, hashAlgorithm, "BC");
    }
    public PKCS7SignedData(
        PrivateKey      privKey,
        Certificate[]   certChain,
        String          hashAlgorithm,
        String          provider)
        throws SecurityException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        this(privKey, certChain, null, hashAlgorithm, provider);
    }
    public PKCS7SignedData(
        PrivateKey      privKey,
        Certificate[]   certChain,
        CRL[]           crlList,
        String          hashAlgorithm,
        String          provider)
        throws SecurityException, InvalidKeyException,
        NoSuchProviderException, NoSuchAlgorithmException
    {
        this.privKey = privKey;
        if (hashAlgorithm.equals("MD5"))
        {
            digestAlgorithm = ID_MD5;
        }
        else if (hashAlgorithm.equals("MD2"))
        {
            digestAlgorithm = ID_MD2;
        }
        else if (hashAlgorithm.equals("SHA"))
        {
            digestAlgorithm = ID_SHA1;
        }
        else if (hashAlgorithm.equals("SHA1"))
        {
            digestAlgorithm = ID_SHA1;
        }
        else
        {
            throw new NoSuchAlgorithmException("Unknown Hash Algorithm "+hashAlgorithm);
        }
        version = signerversion = 1;
        certs = new ArrayList();
        crls = new ArrayList();
        digestalgos = new HashSet();
        digestalgos.add(digestAlgorithm);
        signCert = (X509Certificate)certChain[0];
        for (int i = 0;i < certChain.length;i++)
        {
            certs.add(certChain[i]);
        }
        if (crlList != null)
        {
            for (int i = 0;i < crlList.length;i++)
            {
                crls.add(crlList[i]);
            }
        }
        digestEncryptionAlgorithm = privKey.getAlgorithm();
        if (digestEncryptionAlgorithm.equals("RSA"))
        {
            digestEncryptionAlgorithm = ID_RSA;
        }
        else if (digestEncryptionAlgorithm.equals("DSA"))
        {
            digestEncryptionAlgorithm = ID_DSA;
        }
        else
        {
            throw new NoSuchAlgorithmException("Unknown Key Algorithm "+digestEncryptionAlgorithm);
        }
        sig = Signature.getInstance(getDigestAlgorithm(), provider);
        sig.initSign(privKey);
    }
    public String getDigestAlgorithm()
    {
        String da = digestAlgorithm;
        String dea = digestEncryptionAlgorithm;
        if (digestAlgorithm.equals(ID_MD5))
        {
            da = "MD5";
        }
        else if (digestAlgorithm.equals(ID_MD2))
        {
            da = "MD2";
        }
        else if (digestAlgorithm.equals(ID_SHA1))
        {
            da = "SHA1";
        }
        if (digestEncryptionAlgorithm.equals(ID_RSA))
        {
            dea = "RSA";
        }
        else if (digestEncryptionAlgorithm.equals(ID_DSA))
        {
            dea = "DSA";
        }
        return da + "with" + dea;
    }
    public void reset()
    {
        try
        {
            if (privKey==null)
            {
                sig.initVerify(signCert.getPublicKey());
            }
            else
            {
                sig.initSign(privKey);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }
    public Certificate[] getCertificates()
    {
        return (X509Certificate[])certs.toArray(new X509Certificate[certs.size()]);
    }
    public Collection getCRLs()
    {
        return crls;
    }
    public X509Certificate getSigningCertificate()
    {
        return signCert;
    }
    public int getVersion()
    {
        return version;
    }
    public int getSigningInfoVersion()
    {
        return signerversion;
    }
    public void update(byte buf)
        throws SignatureException
    {
        sig.update(buf);
    }
    public void update(byte[] buf, int off, int len)
        throws SignatureException
    {
        sig.update(buf, off, len);
    }
    public boolean verify()
        throws SignatureException
    {
        return sig.verify(digest);
    }
    private DERObject getIssuer(byte[] enc)
    {
        try
        {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(enc));
            ASN1Sequence seq = (ASN1Sequence)in.readObject();
            return (DERObject)seq.getObjectAt(seq.getObjectAt(0) instanceof DERTaggedObject ? 3 : 2);
        }
        catch (IOException e)
        {
            throw new Error("IOException reading from ByteArray: "+e);
        }
    }
    public byte[] getEncoded()
    {
        try
        {
            digest = sig.sign();
            ASN1EncodableVector v = new ASN1EncodableVector();
            for (Iterator i = digestalgos.iterator(); i.hasNext();)
            {
                AlgorithmIdentifier a = new AlgorithmIdentifier(
                            new DERObjectIdentifier((String)i.next()),
                            null);
                v.add(a);
            }
            DERSet algos = new DERSet(v);
            DERSequence contentinfo = new DERSequence(
                                        new DERObjectIdentifier(ID_PKCS7_DATA));
            v = new ASN1EncodableVector();
            for (Iterator i = certs.iterator();i.hasNext();)
            {
                ASN1InputStream tempstream = new ASN1InputStream(new ByteArrayInputStream(((X509Certificate)i.next()).getEncoded()));
                v.add(tempstream.readObject());
            }
            DERSet dercertificates = new DERSet(v);
            ASN1EncodableVector signerinfo = new ASN1EncodableVector();
            signerinfo.add(new DERInteger(signerversion));
            IssuerAndSerialNumber isAnds = new IssuerAndSerialNumber(
                        new X509Name((ASN1Sequence)getIssuer(signCert.getTBSCertificate())),
                        new DERInteger(signCert.getSerialNumber()));
            signerinfo.add(isAnds);
            signerinfo.add(new AlgorithmIdentifier(
                                new DERObjectIdentifier(digestAlgorithm),
                                DERNull.THE_ONE));
            signerinfo.add(new AlgorithmIdentifier(
                                new DERObjectIdentifier(digestEncryptionAlgorithm),
                                DERNull.THE_ONE));
            signerinfo.add(new DEROctetString(digest));
            ASN1EncodableVector body = new ASN1EncodableVector();
            body.add(new DERInteger(version));
            body.add(algos);
            body.add(contentinfo);
            body.add(new DERTaggedObject(false, 0, dercertificates));
            if (crls.size()>0)
            {
                v = new ASN1EncodableVector();
                for (Iterator i = crls.iterator();i.hasNext();)
                {
                    ASN1InputStream t = new ASN1InputStream(new ByteArrayInputStream(((X509CRL)i.next()).getEncoded()));
                    v.add(t.readObject());
                }
                DERSet dercrls = new DERSet(v);
                body.add(new DERTaggedObject(false, 1, dercrls));
            }
            body.add(new DERSet(new DERSequence(signerinfo)));
            ASN1EncodableVector whole = new ASN1EncodableVector();
            whole.add(new DERObjectIdentifier(ID_PKCS7_SIGNED_DATA));
            whole.add(new DERTaggedObject(0, new DERSequence(body)));
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream dout = new DEROutputStream(bOut);
            dout.writeObject(new DERSequence(whole));
            dout.close();
            return bOut.toByteArray();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.toString());
        }
    }
}

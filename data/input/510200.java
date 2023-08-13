public  class PKIXCertPath
    extends CertPath
{
    static final List certPathEncodings;
    static
    {
        List encodings = new ArrayList();
        encodings.add("PkiPath");
        encodings.add("PEM");
        encodings.add("PKCS7");
        certPathEncodings = Collections.unmodifiableList(encodings);
    }
    private List certificates;
    private List sortCerts(
        List certs)
    {
        if (certs.size() < 2)
        {
            return certs;
        }
        X500Principal   issuer = ((X509Certificate)certs.get(0)).getIssuerX500Principal();
        boolean         okay = true;
        for (int i = 1; i != certs.size(); i++) 
        {
            X509Certificate cert = (X509Certificate)certs.get(i);
            if (issuer.equals(cert.getSubjectX500Principal()))
            {
                issuer = ((X509Certificate)certs.get(i)).getIssuerX500Principal();
            }
            else
            {
                okay = false;
                break;
            }
        }
        if (okay)
        {
            return certs;
        }
        List       retList = new ArrayList(certs.size());
        for (int i = 0; i < certs.size(); i++)
        {
            X509Certificate cert = (X509Certificate)certs.get(i);
            boolean         found = false;
            X500Principal   subject = cert.getSubjectX500Principal();
            for (int j = 0; j != certs.size(); j++)
            {
                X509Certificate c = (X509Certificate)certs.get(j);
                if (c.getIssuerX500Principal().equals(subject))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                retList.add(cert);
                certs.remove(i);
            }
        }
        if (retList.size() > 1)
        {
            for (int i = 0; i != certs.size(); i++)
            {
                retList.add(certs.get(i));
            }
            return retList;
        }
        for (int i = 0; i != retList.size(); i++)
        {
            issuer = ((X509Certificate)retList.get(i)).getIssuerX500Principal();
            for (int j = 0; j < certs.size(); j++)
            {
                X509Certificate c = (X509Certificate)certs.get(j);
                if (issuer.equals(c.getSubjectX500Principal()))
                {
                    retList.add(c);
                    certs.remove(j);
                    break;
                }
            }
        }
        for (int i = 0; i != certs.size(); i++)
        {
            retList.add(certs.get(i));
        }
        return retList;
    }
    PKIXCertPath(List certificates)
    {
        super("X.509");
        this.certificates = sortCerts(new ArrayList(certificates));
    }
    PKIXCertPath(
        InputStream inStream,
        String encoding)
        throws CertificateException
    {
        super("X.509");
        try
        {
            if (encoding.equalsIgnoreCase("PkiPath"))
            {
                ASN1InputStream derInStream = new ASN1InputStream(inStream);
                DERObject derObject = derInStream.readObject();
                if (!(derObject instanceof ASN1Sequence))
                {
                    throw new CertificateException("input stream does not contain a ASN1 SEQUENCE while reading PkiPath encoded data to load CertPath");
                }
                Enumeration e = ((ASN1Sequence)derObject).getObjects();
                InputStream certInStream;
                ByteArrayOutputStream outStream;
                DEROutputStream derOutStream;
                certificates = new ArrayList();
                CertificateFactory certFactory= CertificateFactory.getInstance("X.509", "BC");
                while (e.hasMoreElements())
                {
                    outStream = new ByteArrayOutputStream();
                    derOutStream = new DEROutputStream(outStream);
                    derOutStream.writeObject(e.nextElement());
                    derOutStream.close();
                    certInStream = new ByteArrayInputStream(outStream.toByteArray());
                    certificates.add(0,certFactory.generateCertificate(certInStream));
                }
            }
            else if (encoding.equalsIgnoreCase("PKCS7") || encoding.equalsIgnoreCase("PEM"))
            {
                inStream = new BufferedInputStream(inStream, 8192);
                certificates = new ArrayList();
                CertificateFactory certFactory= CertificateFactory.getInstance("X.509", "BC");
                Certificate cert;
                while ((cert = certFactory.generateCertificate(inStream)) != null)
                {
                    certificates.add(cert);
                }
            }
            else
            {
                throw new CertificateException("unsupported encoding: " + encoding);
            }
        }
        catch (IOException ex) 
        {
            throw new CertificateException("IOException throw while decoding CertPath:\n" + ex.toString()); 
        }
        catch (NoSuchProviderException ex) 
        {
            throw new CertificateException("BouncyCastle provider not found while trying to get a CertificateFactory:\n" + ex.toString()); 
        }
        this.certificates = sortCerts(certificates);
    }
    public Iterator getEncodings()
    {
        return certPathEncodings.iterator();
    }
    public byte[] getEncoded()
        throws CertificateEncodingException
    {
        Iterator iter = getEncodings();
        if (iter.hasNext())
        {
            Object enc = iter.next();
            if (enc instanceof String)
            {
            return getEncoded((String)enc);
            }
        }
        return null;
    }
    public byte[] getEncoded(String encoding)
        throws CertificateEncodingException
    {
        if (encoding.equalsIgnoreCase("PkiPath"))
        {
            ASN1EncodableVector v = new ASN1EncodableVector();
            ListIterator iter = certificates.listIterator(certificates.size());
            while (iter.hasPrevious())
            {
                v.add(toASN1Object((X509Certificate)iter.previous()));
            }
            return toDEREncoded(new DERSequence(v));
        }
        else if (encoding.equalsIgnoreCase("PKCS7"))
        {
            ContentInfo encInfo = new ContentInfo(PKCSObjectIdentifiers.data, null);
            ASN1EncodableVector v = new ASN1EncodableVector();
            for (int i = 0; i != certificates.size(); i++)
            {
                v.add(toASN1Object((X509Certificate)certificates.get(i)));
            }
            SignedData  sd = new SignedData(
                                     new DERInteger(1),
                                     new DERSet(),
                                     encInfo, 
                                     new DERSet(v), 
                                     null, 
                                     new DERSet());
            return toDEREncoded(new ContentInfo(
                    PKCSObjectIdentifiers.signedData, sd));
        }
        else
        {
            throw new CertificateEncodingException("unsupported encoding: " + encoding);
        }
    }
    public List getCertificates()
    {
        return Collections.unmodifiableList(new ArrayList(certificates));
    }
    private DERObject toASN1Object(
        X509Certificate cert)
        throws CertificateEncodingException
    {
        try
        {
            return new ASN1InputStream(cert.getEncoded()).readObject();
        }
        catch (Exception e)
        {
            throw new CertificateEncodingException("Exception while encoding certificate: " + e.toString());
        }
    }
    private byte[] toDEREncoded(ASN1Encodable obj) 
        throws CertificateEncodingException
    {
        try
        {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            DEROutputStream       dOut = new DEROutputStream(bOut);
            dOut.writeObject(obj);
            dOut.close();
            return bOut.toByteArray();
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException("Exeption thrown: " + e);
        }
    }
}

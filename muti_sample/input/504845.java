public class X509CertificateObject
    extends X509Certificate
    implements PKCS12BagAttributeCarrier
{
    private X509CertificateStructure    c;
    private OrderedTable                pkcs12 = new OrderedTable();
    public X509CertificateObject(
        X509CertificateStructure    c)
    {
        this.c = c;
    }
    public void checkValidity()
        throws CertificateExpiredException, CertificateNotYetValidException
    {
        this.checkValidity(new Date());
    }
    public void checkValidity(
        Date    date)
        throws CertificateExpiredException, CertificateNotYetValidException
    {
        if (date.after(this.getNotAfter()))
        {
            throw new CertificateExpiredException("certificate expired on " + c.getEndDate().getTime());
        }
        if (date.before(this.getNotBefore()))
        {
            throw new CertificateNotYetValidException("certificate not valid till " + c.getStartDate().getTime());
        }
    }
    public int getVersion()
    {
        return c.getVersion();
    }
    public BigInteger getSerialNumber()
    {
        return c.getSerialNumber().getValue();
    }
    public Principal getIssuerDN()
    {
        return new X509Principal(c.getIssuer());
    }
    public X500Principal getIssuerX500Principal()
    {
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
            aOut.writeObject(c.getIssuer());
            return new X500Principal(bOut.toByteArray());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("can't encode issuer DN");
        }
    }
    public Principal getSubjectDN()
    {
        return new X509Principal(c.getSubject());
    }
    public X500Principal getSubjectX500Principal()
    {
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
            aOut.writeObject(c.getSubject());
            return new X500Principal(bOut.toByteArray());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("can't encode issuer DN");
        }
    }
    public Date getNotBefore()
    {
        return c.getStartDate().getDate();
    }
    public Date getNotAfter()
    {
        return c.getEndDate().getDate();
    }
    public byte[] getTBSCertificate()
        throws CertificateEncodingException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        try
        {
            dOut.writeObject(c.getTBSCertificate());
            return bOut.toByteArray();
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException(e.toString());
        }
    }
    public byte[] getSignature()
    {
        return c.getSignature().getBytes();
    }
    public String getSigAlgName()
    {
        Provider    prov = Security.getProvider("BC");
        String      algName = prov.getProperty("Alg.Alias.Signature." + this.getSigAlgOID());
        if (algName != null)
        {
            return algName;
        }
        Provider[] provs = Security.getProviders();
        for (int i = 0; i != provs.length; i++)
        {
            algName = provs[i].getProperty("Alg.Alias.Signature." + this.getSigAlgOID());
            if (algName != null)
            {
                return algName;
            }
        }
        return this.getSigAlgOID();
    }
    public String getSigAlgOID()
    {
        return c.getSignatureAlgorithm().getObjectId().getId();
    }
    public byte[] getSigAlgParams()
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        if (c.getSignatureAlgorithm().getParameters() != null)
        {
            try
            {
                DEROutputStream         dOut = new DEROutputStream(bOut);
                dOut.writeObject(c.getSignatureAlgorithm().getParameters());
            }
            catch (Exception e)
            {
                throw new RuntimeException("exception getting sig parameters " + e);
            }
            return bOut.toByteArray();
        }
        else
        {
            return null;
        }
    }
    public boolean[] getIssuerUniqueID()
    {
        DERBitString    id = c.getTBSCertificate().getIssuerUniqueId();
        if (id != null)
        {
            byte[]          bytes = id.getBytes();
            boolean[]       boolId = new boolean[bytes.length * 8 - id.getPadBits()];
            for (int i = 0; i != boolId.length; i++)
            {
                boolId[i] = (bytes[i / 8] & (0x80 >>> (i % 8))) != 0;
            }
            return boolId;
        }
        return null;
    }
    public boolean[] getSubjectUniqueID()
    {
        DERBitString    id = c.getTBSCertificate().getSubjectUniqueId();
        if (id != null)
        {
            byte[]          bytes = id.getBytes();
            boolean[]       boolId = new boolean[bytes.length * 8 - id.getPadBits()];
            for (int i = 0; i != boolId.length; i++)
            {
                boolId[i] = (bytes[i / 8] & (0x80 >>> (i % 8))) != 0;
            }
            return boolId;
        }
        return null;
    }
    public boolean[] getKeyUsage()
    {
        byte[]  bytes = this.getExtensionBytes("2.5.29.15");
        int     length = 0;
        if (bytes != null)
        {
            try
            {
                ASN1InputStream dIn = new ASN1InputStream(bytes);
                DERBitString    bits = (DERBitString)dIn.readObject();
                bytes = bits.getBytes();
                length = (bytes.length * 8) - bits.getPadBits();
            }
            catch (Exception e)
            {
                throw new RuntimeException("error processing key usage extension");
            }
            boolean[]       keyUsage = new boolean[(length < 9) ? 9 : length];
            for (int i = 0; i != length; i++)
            {
                keyUsage[i] = (bytes[i / 8] & (0x80 >>> (i % 8))) != 0;
            }
            return keyUsage;
        }
        return null;
    }
    public List getExtendedKeyUsage() 
        throws CertificateParsingException
    {
        byte[]  bytes = this.getExtensionBytes("2.5.29.37");
        int     length = 0;
        if (bytes != null)
        {
            try
            {
                ASN1InputStream dIn = new ASN1InputStream(bytes);
                ASN1Sequence    seq = (ASN1Sequence)dIn.readObject();
                List            list = new ArrayList();
                for (int i = 0; i != seq.size(); i++)
                {
                    list.add(((DERObjectIdentifier)seq.getObjectAt(i)).getId());
                }
                return Collections.unmodifiableList(list);
            }
            catch (Exception e)
            {
                throw new CertificateParsingException("error processing extended key usage extension");
            }
        }
        return null;
    }
    public int getBasicConstraints()
    {
        byte[]  bytes = this.getExtensionBytes("2.5.29.19");
        if (bytes != null)
        {
            try
            {
                ASN1InputStream dIn = new ASN1InputStream(bytes);
                ASN1Sequence    seq = (ASN1Sequence)dIn.readObject();
                if (seq.size() == 2)
                {
                    if (((DERBoolean)seq.getObjectAt(0)).isTrue())
                    {
                        return ((DERInteger)seq.getObjectAt(1)).getValue().intValue();
                    }
                    else
                    {
                        return -1;
                    }
                }
                else if (seq.size() == 1)
                {
                    if (seq.getObjectAt(0) instanceof DERBoolean)
                    {
                        if (((DERBoolean)seq.getObjectAt(0)).isTrue())
                        {
                            return Integer.MAX_VALUE;
                        }
                        else
                        {
                            return -1;
                        }
                    }
                    else
                    {
                        return -1;
                    }
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("error processing basic constraints extension");
            }
        }
        return -1;
    }
    public Set getCriticalExtensionOIDs() 
    {
        if (this.getVersion() == 3)
        {
            Set             set = new HashSet();
            X509Extensions  extensions = c.getTBSCertificate().getExtensions();
            if (extensions != null)
            {
                Enumeration     e = extensions.oids();
                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    X509Extension       ext = extensions.getExtension(oid);
                    if (ext.isCritical())
                    {
                        set.add(oid.getId());
                    }
                }
                return set;
            }
        }
        return null;
    }
    private byte[] getExtensionBytes(String oid)
    {
        X509Extensions exts = c.getTBSCertificate().getExtensions();
        if (exts != null)
        {
            X509Extension   ext = exts.getExtension(new DERObjectIdentifier(oid));
            if (ext != null)
            {
                return ext.getValue().getOctets();
            }
        }
        return null;
    }
    public byte[] getExtensionValue(String oid) 
    {
        X509Extensions exts = c.getTBSCertificate().getExtensions();
        if (exts != null)
        {
            X509Extension   ext = exts.getExtension(new DERObjectIdentifier(oid));
            if (ext != null)
            {
                ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
                DEROutputStream            dOut = new DEROutputStream(bOut);
                try
                {
                    dOut.writeObject(ext.getValue());
                    return bOut.toByteArray();
                }
                catch (Exception e)
                {
                    throw new RuntimeException("error encoding " + e.toString());
                }
            }
        }
        return null;
    }
    public Set getNonCriticalExtensionOIDs() 
    {
        if (this.getVersion() == 3)
        {
            Set             set = new HashSet();
            X509Extensions  extensions = c.getTBSCertificate().getExtensions();
            if (extensions != null)
            {
                Enumeration     e = extensions.oids();
                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    X509Extension       ext = extensions.getExtension(oid);
                    if (!ext.isCritical())
                    {
                        set.add(oid.getId());
                    }
                }
                return set;
            }
        }
        return null;
    }
    public boolean hasUnsupportedCriticalExtension()
    {
        if (this.getVersion() == 3)
        {
            X509Extensions  extensions = c.getTBSCertificate().getExtensions();
            if (extensions != null)
            {
                Enumeration     e = extensions.oids();
                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    if (oid.getId().equals("2.5.29.15")
                       || oid.getId().equals("2.5.29.19"))
                    {
                        continue;
                    }
                    X509Extension       ext = extensions.getExtension(oid);
                    if (ext.isCritical())
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public PublicKey getPublicKey()
    {
        return JDKKeyFactory.createPublicKeyFromPublicKeyInfo(c.getSubjectPublicKeyInfo());
    }
    private ByteArrayOutputStream encodedOut;
    public byte[] getEncoded()
            throws CertificateEncodingException {
        synchronized (this) {
            if (encodedOut == null) {
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                DEROutputStream dOut = new DEROutputStream(bOut);
                try {
                    dOut.writeObject(c);
                    encodedOut = bOut;
                } catch (IOException e) {
                    throw new CertificateEncodingException(e.toString());
                }
            }
        }
        return encodedOut.toByteArray();
    }
    public boolean equals(
        Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof Certificate))
        {
            return false;
        }
        Certificate other = (Certificate)o;
        try
        {
            byte[] b1 = this.getEncoded();
            byte[] b2 = other.getEncoded();
            return Arrays.areEqual(b1, b2);
        }
        catch (CertificateEncodingException e)
        {
            return false;
        }
    }
    public int hashCode()
    {
        return c.hashCode();
    }
    public void setBagAttribute(
        DERObjectIdentifier oid,
        DEREncodable        attribute)
    {
        pkcs12.add(oid, attribute);
    }
    public DEREncodable getBagAttribute(
        DERObjectIdentifier oid)
    {
        return (DEREncodable)pkcs12.get(oid);
    }
    public Enumeration getBagAttributeKeys()
    {
        return pkcs12.getKeys();
    }
    public String toString()
    {
        StringBuffer    buf = new StringBuffer();
        String          nl = System.getProperty("line.separator");
        buf.append("  [0]         Version: ").append(this.getVersion()).append(nl);
        buf.append("         SerialNumber: ").append(this.getSerialNumber()).append(nl);
        buf.append("             IssuerDN: ").append(this.getIssuerDN()).append(nl);
        buf.append("           Start Date: ").append(this.getNotBefore()).append(nl);
        buf.append("           Final Date: ").append(this.getNotAfter()).append(nl);
        buf.append("            SubjectDN: ").append(this.getSubjectDN()).append(nl);
        buf.append("           Public Key: ").append(this.getPublicKey()).append(nl);
        buf.append("  Signature Algorithm: ").append(this.getSigAlgName()).append(nl);
        byte[]  sig = this.getSignature();
        buf.append("            Signature: ").append(new String(Hex.encode(sig, 0, 20))).append(nl);
        for (int i = 20; i < sig.length; i += 20)
        {
            if (i < sig.length - 20)
            {
                buf.append("                       ").append(new String(Hex.encode(sig, i, 20))).append(nl);
            }
            else
            {
                buf.append("                       ").append(new String(Hex.encode(sig, i, sig.length - i))).append(nl);
            }
        }
        X509Extensions  extensions = c.getTBSCertificate().getExtensions();
        if (extensions != null)
        {
            Enumeration     e = extensions.oids();
            if (e.hasMoreElements())
            {
                buf.append("       Extensions: \n");
            }
            while (e.hasMoreElements())
            {
                DERObjectIdentifier     oid = (DERObjectIdentifier)e.nextElement();
                X509Extension           ext = extensions.getExtension(oid);
                if (ext.getValue() != null)
                {
                    byte[]                  octs = ext.getValue().getOctets();
                    ASN1InputStream         dIn = new ASN1InputStream(octs);
                    buf.append("                       critical(").append(ext.isCritical()).append(") ");
                    try
                    {
                        if (oid.equals(X509Extensions.BasicConstraints))
                        {
                            buf.append(new BasicConstraints((ASN1Sequence)dIn.readObject())).append(nl);
                        }
                        else if (oid.equals(X509Extensions.KeyUsage))
                        {
                            buf.append(new KeyUsage((DERBitString)dIn.readObject())).append(nl);
                        }
                        else if (oid.equals(MiscObjectIdentifiers.netscapeCertType))
                        {
                            buf.append(new NetscapeCertType((DERBitString)dIn.readObject())).append(nl);
                        }
                        else if (oid.equals(MiscObjectIdentifiers.netscapeRevocationURL))
                        {
                            buf.append(new NetscapeRevocationURL((DERIA5String)dIn.readObject())).append(nl);
                        }
                        else if (oid.equals(MiscObjectIdentifiers.verisignCzagExtension))
                        {
                            buf.append(new VerisignCzagExtension((DERIA5String)dIn.readObject())).append(nl);
                        }
                        else 
                        {
                            buf.append(oid.getId());
                            buf.append(" value = ").append(ASN1Dump.dumpAsString(dIn.readObject())).append(nl);
                        }
                    }
                    catch (Exception ex)
                    {
                        buf.append(oid.getId());
                        buf.append(" value = ").append("*****").append(nl);
                    }
                }
                else
                {
                    buf.append(nl);
                }
            }
        }
        return buf.toString();
    }
    public final void verify(
        PublicKey   key)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException, SignatureException
    {
        Signature   signature = null;
        String      sigName = X509SignatureUtil.getSignatureName(c.getSignatureAlgorithm());
        try
        {
            signature = Signature.getInstance(sigName, "BC");
        }
        catch (Exception e)
        {
            signature = Signature.getInstance(sigName);
        }
        checkSignature(key, signature);
    }
    public final void verify(
        PublicKey   key,
        String      sigProvider)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException, SignatureException
    {
        String    sigName = X509SignatureUtil.getSignatureName(c.getSignatureAlgorithm());
        Signature signature = Signature.getInstance(sigName, sigProvider);
        checkSignature(key, signature);
    }
    private void checkSignature(
        PublicKey key, 
        Signature signature) 
        throws CertificateException, NoSuchAlgorithmException, 
            SignatureException, InvalidKeyException, CertificateEncodingException
    {
        if (!c.getSignatureAlgorithm().equals(c.getTBSCertificate().getSignature()))
        {
            throw new CertificateException("signature algorithm in TBS cert not same as outer cert");
        }
        DEREncodable params = c.getSignatureAlgorithm().getParameters();
        X509SignatureUtil.setSignatureParameters(signature, params);
        signature.initVerify(key);
        signature.update(this.getTBSCertificate());
        if (!signature.verify(this.getSignature()))
        {
            throw new InvalidKeyException("Public key presented not for certificate signature");
        }
    }
}

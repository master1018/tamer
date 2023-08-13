public class X509CRLImpl extends X509CRL implements DerEncoder {
    private byte[]      signedCRL = null; 
    private byte[]      signature = null; 
    private byte[]      tbsCertList = null; 
    private AlgorithmId sigAlgId = null; 
    private int              version;
    private AlgorithmId      infoSigAlgId; 
    private X500Name         issuer = null;
    private X500Principal    issuerPrincipal = null;
    private Date             thisUpdate = null;
    private Date             nextUpdate = null;
    private Map<X509IssuerSerial,X509CRLEntry> revokedCerts = new LinkedHashMap<X509IssuerSerial,X509CRLEntry>();
    private CRLExtensions    extensions = null;
    private final static boolean isExplicit = true;
    private static final long YR_2050 = 2524636800000L;
    private boolean readOnly = false;
    private PublicKey verifiedPublicKey;
    private String verifiedProvider;
    private X509CRLImpl() { }
    public X509CRLImpl(byte[] crlData) throws CRLException {
        try {
            parse(new DerValue(crlData));
        } catch (IOException e) {
            signedCRL = null;
            throw new CRLException("Parsing error: " + e.getMessage());
        }
    }
    public X509CRLImpl(DerValue val) throws CRLException {
        try {
            parse(val);
        } catch (IOException e) {
            signedCRL = null;
            throw new CRLException("Parsing error: " + e.getMessage());
        }
    }
    public X509CRLImpl(InputStream inStrm) throws CRLException {
        try {
            parse(new DerValue(inStrm));
        } catch (IOException e) {
            signedCRL = null;
            throw new CRLException("Parsing error: " + e.getMessage());
        }
    }
    public X509CRLImpl(X500Name issuer, Date thisDate, Date nextDate) {
        this.issuer = issuer;
        this.thisUpdate = thisDate;
        this.nextUpdate = nextDate;
    }
    public X509CRLImpl(X500Name issuer, Date thisDate, Date nextDate,
                       X509CRLEntry[] badCerts)
        throws CRLException
    {
        this.issuer = issuer;
        this.thisUpdate = thisDate;
        this.nextUpdate = nextDate;
        if (badCerts != null) {
            X500Principal crlIssuer = getIssuerX500Principal();
            X500Principal badCertIssuer = crlIssuer;
            for (int i = 0; i < badCerts.length; i++) {
                X509CRLEntryImpl badCert = (X509CRLEntryImpl)badCerts[i];
                try {
                    badCertIssuer = getCertIssuer(badCert, badCertIssuer);
                } catch (IOException ioe) {
                    throw new CRLException(ioe);
                }
                badCert.setCertificateIssuer(crlIssuer, badCertIssuer);
                X509IssuerSerial issuerSerial = new X509IssuerSerial
                    (badCertIssuer, badCert.getSerialNumber());
                this.revokedCerts.put(issuerSerial, badCert);
                if (badCert.hasExtensions()) {
                    this.version = 1;
                }
            }
        }
    }
    public X509CRLImpl(X500Name issuer, Date thisDate, Date nextDate,
               X509CRLEntry[] badCerts, CRLExtensions crlExts)
        throws CRLException
    {
        this(issuer, thisDate, nextDate, badCerts);
        if (crlExts != null) {
            this.extensions = crlExts;
            this.version = 1;
        }
    }
    public byte[] getEncodedInternal() throws CRLException {
        if (signedCRL == null) {
            throw new CRLException("Null CRL to encode");
        }
        return signedCRL;
    }
    public byte[] getEncoded() throws CRLException {
        return getEncodedInternal().clone();
    }
    public void encodeInfo(OutputStream out) throws CRLException {
        try {
            DerOutputStream tmp = new DerOutputStream();
            DerOutputStream rCerts = new DerOutputStream();
            DerOutputStream seq = new DerOutputStream();
            if (version != 0) 
                tmp.putInteger(version);
            infoSigAlgId.encode(tmp);
            if ((version == 0) && (issuer.toString() == null))
                throw new CRLException("Null Issuer DN not allowed in v1 CRL");
            issuer.encode(tmp);
            if (thisUpdate.getTime() < YR_2050)
                tmp.putUTCTime(thisUpdate);
            else
                tmp.putGeneralizedTime(thisUpdate);
            if (nextUpdate != null) {
                if (nextUpdate.getTime() < YR_2050)
                    tmp.putUTCTime(nextUpdate);
                else
                    tmp.putGeneralizedTime(nextUpdate);
            }
            if (!revokedCerts.isEmpty()) {
                for (X509CRLEntry entry : revokedCerts.values()) {
                    ((X509CRLEntryImpl)entry).encode(rCerts);
                }
                tmp.write(DerValue.tag_Sequence, rCerts);
            }
            if (extensions != null)
                extensions.encode(tmp, isExplicit);
            seq.write(DerValue.tag_Sequence, tmp);
            tbsCertList = seq.toByteArray();
            out.write(tbsCertList);
        } catch (IOException e) {
             throw new CRLException("Encoding error: " + e.getMessage());
        }
    }
    public void verify(PublicKey key)
    throws CRLException, NoSuchAlgorithmException, InvalidKeyException,
           NoSuchProviderException, SignatureException {
        verify(key, "");
    }
    public synchronized void verify(PublicKey key, String sigProvider)
            throws CRLException, NoSuchAlgorithmException, InvalidKeyException,
            NoSuchProviderException, SignatureException {
        if (sigProvider == null) {
            sigProvider = "";
        }
        if ((verifiedPublicKey != null) && verifiedPublicKey.equals(key)) {
            if (sigProvider.equals(verifiedProvider)) {
                return;
            }
        }
        if (signedCRL == null) {
            throw new CRLException("Uninitialized CRL");
        }
        Signature   sigVerf = null;
        if (sigProvider.length() == 0) {
            sigVerf = Signature.getInstance(sigAlgId.getName());
        } else {
            sigVerf = Signature.getInstance(sigAlgId.getName(), sigProvider);
        }
        sigVerf.initVerify(key);
        if (tbsCertList == null) {
            throw new CRLException("Uninitialized CRL");
        }
        sigVerf.update(tbsCertList, 0, tbsCertList.length);
        if (!sigVerf.verify(signature)) {
            throw new SignatureException("Signature does not match.");
        }
        verifiedPublicKey = key;
        verifiedProvider = sigProvider;
    }
    public void sign(PrivateKey key, String algorithm)
    throws CRLException, NoSuchAlgorithmException, InvalidKeyException,
        NoSuchProviderException, SignatureException {
        sign(key, algorithm, null);
    }
    public void sign(PrivateKey key, String algorithm, String provider)
    throws CRLException, NoSuchAlgorithmException, InvalidKeyException,
        NoSuchProviderException, SignatureException {
        try {
            if (readOnly)
                throw new CRLException("cannot over-write existing CRL");
            Signature sigEngine = null;
            if ((provider == null) || (provider.length() == 0))
                sigEngine = Signature.getInstance(algorithm);
            else
                sigEngine = Signature.getInstance(algorithm, provider);
            sigEngine.initSign(key);
            sigAlgId = AlgorithmId.get(sigEngine.getAlgorithm());
            infoSigAlgId = sigAlgId;
            DerOutputStream out = new DerOutputStream();
            DerOutputStream tmp = new DerOutputStream();
            encodeInfo(tmp);
            sigAlgId.encode(tmp);
            sigEngine.update(tbsCertList, 0, tbsCertList.length);
            signature = sigEngine.sign();
            tmp.putBitString(signature);
            out.write(DerValue.tag_Sequence, tmp);
            signedCRL = out.toByteArray();
            readOnly = true;
        } catch (IOException e) {
            throw new CRLException("Error while encoding data: " +
                                   e.getMessage());
        }
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("X.509 CRL v" + (version+1) + "\n");
        if (sigAlgId != null)
            sb.append("Signature Algorithm: " + sigAlgId.toString() +
                  ", OID=" + (sigAlgId.getOID()).toString() + "\n");
        if (issuer != null)
            sb.append("Issuer: " + issuer.toString() + "\n");
        if (thisUpdate != null)
            sb.append("\nThis Update: " + thisUpdate.toString() + "\n");
        if (nextUpdate != null)
            sb.append("Next Update: " + nextUpdate.toString() + "\n");
        if (revokedCerts.isEmpty())
            sb.append("\nNO certificates have been revoked\n");
        else {
            sb.append("\nRevoked Certificates: " + revokedCerts.size());
            int i = 1;
            for (Iterator<X509CRLEntry> iter = revokedCerts.values().iterator();
                                             iter.hasNext(); i++)
                sb.append("\n[" + i + "] " + iter.next().toString());
        }
        if (extensions != null) {
            Collection<Extension> allExts = extensions.getAllExtensions();
            Object[] objs = allExts.toArray();
            sb.append("\nCRL Extensions: " + objs.length);
            for (int i = 0; i < objs.length; i++) {
                sb.append("\n[" + (i+1) + "]: ");
                Extension ext = (Extension)objs[i];
                try {
                   if (OIDMap.getClass(ext.getExtensionId()) == null) {
                       sb.append(ext.toString());
                       byte[] extValue = ext.getExtensionValue();
                       if (extValue != null) {
                           DerOutputStream out = new DerOutputStream();
                           out.putOctetString(extValue);
                           extValue = out.toByteArray();
                           HexDumpEncoder enc = new HexDumpEncoder();
                           sb.append("Extension unknown: "
                                     + "DER encoded OCTET string =\n"
                                     + enc.encodeBuffer(extValue) + "\n");
                      }
                   } else
                       sb.append(ext.toString()); 
                } catch (Exception e) {
                    sb.append(", Error parsing this extension");
                }
            }
        }
        if (signature != null) {
            HexDumpEncoder encoder = new HexDumpEncoder();
            sb.append("\nSignature:\n" + encoder.encodeBuffer(signature)
                      + "\n");
        } else
            sb.append("NOT signed yet\n");
        return sb.toString();
    }
    public boolean isRevoked(Certificate cert) {
        if (revokedCerts.isEmpty() || (!(cert instanceof X509Certificate))) {
            return false;
        }
        X509Certificate xcert = (X509Certificate) cert;
        X509IssuerSerial issuerSerial = new X509IssuerSerial(xcert);
        return revokedCerts.containsKey(issuerSerial);
    }
    public int getVersion() {
        return version+1;
    }
    public Principal getIssuerDN() {
        return (Principal)issuer;
    }
    public X500Principal getIssuerX500Principal() {
        if (issuerPrincipal == null) {
            issuerPrincipal = issuer.asX500Principal();
        }
        return issuerPrincipal;
    }
    public Date getThisUpdate() {
        return (new Date(thisUpdate.getTime()));
    }
    public Date getNextUpdate() {
        if (nextUpdate == null)
            return null;
        return (new Date(nextUpdate.getTime()));
    }
    public X509CRLEntry getRevokedCertificate(BigInteger serialNumber) {
        if (revokedCerts.isEmpty()) {
            return null;
        }
        X509IssuerSerial issuerSerial = new X509IssuerSerial
            (getIssuerX500Principal(), serialNumber);
        return revokedCerts.get(issuerSerial);
    }
    public X509CRLEntry getRevokedCertificate(X509Certificate cert) {
        if (revokedCerts.isEmpty()) {
            return null;
        }
        X509IssuerSerial issuerSerial = new X509IssuerSerial(cert);
        return revokedCerts.get(issuerSerial);
    }
    public Set<X509CRLEntry> getRevokedCertificates() {
        if (revokedCerts.isEmpty()) {
            return null;
        } else {
            return new HashSet<X509CRLEntry>(revokedCerts.values());
        }
    }
    public byte[] getTBSCertList() throws CRLException {
        if (tbsCertList == null)
            throw new CRLException("Uninitialized CRL");
        byte[] dup = new byte[tbsCertList.length];
        System.arraycopy(tbsCertList, 0, dup, 0, dup.length);
        return dup;
    }
    public byte[] getSignature() {
        if (signature == null)
            return null;
        byte[] dup = new byte[signature.length];
        System.arraycopy(signature, 0, dup, 0, dup.length);
        return dup;
    }
    public String getSigAlgName() {
        if (sigAlgId == null)
            return null;
        return sigAlgId.getName();
    }
    public String getSigAlgOID() {
        if (sigAlgId == null)
            return null;
        ObjectIdentifier oid = sigAlgId.getOID();
        return oid.toString();
    }
    public byte[] getSigAlgParams() {
        if (sigAlgId == null)
            return null;
        try {
            return sigAlgId.getEncodedParams();
        } catch (IOException e) {
            return null;
        }
    }
    public AlgorithmId getSigAlgId() {
        return sigAlgId;
    }
    public KeyIdentifier getAuthKeyId() throws IOException {
        AuthorityKeyIdentifierExtension aki = getAuthKeyIdExtension();
        if (aki != null) {
            KeyIdentifier keyId = (KeyIdentifier)aki.get(aki.KEY_ID);
            return keyId;
        } else {
            return null;
        }
    }
    public AuthorityKeyIdentifierExtension getAuthKeyIdExtension()
        throws IOException {
        Object obj = getExtension(PKIXExtensions.AuthorityKey_Id);
        return (AuthorityKeyIdentifierExtension)obj;
    }
    public CRLNumberExtension getCRLNumberExtension() throws IOException {
        Object obj = getExtension(PKIXExtensions.CRLNumber_Id);
        return (CRLNumberExtension)obj;
    }
    public BigInteger getCRLNumber() throws IOException {
        CRLNumberExtension numExt = getCRLNumberExtension();
        if (numExt != null) {
            BigInteger num = (BigInteger)numExt.get(numExt.NUMBER);
            return num;
        } else {
            return null;
        }
    }
    public DeltaCRLIndicatorExtension getDeltaCRLIndicatorExtension()
        throws IOException {
        Object obj = getExtension(PKIXExtensions.DeltaCRLIndicator_Id);
        return (DeltaCRLIndicatorExtension)obj;
    }
    public BigInteger getBaseCRLNumber() throws IOException {
        DeltaCRLIndicatorExtension dciExt = getDeltaCRLIndicatorExtension();
        if (dciExt != null) {
            BigInteger num = (BigInteger)dciExt.get(dciExt.NUMBER);
            return num;
        } else {
            return null;
        }
    }
    public IssuerAlternativeNameExtension getIssuerAltNameExtension()
        throws IOException {
        Object obj = getExtension(PKIXExtensions.IssuerAlternativeName_Id);
        return (IssuerAlternativeNameExtension)obj;
    }
    public IssuingDistributionPointExtension
        getIssuingDistributionPointExtension() throws IOException {
        Object obj = getExtension(PKIXExtensions.IssuingDistributionPoint_Id);
        return (IssuingDistributionPointExtension) obj;
    }
    public boolean hasUnsupportedCriticalExtension() {
        if (extensions == null)
            return false;
        return extensions.hasUnsupportedCriticalExtension();
    }
    public Set<String> getCriticalExtensionOIDs() {
        if (extensions == null) {
            return null;
        }
        Set<String> extSet = new HashSet<String>();
        for (Extension ex : extensions.getAllExtensions()) {
            if (ex.isCritical()) {
                extSet.add(ex.getExtensionId().toString());
            }
        }
        return extSet;
    }
    public Set<String> getNonCriticalExtensionOIDs() {
        if (extensions == null) {
            return null;
        }
        Set<String> extSet = new HashSet<String>();
        for (Extension ex : extensions.getAllExtensions()) {
            if (!ex.isCritical()) {
                extSet.add(ex.getExtensionId().toString());
            }
        }
        return extSet;
    }
    public byte[] getExtensionValue(String oid) {
        if (extensions == null)
            return null;
        try {
            String extAlias = OIDMap.getName(new ObjectIdentifier(oid));
            Extension crlExt = null;
            if (extAlias == null) { 
                ObjectIdentifier findOID = new ObjectIdentifier(oid);
                Extension ex = null;
                ObjectIdentifier inCertOID;
                for (Enumeration<Extension> e = extensions.getElements();
                                                 e.hasMoreElements();) {
                    ex = e.nextElement();
                    inCertOID = ex.getExtensionId();
                    if (inCertOID.equals(findOID)) {
                        crlExt = ex;
                        break;
                    }
                }
            } else
                crlExt = extensions.get(extAlias);
            if (crlExt == null)
                return null;
            byte[] extData = crlExt.getExtensionValue();
            if (extData == null)
                return null;
            DerOutputStream out = new DerOutputStream();
            out.putOctetString(extData);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
    public Object getExtension(ObjectIdentifier oid) {
        if (extensions == null)
            return null;
        return extensions.get(OIDMap.getName(oid));
    }
    private void parse(DerValue val) throws CRLException, IOException {
        if (readOnly)
            throw new CRLException("cannot over-write existing CRL");
        if ( val.getData() == null || val.tag != DerValue.tag_Sequence)
            throw new CRLException("Invalid DER-encoded CRL data");
        signedCRL = val.toByteArray();
        DerValue seq[] = new DerValue[3];
        seq[0] = val.data.getDerValue();
        seq[1] = val.data.getDerValue();
        seq[2] = val.data.getDerValue();
        if (val.data.available() != 0)
            throw new CRLException("signed overrun, bytes = "
                                     + val.data.available());
        if (seq[0].tag != DerValue.tag_Sequence)
            throw new CRLException("signed CRL fields invalid");
        sigAlgId = AlgorithmId.parse(seq[1]);
        signature = seq[2].getBitString();
        if (seq[1].data.available() != 0)
            throw new CRLException("AlgorithmId field overrun");
        if (seq[2].data.available() != 0)
            throw new CRLException("Signature field overrun");
        tbsCertList = seq[0].toByteArray();
        DerInputStream derStrm = seq[0].data;
        DerValue       tmp;
        byte           nextByte;
        version = 0;   
        nextByte = (byte)derStrm.peekByte();
        if (nextByte == DerValue.tag_Integer) {
            version = derStrm.getInteger();
            if (version != 1)  
                throw new CRLException("Invalid version");
        }
        tmp = derStrm.getDerValue();
        AlgorithmId tmpId = AlgorithmId.parse(tmp);
        if (! tmpId.equals(sigAlgId))
            throw new CRLException("Signature algorithm mismatch");
        infoSigAlgId = tmpId;
        issuer = new X500Name(derStrm);
        if (issuer.isEmpty()) {
            throw new CRLException("Empty issuer DN not allowed in X509CRLs");
        }
        nextByte = (byte)derStrm.peekByte();
        if (nextByte == DerValue.tag_UtcTime) {
            thisUpdate = derStrm.getUTCTime();
        } else if (nextByte == DerValue.tag_GeneralizedTime) {
            thisUpdate = derStrm.getGeneralizedTime();
        } else {
            throw new CRLException("Invalid encoding for thisUpdate"
                                   + " (tag=" + nextByte + ")");
        }
        if (derStrm.available() == 0)
           return;     
        nextByte = (byte)derStrm.peekByte();
        if (nextByte == DerValue.tag_UtcTime) {
            nextUpdate = derStrm.getUTCTime();
        } else if (nextByte == DerValue.tag_GeneralizedTime) {
            nextUpdate = derStrm.getGeneralizedTime();
        } 
        if (derStrm.available() == 0)
            return;     
        nextByte = (byte)derStrm.peekByte();
        if ((nextByte == DerValue.tag_SequenceOf)
            && (! ((nextByte & 0x0c0) == 0x080))) {
            DerValue[] badCerts = derStrm.getSequence(4);
            X500Principal crlIssuer = getIssuerX500Principal();
            X500Principal badCertIssuer = crlIssuer;
            for (int i = 0; i < badCerts.length; i++) {
                X509CRLEntryImpl entry = new X509CRLEntryImpl(badCerts[i]);
                badCertIssuer = getCertIssuer(entry, badCertIssuer);
                entry.setCertificateIssuer(crlIssuer, badCertIssuer);
                X509IssuerSerial issuerSerial = new X509IssuerSerial
                    (badCertIssuer, entry.getSerialNumber());
                revokedCerts.put(issuerSerial, entry);
            }
        }
        if (derStrm.available() == 0)
            return;     
        tmp = derStrm.getDerValue();
        if (tmp.isConstructed() && tmp.isContextSpecific((byte)0)) {
            extensions = new CRLExtensions(tmp.data);
        }
        readOnly = true;
    }
    public static X500Principal getIssuerX500Principal(X509CRL crl) {
        try {
            byte[] encoded = crl.getEncoded();
            DerInputStream derIn = new DerInputStream(encoded);
            DerValue tbsCert = derIn.getSequence(3)[0];
            DerInputStream tbsIn = tbsCert.data;
            DerValue tmp;
            byte nextByte = (byte)tbsIn.peekByte();
            if (nextByte == DerValue.tag_Integer) {
                tmp = tbsIn.getDerValue();
            }
            tmp = tbsIn.getDerValue();  
            tmp = tbsIn.getDerValue();  
            byte[] principalBytes = tmp.toByteArray();
            return new X500Principal(principalBytes);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse issuer", e);
        }
    }
    public static byte[] getEncodedInternal(X509CRL crl) throws CRLException {
        if (crl instanceof X509CRLImpl) {
            return ((X509CRLImpl)crl).getEncodedInternal();
        } else {
            return crl.getEncoded();
        }
    }
    public static X509CRLImpl toImpl(X509CRL crl)
            throws CRLException {
        if (crl instanceof X509CRLImpl) {
            return (X509CRLImpl)crl;
        } else {
            return X509Factory.intern(crl);
        }
    }
    private X500Principal getCertIssuer(X509CRLEntryImpl entry,
        X500Principal prevCertIssuer) throws IOException {
        CertificateIssuerExtension ciExt =
            entry.getCertificateIssuerExtension();
        if (ciExt != null) {
            GeneralNames names = (GeneralNames)
                ciExt.get(CertificateIssuerExtension.ISSUER);
            X500Name issuerDN = (X500Name) names.get(0).getName();
            return issuerDN.asX500Principal();
        } else {
            return prevCertIssuer;
        }
    }
    @Override
    public void derEncode(OutputStream out) throws IOException {
        if (signedCRL == null)
            throw new IOException("Null CRL to encode");
        out.write(signedCRL.clone());
    }
    private final static class X509IssuerSerial {
        final X500Principal issuer;
        final BigInteger serial;
        volatile int hashcode = 0;
        X509IssuerSerial(X500Principal issuer, BigInteger serial) {
            this.issuer = issuer;
            this.serial = serial;
        }
        X509IssuerSerial(X509Certificate cert) {
            this(cert.getIssuerX500Principal(), cert.getSerialNumber());
        }
        X500Principal getIssuer() {
            return issuer;
        }
        BigInteger getSerial() {
            return serial;
        }
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof X509IssuerSerial)) {
                return false;
            }
            X509IssuerSerial other = (X509IssuerSerial) o;
            if (serial.equals(other.getSerial()) &&
                issuer.equals(other.getIssuer())) {
                return true;
            }
            return false;
        }
        public int hashCode() {
            if (hashcode == 0) {
                int result = 17;
                result = 37*result + issuer.hashCode();
                result = 37*result + serial.hashCode();
                hashcode = result;
            }
            return hashcode;
        }
    }
}

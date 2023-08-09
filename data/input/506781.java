public class X509CRLImpl extends X509CRL {
    private final CertificateList crl;
    private final TBSCertList tbsCertList;
    private byte[] tbsCertListEncoding;
    private final Extensions extensions;
    private X500Principal issuer;
    private ArrayList entries;
    private int entriesSize;
    private byte[] signature;
    private String sigAlgOID;
    private String sigAlgName;
    private byte[] sigAlgParams;
    private byte[] encoding;
    private boolean nullSigAlgParams;
    private boolean entriesRetrieved;
    private boolean isIndirectCRL;
    private int nonIndirectEntriesSize;
    public X509CRLImpl(CertificateList crl) {
        this.crl = crl;
        this.tbsCertList = crl.getTbsCertList();
        this.extensions = tbsCertList.getCrlExtensions();
    }
    public X509CRLImpl(InputStream in) throws CRLException {
        try {
            this.crl = (CertificateList) CertificateList.ASN1.decode(in);
            this.tbsCertList = crl.getTbsCertList();
            this.extensions = tbsCertList.getCrlExtensions();
        } catch (IOException e) {
            throw new CRLException(e);
        }
    }
    public X509CRLImpl(byte[] encoding) throws IOException {
        this((CertificateList) CertificateList.ASN1.decode(encoding));
    }
    public byte[] getEncoded() throws CRLException {
        if (encoding == null) {
            encoding = crl.getEncoded();
        }
        byte[] result = new byte[encoding.length];
        System.arraycopy(encoding, 0, result, 0, encoding.length);
        return result;
    }
    public int getVersion() {
        return tbsCertList.getVersion();
    }
    public Principal getIssuerDN() {
        if (issuer == null) {
            issuer = tbsCertList.getIssuer().getX500Principal();
        }
        return issuer;
    }
    public X500Principal getIssuerX500Principal() {
        if (issuer == null) {
            issuer = tbsCertList.getIssuer().getX500Principal();
        }
        return issuer;
    }
    public Date getThisUpdate() {
        return tbsCertList.getThisUpdate();
    }
    public Date getNextUpdate() {
        return tbsCertList.getNextUpdate();
    }
    private void retrieveEntries() {
        entriesRetrieved = true;
        List rcerts = tbsCertList.getRevokedCertificates();
        if (rcerts == null) {
            return;
        }
        entriesSize = rcerts.size();
        entries = new ArrayList(entriesSize);
        X500Principal rcertIssuer = null;
        for (int i=0; i<entriesSize; i++) {
            TBSCertList.RevokedCertificate rcert =
                (TBSCertList.RevokedCertificate) rcerts.get(i);
            X500Principal iss = rcert.getIssuer();
            if (iss != null) {
                rcertIssuer = iss;
                isIndirectCRL = true;
                nonIndirectEntriesSize = i;
            }
            entries.add(new X509CRLEntryImpl(rcert, rcertIssuer));
        }
    }
    public X509CRLEntry getRevokedCertificate(X509Certificate certificate) {
        if (certificate == null) {
            throw new NullPointerException();
        }
        if (!entriesRetrieved) {
            retrieveEntries();
        }
        if (entries == null) {
            return null;
        }
        BigInteger serialN = certificate.getSerialNumber();
        if (isIndirectCRL) {
            X500Principal certIssuer = certificate.getIssuerX500Principal();
            if (certIssuer.equals(getIssuerX500Principal())) {
                certIssuer = null;
            }
            for (int i=0; i<entriesSize; i++) {
                X509CRLEntry entry = (X509CRLEntry) entries.get(i);
                if (serialN.equals(entry.getSerialNumber())) {
                    X500Principal iss = entry.getCertificateIssuer();
                    if (certIssuer != null) {
                        if (certIssuer.equals(iss)) {
                            return entry;
                        }
                    } else if (iss == null) {
                        return entry;
                    }
                }
            }
        } else {
            for (int i=0; i<entriesSize; i++) {
                X509CRLEntry entry = (X509CRLEntry) entries.get(i);
                if (serialN.equals(entry.getSerialNumber())) {
                    return entry;
                }
            }
        }
        return null;
    }
    public X509CRLEntry getRevokedCertificate(BigInteger serialNumber) {
        if (!entriesRetrieved) {
            retrieveEntries();
        }
        if (entries == null) {
            return null;
        }
        for (int i=0; i<nonIndirectEntriesSize; i++) {
            X509CRLEntry entry = (X509CRLEntry) entries.get(i);
            if (serialNumber.equals(entry.getSerialNumber())) {
                return entry;
            }
        }
        return null;
    }
    public Set<? extends X509CRLEntry> getRevokedCertificates() {
        if (!entriesRetrieved) {
            retrieveEntries();
        }
        if (entries == null) {
            return null;
        }
        return new HashSet(entries);
    }
    public byte[] getTBSCertList() throws CRLException {
        if (tbsCertListEncoding == null) {
            tbsCertListEncoding = tbsCertList.getEncoded();
        }
        byte[] result = new byte[tbsCertListEncoding.length];
        System.arraycopy(tbsCertListEncoding, 0,
                result, 0, tbsCertListEncoding.length);
        return result;
    }
    public byte[] getSignature() {
        if (signature == null) {
            signature = crl.getSignatureValue();
        }
        byte[] result = new byte[signature.length];
        System.arraycopy(signature, 0, result, 0, signature.length);
        return result;
    }
    public String getSigAlgName() {
        if (sigAlgOID == null) {
            sigAlgOID = tbsCertList.getSignature().getAlgorithm();
            sigAlgName = AlgNameMapper.map2AlgName(sigAlgOID);
            if (sigAlgName == null) {
                sigAlgName = sigAlgOID;
            }
        }
        return sigAlgName;
    }
    public String getSigAlgOID() {
        if (sigAlgOID == null) {
            sigAlgOID = tbsCertList.getSignature().getAlgorithm();
            sigAlgName = AlgNameMapper.map2AlgName(sigAlgOID);
            if (sigAlgName == null) {
                sigAlgName = sigAlgOID;
            }
        }
        return sigAlgOID;
    }
    public byte[] getSigAlgParams() {
        if (nullSigAlgParams) {
            return null;
        }
        if (sigAlgParams == null) {
            sigAlgParams = tbsCertList.getSignature().getParameters();
            if (sigAlgParams == null) {
                nullSigAlgParams = true;
                return null;
            }
        }
        return sigAlgParams;
    }
    public void verify(PublicKey key)
                     throws CRLException, NoSuchAlgorithmException,
                            InvalidKeyException, NoSuchProviderException,
                            SignatureException {
        Signature signature = Signature.getInstance(getSigAlgName());
        signature.initVerify(key);
        byte[] tbsEncoding = tbsCertList.getEncoded();
        signature.update(tbsEncoding, 0, tbsEncoding.length);
        if (!signature.verify(crl.getSignatureValue())) {
            throw new SignatureException(Messages.getString("security.15C")); 
        }
    }
    public void verify(PublicKey key, String sigProvider)
                     throws CRLException, NoSuchAlgorithmException,
                            InvalidKeyException, NoSuchProviderException,
                            SignatureException {
        Signature signature = Signature.getInstance(
                                            getSigAlgName(), sigProvider);
        signature.initVerify(key);
        byte[] tbsEncoding = tbsCertList.getEncoded();
        signature.update(tbsEncoding, 0, tbsEncoding.length);
        if (!signature.verify(crl.getSignatureValue())) {
            throw new SignatureException(Messages.getString("security.15C")); 
        }
    }
    public boolean isRevoked(Certificate cert) {
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        return getRevokedCertificate((X509Certificate) cert) != null;
    }
    public String toString() {
        return crl.toString();
    }
    public Set getNonCriticalExtensionOIDs() {
        if (extensions == null) {
            return null;
        }
        return extensions.getNonCriticalExtensions();
    }
    public Set getCriticalExtensionOIDs() {
        if (extensions == null) {
            return null;
        }
        return extensions.getCriticalExtensions();
    }
    public byte[] getExtensionValue(String oid) {
        if (extensions == null) {
            return null;
        }
        Extension ext = extensions.getExtensionByOID(oid);
        return (ext == null) ? null : ext.getRawExtnValue();
    }
    public boolean hasUnsupportedCriticalExtension() {
        if (extensions == null) {
            return false;
        }
        return extensions.hasUnsupportedCritical();
    }
}

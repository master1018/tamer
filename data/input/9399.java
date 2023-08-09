public class X509CRLEntryImpl extends X509CRLEntry {
    private SerialNumber serialNumber = null;
    private Date revocationDate = null;
    private CRLExtensions extensions = null;
    private byte[] revokedCert = null;
    private X500Principal certIssuer;
    private final static boolean isExplicit = false;
    private static final long YR_2050 = 2524636800000L;
    public X509CRLEntryImpl(BigInteger num, Date date) {
        this.serialNumber = new SerialNumber(num);
        this.revocationDate = date;
    }
    public X509CRLEntryImpl(BigInteger num, Date date,
                           CRLExtensions crlEntryExts) {
        this.serialNumber = new SerialNumber(num);
        this.revocationDate = date;
        this.extensions = crlEntryExts;
    }
    public X509CRLEntryImpl(byte[] revokedCert) throws CRLException {
        try {
            parse(new DerValue(revokedCert));
        } catch (IOException e) {
            this.revokedCert = null;
            throw new CRLException("Parsing error: " + e.toString());
        }
    }
    public X509CRLEntryImpl(DerValue derValue) throws CRLException {
        try {
            parse(derValue);
        } catch (IOException e) {
            revokedCert = null;
            throw new CRLException("Parsing error: " + e.toString());
        }
    }
    public boolean hasExtensions() {
        return (extensions != null);
    }
    public void encode(DerOutputStream outStrm) throws CRLException {
        try {
            if (revokedCert == null) {
                DerOutputStream tmp = new DerOutputStream();
                serialNumber.encode(tmp);
                if (revocationDate.getTime() < YR_2050) {
                    tmp.putUTCTime(revocationDate);
                } else {
                    tmp.putGeneralizedTime(revocationDate);
                }
                if (extensions != null)
                    extensions.encode(tmp, isExplicit);
                DerOutputStream seq = new DerOutputStream();
                seq.write(DerValue.tag_Sequence, tmp);
                revokedCert = seq.toByteArray();
            }
            outStrm.write(revokedCert);
        } catch (IOException e) {
             throw new CRLException("Encoding error: " + e.toString());
        }
    }
    public byte[] getEncoded() throws CRLException {
        if (revokedCert == null)
            this.encode(new DerOutputStream());
        return revokedCert.clone();
    }
    @Override
    public X500Principal getCertificateIssuer() {
        return certIssuer;
    }
    void setCertificateIssuer(X500Principal crlIssuer, X500Principal certIssuer) {
        if (crlIssuer.equals(certIssuer)) {
            this.certIssuer = null;
        } else {
            this.certIssuer = certIssuer;
        }
    }
    public BigInteger getSerialNumber() {
        return serialNumber.getNumber();
    }
    public Date getRevocationDate() {
        return new Date(revocationDate.getTime());
    }
    @Override
    public CRLReason getRevocationReason() {
        Extension ext = getExtension(PKIXExtensions.ReasonCode_Id);
        if (ext == null) {
            return null;
        }
        CRLReasonCodeExtension rcExt = (CRLReasonCodeExtension) ext;
        return rcExt.getReasonCode();
    }
    public static CRLReason getRevocationReason(X509CRLEntry crlEntry) {
        try {
            byte[] ext = crlEntry.getExtensionValue("2.5.29.21");
            if (ext == null) {
                return null;
            }
            DerValue val = new DerValue(ext);
            byte[] data = val.getOctetString();
            CRLReasonCodeExtension rcExt =
                new CRLReasonCodeExtension(Boolean.FALSE, data);
            return rcExt.getReasonCode();
        } catch (IOException ioe) {
            return null;
        }
    }
    public Integer getReasonCode() throws IOException {
        Object obj = getExtension(PKIXExtensions.ReasonCode_Id);
        if (obj == null)
            return null;
        CRLReasonCodeExtension reasonCode = (CRLReasonCodeExtension)obj;
        return (Integer)(reasonCode.get(reasonCode.REASON));
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(serialNumber.toString());
        sb.append("  On: " + revocationDate.toString());
        if (certIssuer != null) {
            sb.append("\n    Certificate issuer: " + certIssuer);
        }
        if (extensions != null) {
            Collection allEntryExts = extensions.getAllExtensions();
            Object[] objs = allEntryExts.toArray();
            sb.append("\n    CRL Entry Extensions: " + objs.length);
            for (int i = 0; i < objs.length; i++) {
                sb.append("\n    [" + (i+1) + "]: ");
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
        sb.append("\n");
        return sb.toString();
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
    public Extension getExtension(ObjectIdentifier oid) {
        if (extensions == null)
            return null;
        return extensions.get(OIDMap.getName(oid));
    }
    private void parse(DerValue derVal)
    throws CRLException, IOException {
        if (derVal.tag != DerValue.tag_Sequence) {
            throw new CRLException("Invalid encoded RevokedCertificate, " +
                                  "starting sequence tag missing.");
        }
        if (derVal.data.available() == 0)
            throw new CRLException("No data encoded for RevokedCertificates");
        revokedCert = derVal.toByteArray();
        DerInputStream in = derVal.toDerInputStream();
        DerValue val = in.getDerValue();
        this.serialNumber = new SerialNumber(val);
        int nextByte = derVal.data.peekByte();
        if ((byte)nextByte == DerValue.tag_UtcTime) {
            this.revocationDate = derVal.data.getUTCTime();
        } else if ((byte)nextByte == DerValue.tag_GeneralizedTime) {
            this.revocationDate = derVal.data.getGeneralizedTime();
        } else
            throw new CRLException("Invalid encoding for revocation date");
        if (derVal.data.available() == 0)
            return;  
        this.extensions = new CRLExtensions(derVal.toDerInputStream());
    }
    public static X509CRLEntryImpl toImpl(X509CRLEntry entry)
            throws CRLException {
        if (entry instanceof X509CRLEntryImpl) {
            return (X509CRLEntryImpl)entry;
        } else {
            return new X509CRLEntryImpl(entry.getEncoded());
        }
    }
    CertificateIssuerExtension getCertificateIssuerExtension() {
        return (CertificateIssuerExtension)
            getExtension(PKIXExtensions.CertificateIssuer_Id);
    }
    public Map<String, java.security.cert.Extension> getExtensions() {
        Collection<Extension> exts = extensions.getAllExtensions();
        HashMap<String, java.security.cert.Extension> map =
            new HashMap<String, java.security.cert.Extension>(exts.size());
        for (Extension ext : exts) {
            map.put(ext.getId(), ext);
        }
        return map;
    }
}

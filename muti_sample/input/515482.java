public class X509CRLEntryImpl extends X509CRLEntry {
    private final TBSCertList.RevokedCertificate rcert;
    private final Extensions extensions;
    private final X500Principal issuer;
    private byte[] encoding;
    public X509CRLEntryImpl(TBSCertList.RevokedCertificate rcert,
            X500Principal issuer) {
        this.rcert = rcert;
        this.extensions = rcert.getCrlEntryExtensions();
        this.issuer = issuer;
    }
    public byte[] getEncoded() throws CRLException {
        if (encoding == null) {
            encoding = rcert.getEncoded();
        }
        byte[] result = new byte[encoding.length];
        System.arraycopy(encoding, 0, result, 0, encoding.length);
        return result;
    }
    public BigInteger getSerialNumber() {
        return rcert.getUserCertificate();
    }
    public X500Principal getCertificateIssuer() {
        return issuer;
    }
    public Date getRevocationDate() {
        return rcert.getRevocationDate();
    }
    public boolean hasExtensions() {
        return (extensions != null) && (extensions.size() != 0);
    }
    public String toString() {
        return "X509CRLEntryImpl: "+rcert.toString(); 
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

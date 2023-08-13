public abstract class X509CRLEntry implements X509Extension {
    public X509CRLEntry() {}
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof X509CRLEntry)) {
            return false;
        }
        X509CRLEntry obj = (X509CRLEntry) other;
        try {
            return Arrays.equals(getEncoded(), obj.getEncoded());
        } catch (CRLException e) {
            return false;
        }
    }
    public int hashCode() {
        int res = 0;
        try {
            byte[] array = getEncoded();
            for (int i=0; i<array.length; i++) {
                res += array[i] & 0xFF;
            }
        } catch (CRLException e) {
        }
        return res;
    }
    public abstract byte[] getEncoded() throws CRLException;
    public abstract BigInteger getSerialNumber();
    public X500Principal getCertificateIssuer() {
        return null;
    }
    public abstract Date getRevocationDate();
    public abstract boolean hasExtensions();
    public abstract String toString();
}

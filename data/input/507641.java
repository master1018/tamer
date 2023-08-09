public abstract class Certificate {
    public Certificate() {}
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Certificate)) {
            return false;
        }
        Certificate object = (Certificate) obj;
        try {
            return Arrays.equals(getEncoded(), object.getEncoded());
        } catch (CertificateEncodingException e) {
            return false;
        }
    }
    public int hashCode() {
        int res = 0;
        try {
            byte[] array = getEncoded();
            for (int i=0; i<array.length; i++) {
                res += array[i];
            }
        } catch (CertificateEncodingException e) {
        }
        return res;
    }
    public abstract byte[] getEncoded()
            throws CertificateEncodingException;
    public abstract void verify(PublicKey key)
            throws CertificateException, NoSuchAlgorithmException,
                   InvalidKeyException, NoSuchProviderException,
                   SignatureException;
    public abstract void verify(PublicKey key, String sigProvider)
            throws CertificateException, NoSuchAlgorithmException, 
                   InvalidKeyException, NoSuchProviderException,
                   SignatureException;
    public abstract String toString();
    public abstract PublicKey getPublicKey();
}

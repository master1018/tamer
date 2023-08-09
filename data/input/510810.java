public class CertificateStub implements Certificate {
    String format;
    Principal guarantor;
    Principal principal;
    PublicKey key;
    public CertificateStub(String format, Principal guarantor, Principal principal, PublicKey key){
        this.format = format;
        this.guarantor = guarantor;
        this.principal = principal;
        this.key = key;
    }
    public void decode(InputStream stream) throws KeyException, 
            IOException {
    }
    public void encode(OutputStream stream) throws KeyException,
            IOException {
    }
    public String getFormat() {
        return format;
    }
    public Principal getGuarantor() {
        return guarantor;
    }
    public Principal getPrincipal() {
        return principal;
    }
    public PublicKey getPublicKey() {
        return key;
    }
    public String toString(boolean detailed) {
        return null;
    }
}

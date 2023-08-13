public class MyCertStoreParameters implements CertStoreParameters {
    public MyCertStoreParameters() {
        super();
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
}

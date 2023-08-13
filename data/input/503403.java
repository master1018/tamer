public class ORAddress {
    private byte[] encoding;
    public ORAddress() {}
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            new ASN1Sequence(new ASN1Type[] {}) {
                protected Object getDecodedObject(Object[] values) {
                    return null;
                }
                protected void getValues(Object object, Object[] values) {
                }
            }}) {
        protected Object getDecodedObject(BerInputStream in) {
            return new ORAddress();
        }
        private final Object foo = new Object();  
        protected void getValues(Object object, Object[] values) {
            values[0] = foo;
        }
    };
}

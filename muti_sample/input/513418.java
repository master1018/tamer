class ThreeIntegerSequence {
    byte[] p, q, g;
    private byte[] encoding;
    ThreeIntegerSequence(byte[] p, byte[] q, byte[] g) {
        this.p = p;
        this.q = q;
        this.g = g;
        encoding = null;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Integer.getInstance(), ASN1Integer.getInstance(),
            ASN1Integer.getInstance() }) {
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new ThreeIntegerSequence((byte[]) values[0],
                    (byte[]) values[1], (byte[]) values[2]);
        }
        protected void getValues(Object object, Object[] values) {
            ThreeIntegerSequence mySeq = (ThreeIntegerSequence) object;
            values[0] = mySeq.p;
            values[1] = mySeq.q;
            values[2] = mySeq.g;
        }
    };
}

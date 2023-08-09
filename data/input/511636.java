public class MessageImprint {
    private final AlgorithmIdentifier algId;
    private final byte [] hashedMessage;
    public MessageImprint(AlgorithmIdentifier algId, byte [] hashedMessage){
        this.algId = algId;
        this.hashedMessage = hashedMessage;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
        AlgorithmIdentifier.ASN1,
        ASN1OctetString.getInstance()}) {
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new MessageImprint(
                    (AlgorithmIdentifier)values[0],
                    (byte[])values[1]);
        }
        protected void getValues(Object object, Object[] values) {
            MessageImprint mi = (MessageImprint) object;
            values[0] = mi.algId;
            values[1] = mi.hashedMessage;
        }
    };
}

public class CRLNumber extends ExtensionValue {
    private final BigInteger number;
    public CRLNumber(BigInteger number) {
        this.number = number;
    }
    public CRLNumber(byte[] encoding) throws IOException {
        super(encoding);
        number = new BigInteger((byte[]) ASN1.decode(encoding));
    }
    public BigInteger getNumber() {
        return number;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(number.toByteArray());
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("CRL Number: [ ").append(number).append( 
                " ]\n"); 
    }
    public static final ASN1Type ASN1 = ASN1Integer.getInstance();
}

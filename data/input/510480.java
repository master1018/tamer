public class ReasonCode extends ExtensionValue {
    public static final byte UNSPECIFIED = 0;
    public static final byte KEY_COMPROMISE = 1;
    public static final byte CA_COMPROMISE = 2;
    public static final byte AFFILIATION_CHANGED = 3;
    public static final byte SUPERSEDED = 4;
    public static final byte CESSATION_OF_OPERATION = 5;
    public static final byte CERTIFICATE_HOLD = 6;
    public static final byte REMOVE_FROM_CRL = 8;
    public static final byte PRIVILEGE_WITHDRAWN = 9;
    public static final byte AA_COMPROMISE = 10;
    private final byte code;
    public ReasonCode(byte code) {
        this.code = code;
    }
    public ReasonCode(byte[] encoding) throws IOException {
        super(encoding);
        this.code = ((byte[]) ASN1.decode(encoding))[0];
    }
    public int getCode() {
        return code;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(new byte[] {(byte) code});
        }
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Reason Code: [ "); 
        switch (code) {
            case UNSPECIFIED:
                buffer.append("unspecified"); 
                break;
            case KEY_COMPROMISE:
                buffer.append("keyCompromise"); 
                break;
            case CA_COMPROMISE:
                buffer.append("cACompromise"); 
                break;
            case AFFILIATION_CHANGED:
                buffer.append("affiliationChanged"); 
                break;
            case SUPERSEDED:
                buffer.append("superseded"); 
                break;
            case CESSATION_OF_OPERATION:
                buffer.append("cessationOfOperation"); 
                break;
            case CERTIFICATE_HOLD:
                buffer.append("certificateHold"); 
                break;
            case REMOVE_FROM_CRL:
                buffer.append("removeFromCRL"); 
                break;
            case PRIVILEGE_WITHDRAWN:
                buffer.append("privilegeWithdrawn"); 
                break;
            case AA_COMPROMISE:
                buffer.append("aACompromise"); 
                break;
        }
        buffer.append(" ]\n"); 
    }
    public static final ASN1Type ASN1 = ASN1Enumerated.getInstance();
}

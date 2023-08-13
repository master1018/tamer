public class AlertProtocol {
    protected static final byte WARNING = 1;
    protected static final byte FATAL = 2;
    protected static final byte CLOSE_NOTIFY = 0;
    protected static final byte UNEXPECTED_MESSAGE = 10;
    protected static final byte BAD_RECORD_MAC = 20;
    protected static final byte DECRYPTION_FAILED = 21;
    protected static final byte RECORD_OVERFLOW = 22;
    protected static final byte DECOMPRESSION_FAILURE = 30;
    protected static final byte HANDSHAKE_FAILURE = 40;
    protected static final byte BAD_CERTIFICATE = 42;
    protected static final byte UNSUPPORTED_CERTIFICATE = 43;
    protected static final byte CERTIFICATE_REVOKED = 44;
    protected static final byte CERTIFICATE_EXPIRED = 45;
    protected static final byte CERTIFICATE_UNKNOWN = 46;
    protected static final byte ILLEGAL_PARAMETER = 47;
    protected static final byte UNKNOWN_CA = 48;
    protected static final byte ACCESS_DENIED = 49;
    protected static final byte DECODE_ERROR = 50;
    protected static final byte DECRYPT_ERROR = 51;
    protected static final byte EXPORT_RESTRICTION = 60;
    protected static final byte PROTOCOL_VERSION = 70;
    protected static final byte INSUFFICIENT_SECURITY = 71;
    protected static final byte INTERNAL_ERROR = 80;
    protected static final byte USER_CANCELED = 90;
    protected static final byte NO_RENEGOTIATION = 100;
    private final byte[] alert = new byte[2];
    private SSLRecordProtocol recordProtocol;
    private Logger.Stream logger = Logger.getStream("alert");
    protected AlertProtocol() {}
    protected void setRecordProtocol(SSLRecordProtocol recordProtocol) {
        this.recordProtocol = recordProtocol;
    }
    protected void alert(byte level, byte description) {
        if (logger != null) {
            logger.println("Alert.alert: "+level+" "+description);
        }
        this.alert[0] = level;
        this.alert[1] = description;
    }
    protected byte getDescriptionCode() {
        return (alert[0] != 0) ? alert[1] : -100;
    }
    protected void setProcessed() {
        if (logger != null) {
            logger.println("Alert.setProcessed");
        }
        this.alert[0] = 0;
    }
    protected boolean hasAlert() {
        return (alert[0] != 0);
    }
    protected boolean isFatalAlert() {
        return (alert[0] == 2);
    }
    protected String getAlertDescription() {
        switch (alert[1]) {
        case CLOSE_NOTIFY:
            return "close_notify";
        case UNEXPECTED_MESSAGE:
            return "unexpected_message";
        case BAD_RECORD_MAC:
            return "bad_record_mac";
        case DECRYPTION_FAILED:
            return "decryption_failed";
        case RECORD_OVERFLOW:
            return "record_overflow";
        case DECOMPRESSION_FAILURE:
            return "decompression_failure";
        case HANDSHAKE_FAILURE:
            return "handshake_failure";
        case BAD_CERTIFICATE:
            return "bad_certificate";
        case UNSUPPORTED_CERTIFICATE:
            return "unsupported_certificate";
        case CERTIFICATE_REVOKED:
            return "certificate_revoked";
        case CERTIFICATE_EXPIRED:
            return "certificate_expired";
        case CERTIFICATE_UNKNOWN:
            return "certificate_unknown";
        case ILLEGAL_PARAMETER:
            return "illegal_parameter";
        case UNKNOWN_CA:
            return "unknown_ca";
        case ACCESS_DENIED:
            return "access_denied";
        case DECODE_ERROR:
            return "decode_error";
        case DECRYPT_ERROR:
            return "decrypt_error";
        case EXPORT_RESTRICTION:
            return "export_restriction";
        case PROTOCOL_VERSION:
            return "protocol_version";
        case INSUFFICIENT_SECURITY:
            return "insufficient_security";
        case INTERNAL_ERROR:
            return "internal_error";
        case USER_CANCELED:
            return "user_canceled";
        case NO_RENEGOTIATION:
            return "no_renegotiation";
        }
        return null;
    }
    protected byte[] wrap() {
        byte[] res = recordProtocol.wrap(ContentType.ALERT, alert, 0, 2);
        return res;
    }
    protected void shutdown() {
        alert[0] = 0;
        alert[1] = 0;
        recordProtocol = null;
    }
}

public class TSResponse {
    public static final int GRANTED = 0;
    public static final int GRANTED_WITH_MODS = 1;
    public static final int REJECTION = 2;
    public static final int WAITING = 3;
    public static final int REVOCATION_WARNING = 4;
    public static final int REVOCATION_NOTIFICATION = 5;
    public static final int BAD_ALG = 0;
    public static final int BAD_REQUEST = 2;
    public static final int BAD_DATA_FORMAT = 5;
    public static final int TIME_NOT_AVAILABLE = 14;
    public static final int UNACCEPTED_POLICY = 15;
    public static final int UNACCEPTED_EXTENSION = 16;
    public static final int ADD_INFO_NOT_AVAILABLE = 17;
    public static final int SYSTEM_FAILURE = 25;
    private static final boolean DEBUG = false;
    private int status;
    private String[] statusString = null;
    private int failureInfo = -1;
    private byte[] encodedTsToken = null;
    private PKCS7 tsToken = null;
    TSResponse(byte[] tsReply) throws IOException {
        parse(tsReply);
    }
    public int getStatusCode() {
        return status;
    }
    public String[] getStatusMessages() {
        return statusString;
    }
    public int getFailureCode() {
        return failureInfo;
    }
    public String getStatusCodeAsText() {
        switch (status)  {
        case GRANTED:
            return "the timestamp request was granted.";
        case GRANTED_WITH_MODS:
            return
                "the timestamp request was granted with some modifications.";
        case REJECTION:
            return "the timestamp request was rejected.";
        case WAITING:
            return "the timestamp request has not yet been processed.";
        case REVOCATION_WARNING:
            return "warning: a certificate revocation is imminent.";
        case REVOCATION_NOTIFICATION:
            return "notification: a certificate revocation has occurred.";
        default:
            return ("unknown status code " + status + ".");
        }
    }
    public String getFailureCodeAsText() {
        if (failureInfo == -1) {
            return null;
        }
        switch (failureInfo)  {
        case BAD_ALG:
            return "Unrecognized or unsupported alrorithm identifier.";
        case BAD_REQUEST:
            return "The requested transaction is not permitted or supported.";
        case BAD_DATA_FORMAT:
            return "The data submitted has the wrong format.";
        case TIME_NOT_AVAILABLE:
            return "The TSA's time source is not available.";
        case UNACCEPTED_POLICY:
            return "The requested TSA policy is not supported by the TSA.";
        case UNACCEPTED_EXTENSION:
            return "The requested extension is not supported by the TSA.";
        case ADD_INFO_NOT_AVAILABLE:
            return "The additional information requested could not be " +
                "understood or is not available.";
        case SYSTEM_FAILURE:
            return "The request cannot be handled due to system failure.";
        default:
            return ("unknown status code " + status);
        }
    }
    public PKCS7 getToken() {
        return tsToken;
    }
    public byte[] getEncodedToken() {
        return encodedTsToken;
    }
    private void parse(byte[] tsReply) throws IOException {
        DerValue derValue = new DerValue(tsReply);
        if (derValue.tag != DerValue.tag_Sequence) {
            throw new IOException("Bad encoding for timestamp response");
        }
        DerValue status = derValue.data.getDerValue();
        this.status = status.data.getInteger();
        if (DEBUG) {
            System.out.println("timestamp response: status=" + this.status);
        }
        if (status.data.available() > 0) {
            DerValue[] strings = status.data.getSequence(1);
            statusString = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                statusString[i] = strings[i].data.getUTF8String();
            }
        }
        if (status.data.available() > 0) {
            byte[] failInfo = status.data.getBitString();
            int failureInfo = (new Byte(failInfo[0])).intValue();
            if (failureInfo < 0 || failureInfo > 25 || failInfo.length != 1) {
                throw new IOException("Bad encoding for timestamp response: " +
                    "unrecognized value for the failInfo element");
            }
            this.failureInfo = failureInfo;
        }
        if (derValue.data.available() > 0) {
            DerValue timestampToken = derValue.data.getDerValue();
            encodedTsToken = timestampToken.toByteArray();
            tsToken = new PKCS7(encodedTsToken);
        }
        if (this.status == 0 || this.status == 1) {
            if (tsToken == null) {
                throw new TimestampException(
                    "Bad encoding for timestamp response: " +
                    "expected a timeStampToken element to be present");
            }
        } else if (tsToken != null) {
            throw new TimestampException(
                "Bad encoding for timestamp response: " +
                "expected no timeStampToken element to be present");
        }
    }
final static class TimestampException extends IOException {
    TimestampException(String message) {
        super(message);
    }
}
}

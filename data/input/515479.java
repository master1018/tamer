public class ImpsConstants {
    public static enum ImpsVersion {
        IMPS_VERSION_11,
        IMPS_VERSION_12,
        IMPS_VERSION_13;
        public static ImpsVersion fromString(String value) {
            if ("1.1".equals(value)) {
                return IMPS_VERSION_11;
            } else if ("1.2".equals(value)) {
                return IMPS_VERSION_12;
            } else if ("1.3".equals(value)) {
                return IMPS_VERSION_13;
            } else {
                return IMPS_VERSION_12;
            }
        }
    }
    public static final String CLIENT_PRODUCER = "MOKIA";
    public static final String CLIENT_VERSION = "0.1";
    public static final String VERSION_11_NS
        = "http:
    public static final String TRANSACTION_11_NS
        = "http:
    public static final String PRESENCE_11_NS
        = "http:
    public static final String VERSION_12_NS
        = "http:
    public static final String TRANSACTION_12_NS
        = "http:
    public static final String PRESENCE_12_NS
        = "http:
    public static final String VERSION_13_NS
        = "http:
    public static final String TRANSACTION_13_NS
        = "http:
    public static final String PRESENCE_13_NS
        = "http:
    public static final String ADDRESS_PREFIX = "wv:";
    public static final String TRUE = "T";
    public static final String FALSE = "F";
    public static final String SUCCESS_CODE = "200";
    public static final String Open = "Open";
    public static final String DisplayName = "DisplayName";
    public static final String GROUP_INVITATION = "GR";
    public static final String Default = "Default";
    public static final int STATUS_UNAUTHORIZED  = 401;
    public static final int STATUS_NOT_IMPLEMENTED = 501;
    public static final int STATUS_COULD_NOT_RECOVER_SESSION = 502;
    public static final int STATUS_AUTO_SUBSCRIPTION_NOT_SUPPORTED = 760;
    public static final String PRESENCE_AVAILABLE = "AVAILABLE";
    public static final String PRESENCE_NOT_AVAILABLE = "NOT_AVAILABLE";
    public static final String PRESENCE_DISCREET = "DISCREET";
    public static final String PRESENCE_MOBILE_PHONE = "MOBILE_PHONE";
    public static final String PRESENCE_COMPUTER     = "COMPUTER";
    public static final String PRESENCE_PDA          = "PDA";
    public static final String PRESENCE_CLI          = "CLI";
    public static final String PRESENCE_OTHER        = "OTHER";
    public static final String COMMC_CAP_IM      = "IM";
}

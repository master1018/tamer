public class SuppServiceNotification {
    public int notificationType;
    public int code;
    public int index;
    public int type;
    public String number;
    static public final int MO_CODE_UNCONDITIONAL_CF_ACTIVE     = 0;
    static public final int MO_CODE_SOME_CF_ACTIVE              = 1;
    static public final int MO_CODE_CALL_FORWARDED              = 2;
    static public final int MO_CODE_CALL_IS_WAITING             = 3;
    static public final int MO_CODE_CUG_CALL                    = 4;
    static public final int MO_CODE_OUTGOING_CALLS_BARRED       = 5;
    static public final int MO_CODE_INCOMING_CALLS_BARRED       = 6;
    static public final int MO_CODE_CLIR_SUPPRESSION_REJECTED   = 7;
    static public final int MO_CODE_CALL_DEFLECTED              = 8;
    static public final int MT_CODE_FORWARDED_CALL              = 0;
    static public final int MT_CODE_CUG_CALL                    = 1;
    static public final int MT_CODE_CALL_ON_HOLD                = 2;
    static public final int MT_CODE_CALL_RETRIEVED              = 3;
    static public final int MT_CODE_MULTI_PARTY_CALL            = 4;
    static public final int MT_CODE_ON_HOLD_CALL_RELEASED       = 5;
    static public final int MT_CODE_FORWARD_CHECK_RECEIVED      = 6;
    static public final int MT_CODE_CALL_CONNECTING_ECT         = 7;
    static public final int MT_CODE_CALL_CONNECTED_ECT          = 8;
    static public final int MT_CODE_DEFLECTED_CALL              = 9;
    static public final int MT_CODE_ADDITIONAL_CALL_FORWARDED   = 10;
    public String toString()
    {
        return super.toString() + " mobile"
            + (notificationType == 0 ? " originated " : " terminated ")
            + " code: " + code
            + " index: " + index
            + " \""
            + PhoneNumberUtils.stringFromStringAndTOA(number, type) + "\" ";
    }
}

abstract class KrbAppMessage {
    private static boolean DEBUG = Krb5.DEBUG;
    void check(KerberosTime packetTimestamp,
               Integer packetUsec,
               Integer packetSeqNumber,
               HostAddress packetSAddress,
               HostAddress packetRAddress,
               SeqNumber seqNumber,
               HostAddress sAddress,
               HostAddress rAddress,
               boolean timestampRequired,
               boolean seqNumberRequired,
               PrincipalName packetPrincipal,
               Realm packetRealm)
        throws KrbApErrException {
        if (!Krb5.AP_EMPTY_ADDRESSES_ALLOWED || sAddress != null) {
            if (packetSAddress == null || sAddress == null ||
                !packetSAddress.equals(sAddress)) {
                if (DEBUG && packetSAddress == null) {
                    System.out.println("packetSAddress is null");
                }
                if (DEBUG && sAddress == null) {
                    System.out.println("sAddress is null");
                }
                throw new KrbApErrException(Krb5.KRB_AP_ERR_BADADDR);
            }
        }
        if (!Krb5.AP_EMPTY_ADDRESSES_ALLOWED || rAddress != null) {
            if (packetRAddress == null || rAddress == null ||
                !packetRAddress.equals(rAddress))
                throw new KrbApErrException(Krb5.KRB_AP_ERR_BADADDR);
        }
        if (packetTimestamp != null) {
            packetTimestamp.setMicroSeconds(packetUsec);
            if (!packetTimestamp.inClockSkew())
                throw new KrbApErrException(Krb5.KRB_AP_ERR_SKEW);
        } else
            if (timestampRequired)
                throw new KrbApErrException(Krb5.KRB_AP_ERR_SKEW);
        if (seqNumber == null && seqNumberRequired == true)
            throw new KrbApErrException(Krb5.API_INVALID_ARG);
        if (packetSeqNumber != null && seqNumber != null) {
            if (packetSeqNumber.intValue() != seqNumber.current())
                throw new KrbApErrException(Krb5.KRB_AP_ERR_BADORDER);
            seqNumber.step();
        } else {
            if (seqNumberRequired) {
                throw new KrbApErrException(Krb5.KRB_AP_ERR_BADORDER);
            }
        }
        if (packetTimestamp == null && packetSeqNumber == null)
            throw new KrbApErrException(Krb5.KRB_AP_ERR_MODIFIED);
    }
}

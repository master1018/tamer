public class KrbErrException extends sun.security.krb5.KrbException {
    private static final long serialVersionUID = 2186533836785448317L;
    public KrbErrException(int i) {
        super(i);
    }
    public KrbErrException(int i, String s) {
        super(i, s);
    }
}

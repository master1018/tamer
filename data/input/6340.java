public class Nonce {
    public static synchronized int value() {
        return sun.security.krb5.Confounder.intValue() & 0x7fffffff;
    }
}

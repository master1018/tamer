public final class SunProvider extends Provider {
    private static final long serialVersionUID = -238911724858694198L;
    private static final String INFO = "Sun " +
        "(Kerberos v5, SPNEGO)";
    public static final SunProvider INSTANCE = new SunProvider();
    public SunProvider() {
        super("SunJGSS", 1.7d, INFO);
        AccessController.doPrivileged(
                        new java.security.PrivilegedAction<Void>() {
            public Void run() {
                put("GssApiMechanism.1.2.840.113554.1.2.2",
                    "sun.security.jgss.krb5.Krb5MechFactory");
                put("GssApiMechanism.1.3.6.1.5.5.2",
                    "sun.security.jgss.spnego.SpNegoMechFactory");
                return null;
            }
        });
    }
}

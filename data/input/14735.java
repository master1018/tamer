public final class Krb5Helper {
    private Krb5Helper() { }
    private static final String IMPL_CLASS =
        "sun.security.ssl.krb5.Krb5ProxyImpl";
    private static final Krb5Proxy proxy =
        AccessController.doPrivileged(new PrivilegedAction<Krb5Proxy>() {
            public Krb5Proxy run() {
                try {
                    Class<?> c = Class.forName(IMPL_CLASS, true, null);
                    return (Krb5Proxy)c.newInstance();
                } catch (ClassNotFoundException cnf) {
                    return null;
                } catch (InstantiationException e) {
                    throw new AssertionError(e);
                } catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }});
    public static boolean isAvailable() {
        return proxy != null;
    }
    private static void ensureAvailable() {
        if (proxy == null)
            throw new AssertionError("Kerberos should have been available");
    }
    public static Subject getClientSubject(AccessControlContext acc)
            throws LoginException {
        ensureAvailable();
        return proxy.getClientSubject(acc);
    }
    public static Subject getServerSubject(AccessControlContext acc)
            throws LoginException {
        ensureAvailable();
        return proxy.getServerSubject(acc);
    }
    public static SecretKey[] getServerKeys(AccessControlContext acc)
            throws LoginException {
        ensureAvailable();
        return proxy.getServerKeys(acc);
    }
    public static String getServerPrincipalName(SecretKey kerberosKey) {
        ensureAvailable();
        return proxy.getServerPrincipalName(kerberosKey);
    }
    public static String getPrincipalHostName(Principal principal) {
        ensureAvailable();
        return proxy.getPrincipalHostName(principal);
    }
    public static Permission getServicePermission(String principalName,
            String action) {
        ensureAvailable();
        return proxy.getServicePermission(principalName, action);
    }
}

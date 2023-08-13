public class Krb5ProxyImpl implements Krb5Proxy {
    public Krb5ProxyImpl() { }
    @Override
    public Subject getClientSubject(AccessControlContext acc)
            throws LoginException {
        return Krb5Util.getSubject(GSSCaller.CALLER_SSL_CLIENT, acc);
    }
    @Override
    public Subject getServerSubject(AccessControlContext acc)
            throws LoginException {
        return Krb5Util.getSubject(GSSCaller.CALLER_SSL_SERVER, acc);
    }
    @Override
    public SecretKey[] getServerKeys(AccessControlContext acc)
            throws LoginException {
        return Krb5Util.getServiceCreds(GSSCaller.CALLER_SSL_SERVER, null, acc).getKKeys();
    }
    @Override
    public String getServerPrincipalName(SecretKey kerberosKey) {
        return ((KerberosKey)kerberosKey).getPrincipal().getName();
    }
    @Override
    public String getPrincipalHostName(Principal principal) {
        if (principal == null) {
           return null;
        }
        String hostName = null;
        try {
            PrincipalName princName =
                new PrincipalName(principal.getName(),
                        PrincipalName.KRB_NT_SRV_HST);
            String[] nameParts = princName.getNameStrings();
            if (nameParts.length >= 2) {
                hostName = nameParts[1];
            }
        } catch (Exception e) {
        }
        return hostName;
    }
    @Override
    public Permission getServicePermission(String principalName,
            String action) {
        return new ServicePermission(principalName, action);
    }
}

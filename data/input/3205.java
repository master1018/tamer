public final class FactoryImpl implements SaslClientFactory,
SaslServerFactory{
    private static final String myMechs[] = { "DIGEST-MD5" };
    private static final int DIGEST_MD5 = 0;
    private static final int mechPolicies[] = {
        PolicyUtils.NOPLAINTEXT|PolicyUtils.NOANONYMOUS};
    public FactoryImpl() {
    }
    public SaslClient createSaslClient(String[] mechs,
         String authorizationId, String protocol, String serverName,
         Map<String,?> props, CallbackHandler cbh)
         throws SaslException {
         for (int i=0; i<mechs.length; i++) {
            if (mechs[i].equals(myMechs[DIGEST_MD5]) &&
                PolicyUtils.checkPolicy(mechPolicies[DIGEST_MD5], props)) {
                if (cbh == null) {
                    throw new SaslException(
                        "Callback handler with support for RealmChoiceCallback, " +
                        "RealmCallback, NameCallback, and PasswordCallback " +
                        "required");
                }
                return new DigestMD5Client(authorizationId,
                    protocol, serverName, props, cbh);
            }
        }
        return null;
    }
    public SaslServer createSaslServer(String mech,
         String protocol, String serverName, Map<String,?> props, CallbackHandler cbh)
         throws SaslException {
         if (mech.equals(myMechs[DIGEST_MD5]) &&
             PolicyUtils.checkPolicy(mechPolicies[DIGEST_MD5], props)) {
                if (cbh == null) {
                    throw new SaslException(
                        "Callback handler with support for AuthorizeCallback, "+
                        "RealmCallback, NameCallback, and PasswordCallback " +
                        "required");
                }
                return new DigestMD5Server(protocol, serverName, props, cbh);
         }
         return null;
    }
    public String[] getMechanismNames(Map<String,?> env) {
        return PolicyUtils.filterMechs(myMechs, mechPolicies, env);
    }
}

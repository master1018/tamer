final public class ServerFactoryImpl implements SaslServerFactory {
    private static final String myMechs[] = {
        "CRAM-MD5", 
    };
    private static final int mechPolicies[] = {
        PolicyUtils.NOPLAINTEXT|PolicyUtils.NOANONYMOUS,      
    };
    private static final int CRAMMD5 = 0;
    public ServerFactoryImpl() {
    }
    public SaslServer createSaslServer(String mech,
        String protocol,
        String serverName,
        Map<String,?> props,
        CallbackHandler cbh) throws SaslException {
        if (mech.equals(myMechs[CRAMMD5])
            && PolicyUtils.checkPolicy(mechPolicies[CRAMMD5], props)) {
            if (cbh == null) {
                throw new SaslException(
            "Callback handler with support for AuthorizeCallback required");
            }
            return new CramMD5Server(protocol, serverName, props, cbh);
        }
        return null;
    };
    public String[] getMechanismNames(Map<String,?> props) {
        return PolicyUtils.filterMechs(myMechs, mechPolicies, props);
    }
}

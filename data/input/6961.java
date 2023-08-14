final public class ClientFactoryImpl implements SaslClientFactory {
    private static final String myMechs[] = {
        "EXTERNAL",
        "CRAM-MD5",
        "PLAIN",
    };
    private static final int mechPolicies[] = {
        PolicyUtils.NOPLAINTEXT|PolicyUtils.NOACTIVE|PolicyUtils.NODICTIONARY,
        PolicyUtils.NOPLAINTEXT|PolicyUtils.NOANONYMOUS,    
        PolicyUtils.NOANONYMOUS,                            
    };
    private static final int EXTERNAL = 0;
    private static final int CRAMMD5 = 1;
    private static final int PLAIN = 2;
    public ClientFactoryImpl() {
    }
    public SaslClient createSaslClient(String[] mechs,
        String authorizationId,
        String protocol,
        String serverName,
        Map<String,?> props,
        CallbackHandler cbh) throws SaslException {
            for (int i = 0; i < mechs.length; i++) {
                if (mechs[i].equals(myMechs[EXTERNAL])
                    && PolicyUtils.checkPolicy(mechPolicies[EXTERNAL], props)) {
                    return new ExternalClient(authorizationId);
                } else if (mechs[i].equals(myMechs[CRAMMD5])
                    && PolicyUtils.checkPolicy(mechPolicies[CRAMMD5], props)) {
                    Object[] uinfo = getUserInfo("CRAM-MD5", authorizationId, cbh);
                    return new CramMD5Client((String) uinfo[0],
                        (byte []) uinfo[1]);
                } else if (mechs[i].equals(myMechs[PLAIN])
                    && PolicyUtils.checkPolicy(mechPolicies[PLAIN], props)) {
                    Object[] uinfo = getUserInfo("PLAIN", authorizationId, cbh);
                    return new PlainClient(authorizationId,
                        (String) uinfo[0], (byte []) uinfo[1]);
                }
            }
            return null;
    };
    public String[] getMechanismNames(Map<String,?> props) {
        return PolicyUtils.filterMechs(myMechs, mechPolicies, props);
    }
    private Object[] getUserInfo(String prefix, String authorizationId,
        CallbackHandler cbh) throws SaslException {
        if (cbh == null) {
            throw new SaslException(
                "Callback handler to get username/password required");
        }
        try {
            String userPrompt = prefix + " authentication id: ";
            String passwdPrompt = prefix + " password: ";
            NameCallback ncb = authorizationId == null?
                new NameCallback(userPrompt) :
                new NameCallback(userPrompt, authorizationId);
            PasswordCallback pcb = new PasswordCallback(passwdPrompt, false);
            cbh.handle(new Callback[]{ncb,pcb});
            char[] pw = pcb.getPassword();
            byte[] bytepw;
            String authId;
            if (pw != null) {
                bytepw = new String(pw).getBytes("UTF8");
                pcb.clearPassword();
            } else {
                bytepw = null;
            }
            authId = ncb.getName();
            return new Object[]{authId, bytepw};
        } catch (IOException e) {
            throw new SaslException("Cannot get password", e);
        } catch (UnsupportedCallbackException e) {
            throw new SaslException("Cannot get userid/password", e);
        }
    }
}

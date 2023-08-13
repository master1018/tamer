final public class LdapSasl {
    private static final String SASL_CALLBACK = "java.naming.security.sasl.callback";
    private static final String SASL_AUTHZ_ID =
        "java.naming.security.sasl.authorizationId";
    private static final String SASL_REALM =
        "java.naming.security.sasl.realm";
    private static final int LDAP_SUCCESS = 0;
    private static final int LDAP_SASL_BIND_IN_PROGRESS = 14;   
    private LdapSasl() {
    }
    public static LdapResult saslBind(LdapClient clnt, Connection conn,
        String server, String dn, Object pw,
        String authMech, Hashtable env, Control[] bindCtls)
        throws IOException, NamingException {
        SaslClient saslClnt = null;
        boolean cleanupHandler = false;
        CallbackHandler cbh =
            (env != null) ? (CallbackHandler)env.get(SASL_CALLBACK) : null;
        if (cbh == null) {
            cbh = new DefaultCallbackHandler(dn, pw, (String)env.get(SASL_REALM));
            cleanupHandler = true;
        }
        String authzId = (env != null) ? (String)env.get(SASL_AUTHZ_ID) : null;
        String[] mechs = getSaslMechanismNames(authMech);
        try {
            saslClnt = Sasl.createSaslClient(
                mechs, authzId, "ldap", server, env, cbh);
            if (saslClnt == null) {
                throw new AuthenticationNotSupportedException(authMech);
            }
            LdapResult res;
            String mechName = saslClnt.getMechanismName();
            byte[] response = saslClnt.hasInitialResponse() ?
                saslClnt.evaluateChallenge(NO_BYTES) : null;
            res = clnt.ldapBind(null, response, bindCtls, mechName, true);
            while (!saslClnt.isComplete() &&
                (res.status == LDAP_SASL_BIND_IN_PROGRESS ||
                 res.status == LDAP_SUCCESS)) {
                response = saslClnt.evaluateChallenge(
                    res.serverCreds != null? res.serverCreds : NO_BYTES);
                if (res.status == LDAP_SUCCESS) {
                    if (response != null) {
                        throw new AuthenticationException(
                            "SASL client generated response after success");
                    }
                    break;
                }
                res = clnt.ldapBind(null, response, bindCtls, mechName, true);
            }
            if (res.status == LDAP_SUCCESS) {
                if (!saslClnt.isComplete()) {
                    throw new AuthenticationException(
                        "SASL authentication not complete despite server claims");
                }
                String qop = (String) saslClnt.getNegotiatedProperty(Sasl.QOP);
                if (qop != null && (qop.equalsIgnoreCase("auth-int")
                    || qop.equalsIgnoreCase("auth-conf"))) {
                    InputStream newIn = new SaslInputStream(saslClnt,
                        conn.inStream);
                    OutputStream newOut = new SaslOutputStream(saslClnt,
                        conn.outStream);
                    conn.replaceStreams(newIn, newOut);
                } else {
                    saslClnt.dispose();
                }
            }
            return res;
        } catch (SaslException e) {
            NamingException ne = new AuthenticationException(
                authMech);
            ne.setRootCause(e);
            throw ne;
        } finally {
            if (cleanupHandler) {
                ((DefaultCallbackHandler)cbh).clearPassword();
            }
        }
    }
    private static String[] getSaslMechanismNames(String str) {
        StringTokenizer parser = new StringTokenizer(str);
        Vector mechs = new Vector(10);
        while (parser.hasMoreTokens()) {
            mechs.addElement(parser.nextToken());
        }
        String[] mechNames = new String[mechs.size()];
        for (int i = 0; i < mechs.size(); i++) {
            mechNames[i] = (String)mechs.elementAt(i);
        }
        return mechNames;
    }
    private static final byte[] NO_BYTES = new byte[0];
}

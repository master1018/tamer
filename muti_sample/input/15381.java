public class PassSysProps {
    private static final String PLAIN = "PLAIN";
    private static final String DIGEST = "DIGEST-MD5";
    private static final String CRAM = "CRAM-MD5";
    private static final String EXTERNAL = "EXTERNAL";
    private static final String GSSAPI = "GSSAPI";
    public static void main(String[] args) throws Exception {
        String authorizationId = null;
        String protocol = "ldap";
        String serverName = "server1";
        CallbackHandler callbackHandler = new CallbackHandler(){
            public void handle(Callback[] callbacks) {
            }
        };
        Properties sysprops = System.getProperties();
        SaslClient client1 =
            Sasl.createSaslClient(new String[]{DIGEST, PLAIN}, authorizationId,
                protocol, serverName, (Map) sysprops, callbackHandler);
        System.out.println(client1);
        SaslServer server1 =
            Sasl.createSaslServer(DIGEST, protocol, serverName, (Map) sysprops,
                callbackHandler);
        System.out.println(server1);
        Map<String, String> stringProps = new Hashtable<String, String>();
        stringProps.put(Sasl.POLICY_NOPLAINTEXT, "true");
        try {
            SaslClient client2 =
                Sasl.createSaslClient(new String[]{GSSAPI, PLAIN},
                    authorizationId, protocol, serverName, stringProps,
                    callbackHandler);
            System.out.println(client2);
            SaslServer server2 =
                Sasl.createSaslServer(GSSAPI, protocol, serverName,
                    stringProps, callbackHandler);
            System.out.println(server2);
        } catch (SaslException se) {
            Throwable t = se.getCause();
            if (t instanceof GSSException) {
            } else {
                throw se;
            }
        }
        Map<String, Object> objProps = new Hashtable<String, Object>();
        objProps.put("some.object.valued.property", System.err);
        SaslClient client3 =
            Sasl.createSaslClient(new String[]{EXTERNAL, CRAM}, authorizationId,
                protocol, serverName, objProps, callbackHandler);
        System.out.println(client3);
        SaslServer server3 =
            Sasl.createSaslServer(CRAM, protocol, serverName, objProps,
                callbackHandler);
        System.out.println(server3);
        Map rawProps = new Hashtable();
        rawProps.put(Sasl.POLICY_NOPLAINTEXT, "true");
        rawProps.put("some.object.valued.property", System.err);
        SaslClient client4 =
            Sasl.createSaslClient(new String[]{EXTERNAL, CRAM}, authorizationId,
                protocol, serverName, rawProps, callbackHandler);
        System.out.println(client4);
        SaslServer server4 =
            Sasl.createSaslServer(CRAM, protocol, serverName, rawProps,
                callbackHandler);
        System.out.println(server4);
    }
}

public class Cram {
    private static final String MECH = "CRAM-MD5";
    private static final String SERVER_FQDN = "machineX.imc.org";
    private static final String PROTOCOL = "jmx";
    private static final byte[] EMPTY = new byte[0];
    private static boolean auto;
    private static boolean verbose = false;
    private static String pwfile, namesfile;
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            pwfile = "pw.properties";
            namesfile = "names.properties";
            auto = true;
        } else {
            int i = 0;
            if (args[i].equals("-m")) {
                i++;
                auto = false;
            }
            if (args.length > i) {
                pwfile = args[i++];
                if (args.length > i) {
                    namesfile = args[i++];
                }
            } else {
                pwfile = "pw.properties";
                namesfile = "names.properties";
            }
        }
        CallbackHandler clntCbh = new ClientCallbackHandler(auto);
        CallbackHandler srvCbh =
            new PropertiesFileCallbackHandler(pwfile, namesfile, null);
        SaslClient clnt = Sasl.createSaslClient(
            new String[]{MECH}, null, PROTOCOL, SERVER_FQDN, null, clntCbh);
        SaslServer srv = Sasl.createSaslServer(MECH, PROTOCOL, SERVER_FQDN, null,
            srvCbh);
        if (clnt == null) {
            throw new IllegalStateException(
                "Unable to find client impl for " + MECH);
        }
        if (srv == null) {
            throw new IllegalStateException(
                "Unable to find server impl for " + MECH);
        }
        byte[] response = (clnt.hasInitialResponse()?
            clnt.evaluateChallenge(EMPTY) : EMPTY);
        byte[] challenge;
        while (!clnt.isComplete() || !srv.isComplete()) {
            challenge = srv.evaluateResponse(response);
            if (challenge != null) {
                response = clnt.evaluateChallenge(challenge);
            }
        }
        if (clnt.isComplete() && srv.isComplete()) {
            if (verbose) {
                System.out.println("SUCCESS");
                System.out.println("authzid is " + srv.getAuthorizationID());
            }
        } else {
            throw new IllegalStateException("FAILURE: mismatched state:" +
                " client complete? " + clnt.isComplete() +
                " server complete? " + srv.isComplete());
        }
    }
}

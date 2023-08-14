public class GetPeerHost {
    public static void main(String[] argv) throws Exception {
        String testRoot = System.getProperty("test.src", ".");
        System.setProperty("javax.net.ssl.trustStore", testRoot
                            + "/../../../../../../../etc/truststore");
        GetPeerHostServer server = new GetPeerHostServer();
        server.start();
        GetPeerHostClient client = new GetPeerHostClient();
        client.start();
        server.join ();
        if (!server.getPassStatus ()) {
            throw new Exception ("The test failed");
        }
    }
}

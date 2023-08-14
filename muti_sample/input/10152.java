class GetPeerHostServer extends Thread
{
    private String host;
    ServerSocket ss;
    boolean isHostIPAddr = false;
    public GetPeerHostServer ()
    {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("JKS");
            char[] passphrase = "passphrase".toCharArray();
            String testRoot = System.getProperty("test.src", ".");
            ks.load(new FileInputStream(testRoot
                                        + "/../../../../../../../etc/keystore"),
                    passphrase);
            kmf.init(ks, passphrase);
            ctx.init(kmf.getKeyManagers(), null, null);
            ServerSocketFactory ssf = ctx.getServerSocketFactory();
            ss = ssf.createServerSocket(9999);
        }catch (Exception e) {
            System.err.println("Unexpected exceptions: " + e);
            e.printStackTrace();
        }
    }
    public void run() {
        try {
            System.out.println("SERVER: waiting for requests...");
            Socket socket = ss.accept();
            System.out.println("SERVER: got a request!");
            host = ((javax.net.ssl.SSLSocket)socket).getSession().getPeerHost();
            System.out.println("SERVER: Host IP address (not the name): "
                                + host);
        } catch (Exception e) {
            System.err.println("Unexpected exceptions: " + e);
            e.printStackTrace();
          }
        if (host != null && (host.charAt(0) > '9') ||
                            (host.charAt(0) < '0')) {
            System.out.println("Error: bug 4302026 may not be fixed.");
        } else {
             isHostIPAddr = true;
             System.out.println("Passed!");
         }
    }
    boolean getPassStatus () {
        return isHostIPAddr;
    }
}

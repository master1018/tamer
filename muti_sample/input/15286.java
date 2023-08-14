public class SSL {
    private static String krb5Cipher;
    private static final int LOOP_LIMIT = 3;
    private static int loopCount = 0;
    private static volatile String server;
    private static volatile int port;
    public static void main(String[] args) throws Exception {
        krb5Cipher = args[0];
        KDC kdc = KDC.create(OneKDC.REALM);
        try {
            server = InetAddress.getLocalHost().getHostName().toLowerCase();
        } catch (java.net.UnknownHostException e) {
            server = "localhost";
        }
        kdc.addPrincipal(OneKDC.USER, OneKDC.PASS);
        kdc.addPrincipalRandKey("krbtgt/" + OneKDC.REALM);
        KDC.saveConfig(OneKDC.KRB5_CONF, kdc);
        System.setProperty("java.security.krb5.conf", OneKDC.KRB5_CONF);
        KeyTab ktab = KeyTab.create(OneKDC.KTAB);
        PrincipalName service = new PrincipalName(
                "host/" + server, PrincipalName.KRB_NT_SRV_HST);
        ktab.addEntry(service, "pass1".toCharArray(), 1, true);
        ktab.addEntry(service, "pass2".toCharArray(), 2, true);
        ktab.addEntry(service, "pass3".toCharArray(), 3, true);
        ktab.save();
        kdc.addPrincipal("host/" + server, "pass2".toCharArray());
        System.setProperty("java.security.auth.login.config", OneKDC.JAAS_CONF);
        File f = new File(OneKDC.JAAS_CONF);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write((
                "ssl {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule required\n" +
                "    principal=\"host/" + server + "\"\n" +
                "    useKeyTab=true\n" +
                "    keyTab=" + OneKDC.KTAB + "\n" +
                "    isInitiator=false\n" +
                "    storeKey=true;\n};\n"
                ).getBytes());
        fos.close();
        f.deleteOnExit();
        Context c;
        final Context s = Context.fromJAAS("ssl");
        s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        Thread server = new Thread(new Runnable() {
            public void run() {
                try {
                    s.doAs(new JsseServerAction(), null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        server.setDaemon(true);
        server.start();
        Thread.sleep(2000);
        c = Context.fromUserPass(OneKDC.USER, OneKDC.PASS, false);
        c.startAsClient("host/" + server, GSSUtil.GSS_KRB5_MECH_OID);
        c.doAs(new JsseClientAction(), null);
        Thread.sleep(2000);
        ktab = KeyTab.getInstance(OneKDC.KTAB);
        ktab.addEntry(service, "pass4".toCharArray(), 4, true);
        ktab.save();
        kdc.addPrincipal("host/" + server, "pass4".toCharArray());
        c = Context.fromUserPass(OneKDC.USER, OneKDC.PASS, false);
        c.startAsClient("host/" + server, GSSUtil.GSS_KRB5_MECH_OID);
        c.doAs(new JsseClientAction(), null);
    }
    private static class JsseClientAction implements Action {
        public byte[] run(Context s, byte[] input) throws Exception {
            SSLSocketFactory sslsf =
                (SSLSocketFactory) SSLSocketFactory.getDefault();
            System.out.println("Connecting " + server + ":" + port);
            SSLSocket sslSocket = (SSLSocket) sslsf.createSocket(server, port);
            String enabledSuites[] = {krb5Cipher};
            sslSocket.setEnabledCipherSuites(enabledSuites);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                sslSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                sslSocket.getOutputStream()));
            String outStr = "Hello There!\n";
            out.write(outStr);
            out.flush();
            System.out.print("Sending " + outStr);
            String inStr = in.readLine();
            System.out.println("Received " + inStr);
            String cipherSuiteChosen = sslSocket.getSession().getCipherSuite();
            System.out.println("Cipher suite in use: " + cipherSuiteChosen);
            Principal self = sslSocket.getSession().getLocalPrincipal();
            System.out.println("I am: " + self.toString());
            Principal peer = sslSocket.getSession().getPeerPrincipal();
            System.out.println("Server is: " + peer.toString());
            sslSocket.close();
            return null;
        }
    }
    private static class JsseServerAction implements Action {
        public byte[] run(Context s, byte[] input) throws Exception {
            SSLServerSocketFactory sslssf =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket =
                (SSLServerSocket) sslssf.createServerSocket(0); 
            port = sslServerSocket.getLocalPort();
            System.out.println("Listening on " + port);
            String enabledSuites[] = {krb5Cipher};
            sslServerSocket.setEnabledCipherSuites(enabledSuites);
            while (loopCount++ < LOOP_LIMIT) {
                System.out.println("Waiting for incoming connection...");
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
                System.out.println("Got connection from client "
                    + sslSocket.getInetAddress());
                BufferedReader in = new BufferedReader(new InputStreamReader(
                    sslSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    sslSocket.getOutputStream()));
                String inStr = in.readLine();
                System.out.println("Received " + inStr);
                String outStr = inStr + " " + new Date().toString() + "\n";
                out.write(outStr);
                System.out.println("Sending " + outStr);
                out.flush();
                String cipherSuiteChosen =
                    sslSocket.getSession().getCipherSuite();
                System.out.println("Cipher suite in use: " + cipherSuiteChosen);
                Principal self = sslSocket.getSession().getLocalPrincipal();
                System.out.println("I am: " + self.toString());
                Principal peer = sslSocket.getSession().getPeerPrincipal();
                System.out.println("Client is: " + peer.toString());
                sslSocket.close();
            }
            return null;
        }
    }
}

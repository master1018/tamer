public class JavaxHTTPSConnection {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    private static String getPath(DataInputStream in)
        throws IOException
    {
        String line = in.readLine();
        String path = "";
        if (line.startsWith("GET /")) {
            line = line.substring(5, line.length()-1).trim();
            int index = line.indexOf(' ');
            if (index != -1) {
                path = line.substring(0, index);
            }
        }
        do {
            line = in.readLine();
        } while ((line.length() != 0) &&
                 (line.charAt(0) != '\r') && (line.charAt(0) != '\n'));
        if (path.length() != 0) {
            return path;
        } else {
            throw new IOException("Malformed Header");
        }
    }
    private byte[] getBytes(String path)
        throws IOException
    {
        return "Hello world, I am here".getBytes();
    }
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
          (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        DataOutputStream out =
                new DataOutputStream(sslSocket.getOutputStream());
        try {
             DataInputStream in =
                        new DataInputStream(sslSocket.getInputStream());
             String path = getPath(in);
             byte[] bytecodes = getBytes(path);
             try {
                out.writeBytes("HTTP/1.0 200 OK\r\n");
                out.writeBytes("Content-Length: " + bytecodes.length + "\r\n");
                out.writeBytes("Content-Type: text/html\r\n\r\n");
                out.write(bytecodes);
                out.flush();
             } catch (IOException ie) {
                ie.printStackTrace();
                return;
             }
        } catch (Exception e) {
             e.printStackTrace();
             out.writeBytes("HTTP/1.0 400 " + e.getMessage() + "\r\n");
             out.writeBytes("Content-Type: text/html\r\n\r\n");
             out.flush();
        } finally {
             System.out.println("Server closing socket");
             sslSocket.close();
             serverReady = false;
        }
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(new NameVerifier());
        URL url = new URL("https:
                                "/etc/hosts");
        URLConnection urlc = url.openConnection();
        if (!(urlc instanceof javax.net.ssl.HttpsURLConnection)) {
            throw new Exception(
                "URLConnection ! instanceof javax.net.ssl.HttpsURLConnection");
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                               urlc.getInputStream()));
            String inputLine;
            System.out.print("Client reading... ");
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            System.out.println("Cipher Suite: " +
                ((HttpsURLConnection)urlc).getCipherSuite());
            Certificate[] certs =
                ((HttpsURLConnection)urlc).getServerCertificates();
            for (int i = 0; i < certs.length; i++) {
                System.out.println(certs[0]);
            }
            in.close();
        } catch (SSLException e) {
            if (in != null)
                in.close();
            throw e;
        }
        System.out.println("Client reports:  SUCCESS");
    }
    static class NameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            System.out.println(
                "HostnameVerifier: returning true");
            return true;
        }
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String[] args) throws Exception {
        String keyFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + keyStoreFile;
        String trustFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + trustStoreFile;
        System.setProperty("javax.net.ssl.keyStore", keyFilename);
        System.setProperty("javax.net.ssl.keyStorePassword", passwd);
        System.setProperty("javax.net.ssl.trustStore", trustFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", passwd);
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new JavaxHTTPSConnection();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    JavaxHTTPSConnection() throws Exception {
        if (separateServerThread) {
            startServer(true);
            startClient(false);
        } else {
            startClient(true);
            startServer(false);
        }
        if (separateServerThread) {
            serverThread.join();
        } else {
            clientThread.join();
        }
        if (serverException != null) {
            System.out.print("Server Exception:");
            throw serverException;
        }
        if (clientException != null) {
            System.out.print("Client Exception:");
            throw clientException;
        }
    }
    void startServer(boolean newThread) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide();
                    } catch (Exception e) {
                        System.err.println("Server died...");
                        serverReady = true;
                        serverException = e;
                    }
                }
            };
            serverThread.start();
        } else {
            doServerSide();
        }
    }
    void startClient(boolean newThread) throws Exception {
        if (newThread) {
            clientThread = new Thread() {
                public void run() {
                    try {
                        doClientSide();
                    } catch (Exception e) {
                        System.err.println("Client died...");
                        clientException = e;
                    }
                }
            };
            clientThread.start();
        } else {
            doClientSide();
        }
    }
}

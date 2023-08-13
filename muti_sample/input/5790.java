public class AsyncSSLSocketClose implements Runnable
{
    SSLSocket socket;
    SSLServerSocket ss;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    public static void main(String[] args) {
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
        new AsyncSSLSocketClose();
    }
    public AsyncSSLSocketClose() {
        try {
            SSLServerSocketFactory sslssf =
                (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
            ss = (SSLServerSocket) sslssf.createServerSocket(0);
            SSLSocketFactory sslsf =
                (SSLSocketFactory)SSLSocketFactory.getDefault();
            socket = (SSLSocket)sslsf.createSocket("localhost",
                                                        ss.getLocalPort());
            SSLSocket serverSoc = (SSLSocket) ss.accept();
            ss.close();
            (new Thread(this)).start();
            serverSoc.startHandshake();
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            socket.setSoLinger(true, 10);
            System.out.println("Calling Socket.close");
            socket.close();
            System.out.println("ssl socket get closed");
            System.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() {
        try {
            byte[] ba = new byte[1024];
            for (int i=0; i<ba.length; i++)
                ba[i] = 0x7A;
            OutputStream os = socket.getOutputStream();
            int count = 0;
            while (true) {
                count += ba.length;
                System.out.println(count + " bytes to be written");
                os.write(ba);
                System.out.println(count + " bytes written");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

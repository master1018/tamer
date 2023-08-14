public class CloseSocket {
    public static void main(String[] args) throws Exception {
        final ServerSocket serverSocket = new ServerSocket(0);
        int serverPort = serverSocket.getLocalPort();
        new Thread() {
            public void run() {
                try {
                    Socket s = serverSocket.accept();
                    System.out.println("Server accepted connection");
                    Thread.currentThread().sleep(100);
                    s.close();
                    System.out.println("Server closed socket, done.");
                } catch (Exception e) {
                    System.out.println("Server exception:");
                    e.printStackTrace();
                }
            }
        }.start();
        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket)factory.createSocket("localhost", serverPort);
        System.out.println("Client established TCP connection");
        boolean failed = false;
        try {
            System.out.println("Starting handshake...");
            socket.startHandshake();
            System.out.println("ERROR: no exception");
            failed = true;
        } catch (IOException e) {
            System.out.println("Failed as expected: " + e);
        }
        try {
            System.out.println("Trying read...");
            InputStream in = socket.getInputStream();
            int b = in.read();
            System.out.println("ERROR: no exception, read: " + b);
            failed = true;
        } catch (IOException e) {
            System.out.println("Failed as expected: " + e);
        }
        try {
            System.out.println("Trying read...");
            OutputStream out = socket.getOutputStream();
            out.write(43);
            System.out.println("ERROR: no exception");
            failed = true;
        } catch (IOException e) {
            System.out.println("Failed as expected: " + e);
        }
        if (failed) {
            throw new Exception("One or more tests failed");
        }
    }
}

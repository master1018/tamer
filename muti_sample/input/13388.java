public class HttpContinueStackOverflow {
    static class Server implements Runnable {
        int port;
        ServerSocket serverSock ;
        Server() throws IOException {
            serverSock = new ServerSocket(0);
        }
        int getLocalPort() {
            return serverSock.getLocalPort();
        }
        public void run() {
            Socket sock = null;
            try {
                serverSock.setSoTimeout(10000);
                sock = serverSock.accept();
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
                PrintStream out = new PrintStream( sock.getOutputStream() );
                in.readLine();
                out.println("HTTP/1.1 100 Continue\r");
                out.println("\r");
                out.println("junk junk junk");
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { serverSock.close(); } catch (IOException unused) {}
                try { sock.close(); } catch (IOException unused) {}
            }
        }
    }
    HttpContinueStackOverflow() throws Exception {
        Server s = new Server();
        (new Thread(s)).start();
        URL url = new URL("http", "localhost", s.getLocalPort(), "anything.html");
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.getResponseCode();
        System.out.println("TEST PASSED");
    }
    public static void main(String args[]) throws Exception {
        System.out.println("Testing 100-Continue");
        new HttpContinueStackOverflow();
    }
}

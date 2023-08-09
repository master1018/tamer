public class B6890349 extends Thread {
    public static final void main(String[] args) throws Exception {
        try {
            ServerSocket server = new ServerSocket (0);
            int port = server.getLocalPort();
            System.out.println ("listening on "  + port);
            B6890349 t = new B6890349 (server);
            t.start();
            URL u = new URL ("http:
            HttpURLConnection urlc = (HttpURLConnection)u.openConnection ();
            InputStream is = urlc.getInputStream();
            throw new RuntimeException ("Test failed");
        } catch (IOException e) {
            System.out.println ("OK");
        }
    }
    ServerSocket server;
    B6890349 (ServerSocket server) {
        this.server = server;
    }
    String resp = "HTTP/1.1 200 Ok\r\nContent-length: 0\r\n\r\n";
    public void run () {
        try {
            Socket s = server.accept ();
            OutputStream os = s.getOutputStream();
            os.write (resp.getBytes());
        } catch (IOException e) {
            System.out.println (e);
        }
    }
}

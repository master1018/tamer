public class ResendPostBody {
    static class Server extends Thread {
        InputStream     in;
        OutputStream out;
        Socket  sock;
        StringBuffer response;
        ServerSocket server;
        Server (ServerSocket s) throws IOException
        {
            server = s;
        }
        void waitFor (String s) throws IOException
        {
            byte[] w = s.getBytes ();
            for(int c=0; c<w.length; c++ ) {
                byte expected = w[c];
                int b = in.read();
                if (b == -1) {
                    acceptConn ();
                }
                if ((byte)b != expected) {
                    c = 0;
                }
            }
        }
        boolean done = false;
        public synchronized boolean finished () {
            return done;
        }
        public synchronized void setFinished (boolean b) {
            done = b;
        }
        void acceptConn () throws IOException
        {
            sock = server.accept ();
            in = sock.getInputStream ();
            out = sock.getOutputStream ();
        }
        public void run () {
            try {
                response = new StringBuffer (1024);
                acceptConn ();
                waitFor ("POST");
                waitFor ("ZZZ");
                Thread.sleep (500);
                sock.close ();
                acceptConn ();
                waitFor ("POST");
                waitFor ("ZZZ");
                response.append ("HTTP/1.1 200 OK\r\n");
                response.append ("Server: Microsoft-IIS/5.0");
                response.append ("Date: Wed, 26 Jul 2000 14:17:04 GMT\r\n\r\n");
                out.write (response.toString().getBytes());
                while (!finished()) {
                    Thread.sleep (1000);
                }
                out.close();
            } catch (Exception e) {
                System.err.println ("Server Exception: " + e);
            } finally {
                try { server.close(); } catch (IOException unused) {}
            }
        }
    }
    ServerSocket ss;
    Server server;
    public static void main(String[] args) throws Exception {
        try {
            if (args.length == 1 && args[0].equals ("-i")) {
                System.out.println ("Press return when ready");
                System.in.read ();
                System.out.println ("Done");
            }
            ResendPostBody t = new ResendPostBody ();
            t. execute ();
        } catch (IOException  e) {
            System.out.println ("IOException");
        }
    }
    public void execute () throws Exception {
        byte b[] = "X=ABCDEFGHZZZ".getBytes();
        ss = new ServerSocket (0);
        server = new Server (ss);
        server.start ();
        String s = "http:
        URL url = new URL(s);
        HttpURLConnection conURL =  (HttpURLConnection)url.openConnection();
        conURL.setDoOutput(true);
        conURL.setDoInput(true);
        conURL.setAllowUserInteraction(false);
        conURL.setUseCaches(false);
        conURL.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conURL.setRequestProperty("Content-Length", ""+b.length);
        conURL.setRequestProperty("Connection", "Close");
        DataOutputStream OutStream = new DataOutputStream(conURL.getOutputStream());
                          OutStream.write(b, 0, b.length);
        OutStream.flush();
        OutStream.close();
        int resp = conURL.getResponseCode ();
        server.setFinished (true);
        if (resp != 200)
            throw new RuntimeException ("Response code was not 200: " + resp);
  }
}

public class GetContent implements Runnable {
     ServerSocket ss;
     public void run() {
        try {
            Socket s = ss.accept();
            s.setTcpNoDelay(true);
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            out.print("HTTP/1.1 404 Not Found\r\n");
            out.print("Connection: close\r\n");
            out.print("Content-Type: text/html; charset=iso-8859-1\r\n");
            out.print("\r\n");
            out.flush();
            out.print("<HTML><BODY>Sorry, page not found</BODY></HTML>");
            out.flush();
            Thread.sleep(2000);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { ss.close(); } catch (IOException unused) {}
        }
     }
     GetContent() throws Exception {
         ss = new ServerSocket(0);
         Thread thr = new Thread(this);
         thr.start();
         boolean error = true;
         try {
             String name = "http:
                           "/no-such-name";
             java.net.URL url = null;
             url = new java.net.URL(name);
             Object obj = url.getContent();
             InputStream in = (InputStream) obj;
             byte buff[] = new byte[200];
             int len = in.read(buff);
         } catch (IOException ex) {
             error = false;
         }
         if (error)
             throw new RuntimeException("No IOException generated.");
     }
     public static void main(String args[]) throws Exception {
        new GetContent();
     }
}

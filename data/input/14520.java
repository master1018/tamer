public class Modified implements Runnable {
    ServerSocket ss;
    public void run() {
        try {
            Socket s = ss.accept();
            boolean gotIfModified = false;
            BufferedReader in = new BufferedReader(
                new InputStreamReader(s.getInputStream()) );
            String str = null;
            do {
                str = in.readLine();
                if (str.startsWith("If-Modified-Since")) {
                    gotIfModified = true;
                }
                if (str.equals("")) {
                    break;
                }
            } while (str != null);
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            if (gotIfModified) {
                out.print("HTTP/1.1 304 Not Modified\r\n");
            } else {
                out.print("HTTP/1.1 200 OK\r\n");
            }
            out.print("Content-Type: text/html\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            out.flush();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    Modified() throws Exception {
        ss = new ServerSocket(0);
        Thread thr = new Thread(this);
        thr.start();
        URL testURL = new URL("http:
                              "/index.html");
        URLConnection URLConn = testURL.openConnection();
        HttpURLConnection httpConn;
        if (URLConn instanceof HttpURLConnection) {
            httpConn = (HttpURLConnection)URLConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setIfModifiedSince(9990000000000L);
            int response = httpConn.getResponseCode();
            if (response != 304)
                throw new RuntimeException("setModifiedSince failure.");
        }
    }
    public static void main(String args[]) throws Exception {
        new Modified();
    }
}

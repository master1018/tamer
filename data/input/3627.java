public class CloseOptionHeader implements Runnable {
    static ServerSocket ss;
    static boolean hasCloseHeader = false;
    public void run() {
        try {
            Socket s = ss.accept();
            InputStream is = s.getInputStream ();
            MessageHeader mh = new MessageHeader(is);
            String connHeader = mh.findValue("Connection");
            if (connHeader != null && connHeader.equalsIgnoreCase("close")) {
                hasCloseHeader = true;
            }
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: text/html; charset=iso-8859-1\r\n");
            out.print("Content-Length: 0\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            out.print("\r\n");
            out.flush();
            s.close();
            ss.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) throws Exception {
        Thread tester = new Thread(new CloseOptionHeader());
        ss = new ServerSocket(0);
        tester.start();
        URL url = new URL("http:
        HttpURLConnection huc = (HttpURLConnection)url.openConnection();
        huc.connect();
        huc.getResponseCode();
        huc.disconnect();
        tester.join();
        if (!hasCloseHeader) {
            throw new RuntimeException("Test failed : should see 'close' connection header");
        }
    }
}

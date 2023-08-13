public class HttpResponseCode implements Runnable {
    ServerSocket ss;
    public void run() {
        try {
            Socket s = ss.accept();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(s.getInputStream()) );
            String req = in.readLine();
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            out.print("HTTP/1.1 403 Forbidden\r\n");
            out.print("\r\n");
            out.flush();
            s.close();
            ss.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    HttpResponseCode() throws Exception {
        ss = new ServerSocket(0);
        (new Thread(this)).start();
        String url = "http:
            Integer.toString(ss.getLocalPort()) +
            "/missing.nothtml";
        URLConnection uc = new URL(url).openConnection();
        int respCode1 = ((HttpURLConnection)uc).getResponseCode();
        ((HttpURLConnection)uc).disconnect();
        int respCode2 = ((HttpURLConnection)uc).getResponseCode();
        if (respCode1 != 403 || respCode2 != 403) {
            throw new RuntimeException("Testing Http response code failed");
        }
    }
    public static void main(String args[]) throws Exception {
        new HttpResponseCode();
    }
}

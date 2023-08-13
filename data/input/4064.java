public class Encode implements Runnable {
    public static void main(String args[]) throws Exception {
        new Encode();
    }
    final ServerSocket ss = new ServerSocket(0);
    Encode() throws Exception {
        (new Thread(this)).start();
        String toEncode = "\uD800\uDC00 \uD801\uDC01 ";
        String enc1 = URLEncoder.encode(toEncode, "UTF-8");
        byte bytes[] = {};
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        InputStreamReader reader = new InputStreamReader( bais, "8859_1");
        String url = "http:
            "/missing.nothtml";
        HttpURLConnection uc =  (HttpURLConnection)new URL(url).openConnection();
        uc.connect();
        try {
            String enc2 = URLEncoder.encode(toEncode, "UTF-8");
            if (!enc1.equals(enc2)) {
                System.out.println("test failed");
                throw new RuntimeException("test failed");
            }
        } finally {
            uc.disconnect();
        }
    }
    public void run() {
        try (ServerSocket serv = ss;
             Socket s = serv.accept();
             BufferedReader in =
                 new BufferedReader(new InputStreamReader(s.getInputStream())))
        {
            String req = in.readLine();
            try (OutputStream os = s.getOutputStream();
                 BufferedOutputStream bos = new BufferedOutputStream(os);
                 PrintStream out = new PrintStream(bos))
            {
                out.print("HTTP/1.1 403 Forbidden\r\n");
                out.print("\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

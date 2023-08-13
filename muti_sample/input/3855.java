public class Responses {
    static Object[][] getTests() {
        return new Object[][] {
            { "HTTP/1.1 200 OK",        "200",  "OK" },
            { "HTTP/1.1 404 ",          "404",  null },
            { "HTTP/1.1 200",           "200",  null },
            { "HTTP/1.1",               "-1",   null },
            { "Invalid",                "-1",   null },
            { null,                     "-1" ,  null },
        };
    }
    static class HttpServer implements Runnable {
        ServerSocket ss;
        public HttpServer() {
            try {
                ss = new ServerSocket(0);
            } catch (IOException ioe) {
                throw new Error("Unable to create ServerSocket: " + ioe);
            }
        }
        public int port() {
            return ss.getLocalPort();
        }
        public void shutdown() throws IOException {
            ss.close();
        }
        public void run() {
            Object[][] tests = getTests();
            try {
                for (;;) {
                    Socket s = ss.accept();
                    BufferedReader in = new BufferedReader(
                                              new InputStreamReader(
                                                s.getInputStream()));
                    String req = in.readLine();
                    int pos1 = req.indexOf(' ');
                    int pos2 = req.indexOf(' ', pos1+1);
                    int i = Integer.parseInt(req.substring(pos1+2, pos2));
                    PrintStream out = new PrintStream(
                                        new BufferedOutputStream(
                                          s.getOutputStream() ));
                    out.print( tests[i][0] );
                    out.print("\r\n");
                    out.print("Content-Length: 0\r\n");
                    out.print("Connection: close\r\n");
                    out.print("\r\n");
                    out.flush();
                    s.shutdownOutput();
                    s.close();
                }
            } catch (Exception e) {
            }
        }
    }
    public static void main(String args[]) throws Exception {
        HttpServer svr = new HttpServer();
        (new Thread(svr)).start();
        int port = svr.port();
        int failures = 0;
        Object tests[][] = getTests();
        for (int i=0; i<tests.length; i++) {
            System.out.println("******************");
            System.out.println("Test with response: >" + tests[i][0] + "<");
            URL url = new URL("http:
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            try {
                int expectedCode = Integer.parseInt((String)tests[i][1]);
                int actualCode = http.getResponseCode();
                if (actualCode != expectedCode) {
                    System.out.println("getResponseCode returned: " + actualCode +
                        ", expected: " + expectedCode);
                    failures++;
                    continue;
                }
                String expectedPhrase = (String)tests[i][2];
                String actualPhrase = http.getResponseMessage();
                if (actualPhrase == null && expectedPhrase == null) {
                    continue;
                }
                if (!actualPhrase.equals(expectedPhrase)) {
                    System.out.println("getResponseMessage returned: " +
                        actualPhrase + ", expected: " + expectedPhrase);
                }
            } catch (IOException e) {
                e.printStackTrace();
                failures++;
            }
        }
        svr.shutdown();
        if (failures > 0) {
            throw new Exception(failures + " sub-test(s) failed.");
        }
    }
}

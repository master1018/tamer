public class B6726695 extends Thread {
    private ServerSocket server = null;
    private int port = 0;
    private byte[] data = new byte[512];
    private String boundary = "----------------7774563516523621";
    public static void main(String[] args) throws Exception {
        B6726695 test = new B6726695();
        test.setDaemon(true);
        test.start();
        test.test();
    }
    public B6726695() {
        try {
            server = new ServerSocket(0);
            port = server.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void test() throws Exception {
        URL url = new URL("http:
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setRequestProperty("Expect", "100-Continue");
        http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(512);
        OutputStream out = null;
        int errorCode = -1;
        try {
            out = http.getOutputStream();
        } catch (ProtocolException e) {
            errorCode = http.getResponseCode();
        }
        if (errorCode != 417) {
            throw new RuntimeException("Didn't get the ProtocolException");
        }
        http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Expect", "100-Continue");
        http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(data.length);
        out = null;
        try {
            out = http.getOutputStream();
        } catch (ProtocolException e) {
        }
        if (out == null) {
            throw new RuntimeException("Didn't get an OutputStream");
        }
        out.write(data);
        out.flush();
        errorCode = http.getResponseCode();
        if (errorCode != 200) {
            throw new RuntimeException("Response code is " + errorCode);
        }
        out.close();
        http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Expect", "100-Continue");
        http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(data.length);
        out = null;
        try {
            out = http.getOutputStream();
        } catch (ProtocolException e) {
        }
        if (out == null) {
            throw new RuntimeException("Didn't get an OutputStream");
        }
        out.write(data);
        out.flush();
        out.close();
        errorCode = http.getResponseCode();
        if (errorCode != 200) {
            throw new RuntimeException("Response code is " + errorCode);
        }
    }
    @Override
    public void run() {
        try {
            Socket s = server.accept();
            serverReject(s);
            s = server.accept();
            serverAccept(s);
            s = server.accept();
            serverIgnore(s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { server.close(); } catch (IOException unused) {}
        }
    }
    public void serverReject(Socket s) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintStream out = new PrintStream(new BufferedOutputStream(s.getOutputStream()));
        String line = null;
        do {
            line = in.readLine();
        } while (line != null && line.length() != 0);
        out.print("HTTP/1.1 417 Expectation Failed\r\n");
        out.print("Server: Sun-Java-System-Web-Server/7.0\r\n");
        out.print("Connection: close\r\n");
        out.print("Content-Length: 0\r\n");
        out.print("\r\n");
        out.flush();
        out.close();
        in.close();
    }
    public void serverAccept(Socket s) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintStream out = new PrintStream(new BufferedOutputStream(s.getOutputStream()));
        String line = null;
        do {
            line = in.readLine();
        } while (line != null && line.length() != 0);
        out.print("HTTP/1.1 100 Continue\r\n");
        out.print("\r\n");
        out.flush();
        char[] cbuf = new char[512];
        in.read(cbuf);
        System.out.println("server sleeping...");
        try {Thread.sleep(6000); } catch (InterruptedException e) {}
        out.print("HTTP/1.1 200 OK");
        out.print("Server: Sun-Java-System-Web-Server/7.0\r\n");
        out.print("Connection: close\r\n");
        out.print("Content-Length: 0\r\n");
        out.print("\r\n");
        out.flush();
        out.close();
        in.close();
    }
    public void serverIgnore(Socket s) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintStream out = new PrintStream(new BufferedOutputStream(s.getOutputStream()));
        String line = null;
        do {
            line = in.readLine();
        } while (line != null && line.length() != 0);
        char[] cbuf = new char[512];
        int l = in.read(cbuf);
        out.print("HTTP/1.1 200 OK");
        out.print("Server: Sun-Java-System-Web-Server/7.0\r\n");
        out.print("Content-Length: 0\r\n");
        out.print("Connection: close\r\n");
        out.print("\r\n");
        out.flush();
        out.close();
        in.close();
    }
}

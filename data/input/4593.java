public class RequestURI
{
    public static void main(String[] args) {
        ServerSocket ss;
        int port;
        try {
            ss = new ServerSocket(5001);
            port = ss.getLocalPort();
        } catch (Exception e) {
            System.out.println ("Exception: " + e);
            return;
        }
        RequestURIServer server = new RequestURIServer(ss);
        server.start();
        try {
            System.getProperties().setProperty("http.proxyHost", "localhost");
            System.getProperties().setProperty("http.proxyPort", Integer.toString(port));
            URL url = new URL("http:
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            int resp = uc.getResponseCode();
            if (resp != 200)
                throw new RuntimeException("Failed: Fragment is being passed as part of the RequestURI");
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class RequestURIServer extends Thread
{
    ServerSocket ss;
    String replyOK =  "HTTP/1.1 200 OK\r\n" +
                      "Content-Length: 0\r\n\r\n";
    String replyFAILED = "HTTP/1.1 404 Not Found\r\n\r\n";
    public RequestURIServer(ServerSocket ss) {
        this.ss = ss;
    }
    public void run() {
        try {
            Socket sock = ss.accept();
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            MessageHeader headers =  new MessageHeader (is);
            String requestLine = headers.getValue(0);
            int first  = requestLine.indexOf(' ');
            int second  = requestLine.lastIndexOf(' ');
            String URIString = requestLine.substring(first+1, second);
            URI requestURI = new URI(URIString);
            if (requestURI.getFragment() != null)
                os.write(replyFAILED.getBytes("UTF-8"));
            else
                os.write(replyOK.getBytes("UTF-8"));
            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

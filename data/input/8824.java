public class ProtocolRedirect {
    public static void main(String [] args) throws Exception {
        int localPort;
        new Thread(new Redirect()).start();
        while ((localPort = Redirect.listenPort) == -1) {
            Thread.sleep(1000);
        }
        String page = "http:
        URL url = new URL(page);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.connect();
        if (conn.getResponseCode() != 302) {
            throw new RuntimeException("Test failed. Should get RespCode: 302. Got:"+conn.getResponseCode());
        }
    }
}
class Redirect implements Runnable {
    public static int listenPort = -1; 
    private void sendReply() throws IOException {
        OutputStream out = sock.getOutputStream();
        StringBuffer reply = new StringBuffer();
        reply.append("HTTP/1.0 302 Found\r\n"
                     + "Location: https:
                     + "/\r\n\r\n");
        out.write(reply.toString().getBytes());
    }
    Socket sock;
    public void run() {
        try {
            ServerSocket ssock = new ServerSocket();
            ssock.bind(null);
            listenPort = ssock.getLocalPort();
            sock = ssock.accept();
            sock.setTcpNoDelay(true);
            sendReply();
            sock.shutdownOutput();
        } catch(IOException io) {
            throw new RuntimeException(io.getCause());
        }
    }
}

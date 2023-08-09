public class HandleContentTypeWithAttrs {
    URL url;
    public HandleContentTypeWithAttrs (int port) throws Exception {
        String localHostName = InetAddress.getLocalHost().getHostName();
        url = new URL("http:
        URLConnection urlConn = url.openConnection();
        Object obj = urlConn.getContent();
        if (!(obj instanceof PlainTextInputStream))
            throw new Exception("Cannot get the correct content handler.");
    }
    public static void main(String [] argv) throws Exception {
        myHttpServer testServer = new myHttpServer();
        testServer.startServer(0);
        int serverPort = testServer.getServerLocalPort();
        new HandleContentTypeWithAttrs(serverPort);
    }
}
class myHttpServer implements Runnable, Cloneable {
    public Socket clientSocket = null;
    private Thread serverInstance;
    private ServerSocket serverSocket;
    public PrintStream clientOutput;
    public InputStream clientInput;
    static URL defaultContext;
    public void close() throws IOException {
        clientSocket.close();
        clientSocket = null;
        clientInput = null;
        clientOutput = null;
    }
    final public void run() {
        if (serverSocket != null) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try {
                Socket ns = serverSocket.accept();
                myHttpServer n = (myHttpServer)clone();
                n.serverSocket = null;
                n.clientSocket = ns;
                new Thread(n).start();
            } catch(Exception e) {
                System.out.print("Server failure\n");
                e.printStackTrace();
            } finally {
                try { serverSocket.close(); } catch(IOException unused) {}
            }
        } else {
            try {
                clientOutput = new PrintStream(
                    new BufferedOutputStream(clientSocket.getOutputStream()),
                                             false);
                clientInput = new BufferedInputStream(
                                             clientSocket.getInputStream());
                serviceRequest();
            } catch(Exception e) {
            } finally {
                try { close(); }  catch(IOException unused) {}
            }
        }
    }
    final public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port, 50);
        serverInstance = new Thread(this);
        serverInstance.start();
    }
    final public int getServerLocalPort() throws Exception {
        if (serverSocket != null) {
            return serverSocket.getLocalPort();
        }
        throw new Exception("serverSocket is null");
    }
    MessageHeader mh;
    final public void serviceRequest() {
        try {
            mh = new MessageHeader(clientInput);
            String cmd = mh.findValue(null);
            int fsp = cmd.indexOf(' ');
            String k = cmd.substring(0, fsp);
            int nsp = cmd.indexOf(' ', fsp + 1);
            String p1, p2;
            if (nsp > 0) {
                p1 = cmd.substring(fsp + 1, nsp);
                p2 = cmd.substring(nsp + 1);
            } else {
                p1 = cmd.substring(fsp + 1);
                p2 = null;
            }
            if (k.equalsIgnoreCase("get"))
                getRequest(new URL(defaultContext, p1), p2);
            else {
                return;
            }
        } catch(IOException e) {
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    protected void getRequest(URL u, String param) {
        try {
            if (u.getFile().equals("/echo.html")) {
               startHtml("Echo reply");
               clientOutput.print("<p>URL was " + u.toExternalForm() + "\n");
               clientOutput.print("<p>Socket was " + clientSocket + "\n<p><pre>");
               mh.print(clientOutput);
           }
        } catch(Exception e) {
            System.out.print("Failed on "+u.getFile()+"\n");
            e.printStackTrace();
        }
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    public myHttpServer () {
        try {
            defaultContext
            = new URL("http", InetAddress.getLocalHost().getHostName(), "/");
        } catch(Exception e) {
            System.out.println("Failed to construct defauit URL context: "
                               + e);
            e.printStackTrace();
        }
    }
    protected void startHtml(String title) {
        clientOutput.print("HTTP/1.0 200 Document follows\n" +
                           "Server: Java/" + getClass().getName() + "\n" +
                           "Content-type: text/plain; charset=Shift_JIS \n\n");
    }
}

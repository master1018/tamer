public class NetworkServer implements Runnable, Cloneable {
    public Socket clientSocket = null;
    private Thread serverInstance;
    private ServerSocket serverSocket;
    public PrintStream clientOutput;
    public InputStream clientInput;
    public void close() throws IOException {
        clientSocket.close();
        clientSocket = null;
        clientInput = null;
        clientOutput = null;
    }
    public boolean clientIsOpen() {
        return clientSocket != null;
    }
    final public void run() {
        if (serverSocket != null) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            while (true) {
                try {
                    Socket ns = serverSocket.accept();
                    NetworkServer n = (NetworkServer)clone();
                    n.serverSocket = null;
                    n.clientSocket = ns;
                    new Thread(n).start();
                } catch(Exception e) {
                    System.out.print("Server failure\n");
                    e.printStackTrace();
                    try {
                        serverSocket.close();
                    } catch(IOException e2) {}
                    System.out.print("cs="+serverSocket+"\n");
                    break;
                }
            }
        } else {
            try {
                clientOutput = new PrintStream(
                        new BufferedOutputStream(clientSocket.getOutputStream()),
                                               false, "ISO8859_1");
                clientInput = new BufferedInputStream(clientSocket.getInputStream());
                serviceRequest();
            } catch(Exception e) {
            }
            try {
                close();
            } catch(IOException e2) {}
        }
    }
    final public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port, 50);
        serverInstance = new Thread(this);
        serverInstance.start();
    }
    public void serviceRequest() throws IOException {
        byte buf[] = new byte[300];
        int n;
        clientOutput.print("Echo server " + getClass().getName() + "\n");
        clientOutput.flush();
        while ((n = clientInput.read(buf, 0, buf.length)) >= 0) {
            clientOutput.write(buf, 0, n);
        }
    }
    public static void main(String argv[]) {
        try {
            new NetworkServer ().startServer(8888);
        } catch (IOException e) {
            System.out.print("Server failed: "+e+"\n");
        }
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    public NetworkServer () {
    }
}

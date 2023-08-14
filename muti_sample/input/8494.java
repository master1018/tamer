public class FtpServer extends Thread {
    private ServerSocket listener = null;
    private FtpFileSystemHandler fsh = null;
    private FtpAuthHandler auth = null;
    private boolean done = false;
    private ArrayList<FtpCommandHandler> clients = new ArrayList<FtpCommandHandler>();
    public FtpServer(int port) throws IOException {
        listener = new ServerSocket(port);
    }
    public FtpServer() throws IOException {
        this(21);
    }
    public void setFileSystemHandler(FtpFileSystemHandler f) {
        fsh = f;
    }
    public void setAuthHandler(FtpAuthHandler a) {
        auth = a;
    }
    public void terminate() {
        done = true;
        interrupt();
    }
    public void killClients() {
        synchronized (clients) {
            int c = clients.size();
            while (c > 0) {
                c--;
                FtpCommandHandler cl = clients.get(c);
                cl.terminate();
                cl.interrupt();
            }
        }
    }
    public int getLocalPort() {
        return listener.getLocalPort();
    }
    void addClient(Socket client) {
        FtpCommandHandler h = new FtpCommandHandler(client, this);
        h.setHandlers(fsh, auth);
        synchronized (clients) {
            clients.add(h);
        }
        h.start();
    }
    void removeClient(FtpCommandHandler cl) {
        synchronized (clients) {
            clients.remove(cl);
        }
    }
    public int activeClientsCount() {
        synchronized (clients) {
            return clients.size();
        }
    }
    public void run() {
        Socket client;
        try {
            while (!done) {
                client = listener.accept();
                addClient(client);
            }
            listener.close();
        } catch (IOException e) {
        }
    }
}

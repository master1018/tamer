class OldSocketImpl extends SocketImpl  {
    public static void main(String[] args) throws Exception {
        Socket.setSocketImplFactory(new SocketImplFactory() {
                public SocketImpl createSocketImpl() {
                    return new OldSocketImpl();
                }
        });
        Socket socket = new Socket("localhost", 23);
    }
    public void setOption(int optID, Object value) throws SocketException { }
    public Object getOption(int optID) throws SocketException {
        return null;
    }
    protected void create(boolean stream) throws IOException { }
    protected void connect(String host, int port) throws IOException { }
    protected void connect(InetAddress address, int port) throws IOException { }
    protected void bind(InetAddress host, int port) throws IOException { }
    protected void listen(int backlog) throws IOException { }
    protected void accept(SocketImpl s) throws IOException { }
    protected InputStream getInputStream() throws IOException {
        return null;
    }
    protected OutputStream getOutputStream() throws IOException {
        return null;
    }
    protected int available() throws IOException {
        return 0;
    }
    protected void close() throws IOException { }
    protected void sendUrgentData (int data) throws SocketException { }
}

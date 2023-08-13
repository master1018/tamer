class HttpAwareServerSocket extends ServerSocket {
    public HttpAwareServerSocket(int port) throws IOException
    {
        super(port);
    }
    public HttpAwareServerSocket(int port, int backlog) throws IOException
    {
        super(port, backlog);
    }
    public Socket accept() throws IOException
    {
        Socket socket = super.accept();
        BufferedInputStream in =
            new BufferedInputStream(socket.getInputStream());
        RMIMasterSocketFactory.proxyLog.log(Log.BRIEF,
            "socket accepted (checking for POST)");
        in.mark(4);
        boolean isHttp = (in.read() == 'P') &&
                         (in.read() == 'O') &&
                         (in.read() == 'S') &&
                         (in.read() == 'T');
        in.reset();
        if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.BRIEF)) {
            RMIMasterSocketFactory.proxyLog.log(Log.BRIEF,
                (isHttp ? "POST found, HTTP socket returned" :
                          "POST not found, direct socket returned"));
        }
        if (isHttp)
            return new HttpReceiveSocket(socket, in, null);
        else
            return new WrappedSocket(socket, in, null);
    }
    public String toString()
    {
        return "HttpAware" + super.toString();
    }
}

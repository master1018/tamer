class WrappedSocket extends Socket {
    protected Socket socket;
    protected InputStream in = null;
    protected OutputStream out = null;
    public WrappedSocket(Socket socket, InputStream in, OutputStream out)
        throws IOException
    {
        super((java.net.SocketImpl)null);       
        this.socket = socket;
        this.in = in;
        this.out = out;
    }
    public InetAddress getInetAddress()
    {
        return socket.getInetAddress();
    }
    public InetAddress getLocalAddress() {
        return socket.getLocalAddress();
    }
    public int getPort()
    {
        return socket.getPort();
    }
    public int getLocalPort()
    {
        return socket.getLocalPort();
    }
    public InputStream getInputStream() throws IOException
    {
        if (in == null)
            in = socket.getInputStream();
        return in;
    }
    public OutputStream getOutputStream() throws IOException
    {
        if (out == null)
            out = socket.getOutputStream();
        return out;
    }
    public void setTcpNoDelay(boolean on) throws SocketException
    {
        socket.setTcpNoDelay(on);
    }
    public boolean getTcpNoDelay() throws SocketException
    {
        return socket.getTcpNoDelay();
    }
    public void setSoLinger(boolean on, int val) throws SocketException
    {
        socket.setSoLinger(on, val);
    }
    public int getSoLinger() throws SocketException
    {
        return socket.getSoLinger();
    }
    public synchronized void setSoTimeout(int timeout) throws SocketException
    {
        socket.setSoTimeout(timeout);
    }
    public synchronized int getSoTimeout() throws SocketException
    {
        return socket.getSoTimeout();
    }
    public synchronized void close() throws IOException
    {
        socket.close();
    }
    public String toString()
    {
        return "Wrapped" + socket.toString();
    }
}

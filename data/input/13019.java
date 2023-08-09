public class HttpReceiveSocket extends WrappedSocket implements RMISocketInfo {
    private boolean headerSent = false;
    public HttpReceiveSocket(Socket socket, InputStream in, OutputStream out)
        throws IOException
    {
        super(socket, in, out);
        this.in = new HttpInputStream(in != null ? in :
                                                   socket.getInputStream());
        this.out = (out != null ? out :
                    socket.getOutputStream());
    }
    public boolean isReusable()
    {
        return false;
    }
    public InetAddress getInetAddress() {
        return null;
    }
    public OutputStream getOutputStream() throws IOException
    {
        if (!headerSent) { 
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeBytes("HTTP/1.0 200 OK\r\n");
            dos.flush();
            headerSent = true;
            out = new HttpOutputStream(out);
        }
        return out;
    }
    public synchronized void close() throws IOException
    {
        getOutputStream().close(); 
        socket.close();
    }
    public String toString()
    {
        return "HttpReceive" + socket.toString();
    }
}

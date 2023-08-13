public class TCPConnection implements Connection {
    private Socket socket;
    private Channel channel;
    private InputStream in = null;
    private OutputStream out = null;
    private long expiration = Long.MAX_VALUE;
    private long lastuse = Long.MIN_VALUE;
    private long roundtrip = 5; 
    TCPConnection(TCPChannel ch, Socket s, InputStream in, OutputStream out)
    {
        socket   = s;
        channel  = ch;
        this.in  = in;
        this.out = out;
    }
    TCPConnection(TCPChannel ch, InputStream in, OutputStream out)
    {
        this(ch, null, in, out);
    }
    TCPConnection(TCPChannel ch, Socket s)
    {
        this(ch, s, null, null);
    }
    public OutputStream getOutputStream() throws IOException
    {
        if (out == null)
            out = new BufferedOutputStream(socket.getOutputStream());
        return out;
    }
    public void releaseOutputStream() throws IOException
    {
        if (out != null)
            out.flush();
    }
    public InputStream getInputStream() throws IOException
    {
        if (in == null)
            in = new BufferedInputStream(socket.getInputStream());
        return in;
    }
    public void releaseInputStream()
    {
    }
    public boolean isReusable()
    {
        if ((socket != null) && (socket instanceof RMISocketInfo))
            return ((RMISocketInfo) socket).isReusable();
        else
            return true;
    }
    void setExpiration(long time)
    {
        expiration = time;
    }
    void setLastUseTime(long time)
    {
        lastuse = time;
    }
    boolean expired(long time)
    {
        return expiration <= time;
    }
    public boolean isDead()
    {
        InputStream i;
        OutputStream o;
        long start = System.currentTimeMillis();
        if ((roundtrip > 0) && (start < lastuse + roundtrip))
            return (false);     
        try {
            i = getInputStream();
            o = getOutputStream();
        } catch (IOException e) {
            return (true);      
        }
        int response = 0;
        try {
            o.write(TransportConstants.Ping);
            o.flush();
            response = i.read();
        } catch (IOException ex) {
            TCPTransport.tcpLog.log(Log.VERBOSE, "exception: ", ex);
            TCPTransport.tcpLog.log(Log.BRIEF, "server ping failed");
            return (true);      
        }
        if (response == TransportConstants.PingAck) {
            roundtrip = (System.currentTimeMillis() - start) * 2;
            return (false);     
        }
        if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
            TCPTransport.tcpLog.log(Log.BRIEF,
                (response == -1 ? "server has been deactivated" :
                "server protocol error: ping response = " + response));
        }
        return (true);
    }
    public void close() throws IOException
    {
        TCPTransport.tcpLog.log(Log.BRIEF, "close connection");
        if (socket != null)
            socket.close();
        else {
            in.close();
            out.close();
        }
    }
    public Channel getChannel()
    {
        return channel;
    }
}

class HttpSendSocket extends Socket implements RMISocketInfo {
    protected String host;
    protected int port;
    protected URL url;
    protected URLConnection conn = null;
    protected InputStream in = null;
    protected OutputStream out = null;
    protected HttpSendInputStream inNotifier;
    protected HttpSendOutputStream outNotifier;
    private String lineSeparator =
        java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("line.separator"));
    public HttpSendSocket(String host, int port, URL url) throws IOException
    {
        super((SocketImpl)null);        
        if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.VERBOSE)) {
            RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE,
                "host = " + host + ", port = " + port + ", url = " + url);
        }
        this.host = host;
        this.port = port;
        this.url = url;
        inNotifier = new HttpSendInputStream(null, this);
        outNotifier = new HttpSendOutputStream(writeNotify(), this);
    }
    public HttpSendSocket(String host, int port) throws IOException
    {
        this(host, port, new URL("http", host, port, "/"));
    }
    public HttpSendSocket(InetAddress address, int port) throws IOException
    {
        this(address.getHostName(), port);
    }
    public boolean isReusable()
    {
        return false;
    }
    public synchronized OutputStream writeNotify() throws IOException
    {
        if (conn != null) {
            throw new IOException("attempt to write on HttpSendSocket after " +
                                  "request has been sent");
        }
        conn = url.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-type", "application/octet-stream");
        inNotifier.deactivate();
        in = null;
        return out = conn.getOutputStream();
    }
    public synchronized InputStream readNotify() throws IOException
    {
        RMIMasterSocketFactory.proxyLog.log(Log.VERBOSE,
            "sending request and activating input stream");
        outNotifier.deactivate();
        out.close();
        out = null;
        try {
            in = conn.getInputStream();
        } catch (IOException e) {
            RMIMasterSocketFactory.proxyLog.log(Log.BRIEF,
                "failed to get input stream, exception: ", e);
            throw new IOException("HTTP request failed");
        }
        String contentType = conn.getContentType();
        if (contentType == null ||
            !conn.getContentType().equals("application/octet-stream"))
        {
            if (RMIMasterSocketFactory.proxyLog.isLoggable(Log.BRIEF)) {
                String message;
                if (contentType == null) {
                    message = "missing content type in response" +
                        lineSeparator;
                } else {
                    message = "invalid content type in response: " +
                        contentType + lineSeparator;
                }
                message += "HttpSendSocket.readNotify: response body: ";
                try {
                    DataInputStream din = new DataInputStream(in);
                    String line;
                    while ((line = din.readLine()) != null)
                        message += line + lineSeparator;
                } catch (IOException e) {
                }
                RMIMasterSocketFactory.proxyLog.log(Log.BRIEF, message);
            }
            throw new IOException("HTTP request failed");
        }
        return in;
    }
    public InetAddress getInetAddress()
    {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return null;        
        }
    }
    public InetAddress getLocalAddress()
    {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;        
        }
    }
    public int getPort()
    {
        return port;
    }
    public int getLocalPort()
    {
        return -1;      
    }
    public InputStream getInputStream() throws IOException
    {
        return inNotifier;
    }
    public OutputStream getOutputStream() throws IOException
    {
        return outNotifier;
    }
    public void setTcpNoDelay(boolean on) throws SocketException
    {
    }
    public boolean getTcpNoDelay() throws SocketException
    {
        return false;   
    }
    public void setSoLinger(boolean on, int val) throws SocketException
    {
    }
    public int getSoLinger() throws SocketException
    {
        return -1;      
    }
    public synchronized void setSoTimeout(int timeout) throws SocketException
    {
    }
    public synchronized int getSoTimeout() throws SocketException
    {
        return 0;       
    }
    public synchronized void close() throws IOException
    {
        if (out != null) 
            out.close();
    }
    public String toString()
    {
        return "HttpSendSocket[host=" + host +
               ",port=" + port +
               ",url=" + url + "]";
    }
}

class HttpConnection {
    HttpContextImpl context;
    SSLEngine engine;
    SSLContext sslContext;
    SSLStreams sslStreams;
    InputStream i;
    InputStream raw;
    OutputStream rawout;
    SocketChannel chan;
    SelectionKey selectionKey;
    String protocol;
    long time;
    volatile long creationTime; 
    volatile long rspStartedTime; 
    int remaining;
    boolean closed = false;
    Logger logger;
    public enum State {IDLE, REQUEST, RESPONSE};
    volatile State state;
    public String toString() {
        String s = null;
        if (chan != null) {
            s = chan.toString();
        }
        return s;
    }
    HttpConnection () {
    }
    void setChannel (SocketChannel c) {
        chan = c;
    }
    void setContext (HttpContextImpl ctx) {
        context = ctx;
    }
    State getState() {
        return state;
    }
    void setState (State s) {
        state = s;
    }
    void setParameters (
        InputStream in, OutputStream rawout, SocketChannel chan,
        SSLEngine engine, SSLStreams sslStreams, SSLContext sslContext, String protocol,
        HttpContextImpl context, InputStream raw
    )
    {
        this.context = context;
        this.i = in;
        this.rawout = rawout;
        this.raw = raw;
        this.protocol = protocol;
        this.engine = engine;
        this.chan = chan;
        this.sslContext = sslContext;
        this.sslStreams = sslStreams;
        this.logger = context.getLogger();
    }
    SocketChannel getChannel () {
        return chan;
    }
    synchronized void close () {
        if (closed) {
            return;
        }
        closed = true;
        if (logger != null && chan != null) {
            logger.finest ("Closing connection: " + chan.toString());
        }
        if (!chan.isOpen()) {
            ServerImpl.dprint ("Channel already closed");
            return;
        }
        try {
            if (raw != null) {
                raw.close();
            }
        } catch (IOException e) {
            ServerImpl.dprint (e);
        }
        try {
            if (rawout != null) {
                rawout.close();
            }
        } catch (IOException e) {
            ServerImpl.dprint (e);
        }
        try {
            if (sslStreams != null) {
                sslStreams.close();
            }
        } catch (IOException e) {
            ServerImpl.dprint (e);
        }
        try {
            chan.close();
        } catch (IOException e) {
            ServerImpl.dprint (e);
        }
    }
    void setRemaining (int r) {
        remaining = r;
    }
    int getRemaining () {
        return remaining;
    }
    SelectionKey getSelectionKey () {
        return selectionKey;
    }
    InputStream getInputStream () {
            return i;
    }
    OutputStream getRawOutputStream () {
            return rawout;
    }
    String getProtocol () {
            return protocol;
    }
    SSLEngine getSSLEngine () {
            return engine;
    }
    SSLContext getSSLContext () {
            return sslContext;
    }
    HttpContextImpl getHttpContext () {
            return context;
    }
}

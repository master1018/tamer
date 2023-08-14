class SSLStreams {
    SSLContext sslctx;
    SocketChannel chan;
    TimeSource time;
    ServerImpl server;
    SSLEngine engine;
    EngineWrapper wrapper;
    OutputStream os;
    InputStream is;
    Lock handshaking = new ReentrantLock();
    SSLStreams (ServerImpl server, SSLContext sslctx, SocketChannel chan) throws IOException {
        this.server = server;
        this.time= (TimeSource)server;
        this.sslctx= sslctx;
        this.chan= chan;
        InetSocketAddress addr =
                (InetSocketAddress)chan.socket().getRemoteSocketAddress();
        engine = sslctx.createSSLEngine (addr.getHostName(), addr.getPort());
        engine.setUseClientMode (false);
        HttpsConfigurator cfg = server.getHttpsConfigurator();
        configureEngine (cfg, addr);
        wrapper = new EngineWrapper (chan, engine);
    }
    private void configureEngine(HttpsConfigurator cfg, InetSocketAddress addr){
        if (cfg != null) {
            Parameters params = new Parameters (cfg, addr);
            cfg.configure (params);
            SSLParameters sslParams = params.getSSLParameters();
            if (sslParams != null) {
                engine.setSSLParameters (sslParams);
            } else
            {
                if (params.getCipherSuites() != null) {
                    try {
                        engine.setEnabledCipherSuites (
                            params.getCipherSuites()
                        );
                    } catch (IllegalArgumentException e) { }
                }
                engine.setNeedClientAuth (params.getNeedClientAuth());
                engine.setWantClientAuth (params.getWantClientAuth());
                if (params.getProtocols() != null) {
                    try {
                        engine.setEnabledProtocols (
                            params.getProtocols()
                        );
                    } catch (IllegalArgumentException e) { }
                }
            }
        }
    }
    class Parameters extends HttpsParameters {
        InetSocketAddress addr;
        HttpsConfigurator cfg;
        Parameters (HttpsConfigurator cfg, InetSocketAddress addr) {
            this.addr = addr;
            this.cfg = cfg;
        }
        public InetSocketAddress getClientAddress () {
            return addr;
        }
        public HttpsConfigurator getHttpsConfigurator() {
            return cfg;
        }
        SSLParameters params;
        public void setSSLParameters (SSLParameters p) {
            params = p;
        }
        SSLParameters getSSLParameters () {
            return params;
        }
    }
    void close () throws IOException {
        wrapper.close();
    }
    InputStream getInputStream () throws IOException {
        if (is == null) {
            is = new InputStream();
        }
        return is;
    }
    OutputStream getOutputStream () throws IOException {
        if (os == null) {
            os = new OutputStream();
        }
        return os;
    }
    SSLEngine getSSLEngine () {
        return engine;
    }
    void beginHandshake() throws SSLException {
        engine.beginHandshake();
    }
    class WrapperResult {
        SSLEngineResult result;
        ByteBuffer buf;
    }
    int app_buf_size;
    int packet_buf_size;
    enum BufType {
        PACKET, APPLICATION
    };
    private ByteBuffer allocate (BufType type) {
        return allocate (type, -1);
    }
    private ByteBuffer allocate (BufType type, int len) {
        assert engine != null;
        synchronized (this) {
            int size;
            if (type == BufType.PACKET) {
                if (packet_buf_size == 0) {
                    SSLSession sess = engine.getSession();
                    packet_buf_size = sess.getPacketBufferSize();
                }
                if (len > packet_buf_size) {
                    packet_buf_size = len;
                }
                size = packet_buf_size;
            } else {
                if (app_buf_size == 0) {
                    SSLSession sess = engine.getSession();
                    app_buf_size = sess.getApplicationBufferSize();
                }
                if (len > app_buf_size) {
                    app_buf_size = len;
                }
                size = app_buf_size;
            }
            return ByteBuffer.allocate (size);
        }
    }
    private ByteBuffer realloc (ByteBuffer b, boolean flip, BufType type) {
        synchronized (this) {
            int nsize = 2 * b.capacity();
            ByteBuffer n = allocate (type, nsize);
            if (flip) {
                b.flip();
            }
            n.put(b);
            b = n;
        }
        return b;
    }
    class EngineWrapper {
        SocketChannel chan;
        SSLEngine engine;
        Object wrapLock, unwrapLock;
        ByteBuffer unwrap_src, wrap_dst;
        boolean closed = false;
        int u_remaining; 
        EngineWrapper (SocketChannel chan, SSLEngine engine) throws IOException {
            this.chan = chan;
            this.engine = engine;
            wrapLock = new Object();
            unwrapLock = new Object();
            unwrap_src = allocate(BufType.PACKET);
            wrap_dst = allocate(BufType.PACKET);
        }
        void close () throws IOException {
        }
        WrapperResult wrapAndSend(ByteBuffer src) throws IOException {
            return wrapAndSendX(src, false);
        }
        WrapperResult wrapAndSendX(ByteBuffer src, boolean ignoreClose) throws IOException {
            if (closed && !ignoreClose) {
                throw new IOException ("Engine is closed");
            }
            Status status;
            WrapperResult r = new WrapperResult();
            synchronized (wrapLock) {
                wrap_dst.clear();
                do {
                    r.result = engine.wrap (src, wrap_dst);
                    status = r.result.getStatus();
                    if (status == Status.BUFFER_OVERFLOW) {
                        wrap_dst = realloc (wrap_dst, true, BufType.PACKET);
                    }
                } while (status == Status.BUFFER_OVERFLOW);
                if (status == Status.CLOSED && !ignoreClose) {
                    closed = true;
                    return r;
                }
                if (r.result.bytesProduced() > 0) {
                    wrap_dst.flip();
                    int l = wrap_dst.remaining();
                    assert l == r.result.bytesProduced();
                    while (l>0) {
                        l -= chan.write (wrap_dst);
                    }
                }
            }
            return r;
        }
        WrapperResult recvAndUnwrap(ByteBuffer dst) throws IOException {
            Status status = Status.OK;
            WrapperResult r = new WrapperResult();
            r.buf = dst;
            if (closed) {
                throw new IOException ("Engine is closed");
            }
            boolean needData;
            if (u_remaining > 0) {
                unwrap_src.compact();
                unwrap_src.flip();
                needData = false;
            } else {
                unwrap_src.clear();
                needData = true;
            }
            synchronized (unwrapLock) {
                int x;
                do {
                    if (needData) {
                        do {
                        x = chan.read (unwrap_src);
                        } while (x == 0);
                        if (x == -1) {
                            throw new IOException ("connection closed for reading");
                        }
                        unwrap_src.flip();
                    }
                    r.result = engine.unwrap (unwrap_src, r.buf);
                    status = r.result.getStatus();
                    if (status == Status.BUFFER_UNDERFLOW) {
                        if (unwrap_src.limit() == unwrap_src.capacity()) {
                            unwrap_src = realloc (
                                unwrap_src, false, BufType.PACKET
                            );
                        } else {
                            unwrap_src.position (unwrap_src.limit());
                            unwrap_src.limit (unwrap_src.capacity());
                        }
                        needData = true;
                    } else if (status == Status.BUFFER_OVERFLOW) {
                        r.buf = realloc (r.buf, true, BufType.APPLICATION);
                        needData = false;
                    } else if (status == Status.CLOSED) {
                        closed = true;
                        r.buf.flip();
                        return r;
                    }
                } while (status != Status.OK);
            }
            u_remaining = unwrap_src.remaining();
            return r;
        }
    }
    public WrapperResult sendData (ByteBuffer src) throws IOException {
        WrapperResult r=null;
        while (src.remaining() > 0) {
            r = wrapper.wrapAndSend(src);
            Status status = r.result.getStatus();
            if (status == Status.CLOSED) {
                doClosure ();
                return r;
            }
            HandshakeStatus hs_status = r.result.getHandshakeStatus();
            if (hs_status != HandshakeStatus.FINISHED &&
                hs_status != HandshakeStatus.NOT_HANDSHAKING)
            {
                doHandshake(hs_status);
            }
        }
        return r;
    }
    public WrapperResult recvData (ByteBuffer dst) throws IOException {
        WrapperResult r = null;
        assert dst.position() == 0;
        while (dst.position() == 0) {
            r = wrapper.recvAndUnwrap (dst);
            dst = (r.buf != dst) ? r.buf: dst;
            Status status = r.result.getStatus();
            if (status == Status.CLOSED) {
                doClosure ();
                return r;
            }
            HandshakeStatus hs_status = r.result.getHandshakeStatus();
            if (hs_status != HandshakeStatus.FINISHED &&
                hs_status != HandshakeStatus.NOT_HANDSHAKING)
            {
                doHandshake (hs_status);
            }
        }
        dst.flip();
        return r;
    }
    void doClosure () throws IOException {
        try {
            handshaking.lock();
            ByteBuffer tmp = allocate(BufType.APPLICATION);
            WrapperResult r;
            do {
                tmp.clear();
                tmp.flip ();
                r = wrapper.wrapAndSendX (tmp, true);
            } while (r.result.getStatus() != Status.CLOSED);
        } finally {
            handshaking.unlock();
        }
    }
    void doHandshake (HandshakeStatus hs_status) throws IOException {
        try {
            handshaking.lock();
            ByteBuffer tmp = allocate(BufType.APPLICATION);
            while (hs_status != HandshakeStatus.FINISHED &&
                   hs_status != HandshakeStatus.NOT_HANDSHAKING)
            {
                WrapperResult r = null;
                switch (hs_status) {
                    case NEED_TASK:
                        Runnable task;
                        while ((task = engine.getDelegatedTask()) != null) {
                            task.run();
                        }
                    case NEED_WRAP:
                        tmp.clear();
                        tmp.flip();
                        r = wrapper.wrapAndSend(tmp);
                        break;
                    case NEED_UNWRAP:
                        tmp.clear();
                        r = wrapper.recvAndUnwrap (tmp);
                        if (r.buf != tmp) {
                            tmp = r.buf;
                        }
                        assert tmp.position() == 0;
                        break;
                }
                hs_status = r.result.getHandshakeStatus();
            }
        } finally {
            handshaking.unlock();
        }
    }
    class InputStream extends java.io.InputStream {
        ByteBuffer bbuf;
        boolean closed = false;
        boolean eof = false;
        boolean needData = true;
        InputStream () {
            bbuf = allocate (BufType.APPLICATION);
        }
        public int read (byte[] buf, int off, int len) throws IOException {
            if (closed) {
                throw new IOException ("SSL stream is closed");
            }
            if (eof) {
                return 0;
            }
            int available=0;
            if (!needData) {
                available = bbuf.remaining();
                needData = (available==0);
            }
            if (needData) {
                bbuf.clear();
                WrapperResult r = recvData (bbuf);
                bbuf = r.buf== bbuf? bbuf: r.buf;
                if ((available=bbuf.remaining()) == 0) {
                    eof = true;
                    return 0;
                } else {
                    needData = false;
                }
            }
            if (len > available) {
                len = available;
            }
            bbuf.get (buf, off, len);
            return len;
        }
        public int available () throws IOException {
            return bbuf.remaining();
        }
        public boolean markSupported () {
            return false; 
        }
        public void reset () throws IOException {
            throw new IOException ("mark/reset not supported");
        }
        public long skip (long s) throws IOException {
            int n = (int)s;
            if (closed) {
                throw new IOException ("SSL stream is closed");
            }
            if (eof) {
                return 0;
            }
            int ret = n;
            while (n > 0) {
                if (bbuf.remaining() >= n) {
                    bbuf.position (bbuf.position()+n);
                    return ret;
                } else {
                    n -= bbuf.remaining();
                    bbuf.clear();
                    WrapperResult r = recvData (bbuf);
                    bbuf = r.buf==bbuf? bbuf: r.buf;
                }
            }
            return ret; 
        }
        public void close () throws IOException {
            eof = true;
            engine.closeInbound ();
        }
        public int read (byte[] buf) throws IOException {
            return read (buf, 0, buf.length);
        }
        byte single[] = new byte [1];
        public int read () throws IOException {
            int n = read (single, 0, 1);
            if (n == 0) {
                return -1;
            } else {
                return single[0] & 0xFF;
            }
        }
    }
    class OutputStream extends java.io.OutputStream {
        ByteBuffer buf;
        boolean closed = false;
        byte single[] = new byte[1];
        OutputStream() {
            buf = allocate(BufType.APPLICATION);
        }
        public void write(int b) throws IOException {
            single[0] = (byte)b;
            write (single, 0, 1);
        }
        public void write(byte b[]) throws IOException {
            write (b, 0, b.length);
        }
        public void write(byte b[], int off, int len) throws IOException {
            if (closed) {
                throw new IOException ("output stream is closed");
            }
            while (len > 0) {
                int l = len > buf.capacity() ? buf.capacity() : len;
                buf.clear();
                buf.put (b, off, l);
                len -= l;
                off += l;
                buf.flip();
                WrapperResult r = sendData (buf);
                if (r.result.getStatus() == Status.CLOSED) {
                    closed = true;
                    if (len > 0) {
                        throw new IOException ("output stream is closed");
                    }
                }
            }
        }
        public void flush() throws IOException {
        }
        public void close() throws IOException {
            WrapperResult r=null;
            engine.closeOutbound();
            closed = true;
            HandshakeStatus stat = HandshakeStatus.NEED_WRAP;
            buf.clear();
            while (stat == HandshakeStatus.NEED_WRAP) {
                r = wrapper.wrapAndSend (buf);
                stat = r.result.getHandshakeStatus();
            }
            assert r.result.getStatus() == Status.CLOSED;
        }
    }
}

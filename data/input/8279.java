class ChannelIOSecure extends ChannelIO {
    private SSLEngine sslEngine = null;
    private int appBBSize;
    private int netBBSize;
    private ByteBuffer inNetBB;
    private ByteBuffer outNetBB;
    private static ByteBuffer hsBB = ByteBuffer.allocate(0);
    private ByteBuffer fileChannelBB = null;
    private HandshakeStatus initialHSStatus;
    private boolean initialHSComplete;
    private boolean shutdown = false;
    protected ChannelIOSecure(SocketChannel sc, boolean blocking,
            SSLContext sslc) throws IOException {
        super(sc, blocking);
        sslEngine = sslc.createSSLEngine();
        sslEngine.setUseClientMode(false);
        initialHSStatus = HandshakeStatus.NEED_UNWRAP;
        initialHSComplete = false;
        netBBSize = sslEngine.getSession().getPacketBufferSize();
        inNetBB = ByteBuffer.allocate(netBBSize);
        outNetBB = ByteBuffer.allocate(netBBSize);
        outNetBB.position(0);
        outNetBB.limit(0);
    }
    static ChannelIOSecure getInstance(SocketChannel sc, boolean blocking,
            SSLContext sslc) throws IOException {
        ChannelIOSecure cio = new ChannelIOSecure(sc, blocking, sslc);
        cio.appBBSize = cio.sslEngine.getSession().getApplicationBufferSize();
        cio.requestBB = ByteBuffer.allocate(cio.appBBSize);
        return cio;
    }
    protected void resizeRequestBB() {
        resizeRequestBB(appBBSize);
    }
    private void resizeResponseBB() {
        ByteBuffer bb = ByteBuffer.allocate(netBBSize);
        inNetBB.flip();
        bb.put(inNetBB);
        inNetBB = bb;
    }
    private boolean tryFlush(ByteBuffer bb) throws IOException {
        super.write(bb);
        return !bb.hasRemaining();
    }
    boolean doHandshake() throws IOException {
        return doHandshake(null);
    }
    boolean doHandshake(SelectionKey sk) throws IOException {
        SSLEngineResult result;
        if (initialHSComplete) {
            return initialHSComplete;
        }
        if (outNetBB.hasRemaining()) {
            if (!tryFlush(outNetBB)) {
                return false;
            }
            switch (initialHSStatus) {
            case FINISHED:
                initialHSComplete = true;
            case NEED_UNWRAP:
                if (sk != null) {
                    sk.interestOps(SelectionKey.OP_READ);
                }
                break;
            }
            return initialHSComplete;
        }
        switch (initialHSStatus) {
        case NEED_UNWRAP:
            if (sc.read(inNetBB) == -1) {
                sslEngine.closeInbound();
                return initialHSComplete;
            }
needIO:
            while (initialHSStatus == HandshakeStatus.NEED_UNWRAP) {
                resizeRequestBB();    
                inNetBB.flip();
                result = sslEngine.unwrap(inNetBB, requestBB);
                inNetBB.compact();
                initialHSStatus = result.getHandshakeStatus();
                switch (result.getStatus()) {
                case OK:
                    switch (initialHSStatus) {
                    case NOT_HANDSHAKING:
                        throw new IOException(
                            "Not handshaking during initial handshake");
                    case NEED_TASK:
                        initialHSStatus = doTasks();
                        break;
                    case FINISHED:
                        initialHSComplete = true;
                        break needIO;
                    }
                    break;
                case BUFFER_UNDERFLOW:
                    netBBSize = sslEngine.getSession().getPacketBufferSize();
                    if (netBBSize > inNetBB.capacity()) {
                        resizeResponseBB();
                    }
                    if (sk != null) {
                        sk.interestOps(SelectionKey.OP_READ);
                    }
                    break needIO;
                case BUFFER_OVERFLOW:
                    appBBSize =
                        sslEngine.getSession().getApplicationBufferSize();
                    break;
                default: 
                    throw new IOException("Received" + result.getStatus() +
                        "during initial handshaking");
                }
            }  
            if (initialHSStatus != HandshakeStatus.NEED_WRAP) {
                break;
            }
        case NEED_WRAP:
            outNetBB.clear();
            result = sslEngine.wrap(hsBB, outNetBB);
            outNetBB.flip();
            initialHSStatus = result.getHandshakeStatus();
            switch (result.getStatus()) {
            case OK:
                if (initialHSStatus == HandshakeStatus.NEED_TASK) {
                    initialHSStatus = doTasks();
                }
                if (sk != null) {
                    sk.interestOps(SelectionKey.OP_WRITE);
                }
                break;
            default: 
                throw new IOException("Received" + result.getStatus() +
                        "during initial handshaking");
            }
            break;
        default: 
            throw new RuntimeException("Invalid Handshaking State" +
                    initialHSStatus);
        } 
        return initialHSComplete;
    }
    private SSLEngineResult.HandshakeStatus doTasks() {
        Runnable runnable;
        while ((runnable = sslEngine.getDelegatedTask()) != null) {
            runnable.run();
        }
        return sslEngine.getHandshakeStatus();
    }
    int read() throws IOException {
        SSLEngineResult result;
        if (!initialHSComplete) {
            throw new IllegalStateException();
        }
        int pos = requestBB.position();
        if (sc.read(inNetBB) == -1) {
            sslEngine.closeInbound();  
            return -1;
        }
        do {
            resizeRequestBB();    
            inNetBB.flip();
            result = sslEngine.unwrap(inNetBB, requestBB);
            inNetBB.compact();
            switch (result.getStatus()) {
            case BUFFER_OVERFLOW:
                appBBSize = sslEngine.getSession().getApplicationBufferSize();
                break;
            case BUFFER_UNDERFLOW:
                netBBSize = sslEngine.getSession().getPacketBufferSize();
                if (netBBSize > inNetBB.capacity()) {
                    resizeResponseBB();
                    break; 
                }
            case OK:
                if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                    doTasks();
                }
                break;
            default:
                throw new IOException("sslEngine error during data read: " +
                    result.getStatus());
            }
        } while ((inNetBB.position() != 0) &&
            result.getStatus() != Status.BUFFER_UNDERFLOW);
        return (requestBB.position() - pos);
    }
    int write(ByteBuffer src) throws IOException {
        if (!initialHSComplete) {
            throw new IllegalStateException();
        }
        return doWrite(src);
    }
    private int doWrite(ByteBuffer src) throws IOException {
        int retValue = 0;
        if (outNetBB.hasRemaining() && !tryFlush(outNetBB)) {
            return retValue;
        }
        outNetBB.clear();
        SSLEngineResult result = sslEngine.wrap(src, outNetBB);
        retValue = result.bytesConsumed();
        outNetBB.flip();
        switch (result.getStatus()) {
        case OK:
            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                doTasks();
            }
            break;
        default:
            throw new IOException("sslEngine error during data write: " +
                result.getStatus());
        }
        if (outNetBB.hasRemaining()) {
            tryFlush(outNetBB);
        }
        return retValue;
    }
    long transferTo(FileChannel fc, long pos, long len) throws IOException {
        if (!initialHSComplete) {
            throw new IllegalStateException();
        }
        if (fileChannelBB == null) {
            fileChannelBB = ByteBuffer.allocate(appBBSize);
            fileChannelBB.limit(0);
        }
        fileChannelBB.compact();
        int fileRead = fc.read(fileChannelBB);
        fileChannelBB.flip();
        doWrite(fileChannelBB);
        return fileRead;
    }
    boolean dataFlush() throws IOException {
        boolean fileFlushed = true;
        if ((fileChannelBB != null) && fileChannelBB.hasRemaining()) {
            doWrite(fileChannelBB);
            fileFlushed = !fileChannelBB.hasRemaining();
        } else if (outNetBB.hasRemaining()) {
            tryFlush(outNetBB);
        }
        return (fileFlushed && !outNetBB.hasRemaining());
    }
    boolean shutdown() throws IOException {
        if (!shutdown) {
            sslEngine.closeOutbound();
            shutdown = true;
        }
        if (outNetBB.hasRemaining() && tryFlush(outNetBB)) {
            return false;
        }
        outNetBB.clear();
        SSLEngineResult result = sslEngine.wrap(hsBB, outNetBB);
        if (result.getStatus() != Status.CLOSED) {
            throw new SSLException("Improper close state");
        }
        outNetBB.flip();
        if (outNetBB.hasRemaining()) {
            tryFlush(outNetBB);
        }
        return (!outNetBB.hasRemaining() &&
                (result.getHandshakeStatus() != HandshakeStatus.NEED_WRAP));
    }
}

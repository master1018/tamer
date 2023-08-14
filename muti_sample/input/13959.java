public class TunnelProxy {
    ServerSocketChannel schan;
    int threads;
    int cperthread;
    Server[] servers;
    public TunnelProxy () throws IOException {
        this (1, 10, 0);
    }
    public TunnelProxy (int threads, int cperthread)
        throws IOException {
        this (threads, cperthread, 0);
    }
    public TunnelProxy (int threads, int cperthread, int port)
        throws IOException {
        schan = ServerSocketChannel.open ();
        InetSocketAddress addr = new InetSocketAddress (port);
        schan.socket().bind (addr);
        this.threads = threads;
        this.cperthread = cperthread;
        servers = new Server [threads];
        for (int i=0; i<threads; i++) {
            servers[i] = new Server (schan, cperthread);
            servers[i].start();
        }
    }
    public void terminate () {
        for (int i=0; i<threads; i++) {
            servers[i].terminate ();
        }
    }
    public int getLocalPort () {
        return schan.socket().getLocalPort ();
    }
    static class Server extends Thread {
        ServerSocketChannel schan;
        Selector selector;
        SelectionKey listenerKey;
        SelectionKey key; 
        ByteBuffer consumeBuffer;
        int maxconn;
        int nconn;
        ClosedChannelList clist;
        boolean shutdown;
        Pipeline pipe1 = null;
        Pipeline pipe2 = null;
        Server (ServerSocketChannel schan, int maxconn) {
            this.schan = schan;
            this.maxconn = maxconn;
            nconn = 0;
            consumeBuffer = ByteBuffer.allocate (512);
            clist = new ClosedChannelList ();
            try {
                selector = Selector.open ();
                schan.configureBlocking (false);
                listenerKey = schan.register (selector, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                System.err.println ("Server could not start: " + e);
            }
        }
        public synchronized void terminate () {
            shutdown = true;
            if (pipe1 != null) pipe1.terminate();
            if (pipe2 != null) pipe2.terminate();
        }
        public void run ()  {
            try {
                while (true) {
                    selector.select (1000);
                    Set selected = selector.selectedKeys();
                    Iterator iter = selected.iterator();
                    while (iter.hasNext()) {
                        key = (SelectionKey)iter.next();
                        if (key.equals (listenerKey)) {
                            SocketChannel sock = schan.accept ();
                            if (sock == null) {
                                iter.remove();
                                continue;
                            }
                            sock.configureBlocking (false);
                            sock.register (selector, SelectionKey.OP_READ);
                            nconn ++;
                            if (nconn == maxconn) {
                                listenerKey.cancel ();
                                listenerKey = null;
                            }
                        } else {
                            if (key.isReadable()) {
                                boolean closed;
                                SocketChannel chan = (SocketChannel) key.channel();
                                if (key.attachment() != null) {
                                    closed = consume (chan);
                                } else {
                                    closed = read (chan, key);
                                }
                                if (closed) {
                                    chan.close ();
                                    key.cancel ();
                                    if (nconn == maxconn) {
                                        listenerKey = schan.register (selector, SelectionKey.OP_ACCEPT);
                                    }
                                    nconn --;
                                }
                            }
                        }
                        iter.remove();
                    }
                    clist.check();
                    if (shutdown) {
                        clist.terminate ();
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println ("Server exception: " + e);
            }
        }
        boolean consume (SocketChannel chan) {
            try {
                consumeBuffer.clear ();
                int c = chan.read (consumeBuffer);
                if (c == -1)
                    return true;
            } catch (IOException e) {
                return true;
            }
            return false;
        }
        private boolean read (SocketChannel chan, SelectionKey key) {
            HttpTransaction msg;
            boolean res;
            try {
                InputStream is = new BufferedInputStream (new NioInputStream (chan));
                String requestline = readLine (is);
                MessageHeader mhead = new MessageHeader (is);
                String[] req = requestline.split (" ");
                if (req.length < 2) {
                    return false;
                }
                String cmd = req[0];
                URI uri = null;
                if (!("CONNECT".equalsIgnoreCase(cmd))) {
                    return false;
                }
                try {
                    uri = new URI("http:
                } catch (URISyntaxException e) {
                    System.err.println ("Invalid URI: " + e);
                    res = true;
                }
                OutputStream os = new BufferedOutputStream(new NioOutputStream(chan));
                byte[] ack = "HTTP/1.1 200 Connection established\r\n\r\n".getBytes();
                os.write(ack, 0, ack.length);
                os.flush();
                tunnel(is, os, uri);
                res = false;
            } catch (IOException e) {
                res = true;
            }
            return res;
        }
        private void tunnel(InputStream fromClient, OutputStream toClient, URI serverURI) throws IOException {
            Socket sockToServer = new Socket(serverURI.getHost(), serverURI.getPort());
            OutputStream toServer = sockToServer.getOutputStream();
            InputStream fromServer = sockToServer.getInputStream();
            pipe1 = new Pipeline(fromClient, toServer);
            pipe2 = new Pipeline(fromServer, toClient);
            pipe1.start();
            pipe2.start();
            try {
                pipe1.join();
            } catch (InterruptedException e) {
            } finally {
                sockToServer.close();
            }
        }
        private String readLine (InputStream is) throws IOException {
            boolean done=false, readCR=false;
            byte[] b = new byte [512];
            int c, l = 0;
            while (!done) {
                c = is.read ();
                if (c == '\n' && readCR) {
                    done = true;
                } else {
                    if (c == '\r' && !readCR) {
                        readCR = true;
                    } else {
                        b[l++] = (byte)c;
                    }
                }
            }
            return new String (b);
        }
        synchronized void orderlyCloseChannel (SelectionKey key) throws IOException {
            SocketChannel ch = (SocketChannel)key.channel ();
            ch.socket().shutdownOutput();
            key.attach (this);
            clist.add (key);
        }
        synchronized void abortiveCloseChannel (SelectionKey key) throws IOException {
            SocketChannel ch = (SocketChannel)key.channel ();
            Socket s = ch.socket ();
            s.setSoLinger (true, 0);
            ch.close();
        }
    }
    static class NioInputStream extends InputStream {
        SocketChannel channel;
        Selector selector;
        ByteBuffer chanbuf;
        SelectionKey key;
        int available;
        byte[] one;
        boolean closed;
        ByteBuffer markBuf; 
        boolean marked;
        boolean reset;
        int readlimit;
        public NioInputStream (SocketChannel chan) throws IOException {
            this.channel = chan;
            selector = Selector.open();
            chanbuf = ByteBuffer.allocate (1024);
            key = chan.register (selector, SelectionKey.OP_READ);
            available = 0;
            one = new byte[1];
            closed = marked = reset = false;
        }
        public synchronized int read (byte[] b) throws IOException {
            return read (b, 0, b.length);
        }
        public synchronized int read () throws IOException {
            return read (one, 0, 1);
        }
        public synchronized int read (byte[] b, int off, int srclen) throws IOException {
            int canreturn, willreturn;
            if (closed)
                return -1;
            if (reset) { 
                canreturn = markBuf.remaining ();
                willreturn = canreturn>srclen ? srclen : canreturn;
                markBuf.get(b, off, willreturn);
                if (canreturn == willreturn) {
                    reset = false;
                }
            } else { 
                canreturn = available();
                if (canreturn == 0) {
                    block ();
                    canreturn = available();
                }
                willreturn = canreturn>srclen ? srclen : canreturn;
                chanbuf.get(b, off, willreturn);
                available -= willreturn;
                if (marked) { 
                    try {
                        markBuf.put (b, off, willreturn);
                    } catch (BufferOverflowException e) {
                        marked = false;
                    }
                }
            }
            return willreturn;
        }
        public synchronized int available () throws IOException {
            if (closed)
                throw new IOException ("Stream is closed");
            if (reset)
                return markBuf.remaining();
            if (available > 0)
                return available;
            chanbuf.clear ();
            available = channel.read (chanbuf);
            if (available > 0)
                chanbuf.flip();
            else if (available == -1)
                throw new IOException ("Stream is closed");
            return available;
        }
        private synchronized void block () throws IOException {
            int n = selector.select ();
            selector.selectedKeys().clear();
            available ();
        }
        public void close () throws IOException {
            if (closed)
                return;
            channel.close ();
            closed = true;
        }
        public synchronized void mark (int readlimit) {
            if (closed)
                return;
            this.readlimit = readlimit;
            markBuf = ByteBuffer.allocate (readlimit);
            marked = true;
            reset = false;
        }
        public synchronized void reset () throws IOException {
            if (closed )
                return;
            if (!marked)
                throw new IOException ("Stream not marked");
            marked = false;
            reset = true;
            markBuf.flip ();
        }
    }
    static class NioOutputStream extends OutputStream {
        SocketChannel channel;
        ByteBuffer buf;
        SelectionKey key;
        Selector selector;
        boolean closed;
        byte[] one;
        public NioOutputStream (SocketChannel channel) throws IOException {
            this.channel = channel;
            selector = Selector.open ();
            key = channel.register (selector, SelectionKey.OP_WRITE);
            closed = false;
            one = new byte [1];
        }
        public synchronized void write (int b) throws IOException {
            one[0] = (byte)b;
            write (one, 0, 1);
        }
        public synchronized void write (byte[] b) throws IOException {
            write (b, 0, b.length);
        }
        public synchronized void write (byte[] b, int off, int len) throws IOException {
            if (closed)
                throw new IOException ("stream is closed");
            buf = ByteBuffer.allocate (len);
            buf.put (b, off, len);
            buf.flip ();
            int n;
            while ((n = channel.write (buf)) < len) {
                len -= n;
                if (len == 0)
                    return;
                selector.select ();
                selector.selectedKeys().clear ();
            }
        }
        public void close () throws IOException {
            if (closed)
                return;
            channel.close ();
            closed = true;
        }
    }
    static class Pipeline implements Runnable {
        InputStream in;
        OutputStream out;
        Thread t;
        public Pipeline(InputStream is, OutputStream os) {
            in = is;
            out = os;
        }
        public void start() {
            t = new Thread(this);
            t.start();
        }
        public void join() throws InterruptedException {
            t.join();
        }
        public void terminate() {
            t.interrupt();
        }
        public void run() {
            byte[] buffer = new byte[10000];
            try {
                while (!Thread.interrupted()) {
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                }
            } catch(IOException e) {
            } finally {
            }
        }
    }
    private static HashMap conditions = new HashMap();
    private static class BValue {
        boolean v;
    }
    private static class IValue {
        int v;
        IValue (int i) {
            v =i;
        }
    }
    private static BValue getCond (String condition) {
        synchronized (conditions) {
            BValue cond = (BValue) conditions.get (condition);
            if (cond == null) {
                cond = new BValue();
                conditions.put (condition, cond);
            }
            return cond;
        }
    }
    public static void setCondition (String condition) {
        BValue cond = getCond (condition);
        synchronized (cond) {
            if (cond.v) {
                return;
            }
            cond.v = true;
            cond.notifyAll();
        }
    }
    public static void waitForCondition (String condition) {
        BValue cond = getCond (condition);
        synchronized (cond) {
            if (!cond.v) {
                try {
                    cond.wait();
                } catch (InterruptedException e) {}
            }
        }
    }
    static HashMap rv = new HashMap();
    public static void rendezvous (String condition, int N) {
        BValue cond;
        IValue iv;
        String name = "RV_"+condition;
        synchronized (conditions) {
            cond = (BValue)conditions.get (name);
            if (cond == null) {
                if (N < 2) {
                    throw new RuntimeException ("rendezvous must be called with N >= 2");
                }
                cond = new BValue ();
                conditions.put (name, cond);
                iv = new IValue (N-1);
                rv.put (name, iv);
            } else {
                iv = (IValue) rv.get (name);
                iv.v --;
            }
        }
        if (iv.v > 0) {
            waitForCondition (name);
        } else {
            setCondition (name);
            synchronized (conditions) {
                clearCondition (name);
                rv.remove (name);
            }
        }
    }
    public static void clearCondition(String condition) {
        BValue cond;
        synchronized (conditions) {
            cond = (BValue) conditions.get (condition);
            if (cond == null) {
                return;
            }
            synchronized (cond) {
                if (cond.v) {
                    conditions.remove (condition);
                }
            }
        }
    }
}

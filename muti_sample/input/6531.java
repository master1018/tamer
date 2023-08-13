public class B6361557 {
    public static boolean error = false;
    static final int NUM = 1000;
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers rmap = t.getResponseHeaders();
            while (is.read () != -1) ;
            is.close();
            t.sendResponseHeaders (200, -1);
            t.close();
        }
    }
    final static String request = "GET /test/foo.html HTTP/1.1\r\nContent-length: 0\r\n\r\n";
    final static ByteBuffer requestBuf = ByteBuffer.allocate(64).put(request.getBytes());
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor (executor);
        server.start ();
        InetSocketAddress destaddr = new InetSocketAddress (
                "127.0.0.1", server.getAddress().getPort()
        );
        System.out.println ("destaddr " + destaddr);
        Selector selector = Selector.open ();
        int requests = 0;
        int responses = 0;
        while (true) {
            int selres = selector.select (1);
            Set<SelectionKey> selkeys = selector.selectedKeys();
            for (SelectionKey key : selkeys) {
                if (key.isReadable()) {
                    SocketChannel chan = (SocketChannel)key.channel();
                    ByteBuffer buf = (ByteBuffer)key.attachment();
                    try {
                        int x = chan.read(buf);
                        if (x == -1 || responseComplete(buf)) {
                            key.attach(null);
                            chan.close();
                            responses++;
                        }
                    } catch (IOException e) {}
                }
            }
            if (requests < NUM) {
                SocketChannel schan = SocketChannel.open(destaddr);
                requestBuf.rewind();
                int c = 0;
                while (requestBuf.remaining() > 0) {
                    c += schan.write(requestBuf);
                }
                schan.configureBlocking(false);
                schan.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(100));
                requests++;
            }
            if (responses == NUM) {
                System.out.println ("Finished clients");
                break;
            }
        }
        server.stop (1);
        selector.close();
        executor.shutdown ();
    }
    static boolean responseComplete(ByteBuffer buf) {
        int pos = buf.position();
        buf.flip();
        byte[] lookingFor = new byte[] {'\r', '\n', '\r', '\n' };
        int lookingForCount = 0;
        while (buf.hasRemaining()) {
            byte b = buf.get();
            if (b == lookingFor[lookingForCount]) {
                lookingForCount++;
                if (lookingForCount == 4) {
                    return true;
                }
            } else {
                lookingForCount = 0;
            }
        }
        buf.position(pos);
        buf.limit(buf.capacity());
        return false;
    }
}

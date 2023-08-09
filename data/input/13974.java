class Reply implements Sendable {
    static class Code {
        private int number;
        private String reason;
        private Code(int i, String r) { number = i; reason = r; }
        public String toString() { return number + " " + reason; }
        static Code OK = new Code(200, "OK");
        static Code BAD_REQUEST = new Code(400, "Bad Request");
        static Code NOT_FOUND = new Code(404, "Not Found");
        static Code METHOD_NOT_ALLOWED = new Code(405, "Method Not Allowed");
    }
    private Code code;
    private Content content;
    private boolean headersOnly;
    Reply(Code rc, Content c) {
        this(rc, c, null);
    }
    Reply(Code rc, Content c, Request.Action head) {
        code = rc;
        content = c;
        headersOnly = (head == Request.Action.HEAD);
    }
    private static String CRLF = "\r\n";
    private static Charset ascii = Charset.forName("US-ASCII");
    private ByteBuffer hbb = null;
    private ByteBuffer headers() {
        CharBuffer cb = CharBuffer.allocate(1024);
        for (;;) {
            try {
                cb.put("HTTP/1.0 ").put(code.toString()).put(CRLF);
                cb.put("Server: niossl/0.1").put(CRLF);
                cb.put("Content-type: ").put(content.type()).put(CRLF);
                cb.put("Content-length: ")
                    .put(Long.toString(content.length())).put(CRLF);
                cb.put(CRLF);
                break;
            } catch (BufferOverflowException x) {
                assert(cb.capacity() < (1 << 16));
                cb = CharBuffer.allocate(cb.capacity() * 2);
                continue;
            }
        }
        cb.flip();
        return ascii.encode(cb);
    }
    public void prepare() throws IOException {
        content.prepare();
        hbb = headers();
    }
    public boolean send(ChannelIO cio) throws IOException {
        if (hbb == null)
            throw new IllegalStateException();
        if (hbb.hasRemaining()) {
            if (cio.write(hbb) <= 0)
                return true;
        }
        if (!headersOnly) {
            if (content.send(cio))
                return true;
        }
        if (!cio.dataFlush())
            return true;
        return false;
    }
    public void release() throws IOException {
        content.release();
    }
}

class RequestHandler implements Handler {
    private ChannelIO cio;
    private ByteBuffer rbb = null;
    private boolean requestReceived = false;
    private Request request = null;
    private Reply reply = null;
    private static int created = 0;
    RequestHandler(ChannelIO cio) {
        this.cio = cio;
        synchronized (RequestHandler.class) {
            created++;
            if ((created % 50) == 0) {
                System.out.println(".");
                created = 0;
            } else {
                System.out.print(".");
            }
        }
    }
    private boolean receive(SelectionKey sk) throws IOException {
        ByteBuffer tmp = null;
        if (requestReceived) {
            return true;
        }
        if (!cio.doHandshake(sk)) {
            return false;
        }
        if ((cio.read() < 0) || Request.isComplete(cio.getReadBuf())) {
            rbb = cio.getReadBuf();
            return (requestReceived = true);
        }
        return false;
    }
    private boolean parse() throws IOException {
        try {
            request = Request.parse(rbb);
            return true;
        } catch (MalformedRequestException x) {
            reply = new Reply(Reply.Code.BAD_REQUEST,
                              new StringContent(x));
        }
        return false;
    }
    private void build() throws IOException {
        Request.Action action = request.action();
        if ((action != Request.Action.GET) &&
                (action != Request.Action.HEAD)) {
            reply = new Reply(Reply.Code.METHOD_NOT_ALLOWED,
                              new StringContent(request.toString()));
        }
        reply = new Reply(Reply.Code.OK,
                          new FileContent(request.uri()), action);
    }
    public void handle(SelectionKey sk) throws IOException {
        try {
            if (request == null) {
                if (!receive(sk))
                    return;
                rbb.flip();
                if (parse())
                    build();
                try {
                    reply.prepare();
                } catch (IOException x) {
                    reply.release();
                    reply = new Reply(Reply.Code.NOT_FOUND,
                                      new StringContent(x));
                    reply.prepare();
                }
                if (send()) {
                    sk.interestOps(SelectionKey.OP_WRITE);
                } else {
                    if (cio.shutdown()) {
                        cio.close();
                        reply.release();
                    }
                }
            } else {
                if (!send()) {  
                    if (cio.shutdown()) {
                        cio.close();
                        reply.release();
                    }
                }
            }
        } catch (IOException x) {
            String m = x.getMessage();
            if (!m.equals("Broken pipe") &&
                    !m.equals("Connection reset by peer")) {
                System.err.println("RequestHandler: " + x.toString());
            }
            try {
                cio.shutdown();
            } catch (IOException e) {
            }
            cio.close();
            if (reply !=  null) {
                reply.release();
            }
        }
    }
    private boolean send() throws IOException {
        try {
            return reply.send(cio);
        } catch (IOException x) {
            if (x.getMessage().startsWith("Resource temporarily")) {
                System.err.println("## RTA");
                return true;
            }
            throw x;
        }
    }
}

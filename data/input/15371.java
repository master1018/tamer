class RequestServicer implements Runnable {
    private ChannelIO cio;
    private static int created = 0;
    RequestServicer(ChannelIO cio) {
        this.cio = cio;
        synchronized (RequestServicer.class) {
            created++;
            if ((created % 50) == 0) {
                System.out.println(".");
                created = 0;
            } else {
                System.out.print(".");
            }
        }
    }
    private void service() throws IOException {
        Reply rp = null;
        try {
            ByteBuffer rbb = receive();         
            Request rq = null;
            try {                               
                rq = Request.parse(rbb);
            } catch (MalformedRequestException x) {
                rp = new Reply(Reply.Code.BAD_REQUEST,
                               new StringContent(x));
            }
            if (rp == null) rp = build(rq);     
            do {} while (rp.send(cio));         
            do {} while (!cio.shutdown());
            cio.close();
            rp.release();
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
            if (rp != null) {
                rp.release();
            }
        }
    }
    public void run() {
        try {
            service();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }
    ByteBuffer receive() throws IOException {
        do {} while (!cio.doHandshake());
        for (;;) {
            int read = cio.read();
            ByteBuffer bb = cio.getReadBuf();
            if ((read < 0) || (Request.isComplete(bb))) {
                bb.flip();
                return bb;
            }
        }
    }
    Reply build(Request rq) throws IOException {
        Reply rp = null;
        Request.Action action = rq.action();
        if ((action != Request.Action.GET) &&
                (action != Request.Action.HEAD))
            rp = new Reply(Reply.Code.METHOD_NOT_ALLOWED,
                           new StringContent(rq.toString()));
        else
            rp = new Reply(Reply.Code.OK,
                           new FileContent(rq.uri()), action);
        try {
            rp.prepare();
        } catch (IOException x) {
            rp.release();
            rp = new Reply(Reply.Code.NOT_FOUND,
                           new StringContent(x));
            rp.prepare();
        }
        return rp;
    }
}

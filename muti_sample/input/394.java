public class B1 extends Server {
    B1(int port, int backlog, boolean secure) throws Exception {
        super(port, backlog, secure);
    }
    void runServer() throws Exception {
        for (;;) {
            SocketChannel sc = ssc.accept();
            ChannelIO cio = (sslContext != null ?
                ChannelIOSecure.getInstance(
                    sc, true , sslContext) :
                ChannelIO.getInstance(
                    sc, true ));
            RequestServicer svc = new RequestServicer(cio);
            svc.run();
        }
    }
}

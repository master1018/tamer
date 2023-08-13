public class BN extends Server {
    BN(int port, int backlog, boolean secure) throws Exception {
        super(port, backlog, secure);
    }
    void runServer() throws IOException {
        for (;;) {
            SocketChannel sc = ssc.accept();
            ChannelIO cio = (sslContext != null ?
                ChannelIOSecure.getInstance(
                    sc, true , sslContext) :
                ChannelIO.getInstance(
                    sc, true ));
            RequestServicer svc = new RequestServicer(cio);
            Thread th = new Thread(svc);
            th.start();
        }
    }
}

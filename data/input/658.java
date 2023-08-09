public class N2 extends Server {
    N2(int port, int backlog, boolean secure) throws Exception {
        super(port, backlog, secure);
    }
    void runServer() throws Exception {
        Dispatcher d = new DispatcherN();
        Acceptor a = new Acceptor(ssc, d, sslContext);
        new Thread(a).start();
        d.run();
    }
}

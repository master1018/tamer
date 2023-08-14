public class SocksV4Test {
    public static void main(String[] args) throws IOException {
        SocksServer srvr = new SocksServer(8888, true);
        srvr.start();
        System.setProperty("socksProxyHost", "localhost");
        System.setProperty("socksProxyPort", "8888");
        InetSocketAddress ad = new InetSocketAddress("doesnt.exist.name", 1234);
        Socket s = new Socket();
        try {
            s.connect(ad,10000);
        } catch (UnknownHostException ex) {
        } catch (NullPointerException npe) {
            throw new RuntimeException("Got a NUllPointerException");
        } finally {
            srvr.terminate();
            srvr.interrupt();
        }
    }
}

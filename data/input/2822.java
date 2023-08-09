public class NullTransportService extends TransportService {
    public String name() {
        return "Null";
    }
    public String description() {
        return "this is a test transport";
    }
    public Capabilities capabilities() {
        throw new RuntimeException("shouldn't get here");
    }
    public Connection attach(String address, long attachTimeout,
        long handshakeTimeout) throws IOException {
        throw new RuntimeException("shouldn't get here");
    }
    public ListenKey startListening(String address) throws IOException {
        throw new RuntimeException("shouldn't get here");
    }
    public String defaultAddress() {
        return "";
    }
    public ListenKey startListening() throws IOException {
        throw new RuntimeException("shouldn't get here");
    }
    public void stopListening(ListenKey key) throws IOException {
        throw new RuntimeException("shouldn't get here");
    }
    public Connection accept(ListenKey key, long acceptTimeout,
        long handshakeTimeout) throws IOException {
        throw new RuntimeException("shouldn't get here");
    }
}

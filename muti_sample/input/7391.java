public abstract class TransportService {
    public abstract String name();
    public abstract String description();
    public static abstract class Capabilities {
        public abstract boolean supportsMultipleConnections();
        public abstract boolean supportsAttachTimeout();
        public abstract boolean supportsAcceptTimeout();
        public abstract boolean supportsHandshakeTimeout();
    }
    public abstract Capabilities capabilities();
    public abstract Connection attach(String address, long attachTimeout,
        long handshakeTimeout) throws IOException;
    public static abstract class ListenKey {
        public abstract String address();
    }
    public abstract ListenKey startListening(String address) throws IOException;
    public abstract ListenKey startListening() throws IOException;
    public abstract void stopListening(ListenKey listenKey) throws IOException;
    public abstract Connection accept(ListenKey listenKey, long acceptTimeout,
        long handshakeTimeout) throws IOException;
}

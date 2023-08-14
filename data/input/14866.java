public final class SocksProxy extends Proxy {
    private final int version;
    private SocksProxy(SocketAddress addr, int version) {
        super(Proxy.Type.SOCKS, addr);
        this.version = version;
    }
    public static SocksProxy create(SocketAddress addr, int version) {
        return new SocksProxy(addr, version);
    }
    public int protocolVersion() {
        return version;
    }
}

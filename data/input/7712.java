public class B4849451 {
    public static void main(String[] args) {
        InetSocketAddress addr1 = InetSocketAddress.createUnresolved("unresolveable", 10);
        InetSocketAddress addr2 = InetSocketAddress.createUnresolved("UNRESOLVEABLE", 10);
        if (!(addr1.equals(addr2))) {
            throw new RuntimeException(addr1 + " and " + addr2 + " should be equal");
        }
    }
}

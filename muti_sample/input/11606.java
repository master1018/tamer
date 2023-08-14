public class CreateUnresolved {
    public static void main(String[] args) {
        InetSocketAddress a = InetSocketAddress.createUnresolved("unresolved", 1234);
        if (!a.isUnresolved())
            throw new RuntimeException("Address is not flagged as 'unresolved'");
    }
}

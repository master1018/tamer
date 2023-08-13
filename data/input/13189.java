public class B4414825 {
    public static void main(String[] args) throws Exception {
        SocketPermission p = new SocketPermission("invlidhost", "connect");
        if (!p.implies(p))
            throw new RuntimeException("Test failed: SocketPermission instance should imply itself.");
        SocketPermission p1 = new SocketPermission("invlidhost", "connect");
        if (!p.implies(p1))
            throw new RuntimeException("Test failed: Equaled SocketPermission instances should imply each other.");
    }
}

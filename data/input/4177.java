public class WithSecurityManager {
    public static void main(String[] args) throws Exception {
        boolean allow = false;
        String policy = (args[0].equals("allow")) ? "java.policy.allow" :
            "java.policy.deny";
        String testSrc = System.getProperty("test.src");
        if (testSrc == null)
            testSrc = ".";
        System.setProperty("java.security.policy",
            Paths.get(testSrc).resolve(policy).toString());
        System.setSecurityManager(new SecurityManager());
        AsynchronousServerSocketChannel listener =
            AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(0));
        InetAddress lh = InetAddress.getLocalHost();
        int port = ((InetSocketAddress)(listener.getLocalAddress())).getPort();
        SocketChannel sc = SocketChannel.open(new InetSocketAddress(lh, port));
        Future<AsynchronousSocketChannel> result = listener.accept();
        if (allow) {
            result.get().close();
        } else {
            try {
                result.get();
            } catch (ExecutionException x) {
                if (!(x.getCause() instanceof SecurityException))
                    throw new RuntimeException("SecurityException expected");
            }
        }
        sc.close();
        listener.close();
    }
}

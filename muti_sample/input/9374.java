public class IsKeepingAlive {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        SecurityManager security = System.getSecurityManager();
        if (security == null) {
            security = new SecurityManager();
            System.setSecurityManager(security);
        }
        URL url1 = new URL("http:
        HttpClient c1 = HttpClient.New(url1);
        boolean keepAlive = c1.isKeepingAlive();
        ss.close();
    }
}

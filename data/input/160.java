public class OpenServer {
    OpenServer() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        URL myURL = new URL("http:
        HttpClient httpC = new HttpClient(myURL, null, -1);
        ss.close();
    }
    public static void main(String [] args) throws Exception {
        SecurityManager security = System.getSecurityManager();
        if (security == null) {
            security = new SecurityManager();
            System.setSecurityManager(security);
        }
        new OpenServer();
    }
}

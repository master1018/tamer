public class GetProxyPort {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        URL myURL = new URL("http:
        HttpClient httpC = new HttpClient(myURL, null, -1);
        int port = httpC.getProxyPortUsed();
    }
}

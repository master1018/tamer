public class ImplicitFileName {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        URL url = new URL("http:
        HttpClient c = HttpClient.New(url);
        if (!c.getURLFile().equals("/")) {
            throw new Exception("Implicit filename in URL " +
                                url.toString() + " is not '/'");
        }
    }
}

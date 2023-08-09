public class NullSelector {
    public static void main(String[] args) throws Exception {
        URL url = new URL("http:
        ProxySelector.setDefault(null);
        URLConnection con = url.openConnection();
        con.setConnectTimeout(500);
        try {
            con.connect();
        } catch (IOException e) {
        }
    }
}

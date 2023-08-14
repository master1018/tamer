public class HandlerParse {
    public static void main(String[] args) {
        URL u = null;
        try {
            u = new URL("mailto:");
        } catch(MalformedURLException e) {
            u = null;
        }
        if (u != null)
            throw new RuntimeException("MalformedURLException was not thrown!");
    }
}

public class ParseAuthority {
    public static void main(String args[]) throws Exception {
        try {
            URL u1 = new URL("http:
            throw new RuntimeException("URL parser didn't catch" +
                                       " invalid authority field");
        } catch (MalformedURLException me) {
            if (!me.getMessage().startsWith("Invalid authority field")) {
                throw new RuntimeException("URL parser didn't catch" +
                                       " invalid authority field");
            }
        }
        try {
            URL u2 = new URL("http:
            throw new RuntimeException("URL parser didn't catch" +
                                       " invalid host");
        } catch (MalformedURLException me) {
            if (!me.getMessage().startsWith("Invalid host")) {
                throw new RuntimeException("URL parser didn't catch" +
                                       " invalid host");
            }
        }
    }
}

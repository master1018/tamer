public class TestRFC2732 {
    public static void main(String[] args) {
        URL u = null;
        try {
            u = new URL("http", "10:100::1234", 99, "/index");
            if (!u.toString().equals("http:
                !u.toExternalForm().equals("http:
                throw new Exception("Failed test for RFC 2732");
            }
            System.out.println("Test passed!");
        } catch(Exception exp) {
            throw new RuntimeException(exp.getMessage() +
                                       "Expect: " +
                                       "http:
                                       " Got: " + u.toExternalForm());
        }
    }
}

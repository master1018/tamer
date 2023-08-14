public class BadName {
    public static void main(String args[]) throws Exception {
        try {
            X500Name name = new X500Name("John Doe");
            System.out.println(name.toString());
        } catch (IOException ioe) {
        }
    }
}

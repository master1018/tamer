public class Implies {
    public static void main(String[] args) throws Exception {
        URL thisURL = new URL("http", "localhost", "file");
        URL thatURL = new URL("http", null, "file");
        CodeSource thisCs =
            new CodeSource(thisURL, (java.security.cert.Certificate[]) null);
        CodeSource thatCs =
            new CodeSource(thatURL, (java.security.cert.Certificate[]) null);
        if (thisCs.implies(thatCs)) {
            throw new SecurityException("test failed");
        }
        System.out.println("test passed");
    }
}

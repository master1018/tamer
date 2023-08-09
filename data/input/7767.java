public class ExpansionErrorMisleading {
    public static void main(String[] args) {
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream
                ("/tmp/hello");
        } catch (java.io.FileNotFoundException fnfe) {
            System.out.println("Test Failed");
            throw new SecurityException(fnfe.getMessage());
        } catch (SecurityException se) {
            System.out.println("Test Succeeded");
        }
    }
}

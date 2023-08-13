public class ExtDirsDefaultPolicy {
    public static void main(String args[]) throws Exception {
        try {
            ExtDirsA a = new ExtDirsA();
            a.go();
            System.out.println("Test Succeeded");
        } catch (SecurityException se) {
            se.printStackTrace();
            System.out.println("Test Failed");
            throw se;
        }
    }
}

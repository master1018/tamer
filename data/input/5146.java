public class NullClass {
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4518927\n");
        try {
            System.err.println("getting class annotation for null class...");
            String annotation = RMIClassLoader.getClassAnnotation(null);
            throw new RuntimeException(
                "TEST FAILED: NullPointerException not caught!");
        } catch (NullPointerException e) {
            System.err.println("TEST PASSED: NullPointerException caught");
        } catch (Exception e) {
            TestLibrary.bomb(e);
        }
    }
}

public class CheckVMID {
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4171370\n");
        TestLibrary.suggestSecurityManager(null);
        try {
            System.err.println("Create a VMID");
            VMID vmid = new VMID();
            System.err.println("vmid = " + vmid);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        }
    }
}

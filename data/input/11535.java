public class ServicePermissionTest {
    public static void main(String[] args) throws Exception {
        try {
            ServicePermission sp = new ServicePermission("you", null);
        } catch(NullPointerException e) {
            System.out.println("NullPointerException caught: OK");
            e.printStackTrace(System.out);
            return;
        }
        throw new Exception("Test Failed: Not behaved as descibed.");
    }
}

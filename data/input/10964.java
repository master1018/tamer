public class MBeanPermissionTest {
    public static void main(String[] args) {
        int error = 0;
        System.out.println(">>> MBeanPermissionTest");
        try {
            System.out.println("Create MBeanPermission(null,\"\")");
            MBeanPermission mbp = new MBeanPermission(null, "");
            System.out.println("Didn't get expected IllegalArgumentException");
            error++;
        } catch (IllegalArgumentException e) {
            System.out.println("Got expected exception = " + e);
        } catch (Exception e) {
            System.out.println("Got unexpected exception = " + e);
            error++;
        }
        try {
            System.out.println("Create MBeanPermission(\"\", null)");
            MBeanPermission mbp = new MBeanPermission("", null);
            System.out.println("Didn't get expected IllegalArgumentException");
            error++;
        } catch (IllegalArgumentException e) {
            System.out.println("Got expected exception = " + e);
        } catch (Exception e) {
            System.out.println("Got unexpected exception = " + e);
            error++;
        }
        if (error > 0) {
            final String msg = "Test FAILED! Got " + error + " error(s)";
            System.out.println(msg);
            throw new IllegalArgumentException(msg);
        } else {
            System.out.println("Test PASSED!");
        }
    }
}

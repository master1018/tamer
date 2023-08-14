public class PermissionTest {
    public static void main(String args[]) throws Exception {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            throw new RuntimeException("Test configuration error - no security manager set");
        }
        String pid = args[0];
        boolean shouldFail = Boolean.parseBoolean(args[1]);
        try {
            VirtualMachine.attach(pid).detach();
            if (shouldFail) {
                throw new RuntimeException("SecurityException should be thrown");
            }
            System.out.println(" - attached to target VM as expected.");
        } catch (Exception x) {
            if (shouldFail && ((x instanceof AttachNotSupportedException) ||
                (x instanceof SecurityException))) {
                System.out.println(" - exception thrown as expected.");
            } else {
                throw x;
            }
        }
    }
}

public class ProviderTest {
    public static void main(String args[]) throws Exception {
        ClassLoader cl = AttachProvider.class.getClassLoader();
        if (cl != ClassLoader.getSystemClassLoader()) {
            System.out.println("Attach API not loaded by system class loader - test skipped");
            return;
        }
        VirtualMachine.attach("simple:1234").detach();
    }
}

public class PrematureLoadTest {
    static int failures = 0;
    public static void main(String args[]) throws IOException {
        try {
            new BootSupport();
            throw new RuntimeException("Test configuration error - BootSupport loaded unexpectedly!");
        } catch (NoClassDefFoundError x) {
        }
        try {
            new AgentSupport();
            throw new RuntimeException("Test configuration error - AgentSupport loaded unexpectedly!");
        } catch (NoClassDefFoundError x) {
        }
        JarFile bootclasses = new JarFile("BootSupport.jar");
        JarFile agentclasses = new JarFile("AgentSupport.jar");
        Instrumentation ins = Agent.getInstrumentation();
        ins.appendToBootstrapClassLoaderSearch(bootclasses);
        try {
            new BootSupport();
            System.out.println("FAIL: BootSupport resolved");
            failures++;
        } catch (NoClassDefFoundError x) {
            System.out.println("PASS: BootSupport failed to resolve");
        }
        try {
            ins.appendToSystemClassLoaderSearch(agentclasses);
            try {
                new AgentSupport();
                System.out.println("FAIL: AgentSupport resolved");
                failures++;
            } catch (NoClassDefFoundError x) {
                System.out.println("PASS: AgentSupport failed to resolve");
            }
        } catch (UnsupportedOperationException x) {
            System.out.println("System class loader does not support adding to class path");
        }
        if (failures > 0) {
            throw new RuntimeException(failures + " test(s) failed.");
        }
    }
}

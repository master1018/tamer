public class NullOptions {
    public static void main(String[] args) throws Exception {
        CommandEnvironment env1 =
            new CommandEnvironment(null, null);
        CommandEnvironment env2 =
            new CommandEnvironment(null, new String[0]);
        if (env1.equals(env2)) {
            System.err.println("TEST PASSED: environments are equal");
        } else {
            System.err.println("TEST FAILED: environments not equal!");
            throw new RuntimeException("TEST FAILED");
        }
    }
}

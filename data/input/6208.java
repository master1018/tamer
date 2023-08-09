public class ImplVersionTest {
    public static void main(String[] args) {
        try {
            String osName = System.getProperty("os.name");
            System.out.println("osName = " + osName);
            if ("Windows 98".equals(osName)) {
                System.out.println("This test is disabled on Windows 98.");
                System.out.println("Bye! Bye!");
                return;
            }
            String javaHome = System.getProperty("java.home");
            String testSrc = System.getProperty("test.src");
            String testClasses = System.getProperty("test.classes");
            String bootClassPath = System.getProperty("sun.boot.class.path");
            String command =
                javaHome + File.separator + "bin" + File.separator + "java " +
                " -Xbootclasspath/p:" + bootClassPath +
                " -classpath " + testClasses +
                " -Djava.security.manager -Djava.security.policy==" + testSrc +
                File.separator + "policy -Dtest.classes=" + testClasses +
                " ImplVersionCommand " + System.getProperty("java.runtime.version");
            System.out.println("ImplVersionCommand Exec Command = " + command);
            Process proc = Runtime.getRuntime().exec(command);
            new ImplVersionReader(proc, proc.getInputStream()).start();
            new ImplVersionReader(proc, proc.getErrorStream()).start();
            int exitValue = proc.waitFor();
            System.out.println("ImplVersionCommand Exit Value = " +
                               exitValue);
            if (exitValue != 0) {
                System.out.println("TEST FAILED: Incorrect exit value " +
                                   "from ImplVersionCommand");
                System.exit(exitValue);
            }
            System.out.println("Bye! Bye!");
        } catch (Exception e) {
            System.out.println("Unexpected exception caught = " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}

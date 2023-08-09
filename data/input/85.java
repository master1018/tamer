public class InconsistentError {
    public static void main(String[] args) {
        try {
            System.setProperty("java.security.auth.login.config",
                                "=nofile");
            ConfigFile config = new ConfigFile();
            throw new SecurityException("test 1 failed");
        } catch (SecurityException se) {
            if (se.getMessage().indexOf("No such file or directory") > 0) {
                System.out.println("test 1 succeeded");
            } else {
                System.out.println("test 1 failed");
                throw se;
            }
        }
        try {
            System.setProperty("java.security.auth.login.config",
                                "=file:/nofile");
            ConfigFile config = new ConfigFile();
            throw new SecurityException("test 2 failed");
        } catch (SecurityException se) {
            if (se.getMessage().indexOf("No such file or directory") > 0) {
                System.out.println("test 2 succeeded");
            } else {
                if (System.getProperty("os.name").equals("SunOS")) {
                        System.out.println("test 2 failed");
                        throw se;
                }
            }
        }
        System.setProperty("java.security.auth.login.config",
                                "=file:${test.src}/InconsistentError.config");
        ConfigFile config = new ConfigFile();
        System.out.println("test succeeded");
    }
}

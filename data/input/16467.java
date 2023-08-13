public class GetGlobalTest {
    static final java.io.PrintStream out = System.out;
    public static void main(String arg[]) {
        Logger glogger1 = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        Logger glogger2 = Logger.getGlobal();
        if (glogger1.equals(glogger2)) {
            out.println("Test passed");
        } else {
            out.println("Test FAILED");
        }
    }
}

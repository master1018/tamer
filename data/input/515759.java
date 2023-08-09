public class MockStdLogger implements ISdkLog {
    public void error(Throwable t, String errorFormat, Object... args) {
        if (errorFormat != null) {
            System.err.printf("Error: " + errorFormat, args);
            if (!errorFormat.endsWith("\n")) {
                System.err.printf("\n");
            }
        }
        if (t != null) {
            System.err.printf("Error: %s\n", t.getMessage());
        }
    }
    public void warning(String warningFormat, Object... args) {
        System.out.printf("Warning: " + warningFormat, args);
        if (!warningFormat.endsWith("\n")) {
            System.out.printf("\n");
        }
    }
    public void printf(String msgFormat, Object... args) {
        System.out.printf(msgFormat, args);
    }
}

public class NullSdkLog implements ISdkLog {
    private static final ISdkLog sThis = new NullSdkLog();
    public static ISdkLog getLogger() {
        return sThis;
    }
    public void error(Throwable t, String errorFormat, Object... args) {
    }
    public void printf(String msgFormat, Object... args) {
    }
    public void warning(String warningFormat, Object... args) {
    }
}

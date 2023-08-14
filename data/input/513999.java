public class AdtConsoleSdkLog implements ISdkLog {
    private static final String TAG = "SDK Manager"; 
    public void error(Throwable t, String errorFormat, Object... args) {
        if (t != null) {
            AdtPlugin.logAndPrintError(t, TAG, "Error: " + errorFormat, args);
        } else {
            AdtPlugin.printErrorToConsole(TAG, String.format(errorFormat, args));
        }
    }
    public void printf(String msgFormat, Object... args) {
        String msg = String.format(msgFormat, args);
        for (String s : msg.split("\n")) {
            if (s.trim().length() > 0) {
                AdtPlugin.printToConsole(TAG, s);
            }
        }
    }
    public void warning(String warningFormat, Object... args) {
        AdtPlugin.printToConsole(TAG, String.format("Warning: " + warningFormat, args));
    }
}

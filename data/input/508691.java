public final class MessageBoxLog implements ISdkLog {
    final ArrayList<String> logMessages = new ArrayList<String>();
    private final String mMessage;
    private final Display mDisplay;
    private final boolean mLogErrorsOnly;
    public MessageBoxLog(String message, Display display, boolean logErrorsOnly) {
        mMessage = message;
        mDisplay = display;
        mLogErrorsOnly = logErrorsOnly;
    }
    public void error(Throwable throwable, String errorFormat, Object... arg) {
        if (errorFormat != null) {
            logMessages.add(String.format("Error: " + errorFormat, arg));
        }
        if (throwable != null) {
            logMessages.add(throwable.getMessage());
        }
    }
    public void warning(String warningFormat, Object... arg) {
        if (!mLogErrorsOnly) {
            logMessages.add(String.format("Warning: " + warningFormat, arg));
        }
    }
    public void printf(String msgFormat, Object... arg) {
        if (!mLogErrorsOnly) {
            logMessages.add(String.format(msgFormat, arg));
        }
    }
    public void displayResult(final boolean success) {
        if (logMessages.size() > 0) {
            final StringBuilder sb = new StringBuilder(mMessage + "\n\n");
            for (String msg : logMessages) {
                sb.append(msg);
            }
            mDisplay.asyncExec(new Runnable() {
                public void run() {
                    Shell shell = mDisplay.getActiveShell();
                    if (success && !mLogErrorsOnly) {
                        MessageDialog.openInformation(shell, "Android Virtual Devices Manager",
                                sb.toString());
                    } else {
                        MessageDialog.openError(shell, "Android Virtual Devices Manager",
                                    sb.toString());
                    }
                }
            });
        }
    }
}

public class Win32ErrorMode {
    private static final long SEM_FAILCRITICALERRORS     = 0x0001;
    private static final long SEM_NOGPFAULTERRORBOX      = 0x0002;
    private static final long SEM_NOALIGNMENTFAULTEXCEPT = 0x0004;
    private static final long SEM_NOOPENFILEERRORBOX     = 0x8000;
    private Win32ErrorMode() {
    }
    public static void initialize() {
        if (!sun.misc.VM.isBooted()) {
            String s = (String) System.getProperty("sun.io.allowCriticalErrorMessageBox");
            if (s == null || s.equals(Boolean.FALSE.toString())) {
                long mode = setErrorMode(0);
                mode |= SEM_FAILCRITICALERRORS;
                setErrorMode(mode);
            }
        }
    }
    private static native long setErrorMode(long mode);
}

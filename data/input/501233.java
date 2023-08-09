public class SystemUtils {
    public static final int OS_WINDOWS = 1;
    public static final int OS_LINUX = 2;
    public static final int OS_UNKNOWN = -1;
    public static final int ARC_IA32 = 1;
    public static final int ARC_IA64 = 2;
    public static final int ARC_UNKNOWN = -1;
    private static int os = 0;
    private static int arc = 0;
    public static int getOS() {
        if (os == 0) {
            String osname = System.getProperty("os.name").substring(0,3); 
            if (osname.compareToIgnoreCase("win") == 0) { 
                os = OS_WINDOWS;
            } else {
                if (osname.compareToIgnoreCase("lin") == 0) { 
                    os = OS_LINUX;
                } else
                    os = OS_UNKNOWN;
            }
        }
        return os;
    }
}

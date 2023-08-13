public class OS {
    private static boolean macOs;
    private static boolean leopard;
    private static boolean linux;
    private static boolean windows;
    static {
        String osName = System.getProperty("os.name");
        macOs = "Mac OS X".startsWith(osName);
        linux = "Linux".startsWith(osName);
        windows = "Windows".startsWith(osName);
        String version = System.getProperty("os.version");
        final String[] parts = version.split("\\.");
        leopard = Integer.parseInt(parts[0]) >= 10 && Integer.parseInt(parts[1]) >= 5;
    }
    public static boolean isMacOsX() {
        return macOs;
    }
    public static boolean isLeopardOrLater() {
        return leopard;
    }
    public static boolean isLinux() {
        return linux;
    }
    public static boolean isWindows() {
        return windows;
    }
}

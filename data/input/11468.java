public class WinDeviceName {
    private static String devnames[] = {
        "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4",
        "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2",
        "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
        "CLOCK$"
    };
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Windows")) {
            return;
        }
        for (int i = 0; i < devnames.length; i++) {
            if (new File(devnames[i]).isFile() ||
                new File(devnames[i] + ".txt").isFile()) {
                if ("CLOCK$".equals(devnames[i]) &&
                    (osName.startsWith("Windows 9") ||
                     osName.startsWith("Windows Me"))) {
                    continue;
                }
                throw new Exception("isFile() returns true for Device name "
                                    +  devnames[i]);
            }
        }
    }
}

public class HeadlessTray
{
    public static void main (String args[]) {
        System.setProperty("java.awt.headless", "true");
        boolean isSupported = SystemTray.isSupported();
        if (isSupported) {
            throw new RuntimeException("Tray shouldn't be supported in headless mode ");
        }
    }
}

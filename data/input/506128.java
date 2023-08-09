public class Support_NetworkInterface {
    public static boolean useInterface(NetworkInterface theInterface) {
        boolean result = true;
        String platform = System.getProperty("os.name");
        if (platform.startsWith("Windows")) {
            if ((theInterface.getDisplayName()
                    .equals("Teredo Tunneling Pseudo-Interface"))
                    || (theInterface.getDisplayName()
                            .equals("6to4 Tunneling Pseudo-Interface"))
                    || (theInterface.getDisplayName()
                            .equals("Automatic Tunneling Pseudo-Interface"))
                    || (theInterface.getDisplayName()
                            .equals("Loopback Pseudo-Interface"))) {
                result = false;
            }
        }
        return result;
    }
}

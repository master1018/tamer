public class SelProvider {
    public static void main(String[] args) throws Exception {
        String osname = System.getProperty("os.name");
        String osver = System.getProperty("os.version");
        String spName = SelectorProvider.provider().getClass().getName();
        String expected = null;
        if ("SunOS".equals(osname)) {
            expected = "sun.nio.ch.DevPollSelectorProvider";
        } else if ("Linux".equals(osname)) {
            String[] vers = osver.split("\\.", 0);
            if (vers.length >= 2) {
                int major = Integer.parseInt(vers[0]);
                int minor = Integer.parseInt(vers[1]);
                if (major > 2 || (major == 2 && minor >= 6)) {
                    expected = "sun.nio.ch.EPollSelectorProvider";
                } else {
                    expected = "sun.nio.ch.PollSelectorProvider";
                }
            } else {
                throw new RuntimeException("Test does not recognize this operating system");
            }
        } else
            return;
        if (!spName.equals(expected))
            throw new Exception("failed");
    }
}

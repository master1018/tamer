public class DefaultSelectorProvider {
    private DefaultSelectorProvider() { }
    public static SelectorProvider create() {
        String osname = AccessController.doPrivileged(
            new GetPropertyAction("os.name"));
        if ("SunOS".equals(osname)) {
            return new sun.nio.ch.DevPollSelectorProvider();
        }
        if ("Linux".equals(osname)) {
            String osversion = AccessController.doPrivileged(
                new GetPropertyAction("os.version"));
            String[] vers = osversion.split("\\.", 0);
            if (vers.length >= 2) {
                try {
                    int major = Integer.parseInt(vers[0]);
                    int minor = Integer.parseInt(vers[1]);
                    if (major > 2 || (major == 2 && minor >= 6)) {
                        return new sun.nio.ch.EPollSelectorProvider();
                    }
                } catch (NumberFormatException x) {
                }
            }
        }
        return new sun.nio.ch.PollSelectorProvider();
    }
}

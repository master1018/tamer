public class DefaultAsynchronousChannelProvider {
    private DefaultAsynchronousChannelProvider() { }
    public static AsynchronousChannelProvider create() {
        String osname = AccessController
            .doPrivileged(new GetPropertyAction("os.name"));
        if (osname.equals("SunOS"))
            return new SolarisAsynchronousChannelProvider();
        if (osname.equals("Linux"))
            return new LinuxAsynchronousChannelProvider();
        throw new InternalError("platform not recognized");
    }
}

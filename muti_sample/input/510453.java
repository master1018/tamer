public class DebugPortManager {
    public interface IDebugPortProvider {
        public static final int NO_STATIC_PORT = -1;
        public int getPort(IDevice device, String appName);
    }
    private static IDebugPortProvider sProvider = null;
    public static void setProvider(IDebugPortProvider provider) {
        sProvider = provider;
    }
    static IDebugPortProvider getProvider() {
        return sProvider;
    }
}

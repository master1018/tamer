public class SystemService
{
    public static void start(String name) {
        SystemProperties.set("ctl.start", name);
    }
    public static void stop(String name) {
        SystemProperties.set("ctl.stop", name);
    }
}

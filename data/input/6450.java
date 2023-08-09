public class OSMBeanFactory {
    private OSMBeanFactory() {};
    private static UnixOperatingSystem osMBean = null;
    public static synchronized OperatingSystemMXBean
        getOperatingSystemMXBean(VMManagement jvm) {
        if (osMBean == null) {
            osMBean = new UnixOperatingSystem(jvm);
        }
        return (OperatingSystemMXBean) osMBean;
    }
}

public class PlatformMXBeanTest {
    public static void main(String[] argv) throws Exception {
        OperatingSystemMXBean osMBean = getOSPlatformMXBean(OperatingSystemMXBean.class);
        if (osMBean != getOSPlatformMXBean(com.sun.management.OperatingSystemMXBean.class)) {
            throw new RuntimeException(
                "Invalid com.sun.management.OperatingSystemMXBean instance");
        }
        if (!System.getProperty("os.name").startsWith("Windows") &&
                osMBean != getOSPlatformMXBean(com.sun.management.UnixOperatingSystemMXBean.class)) {
            throw new RuntimeException(
                "Invalid com.sun.management.UnixOperatingSystemMXBean instance");
        }
    }
    private static <T extends OperatingSystemMXBean>
            T getOSPlatformMXBean(Class<T> c) {
        List<T> result = ManagementFactory.getPlatformMXBeans(c);
        if (result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new RuntimeException(c.getName() + " has " +
                result.size() + " number of instances");
        }
    }
}

public class GetDiagnosticOptions {
    private static String HOTSPOT_DIAGNOSTIC_MXBEAN_NAME =
        "com.sun.management:type=HotSpotDiagnostic";
    public static void main(String[] args) throws Exception {
        List<HotSpotDiagnosticMXBean> list =
            ManagementFactory.getPlatformMXBeans(HotSpotDiagnosticMXBean.class);
        HotSpotDiagnosticMXBean mbean = list.get(0);
        checkDiagnosticOptions(mbean);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    HOTSPOT_DIAGNOSTIC_MXBEAN_NAME,
                    HotSpotDiagnosticMXBean.class);
        checkDiagnosticOptions(mbean);
    }
    private static void checkDiagnosticOptions(HotSpotDiagnosticMXBean mbean) {
        List<VMOption> options = mbean.getDiagnosticOptions();
        for (VMOption opt : options) {
            System.out.println("option: "+opt.getName()+"="+opt.getValue());
        }
    }
}

public class SetAllVMOptions {
    private static String HOTSPOT_DIAGNOSTIC_MXBEAN_NAME =
        "com.sun.management:type=HotSpotDiagnostic";
    public static void main(String[] args) throws Exception {
        List<HotSpotDiagnosticMXBean> list =
            ManagementFactory.getPlatformMXBeans(HotSpotDiagnosticMXBean.class);
        HotSpotDiagnosticMXBean mbean = list.get(0);
        setAllVMOptions(mbean);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbean = ManagementFactory.newPlatformMXBeanProxy(mbs,
                    HOTSPOT_DIAGNOSTIC_MXBEAN_NAME,
                    HotSpotDiagnosticMXBean.class);
        setAllVMOptions(mbean);
    }
    private static void setAllVMOptions(HotSpotDiagnosticMXBean mbean) {
        List<VMOption> options = mbean.getDiagnosticOptions();
        for (VMOption opt : options) {
            String name = opt.getName();
            String val = opt.getValue();
            System.out.println("option: "+name+"="+val);
            mbean.setVMOption(name,val);
        }
    }
}

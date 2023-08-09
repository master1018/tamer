public class DumpHeap {
    public static void main(String[] argv) throws Exception {
         List<HotSpotDiagnosticMXBean> list = ManagementFactory.getPlatformMXBeans(HotSpotDiagnosticMXBean.class);
         System.out.println("Dumping to file: " + argv[0] + " ....");
         list.get(0).dumpHeap(argv[0], true);
    }
}

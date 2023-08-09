public class ThreadInfoArray {
    public static void main(String[] argv) throws Exception {
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        long [] ids = {new Thread().getId()};
        ThreadInfo[] tinfos = mbean.getThreadInfo(ids);
        if (tinfos[0] != null) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected to have a null element");
        }
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(THREAD_MXBEAN_NAME);
        Object[] params = {ids};
        String[] sigs = {"[J"};
        Object[] result = (Object[]) mbs.invoke(on, "getThreadInfo", params, sigs);
        if (result[0] != null) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected to have a null element via MBeanServer");
        }
        ThreadMXBean proxy = newPlatformMXBeanProxy(mbs,
                                 on.toString(),
                                 ThreadMXBean.class);
        tinfos = proxy.getThreadInfo(ids);
        if (tinfos[0] != null) {
            throw new RuntimeException("TEST FAILED: " +
                "Expected to have a null element");
        }
        System.out.println("Test passed");
    }
}

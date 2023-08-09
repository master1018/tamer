public class OperationImpactTest {
    public static interface ThingMXBean {
        void thing();
    }
    public static class Thing implements ThingMXBean {
        public void thing() {}
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        Thing thing = new Thing();
        mbs.registerMBean(thing, on);
        MBeanInfo mbi = mbs.getMBeanInfo(on);
        MBeanOperationInfo[] ops = mbi.getOperations();
        if (ops.length != 1)
            throw new Exception("TEST FAILED: several ops: " + mbi);
        MBeanOperationInfo op = ops[0];
        if (!op.getName().equals("thing"))
            throw new Exception("TEST FAILED: wrong op name: " + op);
        if (op.getImpact() != MBeanOperationInfo.UNKNOWN)
            throw new Exception("TEST FAILED: wrong impact: " + op);
        op = new OpenMBeanOperationInfoSupport("name", "descr", null,
                                               SimpleType.VOID,
                                               MBeanOperationInfo.UNKNOWN);
        if (op.getImpact() != MBeanOperationInfo.UNKNOWN)
            throw new Exception("TEST FAILED: wrong impact: " + op);
        System.out.println("TEST PASSED");
    }
}

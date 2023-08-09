public class MBeanOperationInfoTest {
    private static final String[][] returnTypes = {
        { "dumpAllThreads(boolean,boolean)", "[Ljavax.management.openmbean.CompositeData;" },
        { "findDeadlockedThreads()", "[J" },
        { "findMonitorDeadlockedThreads()", "[J" },
        { "getThreadInfo(long)","javax.management.openmbean.CompositeData"},
        { "getThreadInfo(long,int)","javax.management.openmbean.CompositeData"},
        { "getThreadInfo([J)","[Ljavax.management.openmbean.CompositeData;"},
        { "getThreadInfo([J,int)","[Ljavax.management.openmbean.CompositeData;"},
        { "getThreadInfo([J,boolean,boolean)","[Ljavax.management.openmbean.CompositeData;"},
        { "getThreadCpuTime(long)","long"},
        { "getThreadUserTime(long)","long"},
        { "resetPeakThreadCount()","void"},
    };
    public static void main(String[] args) throws Exception {
        int error = 0;
        int tested = 0;
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
        MBeanInfo mbi = mbs.getMBeanInfo(on);
        MBeanOperationInfo[] ops = mbi.getOperations();
        for (MBeanOperationInfo op : ops) {
            String name = op.getName();
            String rt = op.getReturnType();
            StringBuilder sb = new StringBuilder();
            sb.append(name + "(");
            for (MBeanParameterInfo param : op.getSignature()) {
                sb.append(param.getType() + ",");
            }
            int comma = sb.lastIndexOf(",");
            if (comma == -1) {
                sb.append(")");
            } else {
                sb.replace(comma, comma + 1, ")");
            }
            System.out.println("\nNAME = " + sb.toString() + "\nRETURN TYPE = " + rt);
            for (String[] rts : returnTypes) {
                if (sb.toString().equals(rts[0])) {
                    if (rt.equals(rts[1])) {
                        System.out.println("OK");
                        tested++;
                    } else {
                        System.out.println("KO: EXPECTED RETURN TYPE = " + rts[1]);
                        error++;
                    }
                }
            }
        }
        if (error > 0) {
            System.out.println("\nTEST FAILED");
            throw new Exception("TEST FAILED: " + error + " wrong return types");
        } else if (tested != returnTypes.length &&
                   !System.getProperty("java.specification.version").equals("1.5")) {
            System.out.println("\nTEST FAILED");
            throw new Exception("TEST FAILED: " + tested + " cases tested, " +
            returnTypes.length + " expected");
        } else {
            System.out.println("\nTEST PASSED");
        }
    }
}

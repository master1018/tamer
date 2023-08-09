public class IsMethodTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Test that Boolean isX() and int isX() both " +
                           "define operations not attributes");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        Object mb = new IsMethod();
        ObjectName on = new ObjectName("a:b=c");
        mbs.registerMBean(mb, on);
        MBeanInfo mbi = mbs.getMBeanInfo(on);
        boolean ok = true;
        MBeanAttributeInfo[] attrs = mbi.getAttributes();
        if (attrs.length == 0)
            System.out.println("OK: MBean defines 0 attributes");
        else {
            ok = false;
            System.out.println("TEST FAILS: MBean should define 0 attributes");
            for (int i = 0; i < attrs.length; i++) {
                System.out.println("  " + attrs[i].getType() + " " +
                                   attrs[i].getName());
            }
        }
        MBeanOperationInfo[] ops = mbi.getOperations();
        if (ops.length == 4)
            System.out.println("OK: MBean defines 4 operations");
        else {
            ok = false;
            System.out.println("TEST FAILS: MBean should define 4 operations");
        }
        for (int i = 0; i < ops.length; i++) {
            System.out.println("  " + ops[i].getReturnType() + " " +
                               ops[i].getName());
        }
        final String[] bogusAttrNames = {"", "Lost", "Thingy", "Whatsit"};
        for (int i = 0; i < bogusAttrNames.length; i++) {
            final String bogusAttrName = bogusAttrNames[i];
            try {
                mbs.getAttribute(on, bogusAttrName);
                ok = false;
                System.out.println("TEST FAILS: getAttribute(\"" +
                                   bogusAttrName + "\") should not work");
            } catch (AttributeNotFoundException e) {
                System.out.println("OK: getAttribute(" + bogusAttrName +
                                   ") got exception as expected");
            }
        }
        final String[] opNames = {"get", "getLost", "isThingy", "isWhatsit"};
        for (int i = 0; i < opNames.length; i++) {
            final String opName = opNames[i];
            try {
                mbs.invoke(on, opName, new Object[0], new String[0]);
                System.out.println("OK: invoke(\"" + opName + "\") worked");
            } catch (Exception e) {
                ok = false;
                System.out.println("TEST FAILS: invoke(" + opName +
                                   ") got exception: " + e);
            }
        }
        if (ok)
            System.out.println("Test passed");
        else {
            System.out.println("TEST FAILED");
            System.exit(1);
        }
    }
    public static interface IsMethodMBean {
        public int get();
        public void getLost();
        public Boolean isThingy();
        public int isWhatsit();
    }
    public static class IsMethod implements IsMethodMBean {
        public int get() {
            return 0;
        }
        public void getLost() {
        }
        public Boolean isThingy() {
            return Boolean.TRUE;
        }
        public int isWhatsit() {
            return 0;
        }
    }
}

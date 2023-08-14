public class InvokeGettersTest {
    public static interface ThingMBean {
        public int getWhatsit();
        public void setWhatsit(int x);
        public boolean isTrue();
    }
    public static class Thing implements ThingMBean {
        public int getWhatsit() {
            return whatsit;
        }
        public void setWhatsit(int x) {
            whatsit = x;
        }
        public boolean isTrue() {
            return true;
        }
        private int whatsit;
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        mbs.registerMBean(new Thing(), on);
        if (test(mbs, on, false))
            System.out.println("OK: invoke without jmx.invoke.getters");
        System.setProperty("jmx.invoke.getters", "true");
        if (test(mbs, on, true))
            System.out.println("OK: invoke with jmx.invoke.getters");
        if (failure == null)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static boolean test(MBeanServer mbs, ObjectName on,
                                boolean shouldWork) throws Exception {
        ++x;
        mbs.setAttribute(on, new Attribute("Whatsit", x));
        Integer got = (Integer) mbs.getAttribute(on, "Whatsit");
        if (got != x)
            return fail("Set attribute was not got: " + got);
        ++x;
        try {
            mbs.invoke(on, "setWhatsit", new Object[] {x}, new String[] {"int"});
            if (!shouldWork)
                return fail("invoke setWhatsit worked but should not have");
            System.out.println("invoke setWhatsit worked as expected");
        } catch (ReflectionException e) {
            if (shouldWork)
                return fail("invoke setWhatsit did not work but should have");
            System.out.println("set got expected exception: " + e);
        }
        try {
            got = (Integer) mbs.invoke(on, "getWhatsit", null, null);
            if (!shouldWork)
                return fail("invoke getWhatsit worked but should not have");
            if (got != x)
                return fail("Set attribute through invoke was not got: " + got);
            System.out.println("invoke getWhatsit worked as expected");
        } catch (ReflectionException e) {
            if (shouldWork)
                return fail("invoke getWhatsit did not work but should have");
            System.out.println("get got expected exception: " + e);
        }
        try {
            boolean t = (Boolean) mbs.invoke(on, "isTrue", null, null);
            if (!shouldWork)
                return fail("invoke isTrue worked but should not have");
            if (!t)
                return fail("isTrue returned false");
            System.out.println("invoke isTrue worked as expected");
        } catch (ReflectionException e) {
            if (shouldWork)
                return fail("invoke isTrue did not work but should have");
            else
                System.out.println("isTrue got expected exception: " + e);
        }
        final Object[][] badInvokes = {
            {"isWhatsit", null, null},
            {"getWhatsit", new Object[] {5}, new String[] {"int"}},
            {"setWhatsit", new Object[] {false}, new String[] {"boolean"}},
            {"getTrue", null, null},
        };
        boolean ok = true;
        for (Object[] args : badInvokes) {
            String name = (String) args[0];
            Object[] params = (Object[]) args[1];
            String[] sig = (String[]) args[2];
            String what =
                "invoke " + name + (sig == null ? "[]" : Arrays.toString(sig));
            try {
                mbs.invoke(on, name, params, sig);
                ok = fail(what + " worked but should not have");
            } catch (ReflectionException e) {
                if (e.getCause() instanceof NoSuchMethodException)
                    System.out.println(what + " failed as expected");
                else {
                    ok = fail(what + " got exception with wrong nested " +
                              "exception: " + e.getCause());
                }
            }
        }
        return ok;
    }
    private static boolean fail(String why) {
        failure = why;
        System.out.println("FAILED: " + why);
        return false;
    }
    private static int x;
    private static String failure;
}

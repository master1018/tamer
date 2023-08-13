public class TabularDataOrderTest {
    private static String failure;
    private static final String COMPAT_PROP_NAME = "jmx.tabular.data.hash.map";
    private static final String[] intNames = {
        "unus", "duo", "tres", "quatuor", "quinque", "sex", "septem",
        "octo", "novem", "decim",
    };
    private static final Map<String, Integer> stringToValue =
            new LinkedHashMap<String, Integer>();
    static {
        for (int i = 0; i < intNames.length; i++)
            stringToValue.put(intNames[i], i + 1);
    }
    public static interface TestMXBean {
        public Map<String, Integer> getMap();
    }
    public static class TestImpl implements TestMXBean {
        public Map<String, Integer> getMap() {
            return stringToValue;
        }
    }
    private static final CompositeType ct;
    private static final TabularType tt;
    static {
        try {
            ct = new CompositeType(
                    "a.b.c", "name and int",
                    new String[] {"name", "int"},
                    new String[] {"name of integer", "value of integer"},
                    new OpenType<?>[] {SimpleType.STRING, SimpleType.INTEGER});
            tt = new TabularType(
                    "d.e.f", "name and int indexed by name", ct,
                    new String[] {"name"});
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
    }
    private static TabularData makeTable() throws OpenDataException {
        TabularData td = new TabularDataSupport(tt);
        for (Map.Entry<String, Integer> entry : stringToValue.entrySet()) {
            CompositeData cd = new CompositeDataSupport(
                    ct,
                    new String[] {"name", "int"},
                    new Object[] {entry.getKey(), entry.getValue()});
            td.put(cd);
        }
        return td;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Testing standard behaviour");
        TabularData td = makeTable();
        System.out.println(td);
        int last = 0;
        boolean ordered = true;
        for (Object x : td.values()) {
            CompositeData cd = (CompositeData) x;
            String name = (String) cd.get("name");
            int value = (Integer) cd.get("int");
            System.out.println(name + " = " + value);
            if (last + 1 != value)
                ordered = false;
            last = value;
        }
        if (!ordered)
            fail("Order not preserved");
        System.out.println("Testing compatible behaviour");
        System.setProperty(COMPAT_PROP_NAME, "true");
        td = makeTable();
        System.out.println(td);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(td);
        oout.close();
        byte[] bytes = bout.toByteArray();
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(bin);
        td = (TabularData) oin.readObject();
        boolean found = false;
        for (Field f : td.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;
            f.setAccessible(true);
            Object x = f.get(td);
            if (x != null && x.getClass() == HashMap.class) {
                found = true;
                System.out.println(
                        x.getClass().getName() + " TabularDataSupport." +
                        f.getName() + " = " + x);
                break;
            }
        }
        if (!found) {
            fail("TabularDataSupport does not contain HashMap though " +
                    COMPAT_PROP_NAME + "=true");
        }
        System.clearProperty(COMPAT_PROP_NAME);
        System.out.println("Testing MXBean behaviour");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("a:b=c");
        mbs.registerMBean(new TestImpl(), name);
        TestMXBean proxy = JMX.newMXBeanProxy(mbs, name, TestMXBean.class);
        Map<String, Integer> map = proxy.getMap();
        List<String> origNames = new ArrayList<String>(stringToValue.keySet());
        List<String> proxyNames = new ArrayList<String>(map.keySet());
        if (!origNames.equals(proxyNames))
            fail("Order mangled after passage through MXBean: " + proxyNames);
        if (failure == null)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void fail(String why) {
        System.out.println("FAILED: " + why);
        failure = why;
    }
}

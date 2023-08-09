public class GcInfoCompositeType {
    private static int tested = 0;
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final ObjectName gcMXBeanPattern =
                new ObjectName("java.lang:type=GarbageCollector,*");
        Set<ObjectName> names =
                mbs.queryNames(gcMXBeanPattern, null);
        if (names.isEmpty())
            throw new Exception("Test incorrect: no GC MXBeans");
        System.gc();
        for (ObjectName n : names)
            tested += test(mbs, n);
        if (tested == 0)
            throw new Exception("No MXBeans were tested");
        System.out.println("Test passed");
    }
    private static int test(MBeanServer mbs, ObjectName n) throws Exception {
        System.out.println("Testing " + n);
        MBeanInfo mbi = mbs.getMBeanInfo(n);
        MBeanAttributeInfo lastGcAI = null;
        for (MBeanAttributeInfo mbai : mbi.getAttributes()) {
            if (mbai.getName().equals("LastGcInfo")) {
                lastGcAI = mbai;
                break;
            }
        }
        if (lastGcAI == null)
            throw new Exception("No LastGcInfo attribute");
        CompositeType declaredType =
                (CompositeType) lastGcAI.getDescriptor().getFieldValue("openType");
        checkType(declaredType);
        CompositeData cd =
                (CompositeData) mbs.getAttribute(n, "LastGcInfo");
        if (cd == null) {
            System.out.println("Value of attribute null");
            return 0;
        } else
            return checkType(cd.getCompositeType());
    }
    private static int checkType(CompositeType ct) throws Exception {
        Method[] methods = GcInfo.class.getMethods();
        Set<String> getters = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (Method m : methods) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0)
                getters.add(m.getName().substring(3));
        }
        Set<String> items = new HashSet<String>(ct.keySet());
        System.out.println("Items at start: " + items);
        final String[] surplus = {"Class", "CompositeType"};
        for (String key : ct.keySet()) {
            if (getters.remove(key))
                items.remove(key);
        }
        if (!getters.equals(new HashSet<String>(Arrays.asList(surplus)))) {
            throw new Exception("Wrong getters: " + getters);
        }
        if (items.isEmpty()) {
            System.out.println("No type-specific items");
            return 0;
        } else {
            System.out.println("Type-specific items: " + items);
            return 1;
        }
    }
}

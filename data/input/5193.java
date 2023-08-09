public class AnnotationTest {
    private static String failed = null;
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Pair {
        @DescriptorKey("x")
        int x();
        @DescriptorKey("y")
        String y();
    }
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Full {
        @DescriptorKey("class")
        Class classValue();
        @DescriptorKey("enum")
        RetentionPolicy enumValue();
        @DescriptorKey("boolean")
        boolean booleanValue();
        @DescriptorKey("stringArray")
        String[] stringArrayValue();
        @DescriptorKey("classArray")
        Class[] classArrayValue();
        @DescriptorKey("intArray")
        int[] intArrayValue();
        @DescriptorKey("enumArray")
        RetentionPolicy[] enumArrayValue();
        @DescriptorKey("booleanArray")
        boolean[] booleanArrayValue();
    }
    private static Descriptor expectedDescriptor =
        new ImmutableDescriptor(new String[] {"x", "y"},
                                new Object[] {3, "foo"});
    private static Descriptor expectedFullDescriptor =
        new ImmutableDescriptor(new String[] {
                                    "class", "enum", "boolean", "stringArray",
                                    "classArray", "intArray", "enumArray",
                                    "booleanArray",
                                },
                                new Object[] {
                                    Full.class.getName(),
                                    RetentionPolicy.RUNTIME.name(),
                                    false,
                                    new String[] {"foo", "bar"},
                                    new String[] {Full.class.getName()},
                                    new int[] {1, 2},
                                    new String[] {RetentionPolicy.RUNTIME.name()},
                                    new boolean[] {false, true},
                                });
    @Pair(x = 3, y = "foo")
    public static interface ThingMBean {
        @Pair(x = 3, y = "foo")
        @Full(classValue=Full.class,
              enumValue=RetentionPolicy.RUNTIME,
              booleanValue=false,
              stringArrayValue={"foo", "bar"},
              classArrayValue={Full.class},
              intArrayValue={1, 2},
              enumArrayValue={RetentionPolicy.RUNTIME},
              booleanArrayValue={false, true})
        int getReadOnly();
        @Pair(x = 3, y = "foo")
        void setWriteOnly(int x);
        @Pair(x = 3, y = "foo")
        int getReadWrite1();
        void setReadWrite1(int x);
        @Pair(x = 3, y = "foo")
        int getReadWrite2();
        @Pair(x = 3, y = "foo")
        void setReadWrite2(int x);
        int getReadWrite3();
        @Pair(x = 3, y = "foo")
        void setReadWrite3(int x);
        @Pair(x = 3, y = "foo")
        int operation(@Pair(x = 3, y = "foo") int p1,
                      @Pair(x = 3, y = "foo") int p2);
    }
    public static class Thing implements ThingMBean {
        @Pair(x = 3, y = "foo")
        public Thing() {}
        @Pair(x = 3, y = "foo")
        public Thing(@Pair(x = 3, y = "foo") int p1) {}
        public int getReadOnly() {return 0;}
        public void setWriteOnly(int x) {}
        public int getReadWrite1() {return 0;}
        public void setReadWrite1(int x) {}
        public int getReadWrite2() {return 0;}
        public void setReadWrite2(int x) {}
        public int getReadWrite3() {return 0;}
        public void setReadWrite3(int x) {}
        public int operation(int p1, int p2) {return 0;}
    }
    @Pair(x = 3, y = "foo")
    public static interface ThingMXBean extends ThingMBean {}
    public static class ThingImpl implements ThingMXBean {
        @Pair(x = 3, y = "foo")
        public ThingImpl() {}
        @Pair(x = 3, y = "foo")
        public ThingImpl(@Pair(x = 3, y = "foo") int p1) {}
        public int getReadOnly() {return 0;}
        public void setWriteOnly(int x) {}
        public int getReadWrite1() {return 0;}
        public void setReadWrite1(int x) {}
        public int getReadWrite2() {return 0;}
        public void setReadWrite2(int x) {}
        public int getReadWrite3() {return 0;}
        public void setReadWrite3(int x) {}
        public int operation(int p1, int p2) {return 0;}
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Testing that annotations are correctly " +
                           "reflected in Descriptor entries");
        MBeanServer mbs =
            java.lang.management.ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        Thing thing = new Thing();
        mbs.registerMBean(thing, on);
        check(mbs, on);
        mbs.unregisterMBean(on);
        ThingImpl thingImpl = new ThingImpl();
        mbs.registerMBean(thingImpl, on);
        Descriptor d = mbs.getMBeanInfo(on).getDescriptor();
        if (!d.getFieldValue("mxbean").equals("true")) {
            System.out.println("NOT OK: expected MXBean");
            failed = "Expected MXBean";
        }
        check(mbs, on);
        if (failed == null)
            System.out.println("Test passed");
        else
            throw new Exception("TEST FAILED: " + failed);
    }
    private static void check(MBeanServer mbs, ObjectName on) throws Exception {
        MBeanInfo mbi = mbs.getMBeanInfo(on);
        check(mbi);
        MBeanAttributeInfo[] attrs = mbi.getAttributes();
        for (MBeanAttributeInfo attr : attrs) {
            check(attr);
            if (attr.getName().equals("ReadOnly"))
                check("@Full", attr.getDescriptor(), expectedFullDescriptor);
        }
        MBeanOperationInfo[] ops = mbi.getOperations();
        for (MBeanOperationInfo op : ops) {
            check(op);
            check(op.getSignature());
        }
        MBeanConstructorInfo[] constrs = mbi.getConstructors();
        for (MBeanConstructorInfo constr : constrs) {
            check(constr);
            check(constr.getSignature());
        }
    }
    private static void check(DescriptorRead x) {
        check(x, x.getDescriptor(), expectedDescriptor);
    }
    private static void check(Object x, Descriptor d, Descriptor expect) {
        String fail = null;
        try {
            Descriptor u = ImmutableDescriptor.union(d, expect);
            if (!u.equals(d))
                fail = "should contain " + expect + "; is " + d;
        } catch (IllegalArgumentException e) {
            fail = e.getMessage();
        }
        if (fail == null) {
            System.out.println("OK: " + x);
        } else {
            failed = "NOT OK: Incorrect descriptor for: " + x;
            System.out.println(failed);
            System.out.println("..." + fail);
        }
    }
    private static void check(DescriptorRead[] xx) {
        for (DescriptorRead x : xx)
            check(x);
    }
}

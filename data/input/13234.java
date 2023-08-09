public class AmbiguousConstructorTest {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        System.out.println("Unambiguous case:");
        ObjectName unambigName = new ObjectName("d:type=Unambiguous");
        mbs.registerMBean(new UnambiguousImpl(), unambigName);
        System.out.println("...OK");
        System.out.println("Ambiguous case:");
        ObjectName ambigName = new ObjectName("d:type=Ambiguous");
        boolean exception = false;
        try {
            mbs.registerMBean(new AmbiguousImpl(), ambigName);
        } catch (Exception e) {
            System.out.println("...OK, got expected exception:");
            e.printStackTrace(System.out);
            exception = true;
        }
        if (!exception) {
            System.out.println("TEST FAILED: expected exception, got none");
            throw new Exception("Did not get expected exception");
        }
        System.out.println("TEST PASSED");
    }
    public static class Unambiguous {
        public byte getA() {return 0;}
        public short getB() {return 0;}
        public int getC() {return 0;}
        public long getD() {return 0;}
        @ConstructorProperties({"a", "b"})
        public Unambiguous(byte a, short b) {}
        @ConstructorProperties({"b", "c"})
        public Unambiguous(short b, int c) {}
        @ConstructorProperties({"a", "b", "c"})
        public Unambiguous(byte a, short b, int c) {}
    }
    public static class Ambiguous {
        public byte getA() {return 0;}
        public short getB() {return 0;}
        public int getC() {return 0;}
        public long getD() {return 0;}
        @ConstructorProperties({"a", "b"})
        public Ambiguous(byte a, short b) {}
        @ConstructorProperties({"b", "c"})
        public Ambiguous(short b, int c) {}
        @ConstructorProperties({"a", "b", "c", "d"})
        public Ambiguous(byte a, short b, int c, long d) {}
    }
    public static interface UnambiguousMXBean {
        public void setUnambiguous(Unambiguous x);
    }
    public static class UnambiguousImpl implements UnambiguousMXBean {
        public void setUnambiguous(Unambiguous x) {}
    }
    public static interface AmbiguousMXBean {
        public void setAmbiguous(Ambiguous x);
    }
    public static class AmbiguousImpl implements AmbiguousMXBean {
        public void setAmbiguous(Ambiguous x) {}
    }
}

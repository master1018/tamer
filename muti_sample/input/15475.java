public class ObjectNameGetInstanceTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Test that ObjectName.getInstance(ObjectName) " +
                           "preserves the order of keys in its input");
        final String nonCanonical = "d:x=y,a=b";
        ObjectName nice = new ObjectName(nonCanonical);
        if (nice.getCanonicalName().equals(nonCanonical)) {
            System.err.println("TEST IS BROKEN: INPUT ALREADY CANONICAL");
            System.exit(1);
        }
        ObjectName evil = new EvilObjectName(nonCanonical);
        ObjectName unEvil = ObjectName.getInstance(evil);
        if (unEvil instanceof EvilObjectName) {
            System.err.println("FAILS: getInstance did not banish evil");
            System.exit(1);
        }
        if (nice.equals(unEvil))
            System.out.println("Test passes: original key order preserved");
        else {
            System.out.println("FAILS: key order changed");
            System.exit(1);
        }
    }
    public static class EvilObjectName extends ObjectName {
        public EvilObjectName(String s) throws MalformedObjectNameException {
            super(s);
        }
        public String getCanonicalName() {
            return "bogus:canonical=name";
        }
    }
}

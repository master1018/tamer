public class Generify {
    public static void main(String[] args) throws Exception {
        long larg = 1234567890L;
        System.setProperty("boolean", "true");
        System.setProperty("integer", "9");
        System.setProperty("long", Long.toString(larg));
        System.setProperty("property", "propertyvalue");
        Boolean b = AccessController.doPrivileged
                        (new GetBooleanAction("boolean"));
        if (b.booleanValue() == true) {
            System.out.println("boolean test passed");
        } else {
            throw new SecurityException("boolean test failed");
        }
        Integer i = AccessController.doPrivileged
                        (new GetIntegerAction("integer"));
        if (i.intValue() == 9) {
            System.out.println("integer test passed");
        } else {
            throw new SecurityException("integer test failed");
        }
        Long l = AccessController.doPrivileged
                        (new GetLongAction("long"));
        if (l.longValue() == larg) {
            System.out.println("long test passed");
        } else {
            throw new SecurityException("long test failed");
        }
        String prop = AccessController.doPrivileged
                        (new GetPropertyAction("property"));
        if (prop.equals("propertyvalue")) {
            System.out.println("property test passed");
        } else {
            throw new SecurityException("property test failed");
        }
        File f = new File(System.getProperty("test.src", "."), "Generify.java");
        FileInputStream fis = AccessController.doPrivileged
                        (new OpenFileInputStreamAction(f));
        if (fis != null) {
            System.out.println("file test passed");
        } else {
            throw new SecurityException("file test failed");
        }
    }
}

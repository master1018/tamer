public class Test6981737 {
    public static void main(String[] args) throws Exception {
        String version = verifyProperty("java.version", "[0-9]+\\.[0-9]+\\..*");
        String major_version_spec = version.split("\\.")[1];
        int major_version = new Integer(major_version_spec).intValue();
        String vendor_re = "Oracle Corporation";
        String vm_spec_version_re = "1\\." + major_version_spec;
        if (major_version < 7) {
            vendor_re = "Sun Microsystems Inc\\.";
            vm_spec_version_re = "1\\.0";
        }
        verifyProperty("java.vendor", vendor_re);
        verifyProperty("java.vm.vendor", vendor_re);
        verifyProperty("java.vm.specification.vendor", vendor_re);
        verifyProperty("java.specification.vendor", vendor_re);
        verifyProperty("java.vm.specification.version", vm_spec_version_re);
        System.out.println("PASS");
    }
    public static String verifyProperty(String name, String expected_re) {
        String value = System.getProperty(name, "");
        System.out.print("Checking " + name + ": \"" + value +
          "\".matches(\"" + expected_re + "\")... ");
        if (!value.matches(expected_re)) {
            System.out.println("no.");
            throw new RuntimeException("FAIL: Wrong value for " + name +
                " property, \"" + value + "\", expected to be of form: \"" +
                expected_re + "\"");
        }
        System.out.println("yes.");
        return value;
    }
}

public class SystemRegistryTest {
    private static String registryDir = System.getProperty("test.classes");
    private static String servicetagDir = System.getProperty("test.src");
    private static List<ServiceTag> list = new ArrayList<ServiceTag>();
    private static String[] files = new String[] {
                                        "servicetag1.properties",
                                        "servicetag2.properties",
                                        "servicetag3.properties"
                                    };
    private static Registry registry;
    public static void main(String[] argv) throws Exception {
        try {
            registry = Util.getSvcTagClientRegistry();
            runTest();
        } finally {
            Util.emptyRegistryFile();
        }
    }
    private static void runTest() throws Exception {
        for (String filename : files) {
            File f = new File(servicetagDir, filename);
            ServiceTag svcTag = Util.newServiceTag(f);
            ServiceTag st = registry.addServiceTag(svcTag);
            list.add(st);
            System.out.println(st);
        }
        testDuplicate(list.get(0));
        testNotFound();
        String urn = list.get(0).getInstanceURN();
        ServiceTag svcTag = registry.removeServiceTag(urn);
        if (!Util.matches(svcTag, list.get(0))) {
           throw new RuntimeException(urn +
               " deleted but does not match.");
        }
        svcTag = list.get(1);
        urn = svcTag.getInstanceURN();
        ServiceTag st = registry.getServiceTag(urn);
        if (!Util.matches(svcTag, st)) {
           throw new RuntimeException(urn +
               " returned from getServiceTag but does not match.");
        }
        registry.updateServiceTag(urn, "My new defined ID");
        st = registry.getServiceTag(urn);
        if (Util.matches(svcTag, st)) {
           throw new RuntimeException(urn +
               " updated but expected to be different.");
        }
        if (!st.getProductDefinedInstanceID().equals("My new defined ID")) {
            throw new RuntimeException("Invalid product_defined_instance_id " +
                st.getProductDefinedInstanceID());
        }
        if (st.getInstallerUID() != -1) {
            throw new RuntimeException("Invalid installer_uid " +
                st.getInstallerUID());
        }
        if (st.getTimestamp().equals(svcTag.getTimestamp())) {
            throw new RuntimeException("Timestamp " +
                st.getTimestamp() + " == " + svcTag.getTimestamp());
        }
    }
    private static void testDuplicate(ServiceTag st) throws IOException {
        boolean dup = false;
        try {
           registry.addServiceTag(st);
        } catch (IllegalArgumentException e) {
           dup = true;
        }
        if (!dup) {
           throw new RuntimeException(st.getInstanceURN() +
               " added successfully but expected to be a duplicated.");
        }
    }
    private static void testNotFound() throws Exception {
        String instanceURN = "urn:st:721cf98a-f4d7-6231-bb1d-f2f5aa903ef7";
        ServiceTag svctag = registry.removeServiceTag(instanceURN);
        if (svctag != null) {
           throw new RuntimeException(instanceURN +
               " exists but expected not found");
        }
        svctag = registry.updateServiceTag(instanceURN, "testing");
        if (svctag != null) {
           throw new RuntimeException(instanceURN +
               " exists but expected not found");
        }
    }
}

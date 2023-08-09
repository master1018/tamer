public class UpdateServiceTagTest {
    private static String servicetagDir = System.getProperty("test.src");
    private static String[] files = new String[] {
                                        "servicetag1.properties",
                                        "servicetag2.properties",
                                        "servicetag3.properties"
                                    };
    private static RegistrationData registration = new RegistrationData();
    private static Set<ServiceTag> set = new HashSet<ServiceTag>();
    public static void main(String[] argv) throws Exception {
        for (String f : files) {
            ServiceTag st = addServiceTag(f);
            set.add(st);
        }
        Thread.sleep(1000);
        for (ServiceTag st : set) {
            updateServiceTag(st);
        }
    }
    private static ServiceTag addServiceTag(String filename) throws Exception {
        File f = new File(servicetagDir, filename);
        ServiceTag svcTag = Util.newServiceTag(f, true );
        ServiceTag st = registration.addServiceTag(svcTag);
        if (!Util.matchesNoInstanceUrn(svcTag, st)) {
            throw new RuntimeException("ServiceTag " +
                " doesn't match.");
        }
        String urn = st.getInstanceURN();
        if (!urn.startsWith("urn:st:")) {
            throw new RuntimeException("Invalid generated instance_urn " +
                urn);
        }
        if (st.getInstallerUID() != -1) {
            throw new RuntimeException("Invalid installer_uid " +
                st.getInstallerUID());
        }
        if (st.getTimestamp() == null) {
            throw new RuntimeException("null timestamp ");
        }
        return st;
    }
    private static String newID = "New product defined instance ID";
    private static void updateServiceTag(ServiceTag svcTag) throws Exception {
        String urn = svcTag.getInstanceURN();
        registration.updateServiceTag(urn, newID);
        ServiceTag st = registration.getServiceTag(urn);
        if (Util.matches(svcTag, st)) {
            throw new RuntimeException("ServiceTag " +
                " should not match.");
        }
        if (!st.getProductDefinedInstanceID().equals(newID)) {
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
}

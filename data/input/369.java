public class InstanceUrnCheck {
    private static String servicetagDir = System.getProperty("test.src");
    private static String[] files = new String[] {
                                        "servicetag1.properties",
                                        "servicetag2.properties",
                                        "servicetag3.properties"
                                    };
    private static RegistrationData registration = new RegistrationData();
    public static void main(String[] argv) throws Exception {
        for (String f : files) {
            addServiceTag(f);
        }
    }
   private static void addServiceTag(String filename) throws Exception {
        File f = new File(servicetagDir, filename);
        ServiceTag svcTag = Util.newServiceTag(f, true );
        ServiceTag st = registration.addServiceTag(svcTag);
        if (!Util.matchesNoInstanceUrn(svcTag, st)) {
            throw new RuntimeException("ServiceTag " +
                " doesn't match.");
        }
        System.out.println("New service tag instance_urn=" + st.getInstanceURN());
        if (!st.getInstanceURN().startsWith("urn:st:")) {
            throw new RuntimeException("Invalid generated instance_urn " +
                st.getInstanceURN());
        }
        if (st.getInstallerUID() != -1) {
            throw new RuntimeException("Invalid installer_uid " +
                st.getInstallerUID());
        }
        if (st.getTimestamp() == null) {
            throw new RuntimeException("null timestamp ");
        }
    }
}

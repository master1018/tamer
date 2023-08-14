public class DuplicateNotFound {
    private static String servicetagDir = System.getProperty("test.src");
    private static String[] files = new String[] {
                                        "servicetag1.properties",
                                        "servicetag2.properties",
                                        "servicetag3.properties"
                                    };
    private static RegistrationData registration = new RegistrationData();
    public static void main(String[] argv) throws Exception {
        ServiceTag svcTag;
        registration.addServiceTag(loadServiceTag(files[0]));
        registration.addServiceTag(loadServiceTag(files[1]));
        testDuplicate(files[0]);
        testDuplicate(files[1]);
        testNotFound(files[2]);
    }
    private static void testDuplicate(String filename) throws Exception {
        boolean dup = false;
        try {
           registration.addServiceTag(loadServiceTag(filename));
        } catch (IllegalArgumentException e) {
           dup = true;
        }
        if (!dup) {
           throw new RuntimeException(filename +
               " added successfully but expected to be a duplicated.");
        }
    }
    private static void testNotFound(String filename) throws Exception {
        ServiceTag st = loadServiceTag(filename);
        ServiceTag svctag = registration.getServiceTag(st.getInstanceURN());
        if (svctag != null) {
           throw new RuntimeException(st.getInstanceURN() +
               " exists but expected not found");
        }
        svctag = registration.removeServiceTag(st.getInstanceURN());
        if (svctag != null) {
           throw new RuntimeException(st.getInstanceURN() +
               " exists but expected not found");
        }
        svctag = registration.updateServiceTag(st.getInstanceURN(), "testing");
        if (svctag != null) {
           throw new RuntimeException(st.getInstanceURN() +
               " updated successfully but expected not found.");
        }
    }
    private static ServiceTag loadServiceTag(String filename) throws Exception {
        File f = new File(servicetagDir, filename);
        return Util.newServiceTag(f);
    }
}

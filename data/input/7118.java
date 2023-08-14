public class JavaServiceTagTest1 {
    private static String registrationDir = System.getProperty("test.classes");
    private static String servicetagDir = System.getProperty("test.src");
    private static File regFile;
    private static File svcTagFile;
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
        System.setProperty("servicetag.dir.path", registrationDir);
        regFile = new File(registrationDir, "registration.xml");
        regFile.delete();
        svcTagFile = new File(registrationDir, "servicetag");
        svcTagFile.delete();
        ServiceTag st1 = testJavaServiceTag("Test1");
        ServiceTag st2 = testJavaServiceTag("Test2");
        if (registry.getServiceTag(st1.getInstanceURN()) != null) {
            throw new RuntimeException("instance_urn: " + st1.getInstanceURN() +
                " exists but expected to be removed");
        }
        if (st1.getInstanceURN().equals(st2.getInstanceURN())) {
            throw new RuntimeException("instance_urn: " + st1.getInstanceURN() +
                " == " + st2.getInstanceURN());
        }
        if (registry.removeServiceTag(st2.getInstanceURN()) == null) {
            throw new RuntimeException("Failed to remove " +
                st1.getInstanceURN() + " from the registry");
        }
        svcTagFile.delete();
        ServiceTag st3 = testJavaServiceTag("Test2");
        if (!Util.matches(st2, st3)) {
            System.out.println(st2);
            System.out.println(st3);
            throw new RuntimeException("Test Failed: Expected to be the same");
        }
    }
    private static ServiceTag testJavaServiceTag(String source) throws Exception {
        ServiceTag svctag = ServiceTag.getJavaServiceTag(source);
        checkServiceTag(svctag, source);
        if (!regFile.exists()) {
            throw new RuntimeException(regFile + " not created.");
        }
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(regFile));
        RegistrationData registration = RegistrationData.loadFromXML(in);
        Set<ServiceTag> c = registration.getServiceTags();
        if (c.size() != 1) {
            throw new RuntimeException(regFile + " has " + c.size() +
                " service tags. Expected 1.");
        }
        ServiceTag st = registration.getServiceTag(svctag.getInstanceURN());
        if (!Util.matches(st, svctag)) {
            throw new RuntimeException("RegistrationData ServiceTag " +
                " doesn't match.");
        }
        st = registry.getServiceTag(svctag.getInstanceURN());
        if (!Util.matches(st, svctag)) {
            throw new RuntimeException("Registry ServiceTag " +
                " doesn't match.");
        }
        if (!svcTagFile.exists()) {
            throw new RuntimeException(svcTagFile + " not created.");
        }
        BufferedReader reader = new BufferedReader(new FileReader(svcTagFile));
        int count = 0;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(svctag.getInstanceURN())) {
                    count++;
                } else {
                    throw new RuntimeException("servicetag contains " +
                        " unexpected instance_urn " + line);
                }
            }
        } finally {
            reader.close();
        }
        if (count != 1) {
            throw new RuntimeException("servicetag contains unexpected " +
                "number of instance_urn = " + count);
        }
        return svctag;
    }
    private static void checkServiceTag(ServiceTag st, String source)
            throws IOException {
        Properties props = loadSwordfishEntries();
        if (st.getProductURN().
                equals(props.getProperty("servicetag.jdk.urn"))) {
            if (!st.getProductName().
                    equals(props.getProperty("servicetag.jdk.name"))) {
                throw new RuntimeException("Product URN and name don't match.");
            }
        } else if (st.getProductURN().
                equals(props.getProperty("servicetag.jre.urn"))) {
            if (!st.getProductName().
                    equals(props.getProperty("servicetag.jre.name"))) {
                throw new RuntimeException("Product URN and name don't match.");
            }
        } else {
            throw new RuntimeException("Unexpected product_urn: " +
                st.getProductURN());
        }
        if (!st.getProductVersion().
                equals(System.getProperty("java.version"))) {
            throw new RuntimeException("Unexpected product_version: " +
                st.getProductVersion());
        }
        if (!st.getProductParent().
                equals(props.getProperty("servicetag.parent.name"))) {
            throw new RuntimeException("Unexpected product_parent: " +
                st.getProductParent());
        }
        if (!st.getProductParentURN().
                equals(props.getProperty("servicetag.parent.urn"))) {
            throw new RuntimeException("Unexpected product_parent_urn: " +
                st.getProductParentURN());
        }
        if (!st.getPlatformArch().
                equals(System.getProperty("os.arch"))) {
            throw new RuntimeException("Unexpected platform_arch: " +
                st.getPlatformArch());
        }
        String vendor = System.getProperty("java.vendor");
        if (!st.getProductVendor().
                equals(vendor)) {
            throw new RuntimeException("Unexpected product_vendor: " +
                st.getProductVendor());
        }
        if (!st.getSource().
                equals(source)) {
            throw new RuntimeException("Unexpected source: " +
                st.getSource() + " expected: " + source);
        }
        String[] ss = st.getProductDefinedInstanceID().split(",");
        boolean id = false;
        boolean dir = false;
        for (String s : ss) {
            String[] values = s.split("=");
            if (values[0].equals("id")) {
                id = true;
                String[] sss = values[1].split(" ");
                if (!sss[0].equals(System.getProperty("java.runtime.version"))) {
                    throw new RuntimeException("Unexpected version in id: " +
                        sss[0]);
                }
                if (sss.length < 2) {
                    throw new RuntimeException("Unexpected id=" + values[1]);
                }
            } else if (values[0].equals("dir")) {
                dir = true;
            }
        }
        if (!id || !dir) {
            throw new RuntimeException("Unexpected product_defined_instance_id: " +
                st.getProductDefinedInstanceID());
        }
    }
    private static Properties loadSwordfishEntries()
           throws IOException {
        int version = sun.misc.Version.jdkMinorVersion();
        String filename = "/com/sun/servicetag/resources/javase_" +
                version + "_swordfish.properties";
        InputStream in = Installer.class.getClass().getResourceAsStream(filename);
        Properties props = new Properties();
        try {
            props.load(in);
        } finally {
            in.close();
        }
        return props;
    }
}

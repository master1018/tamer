public class JavaServiceTagTest {
    public static void main(String[] argv) throws Exception {
        String registrationDir = System.getProperty("test.classes");
        System.setProperty("servicetag.sthelper.supported", "false");
        if (Registry.isSupported()) {
            throw new RuntimeException("Registry.isSupported() should " +
                "return false");
        }
        System.setProperty("servicetag.dir.path", registrationDir);
        File regFile = new File(registrationDir, "registration.xml");
        regFile.delete();
        File svcTagFile = new File(registrationDir, "servicetag");
        svcTagFile.delete();
        ServiceTag svctag = ServiceTag.getJavaServiceTag("JavaServiceTagTest");
        checkServiceTag(svctag);
        if (svcTagFile.exists()) {
            throw new RuntimeException(svcTagFile + " should not exist.");
        }
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
            throw new RuntimeException("ServiceTag " +
                " doesn't match.");
        }
    }
    private static void checkServiceTag(ServiceTag st) throws IOException {
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
                equals("JavaServiceTagTest")) {
            throw new RuntimeException("Unexpected source: " +
                st.getSource());
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

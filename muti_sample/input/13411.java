public class DeleteServiceTag {
    private static RegistrationData registration;
    private static File regFile;
    private static Map<String, ServiceTag> stMap =
        new LinkedHashMap<String, ServiceTag>();
    private static String[] files = new String[] {
                                        "servicetag1.properties",
                                        "servicetag2.properties",
                                        "servicetag3.properties"
                                    };
    public static void main(String[] argv) throws Exception {
        String registrationDir = System.getProperty("test.classes");
        String servicetagDir = System.getProperty("test.src");
        File original = new File(servicetagDir, "registration.xml");
        regFile = new File(registrationDir, "registration.xml");
        copyRegistrationXML(original, regFile);
        for (String f : files) {
            File stfile = new File(servicetagDir, f);
            ServiceTag svcTag = Util.newServiceTag(stfile);
            stMap.put(svcTag.getInstanceURN(), svcTag);
        }
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(regFile));
        registration = RegistrationData.loadFromXML(in);
        if (stMap.size() != files.length) {
            throw new RuntimeException("Invalid service tag count= " +
                stMap.size() + " expected=" + files.length);
        }
        Util.checkRegistrationData(regFile.getCanonicalPath(), stMap);
        deleteServiceTag(servicetagDir, files[0]);
        System.out.println("Test passed: service tags deleted.");
    }
    private static void copyRegistrationXML(File from, File to) throws IOException {
        to.delete();
        BufferedReader reader = new BufferedReader(new FileReader(from));
        PrintWriter writer = new PrintWriter(to);
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
            writer.flush();
        } finally {
            writer.close();
        }
    }
    private static void deleteServiceTag(String parent, String filename) throws Exception {
        File f = new File(parent, filename);
        ServiceTag svcTag = Util.newServiceTag(f);
        ServiceTag st = registration.removeServiceTag(svcTag.getInstanceURN());
        if (st == null) {
            throw new RuntimeException("RegistrationData.remove method" +
                " returns null");
        }
        if (!Util.matches(st, svcTag)) {
            throw new RuntimeException("ServiceTag added in the registration " +
                " doesn't match.");
        }
        Util.checkRegistrationData(regFile.getCanonicalPath(), stMap);
        ServiceTag st1 = registration.getServiceTag(svcTag.getInstanceURN());
        if (st1 != null) {
            throw new RuntimeException("RegistrationData.get method returns " +
                "non-null.");
        }
        stMap.remove(svcTag.getInstanceURN());
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(regFile));
        try {
            registration.storeToXML(out);
        } finally {
            out.close();
        }
    }
}

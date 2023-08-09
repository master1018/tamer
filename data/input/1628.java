public class NewRegistrationData {
    private static RegistrationData regData;
    private static Map<String, ServiceTag> stMap = new LinkedHashMap<String, ServiceTag>();
    private static String[] files = new String[] {
                                        "servicetag1.properties",
                                        "servicetag2.properties",
                                        "servicetag3.properties"
                                    };
    public static void main(String[] argv) throws Exception {
        String regDataDir = System.getProperty("test.classes");
        String servicetagDir = System.getProperty("test.src");
        File reg = new File(regDataDir, "registration.xml");
        reg.delete();
        regData = new RegistrationData();
        if (regData.getRegistrationURN().isEmpty()) {
            throw new RuntimeException("Empty registration urn");
        }
        int count = 0;
        for (String f : files) {
            addServiceTag(servicetagDir, f, ++count);
        }
        Set<ServiceTag> c = regData.getServiceTags();
        for (ServiceTag st : c) {
            if (!Util.matches(st, regData.getServiceTag(st.getInstanceURN()))) {
                throw new RuntimeException("ServiceTag added in the regData " +
                    " doesn't match.");
            }
        }
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(reg));
        try {
            regData.storeToXML(out);
        } finally {
            out.close();
        }
        Util.checkRegistrationData(reg.getCanonicalPath(), stMap);
        System.out.println("Test passed: " + count + " service tags added");
    }
    private static void addServiceTag(String parent, String filename, int count) throws Exception {
        File f = new File(parent, filename);
        ServiceTag svcTag = Util.newServiceTag(f);
        regData.addServiceTag(svcTag);
        stMap.put(svcTag.getInstanceURN(), svcTag);
        Set<ServiceTag> c = regData.getServiceTags();
        if (c.size() != count) {
            throw new RuntimeException("Invalid service tag count= " +
                c.size() + " expected=" + count);
        }
        ServiceTag st = regData.getServiceTag(svcTag.getInstanceURN());
        if (!Util.matches(st, svcTag)) {
            throw new RuntimeException("ServiceTag added in the regData " +
                " doesn't match.");
        }
    }
}

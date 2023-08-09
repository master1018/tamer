public class ValidRegistrationData {
    private static String registrationDir = System.getProperty("test.classes");
    private static String servicetagDir = System.getProperty("test.src");
    private static RegistrationData registration;
    private static Map<String, ServiceTag> stMap =
        new LinkedHashMap<String, ServiceTag>();
    private static String[] files = new String[] {
                                        "servicetag1.properties",
                                        "servicetag2.properties",
                                        "servicetag3.properties"
                                    };
    private static String URN = "urn:st:9543ffaa-a4f1-4f77-b2d1-f561922d4e4a";
    public static void main(String[] argv) throws Exception {
        File f = new File(servicetagDir, "registration.xml");
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        registration = RegistrationData.loadFromXML(in);
        if (!registration.getRegistrationURN().equals(URN)){
            throw new RuntimeException("Invalid URN=" +
                registration.getRegistrationURN());
        }
        Map<String,String> environMap = registration.getEnvironmentMap();
        checkEnvironmentMap(environMap);
        setInvalidEnvironment("hostname", "");
        setInvalidEnvironment("osName", "");
        setInvalidEnvironment("invalid", "");
    }
    private static void checkEnvironmentMap(Map<String,String> envMap)
            throws Exception {
        Properties props = new Properties();
        File f = new File(servicetagDir, "environ.properties");
        FileReader reader = new FileReader(f);
        try {
            props.load(reader);
        } finally {
            reader.close();
        }
        for (Map.Entry<String,String> entry : envMap.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            String expected = props.getProperty(name);
            if (expected == null || !value.equals(expected)) {
                throw new RuntimeException("Invalid environment " +
                    name + "=" + value);
            }
            props.remove(name);
        }
        if (!props.isEmpty()) {
            System.out.println("Environment missing: ");
            for (String s : props.stringPropertyNames()) {
                System.out.println("   " + s + "=" + props.getProperty(s));
            }
            throw new RuntimeException("Invalid environment read");
        }
    }
    private static void setInvalidEnvironment(String name, String value) {
        boolean invalid = false;
        try {
            registration.setEnvironment(name, value);
        } catch (IllegalArgumentException e) {
            invalid = true;
        }
        if (!invalid) {
           throw new RuntimeException(name + "=" + value +
               " set but expected to fail.");
        }
    }
}

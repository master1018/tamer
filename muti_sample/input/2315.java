public class GetInstanceSecurity {
    private static final String JAVA_CONFIG = "JavaLoginConfig";
    public static void main(String[] args) throws Exception {
        try {
            Configuration c = Configuration.getInstance(JAVA_CONFIG, null);
            throw new RuntimeException("did not catch security exception");
        } catch (SecurityException se) {
        }
        try {
            Configuration c = Configuration.getInstance
                        (JAVA_CONFIG, null, "SUN");
            throw new RuntimeException("did not catch security exception");
        } catch (SecurityException se) {
        }
        try {
            Configuration c = Configuration.getInstance
                        (JAVA_CONFIG, null, Security.getProvider("SUN"));
            throw new RuntimeException("did not catch security exception");
        } catch (SecurityException se) {
        }
        File file = new File(System.getProperty("test.src", "."),
                                "GetInstanceSecurity.grantedPolicy");
        URI uri = file.toURI();
        URIParameter param = new URIParameter(uri);
        Policy p = Policy.getInstance("JavaPolicy", param, "SUN");
        Policy.setPolicy(p);
        file = new File(System.getProperty("test.src", "."),
                        "GetInstance.config");
        URIParameter uriParam = new URIParameter(file.toURI());
        try {
            Configuration c = Configuration.getInstance(JAVA_CONFIG, uriParam);
        } catch (SecurityException se) {
            throw new RuntimeException("unexpected SecurityException");
        }
        try {
            Configuration c = Configuration.getInstance
                        (JAVA_CONFIG, uriParam, "SUN");
        } catch (SecurityException se) {
            throw new RuntimeException("unexpected SecurityException");
        }
        try {
            Configuration c = Configuration.getInstance
                        (JAVA_CONFIG, uriParam, Security.getProvider("SUN"));
        } catch (SecurityException se) {
            throw new RuntimeException("unexpected SecurityException");
        }
        System.out.println("test passed");
    }
}

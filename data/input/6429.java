public class SecurityPropFile {
    public static void main(String[] args) {
        System.out.println(java.security.Security.getProperty
                                ("policy.provider"));
        System.out.println(java.security.Security.getProperty
                                ("policy.url.1"));
        System.out.println(java.security.Security.getProperty
                                ("policy.url.2"));
    }
}

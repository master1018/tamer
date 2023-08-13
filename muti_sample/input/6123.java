public class BadKdcDefaultValue {
    public static void main(String[] args) throws Exception {
        if (!"tryLast".equalsIgnoreCase(
                Security.getProperty("krb5.kdc.bad.policy"))) {
            throw new Exception("Default value not correct");
        }
    }
}

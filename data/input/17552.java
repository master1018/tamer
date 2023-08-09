public class CheckAccess {
    public static final void main(String[] args) throws Exception {
        try {
            Class clazz = Class.forName("com.sun.jndi.dns.DnsContext");
            Field field = clazz.getField("debug");
            if (Modifier.isPublic(field.getModifiers())) {
                throw new Exception(
                    "class member 'debug' must not be publicly accessible");
            }
        } catch (NoSuchFieldException e) {
        }
    }
}

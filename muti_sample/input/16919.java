public class SomeExtensionClass {
    public String getUserName() {
        String user = (String) AccessController.doPrivileged(
                new PrivilegedAction() {
            public Object run() {
                return System.getProperty("user.name");
            }
        }
        );
        return user;
    }
}

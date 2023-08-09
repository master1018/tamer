public class GrantAllPermToExtWhenNoPolicy {
    public static void main(String[] args) throws Exception {
        SomeExtensionClass sec = new SomeExtensionClass();
        try {
            sec.getUserName();
        } catch (AccessControlException ace) {
            throw new Exception("Cannot read user name");
        }
    }
}

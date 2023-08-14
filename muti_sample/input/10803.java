public class EmptyValue {
    public static void main(String[] args) throws Exception {
        X500Principal p = new X500Principal("cn=");
        System.out.println(p);
        System.out.println("Test Passed");
    }
}

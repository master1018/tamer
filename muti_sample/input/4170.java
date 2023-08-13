public class FontPrivilege {
    public static void main(String[] args) throws Exception {
        System.setSecurityManager(new SecurityManager());
        new Font("Helvetica", Font.PLAIN, 12).getFamily();
        new Font("foo bar", Font.PLAIN, 12).getFamily();
   }
}

public class UnescapeTest  {
    public static void main(String[] args) {
        try {
            System.out.println(LdapName.unescapeAttributeValue("\\uvw"));
        } catch (IllegalArgumentException e) {
            System.out.println("Caught the right exception");
        }
    }
}

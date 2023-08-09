public class Case {
    public static void main(String[] args) throws Exception {
        LdapName name = new LdapName("cn=Kuwabatake Sanjuro");
        name.setValuesCaseSensitive(false);
        name.size();    
    }
}

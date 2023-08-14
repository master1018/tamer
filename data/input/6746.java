public class CreateLdapPrincipals {
    public static void main(String[] args) throws Exception {
        Set<Principal> principals = new Subject().getPrincipals();
        principals.add(new LdapPrincipal("x=y"));
        principals.add(new LdapPrincipal("x=#04024869"));
        principals.add(new LdapPrincipal("1.2.3=x"));
        principals.add(new LdapPrincipal("A=B"));
        principals.add(new LdapPrincipal("a=b+c=d"));
        principals.add(new LdapPrincipal("a=b,c=d,e=f"));
        principals.add(new LdapPrincipal("f=g, h=i, j=k"));
        System.out.println("Successfully created " + principals.size() +
            " LDAP principals:");
        System.out.println(principals);
    }
}

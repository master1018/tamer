public class LdapNameConstruction {
    public static void main(String args[])
                throws Exception {
        Rdn rdn1 = new Rdn("ou= Juicy\\, Fruit");
        System.out.println("rdn1:" + rdn1.toString());
        Rdn rdn2 = new Rdn(rdn1);
        System.out.println("rdn2:" + rdn2.toString());
        Attributes attrs = new BasicAttributes();
        attrs.put("ou", "Juicy, Fruit");
        attrs.put("cn", "Mango");
        Rdn rdn3 = new Rdn(attrs);
        System.out.println("rdn3:" + rdn3.toString());
        Rdn rdn4 = new Rdn("ou", "Juicy, Fruit");
        System.out.println("rdn4:" + rdn4.toString());
        Rdn rdn5 = new Rdn("SN=Lu\\C4\\8Di\\C4\\87");
        System.out.println("rdn5:" + rdn5.toString());
        List rdns = new ArrayList();
        rdns.add(new Rdn("o=Food"));
        rdns.add(new Rdn("ou=Fruits"));
        rdns.add(rdn3);
        LdapName name1 = new LdapName(rdns);
        System.out.println("ldapname1:" + name1.toString());
        LdapName name2 = new LdapName(
                "ou=Juicy\\, Fruit + cn = Mango, ou=Fruits, o=Food");
        System.out.println("ldapName2:" + name2.toString());
        if (!name2.equals(name1)) {
            throw new Exception("ldapname1 does not equals ldapname2");
        }
        System.out.println("ldapname1 and ldapname2 are equal");
        LdapName name = new LdapName(new ArrayList());
        System.out.println("Empty ldapname:" + name);
    }
}

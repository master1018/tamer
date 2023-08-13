public class AllAttribs {
    public static void main(String[] args) throws Exception {
        X500Name name = null;
        name = new X500Name("dnq=example_ens, "
                            + "t=mr, "
                            + "cn=john doe, "
                            + "givenname=john, "
                            + "surname=doe, "
                            + "initials=jd, "
                            + "generation=Sr, "
                            + "s=xxx gogo drive, "
                            + "l=peak park, "
                            + "st=california, "
                            + "c=usa, "
                            + "ou=example software, "
                            + "o=example com, "
                            + "ip=1.2.3.4, "
                            + "dc=eng, "
                            + "dc=example, "
                            + "dc=com");
        System.out.println("Name was constructed with keyword dnq");
        System.out.println("toString of name is:\n"+name);
        name = new X500Name("dnqualifier=example_ens, "
                            + "t=mr, "
                            + "cn=john doe, "
                            + "givenname=john, "
                            + "surname=doe, "
                            + "initials=jd, "
                            + "generation=Sr, "
                            + "s=xxx gogo drive, "
                            + "l=peak park, "
                            + "st=california, "
                            + "c=usa, "
                            + "ou=example software, "
                            + "o=example com, "
                            + "ip=1.2.3.4, "
                            + "dc=eng, "
                            + "dc=example, "
                            + "dc=com");
        System.out.println("Name was constructed with keyword dnqualifier");
        System.out.println("toString of name is:\n"+name);
    }
}

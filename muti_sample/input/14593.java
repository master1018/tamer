class LdapNameParser implements NameParser {
    public LdapNameParser() {
    }
    public Name parse(String name) throws NamingException {
        return new LdapName(name);
    }
}

class DnsNameParser implements NameParser {
    public Name parse(String name) throws NamingException {
        return new DnsName(name);
    }
    public boolean equals(Object obj) {
        return (obj instanceof DnsNameParser);
    }
    public int hashCode() {
        return DnsNameParser.class.hashCode() + 1;
    }
}

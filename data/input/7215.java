final class LdapEntry {
    String DN;
    Attributes attributes;
    Vector respCtls = null;
    LdapEntry(String DN, Attributes attrs) {
        this.DN = DN;
        this.attributes = attrs;
    }
    LdapEntry(String DN, Attributes attrs, Vector respCtls) {
        this.DN = DN;
        this.attributes = attrs;
        this.respCtls = respCtls;
    }
}

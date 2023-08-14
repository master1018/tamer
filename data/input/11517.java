class JDMInformBlock extends SimpleNode {
    JDMInformBlock(int id) {
        super(id);
    }
    JDMInformBlock(Parser p, int id) {
        super(p, id);
    }
    public static Node jjtCreate(int id) {
        return new JDMInformBlock(id);
    }
    public static Node jjtCreate(Parser p, int id) {
        return new JDMInformBlock(p, id);
    }
    public void buildAclEntries(PrincipalImpl owner, AclImpl acl) {}
    public void buildTrapEntries(Hashtable dest) {}
}

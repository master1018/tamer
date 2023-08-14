class JDMTrapBlock extends SimpleNode {
  JDMTrapBlock(int id) {
    super(id);
  }
  JDMTrapBlock(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMTrapBlock(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMTrapBlock(p, id);
  }
   public void buildAclEntries(PrincipalImpl owner, AclImpl acl) {}
   public void buildInformEntries(Hashtable dest) {}
}

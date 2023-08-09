class JDMAclItem extends SimpleNode {
  protected JDMAccess access= null;
  protected JDMCommunities com= null;
  JDMAclItem(int id) {
    super(id);
  }
  JDMAclItem(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMAclItem(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMAclItem(p, id);
  }
  public JDMAccess getAccess() {
    return access;
  }
  public JDMCommunities getCommunities() {
    return com;
  }
}

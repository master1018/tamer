class JDMSecurityDefs extends SimpleNode {
  JDMSecurityDefs(int id) {
    super(id);
  }
  JDMSecurityDefs(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMSecurityDefs(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMSecurityDefs(p, id);
  }
}

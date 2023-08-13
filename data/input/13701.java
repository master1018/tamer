class JDMManagers extends SimpleNode {
  JDMManagers(int id) {
    super(id);
  }
  JDMManagers(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMManagers(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMManagers(p, id);
  }
}

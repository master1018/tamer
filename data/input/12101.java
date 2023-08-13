class JDMHost extends SimpleNode {
  JDMHost(int id) {
    super(id);
  }
  JDMHost(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMHost(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMHost(p, id);
  }
}

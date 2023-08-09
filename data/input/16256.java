class JDMEnterprise extends SimpleNode {
  protected String enterprise= "";
  JDMEnterprise(int id) {
    super(id);
  }
  JDMEnterprise(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMEnterprise(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMEnterprise(p, id);
  }
}

class JDMHostTrap extends SimpleNode {
  protected String name= "";
  JDMHostTrap(int id) {
    super(id);
  }
  JDMHostTrap(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMHostTrap(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMHostTrap(p, id);
  }
}

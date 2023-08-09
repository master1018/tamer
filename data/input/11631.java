class JDMTrapNum extends SimpleNode {
  protected int low=0;
  protected int high=0;
  JDMTrapNum(int id) {
    super(id);
  }
  JDMTrapNum(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMTrapNum(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMTrapNum(p, id);
  }
}

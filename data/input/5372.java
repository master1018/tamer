class JDMTrapInterestedHost extends SimpleNode {
  JDMTrapInterestedHost(int id) {
    super(id);
  }
  JDMTrapInterestedHost(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMTrapInterestedHost(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMTrapInterestedHost(p, id);
  }
}

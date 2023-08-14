class JDMTrapItem extends SimpleNode {
  protected JDMTrapCommunity comm = null;
  JDMTrapItem(int id) {
    super(id);
  }
  JDMTrapItem(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMTrapItem(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMTrapItem(p, id);
  }
  public JDMTrapCommunity getCommunity(){
        return comm;
  }
}

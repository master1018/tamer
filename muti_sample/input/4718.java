class JDMAclBlock extends SimpleNode {
  JDMAclBlock(int id) {
    super(id);
  }
  JDMAclBlock(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMAclBlock(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMAclBlock(p, id);
  }
   public void buildTrapEntries(Hashtable dest) {}
   public void buildInformEntries(Hashtable dest) {}
}

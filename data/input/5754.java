class JDMCommunity extends SimpleNode {
  protected String communityString= "";
  JDMCommunity(int id) {
    super(id);
  }
  JDMCommunity(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMCommunity(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMCommunity(p, id);
  }
  public String getCommunity(){
        return communityString;
  }
}

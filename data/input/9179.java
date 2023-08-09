class JDMCommunities extends SimpleNode {
  JDMCommunities(int id) {
    super(id);
  }
  JDMCommunities(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMCommunities(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMCommunities(p, id);
  }
  public void buildCommunities(AclEntryImpl entry){
        for (int i =0 ; i < children.length ; i++)
          entry.addCommunity(((JDMCommunity)children[i]).getCommunity());
  }
}

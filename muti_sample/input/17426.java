class JDMIpAddress extends Host {
  private static final long serialVersionUID = 849729919486384484L;
  protected StringBuffer address= new StringBuffer();
  JDMIpAddress(int id) {
    super(id);
  }
  JDMIpAddress(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMIpAddress(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMIpAddress(p, id);
  }
  protected String getHname() {
          return address.toString();
  }
  protected PrincipalImpl createAssociatedPrincipal()
    throws UnknownHostException {
      return new PrincipalImpl(address.toString());
  }
}

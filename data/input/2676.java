class JDMNetMaskV6 extends JDMNetMask {
  private static final long serialVersionUID = 4505256777680576645L;
  public JDMNetMaskV6(int id) {
    super(id);
  }
  public JDMNetMaskV6(Parser p, int id) {
    super(p, id);
  }
    protected PrincipalImpl createAssociatedPrincipal()
    throws UnknownHostException {
      return new NetMaskImpl(address.toString(), Integer.parseInt(mask));
  }
}

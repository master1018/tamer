class GroupImpl extends PrincipalImpl implements Group, Serializable {
  private static final long serialVersionUID = -7777387035032541168L;
  public GroupImpl () throws UnknownHostException {
  }
  public GroupImpl (String mask) throws UnknownHostException {
        super(mask);
  }
    public boolean addMember(Principal p) {
        return true;
    }
  public int hashCode() {
        return super.hashCode();
  }
  public boolean equals (Object p) {
        if (p instanceof PrincipalImpl || p instanceof GroupImpl){
          if ((super.hashCode() & p.hashCode()) == p.hashCode()) return true;
          else return false;
        } else {
          return false;
        }
  }
  public boolean isMember(Principal p) {
        if ((p.hashCode() & super.hashCode()) == p.hashCode()) return true;
        else return false;
  }
  public Enumeration<? extends Principal> members(){
        Vector<Principal> v = new Vector<Principal>(1);
        v.addElement(this);
        return v.elements();
  }
  public boolean removeMember(Principal p) {
        return true;
  }
  public String toString() {
        return ("GroupImpl :"+super.getAddress().toString());
  }
}

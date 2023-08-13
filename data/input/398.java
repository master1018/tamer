class AclEntryImpl implements AclEntry, Serializable {
  private static final long serialVersionUID = -5047185131260073216L;
  private AclEntryImpl (AclEntryImpl i) throws UnknownHostException {
        setPrincipal(i.getPrincipal());
        permList = new Vector<Permission>();
        commList = new Vector<String>();
        for (Enumeration<String> en = i.communities(); en.hasMoreElements();){
          addCommunity(en.nextElement());
        }
        for (Enumeration<Permission> en = i.permissions(); en.hasMoreElements();){
          addPermission(en.nextElement());
        }
        if (i.isNegative()) setNegativePermissions();
  }
  public AclEntryImpl (){
        princ = null;
        permList = new Vector<Permission>();
        commList = new Vector<String>();
  }
  public AclEntryImpl (Principal p) throws UnknownHostException {
        princ = p;
        permList = new Vector<Permission>();
        commList = new Vector<String>();
  }
  public Object clone() {
        AclEntryImpl i;
        try {
          i = new AclEntryImpl(this);
        }catch (UnknownHostException e) {
          i = null;
        }
        return (Object) i;
  }
  public boolean isNegative(){
        return neg;
  }
  public boolean addPermission(java.security.acl.Permission perm){
        if (permList.contains(perm)) return false;
        permList.addElement(perm);
        return true;
  }
  public boolean removePermission(java.security.acl.Permission perm){
        if (!permList.contains(perm)) return false;
        permList.removeElement(perm);
        return true;
  }
  public boolean checkPermission(java.security.acl.Permission perm){
        return (permList.contains(perm));
  }
  public Enumeration<Permission> permissions(){
        return permList.elements();
  }
  public void setNegativePermissions(){
        neg = true;
  }
  public Principal getPrincipal(){
        return princ;
  }
  public boolean setPrincipal(Principal p) {
        if (princ != null )
          return false;
        princ = p;
        return true;
  }
  public String toString(){
        return "AclEntry:"+princ.toString();
  }
  public Enumeration<String> communities(){
        return commList.elements();
  }
  public boolean addCommunity(String comm){
        if (commList.contains(comm)) return false;
        commList.addElement(comm);
        return true;
  }
  public boolean removeCommunity(String comm){
        if (!commList.contains(comm)) return false;
        commList.removeElement(comm);
        return true;
  }
  public boolean checkCommunity(String comm){
        return (commList.contains(comm));
  }
  private Principal princ = null;
  private boolean neg     = false;
  private Vector<Permission> permList = null;
  private Vector<String> commList = null;
}

public class BasicField implements Field {
  private String  name;
  private Type    type;
  private int     accessControl;
  private boolean isStatic;
  private long    offset;
  private Address address;
  public BasicField(String name, Type type, int accessControl, boolean isStatic) {
    this.name = name;
    this.type = type;
    this.accessControl = accessControl;
  }
  public int getAccessControl() { return accessControl; }
  public String getName() { return name; }
  public Type getType() { return type; }
  public boolean isStatic() { return isStatic; }
  public void setOffset(long offset) {
    if (isStatic) throw new RuntimeException("Nonstatic fields only");
    this.offset = offset;
  }
  public long getOffset() {
    if (isStatic) throw new RuntimeException("Nonstatic fields only");
    return offset;
  }
  public void setAddress(Address address) {
    if (!isStatic) throw new RuntimeException("Static fields only");
    this.address = address;
  }
  public Address getAddress() {
    if (!isStatic) throw new RuntimeException("Static fields only");
    return address;
  }
  public void resolveTypes(Type containingType, BasicCDebugInfoDataBase db, ResolveListener listener) {
    type = db.resolveType(containingType, type, listener, "resolving field type");
    if (isStatic) {
      if (address == null) {
        String fieldSymName = getType().getName() + "::" + getName();
        GlobalSym sym = db.lookupSym(fieldSymName);
        if (sym == null) {
          listener.resolveFailed(getType(), getName());
        } else {
          address = sym.getAddress();
        }
      }
    }
  }
}

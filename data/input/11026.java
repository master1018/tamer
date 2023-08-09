  public static class Type {
    private Type() {}
    public static final Type LOADOBJECT_LOAD   = new Type();
    public static final Type LOADOBJECT_UNLOAD = new Type();
    public static final Type BREAKPOINT        = new Type();
    public static final Type SINGLE_STEP       = new Type();
    public static final Type ACCESS_VIOLATION  = new Type();
    public static final Type UNKNOWN           = new Type();
  }
  public Type getType();
  public ThreadProxy getThread();
  public Address getPC();
  public boolean getWasWrite();
  public Address getAddress();
  public String getUnknownEventDetail();
}

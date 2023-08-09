public abstract class BasicSym implements Sym {
  private String name;
  protected BasicSym(String name) {
    this.name = name;
  }
  public String      getName()    { return name; }
  public String      toString()   { return getName(); }
  public BlockSym    asBlock()    { return null; }
  public FunctionSym asFunction() { return null; }
  public GlobalSym   asGlobal()   { return null; }
  public LocalSym    asLocal()    { return null; }
  public boolean     isBlock()    { return (asBlock()    != null); }
  public boolean     isFunction() { return (asFunction() != null); }
  public boolean     isGlobal()   { return (asGlobal()   != null); }
  public boolean     isLocal()    { return (asLocal()    != null); }
  public boolean     isLazy()     { return false; }
  public abstract void resolve(BasicCDebugInfoDataBase db, ResolveListener listener);
}

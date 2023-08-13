public class LazyBlockSym extends BasicSym implements BlockSym {
  private Object key;
  public LazyBlockSym(Object key) {
    super(null);
    this.key = key;
  }
  public BlockSym asBlock()       { return this; }
  public boolean isLazy()         { return true; }
  public Object getKey()          { return key; }
  public BlockSym getParent()     { return null; }
  public long getLength()         { return 0; }
  public Address getAddress()     { return null; }
  public int getNumLocals()       { return 0; }
  public LocalSym getLocal(int i) { throw new RuntimeException("Should not call this"); }
  public void resolve(BasicCDebugInfoDataBase db, ResolveListener listener) {}
}

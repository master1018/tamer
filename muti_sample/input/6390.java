public class BasicLocalSym extends BasicSym implements LocalSym {
  private Type   type;
  private long   frameOffset;
  public BasicLocalSym(String name, Type type, long frameOffset) {
    super(name);
    this.type = type;
    this.frameOffset = frameOffset;
  }
  public LocalSym asLocal()        { return this; }
  public Type     getType()        { return type; }
  public long     getFrameOffset() { return frameOffset; }
  public void resolve(BasicCDebugInfoDataBase db, ResolveListener listener) {
    type = db.resolveType(this, type, listener, "resolving type of local");
  }
}

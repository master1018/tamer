public class DbxX86ThreadFactory implements DbxThreadFactory {
  private DbxDebugger debugger;
  public DbxX86ThreadFactory(DbxDebugger debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new DbxX86Thread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new DbxX86Thread(debugger, id);
  }
}

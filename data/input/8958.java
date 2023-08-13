public class DbxSPARCThreadFactory implements DbxThreadFactory {
  private DbxDebugger debugger;
  public DbxSPARCThreadFactory(DbxDebugger debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new DbxSPARCThread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new DbxSPARCThread(debugger, id);
  }
}

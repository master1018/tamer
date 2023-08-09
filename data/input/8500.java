public class WindbgAMD64ThreadFactory implements WindbgThreadFactory {
  private WindbgDebugger debugger;
  public WindbgAMD64ThreadFactory(WindbgDebugger debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new WindbgAMD64Thread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new WindbgAMD64Thread(debugger, id);
  }
}

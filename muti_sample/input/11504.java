public class WindbgIA64ThreadFactory implements WindbgThreadFactory {
  private WindbgDebugger debugger;
  public WindbgIA64ThreadFactory(WindbgDebugger debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new WindbgIA64Thread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new WindbgIA64Thread(debugger, id);
  }
}

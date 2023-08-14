public class WindbgX86ThreadFactory implements WindbgThreadFactory {
  private WindbgDebugger debugger;
  public WindbgX86ThreadFactory(WindbgDebugger debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new WindbgX86Thread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new WindbgX86Thread(debugger, id);
  }
}

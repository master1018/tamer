public class ProcX86ThreadFactory implements ProcThreadFactory {
  private ProcDebugger debugger;
  public ProcX86ThreadFactory(ProcDebugger debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new ProcX86Thread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new ProcX86Thread(debugger, id);
  }
}

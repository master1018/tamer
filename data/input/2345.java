public class ProcSPARCThreadFactory implements ProcThreadFactory {
  private ProcDebugger debugger;
  public ProcSPARCThreadFactory(ProcDebugger debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new ProcSPARCThread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new ProcSPARCThread(debugger, id);
  }
}

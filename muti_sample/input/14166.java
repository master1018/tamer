public class RemoteX86ThreadFactory implements RemoteThreadFactory {
  private RemoteDebuggerClient debugger;
  public RemoteX86ThreadFactory(RemoteDebuggerClient debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new RemoteX86Thread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new RemoteX86Thread(debugger, id);
  }
}

public class RemoteAMD64ThreadFactory implements RemoteThreadFactory {
  private RemoteDebuggerClient debugger;
  public RemoteAMD64ThreadFactory(RemoteDebuggerClient debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new RemoteAMD64Thread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new RemoteAMD64Thread(debugger, id);
  }
}

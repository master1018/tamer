public class RemoteSPARCThreadFactory implements RemoteThreadFactory {
  private RemoteDebuggerClient debugger;
  public RemoteSPARCThreadFactory(RemoteDebuggerClient debugger) {
    this.debugger = debugger;
  }
  public ThreadProxy createThreadWrapper(Address threadIdentifierAddr) {
    return new RemoteSPARCThread(debugger, threadIdentifierAddr);
  }
  public ThreadProxy createThreadWrapper(long id) {
    return new RemoteSPARCThread(debugger, id);
  }
}

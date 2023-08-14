public class ServiceThread extends JavaThread {
  public ServiceThread(Address addr) {
    super(addr);
  }
  public boolean isJavaThread() { return false; }
  public boolean isHiddenFromExternalView() { return true; }
  public boolean isServiceThread() { return true; }
}

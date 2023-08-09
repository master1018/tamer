public class JvmtiAgentThread extends JavaThread {
  public JvmtiAgentThread(Address addr) {
    super(addr);
  }
  public boolean isJavaThread() { return false; }
  public boolean isJvmtiAgentThread() { return true; }
}

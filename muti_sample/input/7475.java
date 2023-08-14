public class CompilerThread extends JavaThread {
  public CompilerThread(Address addr) {
    super(addr);
  }
  public boolean isJavaThread() { return false; }
  public boolean isHiddenFromExternalView() { return true; }
  public boolean isCompilerThread() { return true; }
}

public class IA64CurrentFrameGuess {
  private IA64ThreadContext context;
  private JavaThread       thread;
  private Address          spFound;
  private Address          fpFound;
  private Address          pcFound;
  private static final boolean DEBUG = false;
  public IA64CurrentFrameGuess(IA64ThreadContext context,
                              JavaThread thread) {
    this.context = context;
    this.thread  = thread;
  }
  public boolean run(long regionInBytesToSearch) {
    return false;
  }
  public Address getSP() { return null; }
  public Address getFP() { return null; }
  public Address getPC() { return null; }
  private void setValues(Address sp, Address fp, Address pc) {
    spFound = sp;
    fpFound = fp;
    pcFound = pc;
  }
}

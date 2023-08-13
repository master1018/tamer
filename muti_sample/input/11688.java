public class StackFrameStream {
  private Frame       fr;
  private RegisterMap regMap;
  private boolean     isDone;
  public StackFrameStream(JavaThread thread) {
    this(thread, true);
  }
  public StackFrameStream(JavaThread thread, boolean update) {
    if (!VM.getVM().isDebugging()) {
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(thread.hasLastJavaFrame(), "sanity check");
      }
      fr = thread.getLastFrame();
      regMap = thread.newRegisterMap(update);
      isDone = false;
    } else {
      fr = thread.getCurrentFrameGuess();
      regMap = thread.newRegisterMap(update);
      while ((fr != null) && (!fr.isJavaFrame())) {
        if (fr.isFirstFrame()) {
          fr = null;
        } else {
          fr = fr.sender(regMap);
        }
      }
      if (fr == null) {
        isDone = true;
      }
    }
  }
  public boolean isDone() {
    if (isDone) {
      return true;
    } else {
      if (fr == null) {
        isDone = true;
        return true;
      }
      isDone = fr.isFirstFrame();
      return false;
    }
  }
  public void next() {
    if (!isDone) {
      fr = fr.sender(regMap);
    }
  }
  public Frame getCurrent()           { return fr;     }
  public RegisterMap getRegisterMap() { return regMap; }
}

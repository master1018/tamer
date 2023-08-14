public class JavaThreadState {
  private String stringVal;
  public static final JavaThreadState UNINITIALIZED     = new JavaThreadState("UNINITIALIZED");
  public static final JavaThreadState NEW               = new JavaThreadState("NEW");
  public static final JavaThreadState NEW_TRANS         = new JavaThreadState("NEW_TRANS");
  public static final JavaThreadState IN_NATIVE         = new JavaThreadState("IN_NATIVE");
  public static final JavaThreadState IN_NATIVE_TRANS   = new JavaThreadState("IN_NATIVE_TRANS");
  public static final JavaThreadState IN_VM             = new JavaThreadState("IN_VM");
  public static final JavaThreadState IN_VM_TRANS       = new JavaThreadState("IN_VM_TRANS");
  public static final JavaThreadState IN_JAVA           = new JavaThreadState("IN_JAVA");
  public static final JavaThreadState IN_JAVA_TRANS     = new JavaThreadState("IN_JAVA_TRANS");
  public static final JavaThreadState BLOCKED           = new JavaThreadState("BLOCKED");
  public static final JavaThreadState BLOCKED_TRANS     = new JavaThreadState("BLOCKED_TRANS");
  private JavaThreadState(String stringVal) {
    this.stringVal = stringVal;
  }
  public String toString() {
    return stringVal;
  }
}

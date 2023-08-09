public class JavaLineNumberInfo {
  private InstanceKlass klass;
  private Method method;
  private int startBCI;
  private int lineNumber;
  public JavaLineNumberInfo(InstanceKlass klass,
                            Method method,
                            int startBCI,
                            int lineNumber) {
    this.klass = klass;
    this.method = method;
    this.startBCI = startBCI;
    this.lineNumber = lineNumber;
  }
  public InstanceKlass getKlass()      { return klass; }
  public Method        getMethod()     { return method; }
  public int           getStartBCI()   { return startBCI; }
  public int           getLineNumber() { return lineNumber; }
}

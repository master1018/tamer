public class ObjectValue extends ScopeValue {
  private int        id;
  private ScopeValue klass;
  private List       fieldsValue; 
  public ObjectValue(int id) {
    this.id = id;
    klass   = null;
    fieldsValue = new ArrayList();
  }
  public boolean isObject() { return true; }
  public int id() { return id; }
  public ScopeValue getKlass() { return klass; }
  public List getFieldsValue() { return fieldsValue; }
  public ScopeValue getFieldAt(int i) { return (ScopeValue)fieldsValue.get(i); }
  public int fieldsSize() { return fieldsValue.size(); }
  public OopHandle getValue() { return null; }
  void readObject(DebugInfoReadStream stream) {
    klass = readFrom(stream);
    Assert.that(klass.isConstantOop(), "should be constant klass oop");
    int length = stream.readInt();
    for (int i = 0; i < length; i++) {
      ScopeValue val = readFrom(stream);
      fieldsValue.add(val);
    }
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    tty.print("scalarObj[" + id + "]");
  }
  void printFieldsOn(PrintStream tty) {
    if (fieldsValue.size() > 0) {
      ((ScopeValue)fieldsValue.get(0)).printOn(tty);
    }
    for (int i = 1; i < fieldsValue.size(); i++) {
      tty.print(", ");
      ((ScopeValue)fieldsValue.get(i)).printOn(tty);
    }
  }
};

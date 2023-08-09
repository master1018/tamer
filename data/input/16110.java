public class LivenessPathElement {
  LivenessPathElement(Oop obj, FieldIdentifier id) {
    this.obj = obj;
    this.id  = id;
  }
  public boolean isRoot() {
    return (obj == null);
  }
  public boolean isTerminal() {
    return (id == null);
  }
  public Oop getObj() {
    return obj;
  }
  public FieldIdentifier getField() {
    return id;
  }
  private Oop             obj;
  private FieldIdentifier id;
}

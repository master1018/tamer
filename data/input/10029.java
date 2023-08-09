public class BasicIndexableFieldIdentifier implements IndexableFieldIdentifier {
  private Type type;
  private int  index;
  public BasicIndexableFieldIdentifier(Type type, int index) {
    this.type = type;
    this.index = index;
  }
  public Type    getType()  { return type; }
  public int     getIndex() { return index; }
  public String  toString() { return Integer.toString(getIndex()); }
}

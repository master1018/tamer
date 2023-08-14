public class IndexableFieldIdentifier extends FieldIdentifier {
  public IndexableFieldIdentifier(int index) {
    this.index = index;
  }
  private int index;
  public int getIndex() { return index; }
  public String getName() { return Integer.toString(getIndex()); }
  public void printOn(PrintStream tty) {
    tty.print(" - " + getIndex() + ":\t");
  }
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof IndexableFieldIdentifier)) {
      return false;
    }
    return (((IndexableFieldIdentifier) obj).getIndex() == index);
  }
  public int hashCode() {
    return index;
  }
};

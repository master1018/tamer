public class BasicNamedFieldIdentifier implements NamedFieldIdentifier {
  private Field field;
  public BasicNamedFieldIdentifier(Field field) {
    this.field = field;
  }
  public String getName()  { return field.getName(); }
  public Type getType()    { return field.getType(); }
  public String toString() { return getName(); }
}

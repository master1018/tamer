public class InputException {
  private final String name;
  private final Queue<InputCode> codes;
  public InputException(final String name) {
    this.name = name;
    codes = new LinkedList<InputCode>();
  }
  public void add(InputCode c)
  {
    codes.offer(c);
  }
  public String getName() {
    return name;
  }
  public Queue<InputCode> getCodes() {
    return codes;
  }
  public String toString() {
    return getClass().getName()
      + "[name=" + name
      + ",codes=" + codes
      + "]";
  }
}

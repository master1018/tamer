public class FieldIdentifier {
  public String getName() { return ""; }
  public void printOn(PrintStream tty) {
    tty.print(" - " + getName() + ":\t");
  }
};

public abstract class PermGen extends VMObject {
  public PermGen(Address addr) {
    super(addr);
  }
  public abstract Generation asGen();
}

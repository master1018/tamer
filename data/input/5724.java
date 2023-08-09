public class VMObject {
  protected Address addr;
  public VMObject(Address addr) {
    this.addr = addr;
  }
  public String toString() {
    return getClass().getName() + "@" + addr;
  }
  public boolean equals(Object arg) {
    if (arg == null) {
      return false;
    }
    if (!getClass().equals(arg.getClass())) {
      return false;
    }
    VMObject obj = (VMObject) arg;
    if (!addr.equals(obj.addr)) {
      return false;
    }
    return true;
  }
  public int hashCode() {
    return addr.hashCode();
  }
  public Address getAddress() {
    return addr;
  }
}

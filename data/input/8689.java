public abstract class Register extends ImmediateOrRegister {
  protected int number;
  public Register() {
    number = -1;
  }
  public Register(int number) {
    this.number = number;
  }
  public abstract int getNumberOfRegisters();
  public boolean isValid() {
    return ((0 <= number) && (number <= getNumberOfRegisters()));
  }
  public int getNumber() {
    return number;
  }
  public boolean equals(Object x) {
    if (x == null) {
      return false;
    }
    if (!getClass().equals(x.getClass())) {
      return false;
    }
    Register reg = (Register) x;
    return (reg.getNumber() == getNumber());
  }
  public int hashCode() {
    return number;
  }
  public boolean isRegister() {
    return true;
  }
  public abstract boolean isStackPointer();
  public abstract boolean isFramePointer();
  public abstract boolean isFloat();
}

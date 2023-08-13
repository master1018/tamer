public class IA64Register extends Register {
  private static final int STACKED_BASE = 32;
  private static final int STACKED_END = 127;
  private static final int APPL_BASE   = 128;
  private static final int nofRegisters = 129;  
  public IA64Register(int number) {
    super(number);
  }
  public int getNumberOfRegisters() {
    return nofRegisters;
  }
  public boolean isStacked() {
    return (32 <= getNumber());
  }
  public long spOffsetInSavedWindow() {
    return 0;
  }
  public String toString() {
    return IA64Registers.getRegisterName(number);
  }
  public boolean isFramePointer() {
    return number == APPL_BASE;
  }
  public boolean isStackPointer() {
    return number == 12;
  }
  public boolean isFloat() {
    return false;
  }
}

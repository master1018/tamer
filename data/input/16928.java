public abstract class X86ThreadContext implements ThreadContext {
  public static final int GS = 0;
  public static final int FS = 1;
  public static final int ES = 2;
  public static final int DS = 3;
  public static final int EDI = 4;
  public static final int ESI = 5;
  public static final int EBP = 6;
  public static final int ESP = 7;
  public static final int EBX = 8;
  public static final int EDX = 9;
  public static final int ECX = 10;
  public static final int EAX = 11;
  public static final int TRAPNO = 12;
  public static final int ERR = 13;
  public static final int EIP = 14;
  public static final int CS = 15;
  public static final int EFL = 16;
  public static final int UESP = 17;
  public static final int SS = 18;
  public static final int DR0 = 19;
  public static final int DR1 = 20;
  public static final int DR2 = 21;
  public static final int DR3 = 22;
  public static final int DR6 = 23;
  public static final int DR7 = 24;
  public static final int PC = EIP;
  public static final int FP = EBP;
  public static final int SP = UESP;
  public static final int PS = EFL;
  public static final int R0 = EAX;
  public static final int R1 = EDX;
  public static final int NPRGREG = 25;
  private static final String[] regNames = {
    "GS",     "FS",    "ES",    "DS",
    "EDI",    "ESI",   "EBP",   "ESP",
    "EBX",    "EDX",   "ECX",   "EAX",
    "TRAPNO", "ERR",   "EIP",   "CS",
    "EFLAGS", "UESP",  "SS",
    "DR0",    "DR1",   "DR2",   "DR3",
    "DR6",    "DR7"
  };
  private long[] data;
  public X86ThreadContext() {
    data = new long[NPRGREG];
  }
  public int getNumRegisters() {
    return NPRGREG;
  }
  public String getRegisterName(int index) {
    return regNames[index];
  }
  public void setRegister(int index, long value) {
    data[index] = value;
  }
  public long getRegister(int index) {
    return data[index];
  }
  public abstract void setRegisterAsAddress(int index, Address value);
  public abstract Address getRegisterAsAddress(int index);
}

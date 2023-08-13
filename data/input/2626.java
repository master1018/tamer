public abstract class SPARCThreadContext implements ThreadContext {
  public static final int R_G0 = 0;
  public static final int R_G1 = 1;
  public static final int R_G2 = 2;
  public static final int R_G3 = 3;
  public static final int R_G4 = 4;
  public static final int R_G5 = 5;
  public static final int R_G6 = 6;
  public static final int R_G7 = 7;
  public static final int R_O0 = 8;
  public static final int R_O1 = 9;
  public static final int R_O2 = 10;
  public static final int R_O3 = 11;
  public static final int R_O4 = 12;
  public static final int R_O5 = 13;
  public static final int R_O6 = 14;
  public static final int R_O7 = 15;
  public static final int R_L0 = 16;
  public static final int R_L1 = 17;
  public static final int R_L2 = 18;
  public static final int R_L3 = 19;
  public static final int R_L4 = 20;
  public static final int R_L5 = 21;
  public static final int R_L6 = 22;
  public static final int R_L7 = 23;
  public static final int R_I0 = 24;
  public static final int R_I1 = 25;
  public static final int R_I2 = 26;
  public static final int R_I3 = 27;
  public static final int R_I4 = 28;
  public static final int R_I5 = 29;
  public static final int R_I6 = 30;
  public static final int R_I7 = 31;
  public static final int R_CCR = 32;
  public static final int R_PSR = 32;
  public static final int R_PC = 33;
  public static final int R_nPC = 34;
  public static final int R_SP = R_O6;
  public static final int R_FP = R_I6;
  public static final int R_Y  = 35;
  public static final int R_ASI = 36;
  public static final int R_FPRS = 37;
  public static final int R_WIM = 36;
  public static final int R_TBR = 37;
  public static final int NPRGREG = 38;
  private static final String[] regNames = {
    "G0",    "G1",    "G2",    "G3",
    "G4",    "G5",    "G6",    "G7",
    "O0",    "O1",    "O2",    "O3",
    "O4",    "O5",    "O6/SP", "O7",
    "L0",    "L1",    "L2",    "L3",
    "L4",    "L5",    "L6",    "L7",
    "I0",    "I1",    "I2",    "I3",
    "I4",    "I5",    "I6/FP", "I7",
    "CCR/PSR", "PC",  "nPC",   "Y",
    "ASI/WIM", "FPRS/TBR"
  };
  private long[] data;
  public SPARCThreadContext() {
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

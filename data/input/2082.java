public class SPARCArgument {
  private int     number;
  private boolean isIn;
  public static final int NUM_REGISTER_PARAMETERS = 6;
  public SPARCArgument(int number, boolean isIn) {
    this.number = number;
    this.isIn   = isIn;
  }
  int     getNumber() { return number;     }
  boolean getIsIn()   { return isIn;       }
  boolean getIsOut()  { return !getIsIn(); }
  public SPARCArgument getSuccessor() { return new SPARCArgument(getNumber() + 1, getIsIn()); }
  public SPARCArgument asIn()         { return new SPARCArgument(getNumber(),     true);      }
  public SPARCArgument asOut()        { return new SPARCArgument(getNumber(),     false);     }
  public boolean isRegister()         { return number < NUM_REGISTER_PARAMETERS; }
  public SPARCRegister asRegister() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isRegister(), "must be a register argument");
    }
    return new SPARCRegister(getIsIn() ? SPARCRegisterType.IN : SPARCRegisterType.OUT, getNumber());
  }
}

public abstract class ScopeValue {
  static final int LOCATION_CODE        = 0;
  static final int CONSTANT_INT_CODE    = 1;
  static final int CONSTANT_OOP_CODE    = 2;
  static final int CONSTANT_LONG_CODE   = 3;
  static final int CONSTANT_DOUBLE_CODE = 4;
  static final int CONSTANT_OBJECT_CODE = 5;
  static final int CONSTANT_OBJECT_ID_CODE = 6;
  public boolean isLocation()       { return false; }
  public boolean isConstantInt()    { return false; }
  public boolean isConstantDouble() { return false; }
  public boolean isConstantLong()   { return false; }
  public boolean isConstantOop()    { return false; }
  public boolean isObject()         { return false; }
  public static ScopeValue readFrom(DebugInfoReadStream stream) {
    switch (stream.readInt()) {
    case LOCATION_CODE:
      return new LocationValue(stream);
    case CONSTANT_INT_CODE:
      return new ConstantIntValue(stream);
    case CONSTANT_OOP_CODE:
      return new ConstantOopReadValue(stream);
    case CONSTANT_LONG_CODE:
      return new ConstantLongValue(stream);
    case CONSTANT_DOUBLE_CODE:
      return new ConstantDoubleValue(stream);
    case CONSTANT_OBJECT_CODE:
      return stream.readObjectValue();
    case CONSTANT_OBJECT_ID_CODE:
      return stream.getCachedObject();
    default:
      Assert.that(false, "should not reach here");
      return null;
    }
  }
  public abstract void printOn(PrintStream tty);
}

public class BasicCIntegerType extends BasicType implements CIntegerType {
  private boolean isUnsigned;
  public BasicCIntegerType(BasicTypeDataBase db, String name, boolean isUnsigned) {
    super(db, name, null);
    this.isUnsigned = isUnsigned;
  }
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof BasicCIntegerType)) {
      return false;
    }
    BasicCIntegerType arg = (BasicCIntegerType) obj;
    if (isUnsigned != arg.isUnsigned) {
      return false;
    }
    return true;
  }
  public String toString() {
    String prefix = null;
    if (isUnsigned) {
      prefix = "unsigned";
    }
    if (prefix != null) {
      return prefix + " " + getName();
    }
    return getName();
  }
  public boolean isCIntegerType() {
    return true;
  }
  public boolean isUnsigned() {
    return isUnsigned;
  }
  public void setIsUnsigned(boolean isUnsigned) {
    this.isUnsigned = isUnsigned;
  }
  public long maxValue() {
    return db.cIntegerTypeMaxValue(getSize(), isUnsigned());
  }
  public long minValue() {
    return db.cIntegerTypeMinValue(getSize(), isUnsigned());
  }
}

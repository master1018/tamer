public class FieldType {
  private Symbol signature;
  private char   first;
  public FieldType(Symbol signature) {
    this.signature = signature;
    this.first     = (char) signature.getByteAt(0);
    if (Assert.ASSERTS_ENABLED) {
       switch (this.first) {
       case 'B':
       case 'C':
       case 'D':
       case 'F':
       case 'I':
       case 'J':
       case 'S':
       case 'Z':
       case 'L':
       case '[':
           break;   
       default:
         Assert.that(false, "Unknown char in field signature \"" + signature.asString() + "\": " + this.first);
       }
    }
  }
  public boolean isOop()     { return isObject() || isArray(); }
  public boolean isByte()    { return first == 'B'; }
  public boolean isChar()    { return first == 'C'; }
  public boolean isDouble()  { return first == 'D'; }
  public boolean isFloat()   { return first == 'F'; }
  public boolean isInt()     { return first == 'I'; }
  public boolean isLong()    { return first == 'J'; }
  public boolean isShort()   { return first == 'S'; }
  public boolean isBoolean() { return first == 'Z'; }
  public boolean isObject()  { return first == 'L'; }
  public boolean isArray()   { return first == '['; }
  public static class ArrayInfo {
    private int dimension;
    private int elementBasicType; 
    public ArrayInfo(int dimension, int elementBasicType) {
      this.dimension = dimension;
      this.elementBasicType = elementBasicType;
    }
    public int dimension()        { return dimension; }
    public int elementBasicType() { return elementBasicType; }
  }
  public ArrayInfo getArrayInfo() {
    int index = 1;
    int dim   = 1;
    index = skipOptionalSize(signature, index);
    while (signature.getByteAt(index) == '[') {
      index++;
      dim++;
      skipOptionalSize(signature, index);
    }
    int elementType = BasicType.charToType((char) signature.getByteAt(index));
    return new ArrayInfo(dim, elementType);
  }
  private int skipOptionalSize(Symbol sig, int index) {
    byte c = sig.getByteAt(index);
    while (c >= '0' && c <= '9') {
      ++index;
      c = sig.getByteAt(index);
    }
    return index;
  }
}

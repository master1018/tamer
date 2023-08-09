public class StackValueCollection {
  private List list;
  public StackValueCollection()           { list = new ArrayList(); }
  public StackValueCollection(int length) { list = new ArrayList(length); }
  public void add(StackValue val) { list.add(val); }
  public int  size()              { return list.size(); }
  public boolean isEmpty()        { return (size() == 0); }
  public StackValue get(int i)    { return (StackValue) list.get(i); }
  public boolean   booleanAt(int slot)   { return (int)get(slot).getInteger() != 0; }
  public byte      byteAt(int slot)      { return (byte) get(slot).getInteger(); }
  public char      charAt(int slot)      { return (char) get(slot).getInteger(); }
  public short     shortAt(int slot)     { return (short) get(slot).getInteger(); }
  public int       intAt(int slot)       { return (int) get(slot).getInteger(); }
  public long      longAt(int slot)      { return VM.getVM().buildLongFromIntsPD((int) get(slot).getInteger(),
                                                                                 (int) get(slot+1).getInteger()); }
  public OopHandle oopHandleAt(int slot) { return get(slot).getObject(); }
  public float     floatAt(int slot)     { return Float.intBitsToFloat(intAt(slot)); }
  public double    doubleAt(int slot)    { return Double.longBitsToDouble(longAt(slot)); }
}

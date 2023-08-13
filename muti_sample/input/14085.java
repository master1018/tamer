class MaskFillerForNative extends NativeSignatureIterator {
  MaskFillerForNative(Method method, BitMap mask, int maskSize) {
    super(method);
    this.mask = mask;
    this.size = maskSize;
  }
  public void passInt()    {  }
  public void passLong()   {  }
  public void passFloat()  {  }
  public void passDouble() {  }
  public void passObject() { mask.atPut(offset(), true); }
  public void generate() {
    super.iterate();
  }
  private BitMap mask;
  private int    size;
}

public class X86RegisterMap extends RegisterMap {
  public X86RegisterMap(JavaThread thread, boolean updateMap) {
    super(thread, updateMap);
  }
  protected X86RegisterMap(RegisterMap map) {
    super(map);
  }
  public Object clone() {
    X86RegisterMap retval = new X86RegisterMap(this);
    return retval;
  }
  protected void clearPD() {}
  protected void initializePD() {}
  protected void initializeFromPD(RegisterMap map) {}
  protected Address getLocationPD(VMReg reg) { return null; }
}

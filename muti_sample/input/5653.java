public class AMD64RegisterMap extends RegisterMap {
  public AMD64RegisterMap(JavaThread thread, boolean updateMap) {
    super(thread, updateMap);
  }
  protected AMD64RegisterMap(RegisterMap map) {
    super(map);
  }
  public Object clone() {
    AMD64RegisterMap retval = new AMD64RegisterMap(this);
    return retval;
  }
  protected void clearPD() {}
  protected void initializePD() {}
  protected void initializeFromPD(RegisterMap map) {}
  protected Address getLocationPD(VMReg reg) { return null; }
}

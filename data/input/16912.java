public class IA64RegisterMap extends RegisterMap {
  public IA64RegisterMap(JavaThread thread, boolean updateMap) {
    super(thread, updateMap);
  }
  protected IA64RegisterMap(RegisterMap map) {
    super(map);
  }
  public Object clone() {
    IA64RegisterMap retval = new IA64RegisterMap(this);
    return retval;
  }
  protected void clearPD() {}
  protected void initializePD() {}
  protected void initializeFromPD(RegisterMap map) {}
  protected Address getLocationPD(VMReg reg) { return null; }
}

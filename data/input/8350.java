public class LinuxVtblAccess extends BasicVtblAccess {
  private String vt;
  public LinuxVtblAccess(SymbolLookup symbolLookup,
                         String[] dllNames) {
    super(symbolLookup, dllNames);
    if (symbolLookup.lookup("libjvm.so", "__vt_10JavaThread") != null ||
        symbolLookup.lookup("libjvm_g.so", "__vt_10JavaThread") != null) {
       vt = "__vt_";
    } else {
       vt = "_ZTV";
    }
  }
  protected String vtblSymbolForType(Type type) {
    return vt + type.getName().length() + type;
  }
}

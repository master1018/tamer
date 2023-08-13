public class Win32VtblAccess extends BasicVtblAccess {
  public Win32VtblAccess(SymbolLookup symbolLookup,
                         String[] dllNames) {
    super(symbolLookup, dllNames);
  }
  protected String vtblSymbolForType(Type type) {
    return "??_7" + type.getName() + "@@6B@";
  }
}

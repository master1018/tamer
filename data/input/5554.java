public class HotSpotSolarisVtblAccess extends BasicVtblAccess {
  public HotSpotSolarisVtblAccess(SymbolLookup symbolLookup,
                                  String[] jvmLibNames) {
    super(symbolLookup, jvmLibNames);
  }
  protected String vtblSymbolForType(Type type) {
    String demangledSymbol = type.getName() + "::__vtbl";
    return mangle(demangledSymbol);
  }
  private String mangle(String symbol) {
    String[] parts = symbol.split("::");
    StringBuffer mangled = new StringBuffer("__1c");
    for (int i = 0; i < parts.length; i++) {
      int len = parts[i].length();
      if (len >= 26) {
        mangled.append((char)('a' + (len / 26)));
        len = len % 26;
      }
      mangled.append((char)('A' + len));
      mangled.append(parts[i]);
    }
    mangled.append("_");
    return mangled.toString();
  }
}

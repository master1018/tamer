public class DummySymbolFinder implements SymbolFinder {
   public String getSymbolFor(long address) {
      return "0x" + Long.toHexString(address);
   }
}

public class VirtualConstructor {
  private TypeDataBase db;
  private Map          map; 
  public VirtualConstructor(TypeDataBase db) {
    this.db = db;
    map     = new HashMap();
  }
  public boolean addMapping(String cTypeName, Class clazz) {
    if (map.get(cTypeName) != null) {
      return false;
    }
    map.put(cTypeName, clazz);
    return true;
  }
  public VMObject instantiateWrapperFor(Address addr) throws WrongTypeException {
    if (addr == null) {
      return null;
    }
    for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
      String typeName = (String) iter.next();
      if (db.addressTypeIsEqualToType(addr, db.lookupType(typeName))) {
        return (VMObject) VMObjectFactory.newObject((Class) map.get(typeName), addr);
      }
    }
    String message = "No suitable match for type of address " + addr;
    CDebugger cdbg = VM.getVM().getDebugger().getCDebugger();
    if (cdbg != null) {
      Address vtblPtr = addr.getAddressAt(0);
      LoadObject lo = cdbg.loadObjectContainingPC(vtblPtr);
      if (lo != null) {
        ClosestSymbol symbol = lo.closestSymbolToPC(vtblPtr);
        if (symbol != null) {
          message += " (nearest symbol is " + symbol.getName() + ")";
        }
      }
    }
    throw new WrongTypeException(message);
  }
}

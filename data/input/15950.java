public abstract class BytecodeWithCPIndex extends Bytecode {
  BytecodeWithCPIndex(Method method, int bci) {
    super(method, bci);
  }
  public int index() { return 0xFFFF & javaShortAt(1); }
  public int getSecondaryIndex() {
     throw new IllegalArgumentException("must be invokedynamic");
  }
  protected int indexForFieldOrMethod() {
     ConstantPoolCache cpCache = method().getConstants().getCache();
     int cpCacheIndex = index();
     if (cpCache == null) {
        return cpCacheIndex;
     } else if (code() == Bytecodes._invokedynamic) {
        int secondaryIndex = getSecondaryIndex();
        return cpCache.getMainEntryAt(secondaryIndex).getConstantPoolIndex();
     } else {
        return cpCache.getEntryAt((int) (0xFFFF & VM.getVM().getBytes().swapShort((short) cpCacheIndex))).getConstantPoolIndex();
     }
  }
}

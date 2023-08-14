public abstract class AbstractInstruction implements Instruction {
   protected final String name;
   public AbstractInstruction(String name) {
      this.name = name;
   }
   public String getName() {
      return name;
   }
   public boolean isIllegal() {
      return false;
   }
   public boolean isArithmetic() {
      return false;
   }
   public boolean isLogical() {
      return false;
   }
   public boolean isShift() {
      return false;
   }
   public boolean isMove() {
      return false;
   }
   public boolean isBranch() {
      return false;
   }
   public boolean isCall() {
      return false;
   }
   public boolean isReturn() {
      return false;
   }
   public boolean isLoad() {
      return false;
   }
   public boolean isStore() {
      return false;
   }
   public boolean isFloat() {
      return false;
   }
   public boolean isTrap() {
      return false;
   }
   public boolean isNoop() {
      return false;
   }
   public String asString(long currentPc, SymbolFinder symFinder) {
      return name;
   }
}

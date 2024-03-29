public class X86GeneralInstruction extends X86Instruction {
   final private Operand operand1;
   final private Operand operand2;
   final private Operand operand3;
   final private String description;
   public X86GeneralInstruction(String name, Operand op1, Operand op2, Operand op3, int size, int prefixes) {
      super(name, size, prefixes);
      this.operand1 = op1;
      this.operand2 = op2;
      this.operand3 = op3;
      description = initDescription();
   }
   public X86GeneralInstruction(String name, Operand op1, Operand op2, int size, int prefixes) {
      this(name, op1, op2, null, size, prefixes);
   }
   public X86GeneralInstruction(String name, Operand op1, int size, int prefixes) {
      this(name, op1, null, null, size, prefixes);
   }
   protected String initDescription() {
      StringBuffer buf = new StringBuffer();
      buf.append(getPrefixString());
      buf.append(getName());
      buf.append(spaces);
      if (operand1 != null) {
         buf.append(getOperandAsString(operand1));
      }
      if (operand2 != null) {
         buf.append(comma);
         buf.append(getOperandAsString(operand2));
      }
      if(operand3 != null) {
          buf.append(comma);
          buf.append(getOperandAsString(operand3));
      }
      return buf.toString();
   }
   public String asString(long currentPc, SymbolFinder symFinder) {
      return description;
   }
   public Operand getOperand1() {
      return operand1;
   }
   public Operand getOperand2() {
      return operand2;
   }
   public Operand getOperand3() {
      return operand3;
   }
}

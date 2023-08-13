public class X86FPLoadInstruction extends X86FPInstruction {
   final private Operand source;
   public X86FPLoadInstruction(String name, Operand operand, int size, int prefixes) {
      super(name, size, prefixes);
      this.source = operand;
   }
   public String asString(long currentPc, SymbolFinder symFinder) {
      StringBuffer buf = new StringBuffer();
      buf.append(getPrefixString());
      buf.append(getName());
      buf.append(spaces);
      buf.append(source.toString());
      return buf.toString();
   }
}

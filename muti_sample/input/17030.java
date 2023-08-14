public class X86MMXRegister extends X86Register {
   public X86MMXRegister(int num, String name) {
     super(num, name);
   }
   public int getNumberOfRegisters() {
     return X86MMXRegisters.getNumberOfRegisters();
   }
   public String toString() {
     return name;
   }
}

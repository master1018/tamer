public class X86XMMRegister extends X86Register {
   public X86XMMRegister(int num, String name) {
     super(num, name);
   }
   public int getNumberOfRegisters() {
     return X86XMMRegisters.getNumberOfRegisters();
   }
   public String toString() {
     return name;
   }
}

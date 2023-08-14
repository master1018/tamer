public class X86SegmentRegister extends X86Register {
   public X86SegmentRegister(int num, String name) {
     super(num, name);
   }
   public int getNumberOfRegisters() {
     return X86SegmentRegisters.getNumberOfRegisters();
   }
   public String toString() {
     return name;
   }
   public boolean isSegmentPointer() {
     return true;
   }
}

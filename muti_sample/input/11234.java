public class X86Register extends Register {
   protected String name;
   public X86Register(int num, String name) {
     super(num);
     this.name = name;
   }
   public int getNumberOfRegisters() {
     return X86Registers.getNumberOfRegisters();
   }
   public String toString() {
     return name;
   }
   public boolean isFramePointer() {
     return number == 5; 
   }
   public boolean isStackPointer() {
     return number == 4; 
   }
   public boolean isFloat() {
     return false;
   }
   public boolean isSegmentPointer() {
     return false;
   }
}

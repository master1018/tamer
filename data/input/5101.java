public class X86SegmentRegisters {
   public static final int NUM_SEGMENT_REGISTERS = 6;
   public static final X86SegmentRegister ES;
   public static final X86SegmentRegister CS;
   public static final X86SegmentRegister SS;
   public static final X86SegmentRegister DS;
   public static final X86SegmentRegister FS;
   public static final X86SegmentRegister GS;
   private static X86SegmentRegister segmentRegisters[];
   static {
      ES = new X86SegmentRegister(0, "%es");
      CS = new X86SegmentRegister(1, "%cs");
      SS = new X86SegmentRegister(2, "%ss");
      DS = new X86SegmentRegister(3, "%ds");
      FS = new X86SegmentRegister(4, "%fs");
      GS = new X86SegmentRegister(5, "%gs");
      segmentRegisters = (new X86SegmentRegister[] {
            ES, CS, SS, DS, FS, GS
      });
   }
   public static int getNumberOfRegisters() {
      return NUM_SEGMENT_REGISTERS;
   }
   public static X86SegmentRegister getSegmentRegister(int regNum) {
      if (Assert.ASSERTS_ENABLED) {
         Assert.that(regNum > -1 && regNum < NUM_SEGMENT_REGISTERS, "invalid segment register number!");
      }
     return segmentRegisters[regNum];
   }
}

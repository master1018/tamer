public class FloatGRPDecoder extends FPInstructionDecoder {
   final private int number;
   private static final FPInstructionDecoder floatGRPMap[][] = {
      {
      new FPInstructionDecoder("fnop"),
      null,
      null,
      null,
      null,
      null,
      null,
      null
      },
      {
      new FPInstructionDecoder("fchs"),
      new FPInstructionDecoder("fabs"),
      null,
      null,
      new FPInstructionDecoder("ftst"),
      new FPInstructionDecoder("fxam"),
      null,
      null
      },
      {
      new FPInstructionDecoder("fld1"),
      new FPInstructionDecoder("fldl2t"),
      new FPInstructionDecoder("fldl2e"),
      new FPInstructionDecoder("fldpi"),
      new FPInstructionDecoder("fldlg2"),
      new FPInstructionDecoder("fldln2"),
      new FPInstructionDecoder("fldz"),
      null
      },
      {
      new FPInstructionDecoder("f2xm1"),
      new FPInstructionDecoder("fyl2x"),
      new FPInstructionDecoder("fptan"),
      new FPInstructionDecoder("fpatan"),
      new FPInstructionDecoder("fxtract"),
      new FPInstructionDecoder("fprem1"),
      new FPInstructionDecoder("fdecstp"),
      new FPInstructionDecoder("fincstp")
      },
      {
      new FPInstructionDecoder("fprem"),
      new FPInstructionDecoder("fyl2xp1"),
      new FPInstructionDecoder("fsqrt"),
      new FPInstructionDecoder("fsincos"),
      new FPInstructionDecoder("frndint"),
      new FPInstructionDecoder("fscale"),
      new FPInstructionDecoder("fsin"),
      new FPInstructionDecoder("fcos")
      },
      {
      null,
      new FPInstructionDecoder("fucompp"),
      null,
      null,
      null,
      null,
      null,
      null
      },
      {
      new FPInstructionDecoder("feni(287 only)"),
      new FPInstructionDecoder("fdisi(287 only)"),
      new FPInstructionDecoder("fNclex"),
      new FPInstructionDecoder("fNinit"),
      new FPInstructionDecoder("fNsetpm(287 only)"),
      null,
      null,
      null
      },
      {
      null,
      new FPInstructionDecoder("fcompp"),
      null,
      null,
      null,
      null,
      null,
      null
      },
      {
      new FPInstructionDecoder("fNstsw"),
      null,
      null,
      null,
      null,
      null,
      null,
      null
      }
   };
   public FloatGRPDecoder(String name, int number) {
      super(name);
      this.number = number;
   }
   public Instruction decode(byte[] bytesArray, int index, int instrStartIndex, int segmentOverride, int prefixes, X86InstructionFactory factory) {
      this.byteIndex = index;
      this.instrStartIndex = instrStartIndex;
      this.prefixes = prefixes;
      int ModRM = readByte(bytesArray, byteIndex);
      int rm = ModRM & 7;
      FPInstructionDecoder instrDecoder = null;
      instrDecoder = floatGRPMap[number][rm];
      Instruction instr = null;
      if(instrDecoder != null) {
         instr = instrDecoder.decode(bytesArray, byteIndex, instrStartIndex, segmentOverride, prefixes, factory);
         byteIndex = instrDecoder.getCurrentIndex();
      } else {
         instr = factory.newIllegalInstruction();
      }
      return instr;
   }
}

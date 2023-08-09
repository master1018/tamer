abstract class InstructionDecoder implements  SPARCOpcodes , RTLDataTypes, RTLOperations {
    static int extractSignedIntFromNBits(int value, int num_bits) {
        return (value << (32 - num_bits)) >> (32 - num_bits);
    }
    static int getSourceRegister1(int instruction) {
        return (instruction & RS1_MASK) >>> RS1_START_BIT;
    }
    static int getSourceRegister2(int instruction) {
        return (instruction & RS2_MASK);
    }
    static int getDestinationRegister(int instruction) {
        return (instruction & RD_MASK) >>> RD_START_BIT;
    }
    static int getConditionCode(int instruction) {
        return (instruction & CONDITION_CODE_MASK) >>> CONDITION_CODE_START_BIT;
    }
    static boolean isIBitSet(int instruction) {
        return (instruction & I_MASK) != 0;
    }
    static ImmediateOrRegister getOperand2(int instruction) {
        boolean iBit = isIBitSet(instruction);
        ImmediateOrRegister operand2 = null;
        if (iBit) {
           operand2 = new Immediate(new Short((short)extractSignedIntFromNBits(instruction, 13)));
        } else {
           operand2 = SPARCRegisters.getRegister(getSourceRegister2(instruction));
        }
        return operand2;
    }
    static int getOpf(int instruction) {
        return (instruction & OPF_MASK) >>> OPF_START_BIT;
    }
    abstract Instruction decode(int instruction, SPARCInstructionFactory factory);
}

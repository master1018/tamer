class JmplDecoder extends MemoryInstructionDecoder {
    JmplDecoder() {
        super(JMPL, "jmpl", RTLDT_UNSIGNED_WORD);
    }
    Instruction decodeMemoryInstruction(int instruction, SPARCRegisterIndirectAddress addr,
                       SPARCRegister rd,  SPARCInstructionFactory factory) {
        Instruction instr = null;
        if (rd == SPARCRegisters.O7) {
            instr = factory.newIndirectCallInstruction(addr, rd);
        } else if (rd == SPARCRegisters.G0) {
            int disp = (int) addr.getDisplacement();
            Register base = addr.getBase();
            if (base == SPARCRegisters.I7 && disp == 8) {
                instr = factory.newReturnInstruction(addr, rd, false );
            } else if (base == SPARCRegisters.O7 && disp == 8) {
                instr = factory.newReturnInstruction(addr, rd, true );
            } else {
                instr = factory.newJmplInstruction(addr, rd);
            }
        } else {
            instr = factory.newJmplInstruction(addr, rd);
        }
        return instr;
    }
}

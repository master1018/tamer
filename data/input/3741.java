class RettDecoder extends MemoryInstructionDecoder {
    RettDecoder() {
        super(RETT, "rett", RTLDT_UNKNOWN);
    }
    Instruction decodeMemoryInstruction(int instruction, SPARCRegisterIndirectAddress addr,
                      SPARCRegister rd, SPARCInstructionFactory factory) {
        return factory.newRettInstruction(addr);
    }
}

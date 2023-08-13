class FlushDecoder extends MemoryInstructionDecoder {
    FlushDecoder() {
        super(FLUSH, "flush", RTLDT_UNKNOWN);
    }
    Instruction decodeMemoryInstruction(int instruction, SPARCRegisterIndirectAddress addr,
                      SPARCRegister rd, SPARCInstructionFactory factory) {
        return factory.newFlushInstruction(addr);
    }
}

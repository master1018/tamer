class V9PrefetchDecoder extends MemoryInstructionDecoder
           implements V9InstructionDecoder {
    V9PrefetchDecoder() {
      super(PREFETCH, "prefetch", RTLDT_SIGNED_WORD);
    }
    Instruction decodeMemoryInstruction(int instruction,
                                   SPARCRegisterIndirectAddress addr,
                                   SPARCRegister rd, SPARCInstructionFactory factory) {
        SPARCV9InstructionFactory v9factory = (SPARCV9InstructionFactory) factory;
        return v9factory.newV9PrefetchInstruction(name, addr, rd.getNumber());
    }
}

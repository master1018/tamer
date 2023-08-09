class V9AlternateSpacePrefetchDecoder extends V9AlternateSpaceDecoder {
    V9AlternateSpacePrefetchDecoder() {
      super(PREFETCHA, "prefetcha", RTLDT_SIGNED_WORD);
    }
    Instruction decodeV9AsiLoadStore(int instruction,
                                     SPARCV9RegisterIndirectAddress addr,
                                     SPARCRegister rd,
                                     SPARCV9InstructionFactory factory) {
        SPARCV9InstructionFactory v9factory = (SPARCV9InstructionFactory) factory;
        return v9factory.newV9PrefetchInstruction(name, addr, rd.getNumber());
    }
}

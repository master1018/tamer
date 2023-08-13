class AlternateSpaceSwapDecoder extends SwapDecoder {
    AlternateSpaceSwapDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    Instruction decodeMemoryInstruction(int instruction,
                                     SPARCRegisterIndirectAddress addr,
                                     SPARCRegister rd,
                                     SPARCInstructionFactory factory) {
        setAddressSpace(instruction, addr);
        return factory.newSwapInstruction(name, addr, rd);
    }
}

class AlternateSpaceLdstubDecoder extends LdstubDecoder {
    AlternateSpaceLdstubDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    Instruction decodeMemoryInstruction(int instruction,
                                               SPARCRegisterIndirectAddress addr,
                                               SPARCRegister rd,
                                               SPARCInstructionFactory factory) {
        setAddressSpace(instruction, addr);
        return factory.newLdstubInstruction(name, addr, rd);
    }
}

abstract class V9AlternateSpaceDecoder extends MemoryInstructionDecoder
                    implements V9InstructionDecoder {
    V9AlternateSpaceDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    SPARCRegisterIndirectAddress newRegisterIndirectAddress(SPARCRegister rs1, SPARCRegister rs2) {
        return new SPARCV9RegisterIndirectAddress(rs1, rs2);
    }
    SPARCRegisterIndirectAddress newRegisterIndirectAddress(SPARCRegister rs1, int offset) {
        return new SPARCV9RegisterIndirectAddress(rs1, offset);
    }
    abstract Instruction decodeV9AsiLoadStore(int instruction,
                                              SPARCV9RegisterIndirectAddress addr,
                                              SPARCRegister rd,
                                              SPARCV9InstructionFactory factory);
    Instruction decodeMemoryInstruction(int instruction,
                                   SPARCRegisterIndirectAddress addr,
                                   SPARCRegister rd, SPARCInstructionFactory factory) {
        SPARCV9RegisterIndirectAddress v9addr = (SPARCV9RegisterIndirectAddress) addr;
        if (isIBitSet(instruction)) {
            v9addr.setIndirectAsi(true);
        } else {
            int asi = (instruction & ASI_MASK) >>> ASI_START_BIT;
            v9addr.setAddressSpace(asi);
        }
        return decodeV9AsiLoadStore(instruction, v9addr, rd,
                                    (SPARCV9InstructionFactory) factory);
    }
}

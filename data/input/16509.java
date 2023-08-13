class WriteDecoder extends ReadWriteDecoder {
    WriteDecoder(int specialRegNum) {
        super(specialRegNum);
    }
    Instruction decodeReadWrite(int instruction, SPARCInstructionFactory factory,
                                int rs1Num, int rdNum) {
        Instruction instr = null;
        int specialReg = specialRegNum;
        if (rdNum == 0)
            specialReg = SPARCSpecialRegisters.Y;
        return factory.newWriteInstruction(specialReg, rdNum,
                              SPARCRegisters.getRegister(rs1Num),
                              getOperand2(instruction));
    }
}

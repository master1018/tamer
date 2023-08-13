abstract class V9PrivilegedReadWriteDecoder extends InstructionDecoder
                  implements V9InstructionDecoder {
    static boolean isLegalPrivilegedRegister(int reg) {
        return (reg > -1 && reg < 16) || reg == 31;
    }
}

public class SPARCV9ReturnInstruction extends SPARCRettInstruction
                    implements SPARCV9Instruction {
    public SPARCV9ReturnInstruction(SPARCRegisterIndirectAddress addr) {
        super("return", addr);
    }
}

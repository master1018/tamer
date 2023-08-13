public class SPARCNoopInstruction extends SPARCInstruction {
    public SPARCNoopInstruction() {
        super("nop");
    }
    public boolean isNoop() {
        return true;
    }
}

import com.sun.tools.classfile.Instruction;
public abstract class InstructionDetailWriter extends BasicWriter {
    public enum Kind {
        LOCAL_VARS("localVariables"),
        LOCAL_VAR_TYPES("localVariableTypes"),
        SOURCE("source"),
        STACKMAPS("stackMaps"),
        TRY_BLOCKS("tryBlocks");
        Kind(String option) {
            this.option = option;
        }
        final String option;
    }
    InstructionDetailWriter(Context context) {
        super(context);
    }
    abstract void writeDetails(Instruction instr);
    void flush() { }
}

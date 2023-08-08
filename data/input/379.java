public class LDC_W extends Instruction {
    public LDC_W() {
        super(Opcode.LDC_W);
        super.setOperandNumber(1);
        super.setOperandSize(2);
        super.setOperandTypes(OPERAND_CPINFO);
    }
    public LDC_W(int value) {
        super(Opcode.LDC_W);
        super.setOperandNumber(1);
        super.setOperandSize(2);
        super.setOperandTypes(OPERAND_CPINFO);
        super.setCpoolElement(new Integer(value), DConstantPool.CONSTANT_Integer);
    }
    public LDC_W(float value) {
        super(Opcode.LDC_W);
        super.setOperandNumber(1);
        super.setOperandSize(2);
        super.setOperandTypes(OPERAND_CPINFO);
        super.setCpoolElement(new Float(value), DConstantPool.CONSTANT_Float);
    }
    public LDC_W(String value, boolean isClassReference) {
        super(Opcode.LDC_W);
        super.setOperandNumber(1);
        super.setOperandSize(2);
        super.setOperandTypes(OPERAND_CPINFO);
        super.setCpoolElement(value, isClassReference ? DConstantPool.CONSTANT_Class : DConstantPool.CONSTANT_String);
    }
    public int getStackUse() {
        return 1;
    }
    public int[] stackResponse() {
        return new int[] { (cpoolType == DConstantPool.CONSTANT_Class || cpoolType == DConstantPool.CONSTANT_String ? STACK_REFERENCE : ((cpoolType == DConstantPool.CONSTANT_Float ? STACK_FLOAT : ((cpoolType == DConstantPool.CONSTANT_Integer ? STACK_INT : STACK_UNKNOWN))))) };
    }
}

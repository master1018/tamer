class AssignShiftRightExpression extends AssignOpExpression {
    public AssignShiftRightExpression(long where, Expression left, Expression right) {
        super(ASGRSHIFT, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ishr + itype.getTypeCodeOffset());
    }
}

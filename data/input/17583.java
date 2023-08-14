class AssignUnsignedShiftRightExpression extends AssignOpExpression {
    public AssignUnsignedShiftRightExpression(long where, Expression left, Expression right) {
        super(ASGURSHIFT, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_iushr + itype.getTypeCodeOffset());
    }
}

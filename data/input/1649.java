class IntegerExpression extends ConstantExpression {
    int value;
    IntegerExpression(int op, long where, Type type, int value) {
        super(op, where, type);
        this.value = value;
    }
    public boolean fitsType(Environment env, Context ctx, Type t) {
        if (this.type.isType(TC_CHAR)) {
            return super.fitsType(env, ctx, t);
        }
        switch (t.getTypeCode()) {
          case TC_BYTE:
            return value == (byte)value;
          case TC_SHORT:
            return value == (short)value;
          case TC_CHAR:
            return value == (char)value;
        }
        return super.fitsType(env, ctx, t);
    }
    public Object getValue() {
        return new Integer(value);
    }
    public boolean equals(int i) {
        return value == i;
    }
    public boolean equalsDefault() {
        return value == 0;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc, new Integer(value));
    }
}

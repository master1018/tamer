class AddExpression extends BinaryArithmeticExpression {
    public AddExpression(long where, Expression left, Expression right) {
        super(ADD, where, left, right);
    }
    void selectType(Environment env, Context ctx, int tm) {
        if ((left.type == Type.tString) && !right.type.isType(TC_VOID)) {
            type = Type.tString;
            return;
        } else if ((right.type == Type.tString) && !left.type.isType(TC_VOID)) {
            type = Type.tString;
            return;
        }
        super.selectType(env, ctx, tm);
    }
    public boolean isNonNull() {
        return true;
    }
    Expression eval(int a, int b) {
        return new IntExpression(where, a + b);
    }
    Expression eval(long a, long b) {
        return new LongExpression(where, a + b);
    }
    Expression eval(float a, float b) {
        return new FloatExpression(where, a + b);
    }
    Expression eval(double a, double b) {
        return new DoubleExpression(where, a + b);
    }
    Expression eval(String a, String b) {
        return new StringExpression(where, a + b);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (type == Type.tString && isConstant()) {
            StringBuffer buffer = inlineValueSB(env, ctx, new StringBuffer());
            if (buffer != null) {
                return new StringExpression(where, buffer.toString());
            }
        }
        return super.inlineValue(env, ctx);
    }
    protected StringBuffer inlineValueSB(Environment env,
                                         Context ctx,
                                         StringBuffer buffer) {
        if (type != Type.tString) {
            return super.inlineValueSB(env, ctx, buffer);
        }
        buffer = left.inlineValueSB(env, ctx, buffer);
        if (buffer != null) {
            buffer = right.inlineValueSB(env, ctx, buffer);
        }
        return buffer;
    }
    Expression simplify() {
        if (!type.isType(TC_CLASS)) {
            if (type.inMask(TM_INTEGER)) {
                if (left.equals(0)) {
                    return right;
                }
                if (right.equals(0)) {
                    return left;
                }
            }
        } else if (right.type.isType(TC_NULL)) {
            right = new StringExpression(right.where, "null");
        } else if (left.type.isType(TC_NULL)) {
            left = new StringExpression(left.where, "null");
        }
        return this;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return (type.isType(TC_CLASS) ? 12 : 1)
            + left.costInline(thresh, env, ctx)
            + right.costInline(thresh, env, ctx);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_iadd + type.getTypeCodeOffset());
    }
    void codeAppend(Environment env, Context ctx, Assembler asm,
                    ClassDeclaration sbClass, boolean needBuffer)
        throws ClassNotFound, AmbiguousMember {
        if (type.isType(TC_CLASS)) {
            left.codeAppend(env, ctx, asm, sbClass, needBuffer);
            right.codeAppend(env, ctx, asm, sbClass, false);
        } else {
            super.codeAppend(env, ctx, asm, sbClass, needBuffer);
        }
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        if (type.isType(TC_CLASS)) {
            try {
                if (left.equals("")) {
                    right.codeValue(env, ctx, asm);
                    right.ensureString(env, ctx, asm);
                    return;
                }
                if (right.equals("")) {
                    left.codeValue(env, ctx, asm);
                    left.ensureString(env, ctx, asm);
                    return;
                }
                ClassDeclaration sbClass =
                    env.getClassDeclaration(idJavaLangStringBuffer);
                ClassDefinition sourceClass = ctx.field.getClassDefinition();
                codeAppend(env, ctx, asm, sbClass, true);
                MemberDefinition f =
                    sbClass.getClassDefinition(env).matchMethod(env,
                                                                sourceClass,
                                                                idToString);
                asm.add(where, opc_invokevirtual, f);
            } catch (ClassNotFound e) {
                throw new CompilerError(e);
            } catch (AmbiguousMember e) {
                throw new CompilerError(e);
            }
        } else {
            super.codeValue(env, ctx, asm);
        }
    }
}

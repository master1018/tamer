class ArrayAccessExpression extends UnaryExpression {
    Expression index;
    public ArrayAccessExpression(long where, Expression right, Expression index) {
        super(ARRAYACCESS, where, Type.tError, right);
        this.index = index;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = right.checkValue(env, ctx, vset, exp);
        if (index == null) {
            env.error(where, "array.index.required");
            return vset;
        }
        vset = index.checkValue(env, ctx, vset, exp);
        index = convert(env, ctx, Type.tInt, index);
        if (!right.type.isType(TC_ARRAY)) {
            if (!right.type.isType(TC_ERROR)) {
                env.error(where, "not.array", right.type);
            }
            return vset;
        }
        type = right.type.getElementType();
        return vset;
    }
    public Vset checkAmbigName(Environment env, Context ctx,
                               Vset vset, Hashtable exp,
                               UnaryExpression loc) {
        if (index == null) {
            vset = right.checkAmbigName(env, ctx, vset, exp, this);
            if (right.type == Type.tPackage) {
                FieldExpression.reportFailedPackagePrefix(env, right);
                return vset;
            }
            if (right instanceof TypeExpression) {
                Type atype = Type.tArray(right.type);
                loc.right = new TypeExpression(where, atype);
                return vset;
            }
            env.error(where, "array.index.required");
            return vset;
        }
        return super.checkAmbigName(env, ctx, vset, exp, loc);
    }
    public Vset checkLHS(Environment env, Context ctx,
                         Vset vset, Hashtable exp) {
        return checkValue(env, ctx, vset, exp);
    }
    public Vset checkAssignOp(Environment env, Context ctx,
                              Vset vset, Hashtable exp, Expression outside) {
        return checkValue(env, ctx, vset, exp);
    }
    public FieldUpdater getAssigner(Environment env, Context ctx) {
        return null;
    }
    public FieldUpdater getUpdater(Environment env, Context ctx) {
        return null;
    }
    Type toType(Environment env, Context ctx) {
        return toType(env, right.toType(env, ctx));
    }
    Type toType(Environment env, Type t) {
        if (index != null) {
            env.error(index.where, "array.dim.in.type");
        }
        return Type.tArray(t);
    }
    public Expression inline(Environment env, Context ctx) {
        right = right.inlineValue(env, ctx);
        index = index.inlineValue(env, ctx);
        return this;
    }
    public Expression inlineValue(Environment env, Context ctx) {
        right = right.inlineValue(env, ctx);
        index = index.inlineValue(env, ctx);
        return this;
    }
    public Expression inlineLHS(Environment env, Context ctx) {
        return inlineValue(env, ctx);
    }
    public Expression copyInline(Context ctx) {
        ArrayAccessExpression e = (ArrayAccessExpression)clone();
        e.right = right.copyInline(ctx);
        if (index == null) {
            e.index = null;
        } else {
            e.index = index.copyInline(ctx);
        }
        return e;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + right.costInline(thresh, env, ctx)
            + index.costInline(thresh, env, ctx);
    }
    int codeLValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        index.codeValue(env, ctx, asm);
        return 2;
    }
    void codeLoad(Environment env, Context ctx, Assembler asm) {
        switch (type.getTypeCode()) {
          case TC_BOOLEAN:
          case TC_BYTE:
            asm.add(where, opc_baload);
            break;
          case TC_CHAR:
            asm.add(where, opc_caload);
            break;
          case TC_SHORT:
            asm.add(where, opc_saload);
            break;
          default:
            asm.add(where, opc_iaload + type.getTypeCodeOffset());
        }
    }
    void codeStore(Environment env, Context ctx, Assembler asm) {
        switch (type.getTypeCode()) {
          case TC_BOOLEAN:
          case TC_BYTE:
            asm.add(where, opc_bastore);
            break;
          case TC_CHAR:
            asm.add(where, opc_castore);
            break;
          case TC_SHORT:
            asm.add(where, opc_sastore);
            break;
          default:
            asm.add(where, opc_iastore + type.getTypeCodeOffset());
        }
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeLValue(env, ctx, asm);
        codeLoad(env, ctx, asm);
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        right.print(out);
        out.print(" ");
        if (index != null) {
            index.print(out);
        } else {
        out.print("<empty>");
        }
        out.print(")");
    }
}

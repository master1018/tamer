class ThisExpression extends Expression {
    LocalMember field;
    Expression implementation;
    Expression outerArg;
    public ThisExpression(long where) {
        super(THIS, where, Type.tObject);
    }
    protected ThisExpression(int op, long where) {
        super(op, where, Type.tObject);
    }
    public ThisExpression(long where, LocalMember field) {
        super(THIS, where, Type.tObject);
        this.field = field;
        field.readcount++;
    }
    public ThisExpression(long where, Context ctx) {
        super(THIS, where, Type.tObject);
        field = ctx.getLocalField(idThis);
        field.readcount++;
    }
    public ThisExpression(long where, Expression outerArg) {
        this(where);
        this.outerArg = outerArg;
    }
    public Expression getImplementation() {
        if (implementation != null)
            return implementation;
        return this;
    }
    public Expression getOuterArg() {
        return outerArg;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        if (ctx.field.isStatic()) {
            env.error(where, "undef.var", opNames[op]);
            type = Type.tError;
            return vset;
        }
        if (field == null) {
            field = ctx.getLocalField(idThis);
            field.readcount++;
        }
        if (field.scopeNumber < ctx.frameNumber) {
            implementation = ctx.makeReference(env, field);
        }
        if (!vset.testVar(field.number)) {
            env.error(where, "access.inst.before.super", opNames[op]);
        }
        if (field == null) {
            type = ctx.field.getClassDeclaration().getType();
        } else {
            type = field.getType();
        }
        return vset;
    }
    public boolean isNonNull() {
        return true;
    }
    public FieldUpdater getAssigner(Environment env, Context ctx) {
        return null;
    }
    public FieldUpdater getUpdater(Environment env, Context ctx) {
        return null;
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineValue(env, ctx);
        if (field != null && field.isInlineable(env, false)) {
            Expression e = (Expression)field.getValue(env);
            if (e != null) {
                e = e.copyInline(ctx);
                e.type = type;  
                return e;
            }
        }
        return this;
    }
    public Expression copyInline(Context ctx) {
        if (implementation != null)
            return implementation.copyInline(ctx);
        ThisExpression e = (ThisExpression)clone();
        if (field == null) {
            e.field = ctx.getLocalField(idThis);
            e.field.readcount++;
        } else {
            e.field = field.getCurrentInlineCopy(ctx);
        }
        if (outerArg != null) {
            e.outerArg = outerArg.copyInline(ctx);
        }
        return e;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_aload, new Integer(field.number));
    }
    public void print(PrintStream out) {
        if (outerArg != null) {
            out.print("(outer=");
            outerArg.print(out);
            out.print(" ");
        }
        String pfx = (field == null) ? ""
            : field.getClassDefinition().getName().getFlatName().getName()+".";
        pfx += opNames[op];
        out.print(pfx + "#" + ((field != null) ? field.hashCode() : 0));
        if (outerArg != null)
            out.print(")");
    }
}

class UplevelReference implements Constants {
    ClassDefinition client;
    LocalMember target;
    LocalMember localArgument;
    MemberDefinition localField;
    UplevelReference next;
    public UplevelReference(ClassDefinition client, LocalMember target) {
        this.client = client;
        this.target = target;
        Identifier valName;
        if (target.getName().equals(idThis)) {
            ClassDefinition tc = target.getClassDefinition();
            int depth = 0;
            for (ClassDefinition pd = tc; !pd.isTopLevel(); pd = pd.getOuterClass()) {
                depth += 1;
            }
            valName = Identifier.lookup(prefixThis + depth);
        } else {
            valName = Identifier.lookup(prefixVal + target.getName());
        }
        Identifier base = valName;
        int tick = 0;
        while (true) {
            boolean failed = (client.getFirstMatch(valName) != null);
            for (UplevelReference r = client.getReferences();
                    r != null; r = r.next) {
                if (r.target.getName().equals(valName)) {
                    failed = true;
                }
            }
            if (!failed) {
                break;
            }
            valName = Identifier.lookup(base + "$" + (++tick));
        }
        localArgument = new LocalMember(target.getWhere(),
                                       client,
                                       M_FINAL | M_SYNTHETIC,
                                       target.getType(),
                                       valName);
    }
    public UplevelReference insertInto(UplevelReference references) {
        if (references == null || isEarlierThan(references)) {
            next = references;
            return this;
        } else {
            UplevelReference prev = references;
            while (!(prev.next == null || isEarlierThan(prev.next))) {
                prev = prev.next;
            }
            next = prev.next;
            prev.next = this;
            return references;
        }
    }
    public final boolean isEarlierThan(UplevelReference other) {
        if (isClientOuterField()) {
            return true;
        } else if (other.isClientOuterField()) {
            return false;
        }
        LocalMember target2 = other.target;
        Identifier name = target.getName();
        Identifier name2 = target2.getName();
        int cmp = name.toString().compareTo(name2.toString());
        if (cmp != 0) {
            return cmp < 0;
        }
        Identifier cname = target.getClassDefinition().getName();
        Identifier cname2 = target2.getClassDefinition().getName();
        int ccmp = cname.toString().compareTo(cname2.toString());
        return ccmp < 0;
    }
    public final LocalMember getTarget() {
        return target;
    }
    public final LocalMember getLocalArgument() {
        return localArgument;
    }
    public final MemberDefinition getLocalField() {
        return localField;
    }
    public final MemberDefinition getLocalField(Environment env) {
        if (localField == null) {
            makeLocalField(env);
        }
        return localField;
    }
    public final ClassDefinition getClient() {
        return client;
    }
    public final UplevelReference getNext() {
        return next;
    }
    public boolean isClientOuterField() {
        MemberDefinition outerf = client.findOuterMember();
        return (outerf != null) && (localField == outerf);
    }
    public boolean localArgumentAvailable(Environment env, Context ctx) {
        MemberDefinition reff = ctx.field;
        if (reff.getClassDefinition() != client) {
            throw new CompilerError("localArgumentAvailable");
        }
        return (   reff.isConstructor()
                || reff.isVariable()
                || reff.isInitializer() );
    }
    public void noteReference(Environment env, Context ctx) {
        if (localField == null && !localArgumentAvailable(env, ctx)) {
            makeLocalField(env);
        }
    }
    private void makeLocalField(Environment env) {
        client.referencesMustNotBeFrozen();
        int mod = M_PRIVATE | M_FINAL | M_SYNTHETIC;
        localField = env.makeMemberDefinition(env,
                                             localArgument.getWhere(),
                                             client, null,
                                             mod,
                                             localArgument.getType(),
                                             localArgument.getName(),
                                             null, null, null);
    }
    public Expression makeLocalReference(Environment env, Context ctx) {
        if (ctx.field.getClassDefinition() != client) {
            throw new CompilerError("makeLocalReference");
        }
        if (localArgumentAvailable(env, ctx)) {
            return new IdentifierExpression(0, localArgument);
        } else {
            return makeFieldReference(env, ctx);
        }
    }
    public Expression makeFieldReference(Environment env, Context ctx) {
        Expression e = ctx.findOuterLink(env, 0, localField);
        return new FieldExpression(0, e, localField);
    }
    public void willCodeArguments(Environment env, Context ctx) {
        if (!isClientOuterField()) {
            ctx.noteReference(env, target);
        }
        if (next != null) {
            next.willCodeArguments(env, ctx);
        }
    }
    public void codeArguments(Environment env, Context ctx, Assembler asm,
                              long where, MemberDefinition conField) {
        if (!isClientOuterField()) {
            Expression e = ctx.makeReference(env, target);
            e.codeValue(env, ctx, asm);
        }
        if (next != null) {
            next.codeArguments(env, ctx, asm, where, conField);
        }
    }
    public void codeInitialization(Environment env, Context ctx, Assembler asm,
                                   long where, MemberDefinition conField) {
        if (localField != null && !isClientOuterField()) {
            Expression e = ctx.makeReference(env, target);
            Expression f = makeFieldReference(env, ctx);
            e = new AssignExpression(e.getWhere(), f, e);
            e.type = localField.getType();
            e.code(env, ctx, asm);
        }
        if (next != null) {
            next.codeInitialization(env, ctx, asm, where, conField);
        }
    }
    public String toString() {
        return "[" + localArgument + " in " + client + "]";
    }
}

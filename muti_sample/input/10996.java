class LocalMember extends MemberDefinition {
    int number = -1;
    int readcount;
    int writecount;
    int scopeNumber;
    public int getScopeNumber() {
        return scopeNumber;
    }
    LocalMember originalOfCopy;
    LocalMember prev;
    public LocalMember(long where, ClassDefinition clazz, int modifiers, Type type,
                      Identifier name) {
        super(where, clazz, modifiers, type, name, null, null);
    }
    public LocalMember(ClassDefinition innerClass) {
        super(innerClass);
        name = innerClass.getLocalName();
    }
    LocalMember(MemberDefinition field) {
        this(0, null, 0, field.getType(), idClass);
        accessPeer = field;
    }
    final MemberDefinition getMember() {
        return (name == idClass) ? accessPeer : null;
    }
    public boolean isLocal() {
        return true;
    }
    public LocalMember copyInline(Context ctx) {
        LocalMember copy = new LocalMember(where, clazz, modifiers, type, name);
        copy.readcount = this.readcount;
        copy.writecount = this.writecount;
        copy.originalOfCopy = this;
        copy.addModifiers(M_LOCAL);
        if (this.accessPeer != null
            && (this.accessPeer.getModifiers() & M_LOCAL) == 0) {
            throw new CompilerError("local copyInline");
        }
        this.accessPeer = copy;
        return copy;
    }
    public LocalMember getCurrentInlineCopy(Context ctx) {
        MemberDefinition accessPeer = this.accessPeer;
        if (accessPeer != null && (accessPeer.getModifiers() & M_LOCAL) != 0) {
            LocalMember copy = (LocalMember)accessPeer;
            return copy;
        }
        return this;
    }
    static public LocalMember[] copyArguments(Context ctx, MemberDefinition field) {
        Vector v = field.getArguments();
        LocalMember res[] = new LocalMember[v.size()];
        v.copyInto(res);
        for (int i = 0; i < res.length; i++) {
            res[i] = res[i].copyInline(ctx);
        }
        return res;
    }
    static public void doneWithArguments(Context ctx, LocalMember res[]) {
        for (int i = 0; i < res.length; i++) {
            if (res[i].originalOfCopy.accessPeer == res[i]) {
                res[i].originalOfCopy.accessPeer = null;
            }
        }
    }
    public boolean isInlineable(Environment env, boolean fromFinal) {
        return (getModifiers() & M_INLINEABLE) != 0;
    }
    public boolean isUsed() {
        return (readcount != 0) || (writecount != 0);
    }
    LocalMember getAccessVar() {
        return (LocalMember)accessPeer;
    }
    void setAccessVar(LocalMember f) {
        accessPeer = f;
    }
    MemberDefinition getAccessVarMember() {
        return accessPeer;
    }
    void setAccessVarMember(MemberDefinition f) {
        accessPeer = f;
    }
    public Node getValue(Environment env) {
        return (Expression)getValue();
    }
    public int getNumber(Context ctx) {
        return number;
    }
}

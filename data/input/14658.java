class MemberDefinition implements Constants {
    protected long where;
    protected int modifiers;
    protected Type type;
    protected String documentation;
    protected IdentifierToken expIds[];
    protected ClassDeclaration exp[];
    protected Node value;
    protected ClassDefinition clazz;
    protected Identifier name;
    protected ClassDefinition innerClass;
    protected MemberDefinition nextMember;
    protected MemberDefinition nextMatch;
    protected MemberDefinition accessPeer;
    protected boolean superAccessMethod;
    public MemberDefinition(long where, ClassDefinition clazz, int modifiers,
                            Type type, Identifier name,
                            IdentifierToken expIds[], Node value) {
        if (expIds == null) {
            expIds = new IdentifierToken[0];
        }
        this.where = where;
        this.clazz = clazz;
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
        this.expIds = expIds;
        this.value = value;
    }
    public MemberDefinition(ClassDefinition innerClass) {
        this(innerClass.getWhere(),
             innerClass.getOuterClass(),
             innerClass.getModifiers(),
             innerClass.getType(),
             innerClass.getName().getFlatName().getName(),
             null, null);
        this.innerClass = innerClass;
    }
    static private Map proxyCache;
    public static MemberDefinition makeProxyMember(MemberDefinition field,
                                                   ClassDefinition classDef,
                                                   Environment env) {
        if (proxyCache == null) {
            proxyCache = new HashMap();
        }
        String key = field.toString() + "@" + classDef.toString();
        MemberDefinition proxy = (MemberDefinition)proxyCache.get(key);
        if (proxy != null)
            return proxy;
        proxy = new MemberDefinition(field.getWhere(), classDef,
                                     field.getModifiers(), field.getType(),
                                     field.getName(), field.getExceptionIds(),
                                     null);
        proxy.exp = field.getExceptions(env);
        proxyCache.put(key, proxy);
        return proxy;
    }
    public final long getWhere() {
        return where;
    }
    public final ClassDeclaration getClassDeclaration() {
        return clazz.getClassDeclaration();
    }
    public void resolveTypeStructure(Environment env) {
    }
    public ClassDeclaration getDefiningClassDeclaration() {
        return getClassDeclaration();
    }
    public final ClassDefinition getClassDefinition() {
        return clazz;
    }
    public final ClassDefinition getTopClass() {
        return clazz.getTopClass();
    }
    public final int getModifiers() {
        return modifiers;
    }
    public final void subModifiers(int mod) {
        modifiers &= ~mod;
    }
    public final void addModifiers(int mod) {
        modifiers |= mod;
    }
    public final Type getType() {
        return type;
    }
    public final Identifier getName() {
        return name;
    }
    public Vector getArguments() {
        return isMethod() ? new Vector() : null;
    }
    public ClassDeclaration[] getExceptions(Environment env) {
        if (expIds != null && exp == null) {
            if (expIds.length == 0)
                exp = new ClassDeclaration[0];
            else
                throw new CompilerError("getExceptions "+this);
        }
        return exp;
    }
    public final IdentifierToken[] getExceptionIds() {
        return expIds;
    }
    public ClassDefinition getInnerClass() {
        return innerClass;
    }
    public boolean isUplevelValue() {
        if (!isSynthetic() || !isVariable() || isStatic()) {
            return false;
        }
        String name = this.name.toString();
        return name.startsWith(prefixVal)
            || name.toString().startsWith(prefixLoc)
            || name.toString().startsWith(prefixThis);
    }
    public boolean isAccessMethod() {
        return isSynthetic() && isMethod() && (accessPeer != null);
    }
    public MemberDefinition getAccessMethodTarget() {
        if (isAccessMethod()) {
            for (MemberDefinition f = accessPeer; f != null; f = f.accessPeer) {
                if (!f.isAccessMethod()) {
                    return f;
                }
            }
        }
        return null;
    }
    public void setAccessMethodTarget(MemberDefinition target) {
        if (getAccessMethodTarget() != target) {
            if (accessPeer != null || target.accessPeer != null) {
                throw new CompilerError("accessPeer");
            }
            accessPeer = target;
        }
    }
    public MemberDefinition getAccessUpdateMember() {
        if (isAccessMethod()) {
            for (MemberDefinition f = accessPeer; f != null; f = f.accessPeer) {
                if (f.isAccessMethod()) {
                    return f;
                }
            }
        }
        return null;
    }
    public void setAccessUpdateMember(MemberDefinition updater) {
        if (getAccessUpdateMember() != updater) {
            if (!isAccessMethod() ||
                    updater.getAccessMethodTarget() != getAccessMethodTarget()) {
                throw new CompilerError("accessPeer");
            }
            updater.accessPeer = accessPeer;
            accessPeer = updater;
        }
    }
    public final boolean isSuperAccessMethod() {
        return superAccessMethod;
    }
    public final void setIsSuperAccessMethod(boolean b) {
        superAccessMethod = b;
    }
    public final boolean isBlankFinal() {
        return isFinal() && !isSynthetic() && getValue() == null;
    }
    public boolean isNeverNull() {
        if (isUplevelValue()) {
            return !name.toString().startsWith(prefixVal);
        }
        return false;
    }
    public Node getValue(Environment env) throws ClassNotFound {
        return value;
    }
    public final Node getValue() {
        return value;
    }
    public final void setValue(Node value) {
        this.value = value;
    }
    public Object getInitialValue() {
        return null;
    }
    public final MemberDefinition getNextMember() {
        return nextMember;
    }
    public final MemberDefinition getNextMatch() {
        return nextMatch;
    }
    public String getDocumentation() {
        return documentation;
    }
    public void check(Environment env) throws ClassNotFound {
    }
    public Vset check(Environment env, Context ctx, Vset vset) throws ClassNotFound {
        return vset;
    }
    public void code(Environment env, Assembler asm) throws ClassNotFound {
        throw new CompilerError("code");
    }
    public void codeInit(Environment env, Context ctx, Assembler asm) throws ClassNotFound {
        throw new CompilerError("codeInit");
    }
    public boolean reportDeprecated(Environment env) {
        return (isDeprecated() || clazz.reportDeprecated(env));
    }
    public final boolean canReach(Environment env, MemberDefinition f) {
        if (f.isLocal() || !f.isVariable() || !(isVariable() || isInitializer()))
            return true;
        if ((getClassDeclaration().equals(f.getClassDeclaration())) &&
            (isStatic() == f.isStatic())) {
            while (((f = f.getNextMember()) != null) && (f != this));
            return f != null;
        }
        return true;
    }
    static final int PUBLIC_ACCESS = 1;
    static final int PROTECTED_ACCESS = 2;
    static final int PACKAGE_ACCESS = 3;
    static final int PRIVATE_ACCESS = 4;
    private int getAccessLevel() {
        if (isPublic()) {
            return PUBLIC_ACCESS;
        } else if (isProtected()) {
            return PROTECTED_ACCESS;
        } else if (isPackagePrivate()) {
            return PACKAGE_ACCESS;
        } else if (isPrivate()) {
            return PRIVATE_ACCESS;
        } else {
            throw new CompilerError("getAccessLevel()");
        }
    }
    private void reportError(Environment env, String errorString,
                             ClassDeclaration clazz,
                             MemberDefinition method) {
        if (clazz == null) {
            env.error(getWhere(), errorString,
                      this, getClassDeclaration(),
                      method.getClassDeclaration());
        } else {
            env.error(clazz.getClassDefinition().getWhere(),
                      errorString,
                      this, getClassDeclaration(),
                      method.getClassDeclaration());
        }
    }
    public boolean sameReturnType(MemberDefinition method) {
        if (!isMethod() || !method.isMethod()) {
            throw new CompilerError("sameReturnType: not method");
        }
        Type myReturnType = getType().getReturnType();
        Type yourReturnType = method.getType().getReturnType();
        return (myReturnType == yourReturnType);
    }
    public boolean checkOverride(Environment env, MemberDefinition method) {
        return checkOverride(env, method, null);
    }
    private boolean checkOverride(Environment env,
                                  MemberDefinition method,
                                  ClassDeclaration clazz) {
        boolean success = true;
        if (!isMethod()) {
            throw new CompilerError("checkOverride(), expected method");
        }
        if (isSynthetic()) {
            if (method.isFinal() ||
                (!method.isConstructor() &&
                 !method.isStatic() && !isStatic())) {
            }
            return true;
        }
        if (getName() != method.getName() ||
            !getType().equalArguments(method.getType())) {
            throw new CompilerError("checkOverride(), signature mismatch");
        }
        if (method.isStatic() && !isStatic()) {
            reportError(env, "override.static.with.instance", clazz, method);
            success = false;
        }
        if (!method.isStatic() && isStatic()) {
            reportError(env, "hide.instance.with.static", clazz, method);
            success = false;
        }
        if (method.isFinal()) {
            reportError(env, "override.final.method", clazz, method);
            success = false;
        }
        if (method.reportDeprecated(env) && !isDeprecated()
               && this instanceof sun.tools.javac.SourceMember) {
            reportError(env, "warn.override.is.deprecated",
                        clazz, method);
        }
        if (getAccessLevel() > method.getAccessLevel()) {
            reportError(env, "override.more.restrictive", clazz, method);
            success = false;
        }
        if (!sameReturnType(method)) {
        }
        if (!exceptionsFit(env, method)) {
            reportError(env, "override.incompatible.exceptions",
                        clazz, method);
            success = false;
        }
        return success;
    }
    public boolean checkMeet(Environment env,
                             MemberDefinition method,
                             ClassDeclaration clazz) {
        if (!isMethod()) {
            throw new CompilerError("checkMeet(), expected method");
        }
        if (!isAbstract() && !method.isAbstract()) {
            throw new CompilerError("checkMeet(), no abstract method");
        }
        else if (!isAbstract()) {
            return checkOverride(env, method, clazz);
        } else if (!method.isAbstract()) {
            return method.checkOverride(env, this, clazz);
        }
        if (getName() != method.getName() ||
            !getType().equalArguments(method.getType())) {
            throw new CompilerError("checkMeet(), signature mismatch");
        }
        if (!sameReturnType(method)) {
            env.error(clazz.getClassDefinition().getWhere(),
                      "meet.different.return",
                      this, this.getClassDeclaration(),
                      method.getClassDeclaration());
            return false;
        }
        return true;
    }
    public boolean couldOverride(Environment env,
                                 MemberDefinition method) {
        if (!isMethod()) {
            throw new CompilerError("coulcOverride(), expected method");
        }
        if (!method.isAbstract()) {
            return false;
        }
        if (getAccessLevel() > method.getAccessLevel()) {
            return false;
        }
        if (!exceptionsFit(env, method)) {
            return false;
        }
        return true;
    }
    private boolean exceptionsFit(Environment env,
                                  MemberDefinition method) {
        ClassDeclaration e1[] = getExceptions(env);        
        ClassDeclaration e2[] = method.getExceptions(env); 
    outer:
        for (int i = 0 ; i < e1.length ; i++) {
            try {
                ClassDefinition c1 = e1[i].getClassDefinition(env);
                for (int j = 0 ; j < e2.length ; j++) {
                    if (c1.subClassOf(env, e2[j])) {
                        continue outer;
                    }
                }
                if (c1.subClassOf(env,
                                  env.getClassDeclaration(idJavaLangError)))
                    continue outer;
                if (c1.subClassOf(env,
                                  env.getClassDeclaration(idJavaLangRuntimeException)))
                    continue outer;
                return false;
            } catch (ClassNotFound ee) {
                env.error(getWhere(), "class.not.found",
                          ee.name, method.getClassDeclaration());
            }
        }
        return true;
    }
    public final boolean isPublic() {
        return (modifiers & M_PUBLIC) != 0;
    }
    public final boolean isPrivate() {
        return (modifiers & M_PRIVATE) != 0;
    }
    public final boolean isProtected() {
        return (modifiers & M_PROTECTED) != 0;
    }
    public final boolean isPackagePrivate() {
        return (modifiers & (M_PUBLIC | M_PRIVATE | M_PROTECTED)) == 0;
    }
    public final boolean isFinal() {
        return (modifiers & M_FINAL) != 0;
    }
    public final boolean isStatic() {
        return (modifiers & M_STATIC) != 0;
    }
    public final boolean isSynchronized() {
        return (modifiers & M_SYNCHRONIZED) != 0;
    }
    public final boolean isAbstract() {
        return (modifiers & M_ABSTRACT) != 0;
    }
    public final boolean isNative() {
        return (modifiers & M_NATIVE) != 0;
    }
    public final boolean isVolatile() {
        return (modifiers & M_VOLATILE) != 0;
    }
    public final boolean isTransient() {
        return (modifiers & M_TRANSIENT) != 0;
    }
    public final boolean isMethod() {
        return type.isType(TC_METHOD);
    }
    public final boolean isVariable() {
        return !type.isType(TC_METHOD) && innerClass == null;
    }
    public final boolean isSynthetic() {
        return (modifiers & M_SYNTHETIC) != 0;
    }
    public final boolean isDeprecated() {
        return (modifiers & M_DEPRECATED) != 0;
    }
    public final boolean isStrict() {
        return (modifiers & M_STRICTFP) != 0;
    }
    public final boolean isInnerClass() {
        return innerClass != null;
    }
    public final boolean isInitializer() {
        return getName().equals(idClassInit);
    }
    public final boolean isConstructor() {
        return getName().equals(idInit);
    }
    public boolean isLocal() {
        return false;
    }
    public boolean isInlineable(Environment env, boolean fromFinal) throws ClassNotFound {
        return (isStatic() || isPrivate() || isFinal() || isConstructor() || fromFinal) &&
            !(isSynchronized() || isNative());
    }
    public boolean isConstant() {
        if (isFinal() && isVariable() && value != null) {
            try {
                modifiers &= ~M_FINAL;
                return ((Expression)value).isConstant();
            } finally {
                modifiers |= M_FINAL;
            }
        }
        return false;
    }
    public String toString() {
        Identifier name = getClassDefinition().getName();
        if (isInitializer()) {
            return isStatic() ? "static {}" : "instance {}";
        } else if (isConstructor()) {
            StringBuffer buf = new StringBuffer();
            buf.append(name);
            buf.append('(');
            Type argTypes[] = getType().getArgumentTypes();
            for (int i = 0 ; i < argTypes.length ; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(argTypes[i].toString());
            }
            buf.append(')');
            return buf.toString();
        } else if (isInnerClass()) {
            return getInnerClass().toString();
        }
        return type.typeString(getName().toString());
    }
    public void print(PrintStream out) {
        if (isPublic()) {
            out.print("public ");
        }
        if (isPrivate()) {
            out.print("private ");
        }
        if (isProtected()) {
            out.print("protected ");
        }
        if (isFinal()) {
            out.print("final ");
        }
        if (isStatic()) {
            out.print("static ");
        }
        if (isSynchronized()) {
            out.print("synchronized ");
        }
        if (isAbstract()) {
            out.print("abstract ");
        }
        if (isNative()) {
            out.print("native ");
        }
        if (isVolatile()) {
            out.print("volatile ");
        }
        if (isTransient()) {
            out.print("transient ");
        }
        out.println(toString() + ";");
    }
    public void cleanup(Environment env) {
        documentation = null;
        if (isMethod() && value != null) {
            int cost = 0;
            if (isPrivate() || isInitializer()) {
                value = Statement.empty;
            } else if ((cost =
                        ((Statement)value)
                       .costInline(Statement.MAXINLINECOST, null, null))
                                >= Statement.MAXINLINECOST) {
                value = Statement.empty;
            } else {
                try {
                    if (!isInlineable(null, true)) {
                        value = Statement.empty;
                    }
                }
                catch (ClassNotFound ee) { }
            }
            if (value != Statement.empty && env.dump()) {
                env.output("[after cleanup of " + getName() + ", " +
                           cost + " expression cost units remain]");
            }
        } else if (isVariable()) {
            if (isPrivate() || !isFinal() || type.isType(TC_ARRAY)) {
                value = null;
            }
        }
    }
}

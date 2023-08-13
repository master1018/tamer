class BinaryMember extends MemberDefinition {
    Expression value;
    BinaryAttribute atts;
    public BinaryMember(ClassDefinition clazz, int modifiers, Type type,
                       Identifier name, BinaryAttribute atts) {
        super(0, clazz, modifiers, type, name, null, null);
        this.atts = atts;
        if (getAttribute(idDeprecated) != null) {
            this.modifiers |= M_DEPRECATED;
        }
        if (getAttribute(idSynthetic) != null) {
            this.modifiers |= M_SYNTHETIC;
        }
    }
    public BinaryMember(ClassDefinition innerClass) {
        super(innerClass);
    }
    public boolean isInlineable(Environment env, boolean fromFinal) {
        return isConstructor() && (getClassDefinition().getSuperClass() == null);
    }
    public Vector getArguments() {
        if (isConstructor() && (getClassDefinition().getSuperClass() == null)) {
            Vector v = new Vector();
            v.addElement(new LocalMember(0, getClassDefinition(), 0,
                                        getClassDefinition().getType(), idThis));
            return v;
        }
        return null;
    }
    public ClassDeclaration[] getExceptions(Environment env) {
        if ((!isMethod()) || (exp != null)) {
            return exp;
        }
        byte data[] = getAttribute(idExceptions);
        if (data == null) {
            return new ClassDeclaration[0];
        }
        try {
            BinaryConstantPool cpool = ((BinaryClass)getClassDefinition()).getConstants();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            int n = in.readUnsignedShort();
            exp = new ClassDeclaration[n];
            for (int i = 0 ; i < n ; i++) {
                exp[i] = cpool.getDeclaration(env, in.readUnsignedShort());
            }
            return exp;
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }
    public String getDocumentation() {
        if (documentation != null) {
            return documentation;
        }
        byte data[] = getAttribute(idDocumentation);
        if (data == null) {
            return null;
        }
        try {
            return documentation = new DataInputStream(new ByteArrayInputStream(data)).readUTF();
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }
    private boolean isConstantCache = false;
    private boolean isConstantCached = false;
    public boolean isConstant() {
        if (!isConstantCached) {
            isConstantCache = isFinal()
                              && isVariable()
                              && getAttribute(idConstantValue) != null;
            isConstantCached = true;
        }
        return isConstantCache;
    }
    public Node getValue(Environment env) {
        if (isMethod()) {
            return null;
        }
        if (!isFinal()) {
            return null;
        }
        if (getValue() != null) {
            return (Expression)getValue();
        }
        byte data[] = getAttribute(idConstantValue);
        if (data == null) {
            return null;
        }
        try {
            BinaryConstantPool cpool = ((BinaryClass)getClassDefinition()).getConstants();
            Object obj = cpool.getValue(new DataInputStream(new ByteArrayInputStream(data)).readUnsignedShort());
            switch (getType().getTypeCode()) {
              case TC_BOOLEAN:
                setValue(new BooleanExpression(0, ((Number)obj).intValue() != 0));
                break;
              case TC_BYTE:
              case TC_SHORT:
              case TC_CHAR:
              case TC_INT:
                setValue(new IntExpression(0, ((Number)obj).intValue()));
                break;
              case TC_LONG:
                setValue(new LongExpression(0, ((Number)obj).longValue()));
                break;
              case TC_FLOAT:
                setValue(new FloatExpression(0, ((Number)obj).floatValue()));
                break;
              case TC_DOUBLE:
                setValue(new DoubleExpression(0, ((Number)obj).doubleValue()));
                break;
              case TC_CLASS:
                setValue(new StringExpression(0, (String)cpool.getValue(((Number)obj).intValue())));
                break;
            }
            return (Expression)getValue();
        } catch (IOException e) {
            throw new CompilerError(e);
        }
    }
    public byte[] getAttribute(Identifier name) {
        for (BinaryAttribute att = atts ; att != null ; att = att.next) {
            if (att.name.equals(name)) {
                return att.data;
            }
        }
        return null;
    }
    public boolean deleteAttribute(Identifier name) {
        BinaryAttribute walker = null, next = null;
        boolean succeed = false;
        while (atts.name.equals(name)) {
            atts = atts.next;
            succeed = true;
        }
        for (walker = atts; walker != null; walker = next) {
            next = walker.next;
            if (next != null) {
                if (next.name.equals(name)) {
                    walker.next = next.next;
                    next = next.next;
                    succeed = true;
                }
            }
        }
        for (walker = atts; walker != null; walker = walker.next) {
            if (walker.name.equals(name)) {
                throw new InternalError("Found attribute " + name);
            }
        }
        return succeed;
    }
    public void addAttribute(Identifier name, byte data[], Environment env) {
        this.atts = new BinaryAttribute(name, data, this.atts);
        ((BinaryClass)(this.clazz)).cpool.indexString(name.toString(), env);
    }
}

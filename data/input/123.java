class BinaryClass extends ClassDefinition implements Constants {
    BinaryConstantPool cpool;
    BinaryAttribute atts;
    Vector dependencies;
    private boolean haveLoadedNested = false;
    public BinaryClass(Object source, ClassDeclaration declaration, int modifiers,
                           ClassDeclaration superClass, ClassDeclaration interfaces[],
                           Vector dependencies) {
        super(source, 0, declaration, modifiers, null, null);
        this.dependencies = dependencies;
        this.superClass = superClass;
        this.interfaces = interfaces;
    }
    private boolean basicCheckDone = false;
    private boolean basicChecking = false;
    protected void basicCheck(Environment env) throws ClassNotFound {
        if (tracing) env.dtEnter("BinaryClass.basicCheck: " + getName());
        if (basicChecking || basicCheckDone) {
            if (tracing) env.dtExit("BinaryClass.basicCheck: OK " + getName());
            return;
        }
        if (tracing) env.dtEvent("BinaryClass.basicCheck: CHECKING " + getName());
        basicChecking = true;
        super.basicCheck(env);
        if (doInheritanceChecks) {
            collectInheritedMethods(env);
        }
        basicCheckDone = true;
        basicChecking = false;
        if (tracing) env.dtExit("BinaryClass.basicCheck: " + getName());
    }
    public static BinaryClass load(Environment env, DataInputStream in) throws IOException {
        return load(env, in, ~(ATT_CODE|ATT_ALLCLASSES));
    }
    public static BinaryClass load(Environment env,
                                   DataInputStream in, int mask) throws IOException {
        int magic = in.readInt();                    
        if (magic != JAVA_MAGIC) {
            throw new ClassFormatError("wrong magic: " + magic + ", expected " + JAVA_MAGIC);
        }
        int minor_version = in.readUnsignedShort();  
        int version = in.readUnsignedShort();        
        if (version < JAVA_MIN_SUPPORTED_VERSION) {
            throw new ClassFormatError(
                           sun.tools.javac.Main.getText(
                               "javac.err.version.too.old",
                               String.valueOf(version)));
        } else if ((version > JAVA_MAX_SUPPORTED_VERSION)
                     || (version == JAVA_MAX_SUPPORTED_VERSION
                  && minor_version > JAVA_MAX_SUPPORTED_MINOR_VERSION)) {
            throw new ClassFormatError(
                           sun.tools.javac.Main.getText(
                               "javac.err.version.too.recent",
                               version+"."+minor_version));
        }
        BinaryConstantPool cpool = new BinaryConstantPool(in);
        Vector dependencies = cpool.getDependencies(env);
        int classMod = in.readUnsignedShort() & ACCM_CLASS;  
        ClassDeclaration classDecl = cpool.getDeclaration(env, in.readUnsignedShort());
        ClassDeclaration superClassDecl = cpool.getDeclaration(env, in.readUnsignedShort());
        ClassDeclaration interfaces[] = new ClassDeclaration[in.readUnsignedShort()];
        for (int i = 0 ; i < interfaces.length ; i++) {
            interfaces[i] = cpool.getDeclaration(env, in.readUnsignedShort());
        }
        BinaryClass c = new BinaryClass(null, classDecl, classMod, superClassDecl,
                                        interfaces, dependencies);
        c.cpool = cpool;
        c.addDependency(superClassDecl);
        int nfields = in.readUnsignedShort();  
        for (int i = 0 ; i < nfields ; i++) {
            int fieldMod = in.readUnsignedShort() & ACCM_FIELD;
            Identifier fieldName = cpool.getIdentifier(in.readUnsignedShort());
            Type fieldType = cpool.getType(in.readUnsignedShort());
            BinaryAttribute atts = BinaryAttribute.load(in, cpool, mask);
            c.addMember(new BinaryMember(c, fieldMod, fieldType, fieldName, atts));
        }
        int nmethods = in.readUnsignedShort();  
        for (int i = 0 ; i < nmethods ; i++) {
            int methMod = in.readUnsignedShort() & ACCM_METHOD;
            Identifier methName = cpool.getIdentifier(in.readUnsignedShort());
            Type methType = cpool.getType(in.readUnsignedShort());
            BinaryAttribute atts = BinaryAttribute.load(in, cpool, mask);
            c.addMember(new BinaryMember(c, methMod, methType, methName, atts));
        }
        c.atts = BinaryAttribute.load(in, cpool, mask);
        byte data[] = c.getAttribute(idSourceFile);
        if (data != null) {
            DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(data));
            c.source = cpool.getString(dataStream.readUnsignedShort());
        }
        data = c.getAttribute(idDocumentation);
        if (data != null) {
            c.documentation = new DataInputStream(new ByteArrayInputStream(data)).readUTF();
        }
        if (c.getAttribute(idDeprecated) != null) {
            c.modifiers |= M_DEPRECATED;
        }
        if (c.getAttribute(idSynthetic) != null) {
            c.modifiers |= M_SYNTHETIC;
        }
        return c;
    }
    public void loadNested(Environment env) {
        loadNested(env, 0);
    }
    public void loadNested(Environment env, int flags) {
        if (haveLoadedNested) {
            if (tracing) env.dtEvent("loadNested: DUPLICATE CALL SKIPPED");
            return;
        }
        haveLoadedNested = true;
        try {
            byte data[];
            data = getAttribute(idInnerClasses);
            if (data != null) {
                initInnerClasses(env, data, flags);
            }
        } catch (IOException ee) {
            env.error(0, "malformed.attribute", getClassDeclaration(),
                      idInnerClasses);
            if (tracing)
                env.dtEvent("loadNested: MALFORMED ATTRIBUTE (InnerClasses)");
        }
    }
    private void initInnerClasses(Environment env,
                                  byte data[],
                                  int flags) throws IOException {
        DataInputStream ds = new DataInputStream(new ByteArrayInputStream(data));
        int nrec = ds.readUnsignedShort();  
        for (int i = 0; i < nrec; i++) {
            int inner_index = ds.readUnsignedShort();
            ClassDeclaration inner = cpool.getDeclaration(env, inner_index);
            ClassDeclaration outer = null;
            int outer_index = ds.readUnsignedShort();
            if (outer_index != 0) {
                outer = cpool.getDeclaration(env, outer_index);
            }
            Identifier inner_nm = idNull;
            int inner_nm_index = ds.readUnsignedShort();
            if (inner_nm_index != 0) {
                inner_nm = Identifier.lookup(cpool.getString(inner_nm_index));
            }
            int mods = ds.readUnsignedShort();
            boolean accessible =
                (outer != null) &&
                (!inner_nm.equals(idNull)) &&
                ((mods & M_PRIVATE) == 0 ||
                 (flags & ATT_ALLCLASSES) != 0);
            if (accessible) {
                Identifier nm =
                    Identifier.lookupInner(outer.getName(), inner_nm);
                Type.tClass(nm);
                if (inner.equals(getClassDeclaration())) {
                    try {
                        ClassDefinition outerClass = outer.getClassDefinition(env);
                        initInner(outerClass, mods);
                    } catch (ClassNotFound e) {
                    }
                } else if (outer.equals(getClassDeclaration())) {
                    try {
                        ClassDefinition innerClass =
                            inner.getClassDefinition(env);
                        initOuter(innerClass, mods);
                    } catch (ClassNotFound e) {
                    }
                }
            }
        }
    }
    private void initInner(ClassDefinition outerClass, int mods) {
        if (getOuterClass() != null)
            return;             
        if ((mods & M_PRIVATE) != 0) {
            mods &= ~(M_PUBLIC | M_PROTECTED);
        } else if ((mods & M_PROTECTED) != 0) {
            mods &= ~M_PUBLIC;
        }
        if ((mods & M_INTERFACE) != 0) {
            mods |= (M_ABSTRACT | M_STATIC);
        }
        if (outerClass.isInterface()) {
            mods |= (M_PUBLIC | M_STATIC);
            mods &= ~(M_PRIVATE | M_PROTECTED);
        }
        modifiers = mods;
        setOuterClass(outerClass);
        for (MemberDefinition field = getFirstMember();
             field != null;
             field = field.getNextMember()) {
            if (field.isUplevelValue()
                    && outerClass.getType().equals(field.getType())
                    && field.getName().toString().startsWith(prefixThis)) {
                setOuterMember(field);
            }
        }
    }
    private void initOuter(ClassDefinition innerClass, int mods) {
        if (innerClass instanceof BinaryClass)
            ((BinaryClass)innerClass).initInner(this, mods);
        addMember(new BinaryMember(innerClass));
    }
    public void write(Environment env, OutputStream out) throws IOException {
        DataOutputStream data = new DataOutputStream(out);
        data.writeInt(JAVA_MAGIC);
        data.writeShort(env.getMinorVersion());
        data.writeShort(env.getMajorVersion());
        cpool.write(data, env);
        data.writeShort(getModifiers() & ACCM_CLASS);
        data.writeShort(cpool.indexObject(getClassDeclaration(), env));
        data.writeShort((getSuperClass() != null)
                        ? cpool.indexObject(getSuperClass(), env) : 0);
        data.writeShort(interfaces.length);
        for (int i = 0 ; i < interfaces.length ; i++) {
            data.writeShort(cpool.indexObject(interfaces[i], env));
        }
        int fieldCount = 0, methodCount = 0;
        for (MemberDefinition f = firstMember; f != null; f = f.getNextMember())
            if (f.isMethod()) methodCount++; else fieldCount++;
        data.writeShort(fieldCount);
        for (MemberDefinition f = firstMember; f != null; f = f.getNextMember()) {
            if (!f.isMethod()) {
                data.writeShort(f.getModifiers() & ACCM_FIELD);
                String name = f.getName().toString();
                String signature = f.getType().getTypeSignature();
                data.writeShort(cpool.indexString(name, env));
                data.writeShort(cpool.indexString(signature, env));
                BinaryAttribute.write(((BinaryMember)f).atts, data, cpool, env);
            }
        }
        data.writeShort(methodCount);
        for (MemberDefinition f = firstMember; f != null; f = f.getNextMember()) {
            if (f.isMethod()) {
                data.writeShort(f.getModifiers() & ACCM_METHOD);
                String name = f.getName().toString();
                String signature = f.getType().getTypeSignature();
                data.writeShort(cpool.indexString(name, env));
                data.writeShort(cpool.indexString(signature, env));
                BinaryAttribute.write(((BinaryMember)f).atts, data, cpool, env);
            }
        }
        BinaryAttribute.write(atts, data, cpool, env);
        data.flush();
    }
    public Enumeration getDependencies() {
        return dependencies.elements();
    }
    public void addDependency(ClassDeclaration c) {
        if ((c != null) && !dependencies.contains(c)) {
            dependencies.addElement(c);
        }
    }
    public BinaryConstantPool getConstants() {
        return cpool;
    }
    public byte getAttribute(Identifier name)[] {
        for (BinaryAttribute att = atts ; att != null ; att = att.next) {
            if (att.name.equals(name)) {
                return att.data;
            }
        }
        return null;
    }
}

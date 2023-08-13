public abstract class CompoundType extends Type {
    protected Method[] methods;
    protected InterfaceType[] interfaces;
    protected Member[] members;
    protected ClassDefinition classDef;
    protected ClassDeclaration classDecl;
    protected boolean isCORBAObject = false;
    protected boolean isIDLEntity = false;
    protected boolean isAbstractBase = false;
    protected boolean isValueBase = false;
    protected boolean isCORBAUserException = false;
    protected boolean isException = false;
    protected boolean isCheckedException = false;
    protected boolean isRemoteExceptionOrSubclass = false;
    protected String idlExceptionName;
    protected String qualifiedIDLExceptionName;
    public boolean isCORBAObject () {
        return isCORBAObject;
    }
    public boolean isIDLEntity () {
        return isIDLEntity;
    }
    public boolean isValueBase () {
        return isValueBase;
    }
    public boolean isAbstractBase () {
        return isAbstractBase;
    }
    public boolean isException () {
        return isException;
    }
    public boolean isCheckedException () {
        return isCheckedException;
    }
    public boolean isRemoteExceptionOrSubclass () {
        return isRemoteExceptionOrSubclass;
    }
    public boolean isCORBAUserException () {
        return isCORBAUserException;
    }
    public boolean isIDLEntityException () {
        return isIDLEntity() && isException();
    }
    public boolean isBoxed () {
        return (isIDLEntity() && !isValueBase() &&
                !isAbstractBase() && !isCORBAObject() &&
                !isIDLEntityException());
    }
    public String getIDLExceptionName () {
        return idlExceptionName;
    }
    public String getQualifiedIDLExceptionName (boolean global) {
        if (qualifiedIDLExceptionName != null &&
            global &&
            getIDLModuleNames().length > 0) {
            return IDL_NAME_SEPARATOR + qualifiedIDLExceptionName;
        } else {
            return qualifiedIDLExceptionName;
        }
    }
    public String getSignature() {
        String sig = classDecl.getType().getTypeSignature();
        if (sig.endsWith(";")) {
            sig = sig.substring(0,sig.length()-1);
        }
        return sig;
    }
    public ClassDeclaration getClassDeclaration() {
        return classDecl;
    }
    public ClassDefinition getClassDefinition() {
        return classDef;
    }
    public ClassType getSuperclass() {
        return null;
    }
    public InterfaceType[] getInterfaces() {
        if( interfaces != null ) {
            return (InterfaceType[]) interfaces.clone();
        }
        return null;
    }
    public Method[] getMethods() {
        if( methods != null ) {
            return (Method[]) methods.clone();
        }
        return null;
    }
    public Member[] getMembers() {
        if( members != null ) {
            return (Member[]) members.clone();
        }
        return null;
    }
    public static CompoundType forCompound (ClassDefinition classDef,
                                            ContextStack stack) {
        CompoundType result = null;
        try {
            result = (CompoundType) makeType(classDef.getType(),classDef,stack);
        } catch (ClassCastException e) {}
        return result;
    }
    protected void destroy () {
        if (!destroyed) {
            super.destroy();
            if (methods != null) {
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i] != null) methods[i].destroy();
                }
                methods = null;
            }
            if (interfaces != null) {
                for (int i = 0; i < interfaces.length; i++) {
                    if (interfaces[i] != null) interfaces[i].destroy();
                }
                interfaces = null;
            }
            if (members != null) {
                for (int i = 0; i < members.length; i++) {
                    if (members[i] != null) members[i].destroy();
                }
                members = null;
            }
            classDef = null;
            classDecl = null;
        }
    }
    protected Class loadClass() {
        Class ourClass = null;
        try {
            env.getMain().compileAllClasses(env);
        } catch (Exception e1) {
            for (Enumeration e = env.getClasses() ; e.hasMoreElements() ; ) {
                ClassDeclaration c = (ClassDeclaration)e.nextElement();
            }
            failedConstraint(26,false,stack,"required classes");
            env.flushErrors();
        }
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            ourClass = cl.loadClass(getQualifiedName());
        } catch(ClassNotFoundException cfe) {
            try {
                ourClass = env.classPathLoader.loadClass(getQualifiedName());
            } catch (NullPointerException e) {
            } catch (ClassNotFoundException e) {
            }
        }
        if (ourClass == null) {
            if (env.loader == null) {
                File destDir = env.getMain().getDestinationDir();
                if (destDir == null) {
                    destDir = new File(".");
                }
                env.loader = new DirectoryLoader(destDir);
            }
            try {
                ourClass = env.loader.loadClass(getQualifiedName());
            } catch (Exception e) {}
        }
        return ourClass;
    }
    protected boolean printExtends (IndentingWriter writer,
                                    boolean useQualifiedNames,
                                    boolean useIDLNames,
                                    boolean globalIDLNames) throws IOException {
        ClassType parent = getSuperclass();
        if (parent != null && (!useIDLNames ||
                               (!parent.isType(TYPE_ANY) && !parent.isType(TYPE_CORBA_OBJECT)))) {
            writer.p(" extends ");
            parent.printTypeName(writer,useQualifiedNames,useIDLNames,globalIDLNames);
            return true;
        }
        return false;
    }
    protected void printImplements (IndentingWriter writer,
                                    String prefix,
                                    boolean useQualifiedNames,
                                    boolean useIDLNames,
                                    boolean globalIDLNames) throws IOException {
        InterfaceType[] interfaces = getInterfaces();
        String adjective = " implements";
        if (isInterface()) {
            adjective = " extends";
        }
        if (useIDLNames) {
            adjective = ":";
        }
        for (int i = 0; i < interfaces.length; i++) {
            if (!useIDLNames || (!interfaces[i].isType(TYPE_ANY) && !interfaces[i].isType(TYPE_CORBA_OBJECT))) {
                if (i == 0) {
                    writer.p(prefix + adjective + " ");
                } else {
                    writer.p(", ");
                }
                interfaces[i].printTypeName(writer,useQualifiedNames,useIDLNames,globalIDLNames);
            }
        }
    }
    protected void printMembers (       IndentingWriter writer,
                                        boolean useQualifiedNames,
                                        boolean useIDLNames,
                                        boolean globalIDLNames) throws IOException {
        CompoundType.Member[] members = getMembers();
        for (int i = 0; i < members.length; i++) {
            if (!members[i].isInnerClassDeclaration()) {
                Type it = members[i].getType();
                String visibility = members[i].getVisibility();
                String name;
                if (useIDLNames) {
                    name = members[i].getIDLName();
                } else {
                    name = members[i].getName();
                }
                String value = members[i].getValue();
                writer.p(visibility);
                if (visibility.length() > 0) {
                    writer.p(" ");
                }
                it.printTypeName(writer,useQualifiedNames,useIDLNames,globalIDLNames);
                writer.p(" " + name);
                if (value != null) {
                    writer.pln(" = " + value + ";");
                } else {
                    writer.pln(";");
                }
            }
        }
    }
    protected void printMethods (       IndentingWriter writer,
                                        boolean useQualifiedNames,
                                        boolean useIDLNames,
                                        boolean globalIDLNames) throws IOException {
        CompoundType.Method[] methods = getMethods();
        for (int m = 0; m < methods.length; m++) {
            CompoundType.Method theMethod = methods[m];
            printMethod(theMethod,writer,useQualifiedNames,useIDLNames,globalIDLNames);
        }
    }
    protected void printMethod (CompoundType.Method it,
                                IndentingWriter writer,
                                boolean useQualifiedNames,
                                boolean useIDLNames,
                                boolean globalIDLNames) throws IOException {
        String visibility = it.getVisibility();
        writer.p(visibility);
        if (visibility.length() > 0) {
            writer.p(" ");
        }
        it.getReturnType().printTypeName(writer,useQualifiedNames,useIDLNames,globalIDLNames);
        if (useIDLNames) {
            writer.p(" " + it.getIDLName());
        } else {
            writer.p(" " + it.getName());
        }
        writer.p(" (");
        Type[] args = it.getArguments();
        String[] argNames = it.getArgumentNames();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                writer.p(", ");
            }
            if (useIDLNames) {
                writer.p("in ");
            }
            args[i].printTypeName(writer,useQualifiedNames,useIDLNames,globalIDLNames);
            writer.p(" " + argNames[i]);
        }
        writer.p(")");
        ClassType[] exceptions;
        if (isType(TYPE_IMPLEMENTATION)) {
            exceptions = it.getImplExceptions();
        } else {
            exceptions = it.getExceptions();
        }
        for (int i = 0; i < exceptions.length; i++) {
            if (i == 0) {
                if (useIDLNames) {
                    writer.p(" raises (");
                } else {
                    writer.p(" throws ");
                }
            } else {
                writer.p(", ");
            }
            if (useIDLNames) {
                if (useQualifiedNames) {
                    writer.p(exceptions[i].getQualifiedIDLExceptionName(globalIDLNames));
                } else {
                    writer.p(exceptions[i].getIDLExceptionName());
                }
                writer.p(" [a.k.a. ");
                exceptions[i].printTypeName(writer,useQualifiedNames,useIDLNames,globalIDLNames);
                writer.p("]");
            } else {
                exceptions[i].printTypeName(writer,useQualifiedNames,useIDLNames,globalIDLNames);
            }
        }
        if (useIDLNames && exceptions.length > 0) {
            writer.p(")");
        }
        if (it.isInherited()) {
            writer.p(" 
        writer.p(it.getDeclaredBy());
        }
        writer.pln(";");
    }
    protected CompoundType(ContextStack stack, int typeCode, ClassDefinition classDef) {
        super(stack,typeCode);
        this.classDef = classDef;
        classDecl = classDef.getClassDeclaration();
        interfaces = new InterfaceType[0];
        methods = new Method[0];
        members = new Member[0];
        if (classDef.isInnerClass()) {
            setTypeCode(typeCode | TM_INNER);
        }
        setFlags();
    }
    private void setFlags() {
        try {
            isCORBAObject = env.defCorbaObject.implementedBy(env,classDecl);
            isIDLEntity = env.defIDLEntity.implementedBy(env,classDecl);
            isValueBase = env.defValueBase.implementedBy(env,classDecl);
            isAbstractBase = isInterface() &&   
                             isIDLEntity &&     
                             !isValueBase &&    
                             !isCORBAObject;    
            isCORBAUserException = (classDecl.getName() == idCorbaUserException);
            if (env.defThrowable.implementedBy(env, classDecl)) {
                isException = true;
                if (env.defRuntimeException.implementedBy(env,classDecl) ||
                    env.defError.implementedBy(env,classDecl)) {
                    isCheckedException = false;
                } else {
                    isCheckedException = true;
                }
                if (env.defRemoteException.implementedBy(env,classDecl)) {
                    isRemoteExceptionOrSubclass = true;
                } else {
                    isRemoteExceptionOrSubclass = false;
                }
            } else {
                isException = false;
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }
    }
    protected CompoundType(ContextStack stack, ClassDefinition classDef,
                           int typeCode) {
        super(stack,typeCode);
        this.classDef = classDef;
        classDecl = classDef.getClassDeclaration();
        if (classDef.isInnerClass()) {
            setTypeCode(typeCode | TM_INNER);
        }
        setFlags();
        Identifier id = classDef.getName();
        String idlName;
        String[] idlModuleNames;
        try {
            idlName = IDLNames.getClassOrInterfaceName(id,env);
            idlModuleNames = IDLNames.getModuleNames(id,isBoxed(),env);
            setNames(id,idlModuleNames,idlName);
            if (isException()) {
                isException = true;
                idlExceptionName = IDLNames.getExceptionName(getIDLName());
                qualifiedIDLExceptionName =
                    IDLNames.getQualifiedName(getIDLModuleNames(),idlExceptionName);
            }
            interfaces = null;          
            methods = null;                     
            members = null;                 
        } catch (Exception e) {
            failedConstraint(7,false,stack,id.toString(),e.getMessage());
            throw new CompilerError("");
        }
    }
    protected boolean initialize (      Vector directInterfaces,
                                        Vector directMethods,
                                        Vector directMembers,
                                        ContextStack stack,
                                        boolean quiet) {
        boolean result = true;
        if (directInterfaces != null && directInterfaces.size() > 0) {
            interfaces = new InterfaceType[directInterfaces.size()];
            directInterfaces.copyInto(interfaces);
        } else {
            interfaces = new InterfaceType[0];
        }
        if (directMethods != null && directMethods.size() > 0) {
            methods = new Method[directMethods.size()];
            directMethods.copyInto(methods);
            try {
                IDLNames.setMethodNames(this, methods,env);
            } catch (Exception e) {
                failedConstraint(13,quiet,stack,getQualifiedName(),e.getMessage());
                result = false;
            }
        } else {
            methods = new Method[0];
        }
        if (directMembers != null && directMembers.size() > 0) {
            members = new Member[directMembers.size()];
            directMembers.copyInto(members);
            for (int i = 0; i < members.length; i++) {
                if (members[i].isInnerClassDeclaration()) {
                    try {
                        members[i].init(stack,this);
                    } catch (CompilerError e) {
                        return false;
                    }
                }
            }
            try {
                IDLNames.setMemberNames(this, members,methods,env);
            } catch (Exception e) {
                int constraint = classDef.isInterface() ? 19 : 20;
                failedConstraint(constraint,quiet,stack,getQualifiedName(),e.getMessage());
                result = false;
            }
        } else {
            members = new Member[0];
        }
        if (result) {
            result = setRepositoryID();
        }
        return result;
    }
    protected static Type makeType (sun.tools.java.Type theType,
                                    ClassDefinition classDef,
                                    ContextStack stack) {
        if (stack.anyErrors()) return null;
        String key = theType.toString();
        Type result = getType(key,stack);
        if (result != null) {
            return result;
        }
        result = getType(key + stack.getContextCodeString(),stack);
        if (result != null) {
            return result;
        }
        BatchEnvironment env = stack.getEnv();
        int typeCode = theType.getTypeCode();
        switch (typeCode) {
        case TC_BOOLEAN:
        case TC_BYTE:
        case TC_CHAR:
        case TC_SHORT:
        case TC_INT:
        case TC_LONG:
        case TC_FLOAT:
        case TC_DOUBLE:
            {
                result = PrimitiveType.forPrimitive(theType,stack);
                break;
            }
        case TC_ARRAY:
            {
                result = ArrayType.forArray(theType,stack);
                break;
            }
        case TC_CLASS:
            {
                try {
                    ClassDefinition theClass = classDef;
                    if (theClass == null) {
                        theClass = env.getClassDeclaration(theType).getClassDefinition(env);
                    }
                    if (theClass.isInterface()) {
                        result = SpecialInterfaceType.forSpecial(theClass,stack);
                        if (result == null) {
                            if (env.defRemote.implementedBy(env,theClass.getClassDeclaration())) {
                                boolean parentIsValue = stack.isParentAValue();
                                result = RemoteType.forRemote(theClass,stack,parentIsValue);
                                if (result == null && parentIsValue) {
                                    result = NCInterfaceType.forNCInterface(theClass,stack);
                                }
                            } else {
                                result = AbstractType.forAbstract(theClass,stack,true);
                                if (result == null) {
                                    result = NCInterfaceType.forNCInterface(theClass,stack);
                                }
                            }
                        }
                    } else {
                        result = SpecialClassType.forSpecial(theClass,stack);
                        if (result == null) {
                            ClassDeclaration classDecl = theClass.getClassDeclaration();
                            if (env.defRemote.implementedBy(env,classDecl)) {
                                boolean parentIsValue = stack.isParentAValue();
                                result = ImplementationType.forImplementation(theClass,stack,parentIsValue);
                                if (result == null && parentIsValue) {
                                    result = NCClassType.forNCClass(theClass,stack);
                                }
                            } else {
                                if (env.defSerializable.implementedBy(env,classDecl)) {
                                    result = ValueType.forValue(theClass,stack,true);
                                }
                                if (result == null) {
                                    result = NCClassType.forNCClass(theClass,stack);
                                }
                            }
                        }
                    }
                } catch (ClassNotFound e) {
                    classNotFound(stack,e);
                }
                break;
            }
        default: throw new CompilerError("Unknown typecode (" + typeCode + ") for " + theType.getTypeSignature());
        }
        return result;
    }
    public static boolean isRemoteException (ClassType ex,
                                             BatchEnvironment env) {
        sun.tools.java.Type exceptionType = ex.getClassDeclaration().getType();
        if (exceptionType.equals(env.typeRemoteException) ||
            exceptionType.equals(env.typeIOException) ||
            exceptionType.equals(env.typeException) ||
            exceptionType.equals(env.typeThrowable)) {
            return true;
        }
        return false;
    }
    protected boolean isConformingRemoteMethod (Method method, boolean quiet)
        throws ClassNotFound {
        boolean haveRemote = false;
        ClassType[] exceptions = method.getExceptions();
        for (int i = 0; i < exceptions.length; i++) {
            if (isRemoteException(exceptions[i],env)) {
                haveRemote = true;
                break;
            }
        }
        if (!haveRemote) {
            failedConstraint(5,quiet,stack,method.getEnclosing(), method.toString());
        }
        boolean noIDLEntity = !isIDLEntityException(method.getReturnType(),method,quiet);
        if (noIDLEntity) {
            Type[] args = method.getArguments();
            for (int i = 0; i < args.length; i++) {
                if (isIDLEntityException(args[i],method,quiet)) {
                    noIDLEntity = false;
                    break;
                }
            }
        }
        return (haveRemote && noIDLEntity);
    }
    protected boolean isIDLEntityException(Type type, CompoundType.Method method,boolean quiet)
        throws ClassNotFound {
        if (type.isArray()) {
            type = type.getElementType();
        }
        if (type.isCompound()){
            if (((CompoundType)type).isIDLEntityException()) {
                failedConstraint(18,quiet,stack,method.getEnclosing(), method.toString());
                return true;
            }
        }
        return false;
    }
    protected void swapInvalidTypes () {
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].getStatus() != STATUS_VALID) {
                interfaces[i] = (InterfaceType)getValidType(interfaces[i]);
            }
        }
        for (int i = 0; i < methods.length; i++) {
            methods[i].swapInvalidTypes();
        }
        for (int i = 0; i < members.length; i++) {
            members[i].swapInvalidTypes();
        }
    }
    protected boolean addTypes (int typeCodeFilter,
                                HashSet checked,
                                Vector matching) {
        boolean result = super.addTypes(typeCodeFilter,checked,matching);
        if (result) {
            ClassType parent = getSuperclass();
            if (parent != null) {
                parent.addTypes(typeCodeFilter,checked,matching);
            }
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i].addTypes(typeCodeFilter,checked,matching);
            }
            for (int i = 0; i < methods.length; i++) {
                methods[i].getReturnType().addTypes(typeCodeFilter,checked,matching);
                Type[] args = methods[i].getArguments();
                for (int j = 0; j < args.length; j++) {
                    Type arg = args[j];
                    arg.addTypes(typeCodeFilter,checked,matching);
                }
                ClassType[] exceptions = methods[i].getExceptions();
                for (int j = 0; j < exceptions.length; j++) {
                    ClassType ex = exceptions[j];
                    ex.addTypes(typeCodeFilter,checked,matching);
                }
            }
            for (int i = 0; i < members.length; i++) {
                Type cType = members[i].getType();
                cType.addTypes(typeCodeFilter,checked,matching);
            }
        }
        return result;
    }
    private boolean isConformingConstantType (MemberDefinition member) {
        return isConformingConstantType(member.getType(),member);
    }
    private boolean isConformingConstantType (sun.tools.java.Type theType,MemberDefinition member) {
        boolean result = true;
        int typeCode = theType.getTypeCode();
        switch (typeCode) {
        case TC_BOOLEAN:
        case TC_BYTE:
        case TC_CHAR:
        case TC_SHORT:
        case TC_INT:
        case TC_LONG:
        case TC_FLOAT:
        case TC_DOUBLE: 
            {
                break;
            }
        case TC_CLASS:  
            {
                if (theType.getClassName() != idJavaLangString) {
                    failedConstraint(3,false,stack,member.getClassDefinition(),member.getName());
                    result = false;
                }
                break;
            }
        case TC_ARRAY: 
            {
                failedConstraint(3,false,stack,member.getClassDefinition(),member.getName());
                result = false;
                break;
            }
        default:
            throw new Error("unexpected type code: " + typeCode);
        }
        return result;
    }
    protected Vector updateParentClassMethods(ClassDefinition current,
                                              Vector currentMethods,
                                              boolean quiet,
                                              ContextStack stack)
        throws ClassNotFound {
        ClassDeclaration parentDecl = current.getSuperClass(env);
        while (parentDecl != null) {
            ClassDefinition parentDef = parentDecl.getClassDefinition(env);
            Identifier currentID = parentDecl.getName();
            if ( currentID == idJavaLangObject ) break;
            for (MemberDefinition member = parentDef.getFirstMember();
                 member != null;
                 member = member.getNextMember()) {
                if (member.isMethod() &&
                    !member.isInitializer() &&
                    !member.isConstructor() &&
                    !member.isPrivate()) {
                    Method method;
                    try {
                        method = new Method((CompoundType)this,member,quiet,stack);
                    } catch (Exception e) {
                        return null;
                    }
                    int index = currentMethods.indexOf(method);
                    if (index >= 0) {
                        Method currentMethod = (Method)currentMethods.elementAt(index);
                        currentMethod.setDeclaredBy(currentID);
                    }
                    else currentMethods.addElement(method);
                }
            }
            parentDecl = parentDef.getSuperClass(env);
        }
        return currentMethods;
    }
    protected Vector addAllMethods (ClassDefinition current, Vector directMethods,
                                    boolean noMultiInheritedMethods,
                                    boolean quiet,
                                    ContextStack stack)
        throws ClassNotFound {
        ClassDeclaration[] interfaces = current.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Vector result = addAllMethods(interfaces[i].getClassDefinition(env),
                                          directMethods,
                                          noMultiInheritedMethods,quiet,stack);
            if (result == null) {
                return null;
            }
        }
        for (MemberDefinition member = current.getFirstMember();
             member != null;
             member = member.getNextMember())
            {
                if (member.isMethod() &&
                    !member.isInitializer() &&
                    !member.isPrivate()) {
                    Method method;
                    try {
                        method = new Method((CompoundType)this,member,quiet,stack);
                    } catch (Exception e) {
                        return null;
                    }
                    if (!directMethods.contains(method)) {
                        directMethods.addElement(method);
                    } else {
                        if (noMultiInheritedMethods && current != classDef  &&
                            !stack.isParentAValue() && !stack.getContext().isValue()) {
                            Method existingMethod = (Method) directMethods.elementAt(directMethods.indexOf(method));
                            ClassDefinition existingMemberClassDef = existingMethod.getMemberDefinition().getClassDefinition();
                            if ( current != existingMemberClassDef &&
                                 ! inheritsFrom(current, existingMemberClassDef) &&
                                 ! inheritsFrom(existingMemberClassDef, current))
                            {
                                String message = existingMemberClassDef.getName() + " and " + current.getName();
                                failedConstraint(6,quiet,stack,classDef,message,method);
                                return null;
                            }
                        }
                        int index = directMethods.indexOf(method);
                        Method other = (Method) directMethods.get(index);
                        Method newMethod = method.mergeWith(other);
                        directMethods.set(index, newMethod);
                    }
                }
            }
        return directMethods;
    }
    protected boolean inheritsFrom(ClassDefinition def, ClassDefinition otherDef) {
        if (def == otherDef)
            return true;
        ClassDefinition superDef;
        if (def.getSuperClass() != null) {
            superDef = def.getSuperClass().getClassDefinition();
            if (inheritsFrom(superDef, otherDef))
                return true;
        }
        ClassDeclaration[] interfaces = def.getInterfaces();
        for (int i=0; i<interfaces.length; i++) {
            superDef = interfaces[i].getClassDefinition();
            if (inheritsFrom(superDef, otherDef))
                return true;
        }
        return false;
    }
    protected Vector addRemoteInterfaces (Vector list,
                                          boolean allowNonConforming,
                                          ContextStack stack) throws ClassNotFound {
        ClassDefinition theInterface = getClassDefinition();
        ClassDeclaration[] interfaces = theInterface.getInterfaces();
        stack.setNewContextCode(ContextStack.IMPLEMENTS);
        for (int i = 0; i < interfaces.length; i++) {
            ClassDefinition def = interfaces[i].getClassDefinition(env);
            InterfaceType it = SpecialInterfaceType.forSpecial(def,stack);;
            if (it == null) {
                if (env.defRemote.implementedBy(env, interfaces[i])) {
                    it = RemoteType.forRemote(def,stack,false);
                } else {
                    it = AbstractType.forAbstract(def,stack,true);
                    if (it == null && allowNonConforming) {
                        it = NCInterfaceType.forNCInterface(def,stack);
                    }
                }
            }
            if (it != null) {
                list.addElement(it);
            } else {
                return null;
            }
        }
        return list;
    }
    protected Vector addNonRemoteInterfaces (Vector list,
                                             ContextStack stack) throws ClassNotFound {
        ClassDefinition theInterface = getClassDefinition();
        ClassDeclaration[] interfaces = theInterface.getInterfaces();
        stack.setNewContextCode(ContextStack.IMPLEMENTS);
        for (int i = 0; i < interfaces.length; i++) {
            ClassDefinition def = interfaces[i].getClassDefinition(env);
            InterfaceType it = SpecialInterfaceType.forSpecial(def,stack);
            if (it == null) {
                it = AbstractType.forAbstract(def,stack,true);
                if (it == null) {
                    it = NCInterfaceType.forNCInterface(def,stack);
                }
            }
            if (it != null) {
                list.addElement(it);
            } else {
                return null;
            }
        }
        return list;
    }
    protected boolean addAllMembers (Vector allMembers,
                                     boolean onlyConformingConstants,   
                                     boolean quiet,
                                     ContextStack stack) {
        boolean result = true;
        for (MemberDefinition member = getClassDefinition().getFirstMember();
             member != null && result;
             member = member.getNextMember())
            {
                if (!member.isMethod()) {
                    try {
                        String value = null;
                        member.getValue(env);
                        Node node = member.getValue();
                        if (node != null) {
                            if (member.getType().getTypeCode() == TC_CHAR) {
                                Integer intValue = (Integer)((IntegerExpression)node).getValue();
                                value = "L'" + String.valueOf((char)intValue.intValue()) + "'";
                            } else {
                                value = node.toString();
                            }
                        }
                        if (onlyConformingConstants && member.getInnerClass() == null) {
                            if (value == null || !isConformingConstantType(member)) {
                                failedConstraint(3,quiet,stack,member.getClassDefinition(),member.getName());
                                result = false;
                                break;
                            }
                        }
                        try {
                            Member newMember = new Member(member,value,stack,this);
                            allMembers.addElement(newMember);
                        } catch (CompilerError e) {
                            result = false;
                        }
                    } catch (ClassNotFound e) {
                        classNotFound(stack,e);
                        result = false;
                    }
                }
            }
        return result;
    }
    protected boolean addConformingConstants (Vector allMembers,
                                              boolean quiet,
                                              ContextStack stack) {
        boolean result = true;
        for (MemberDefinition member = getClassDefinition().getFirstMember();
             member != null && result;
             member = member.getNextMember())
            {
                if (!member.isMethod()) {
                    try {
                        String value = null;
                        member.getValue(env);
                        Node node = member.getValue();
                        if (node != null) {
                            value = node.toString();
                        }
                        if (value != null) {
                            if (!isConformingConstantType(member)) {
                                failedConstraint(3,quiet,stack,member.getClassDefinition(),member.getName());
                                result = false;
                                break;
                            }
                            try {
                                Member newMember = new Member(member,value,stack,this);
                                allMembers.addElement(newMember);
                            } catch (CompilerError e) {
                                result = false;
                            }
                        }
                    } catch (ClassNotFound e) {
                        classNotFound(stack,e);
                        result = false;
                    }
                }
            }
        return result;
    }
    protected ValueType[] getMethodExceptions (MemberDefinition member,
                                               boolean quiet,
                                               ContextStack stack) throws Exception {
        boolean result = true;
        stack.setNewContextCode(ContextStack.METHOD_EXCEPTION);
        ClassDeclaration[] except = member.getExceptions(env);
        ValueType[] exceptions = new ValueType[except.length];
        try {
            for (int i = 0; i < except.length; i++) {
                ClassDefinition theClass = except[i].getClassDefinition(env);
                try {
                    ValueType type = ValueType.forValue(theClass,stack,false);
                    if (type != null) {
                            exceptions[i] = type;
                        } else {
                            result = false;
                        }
                } catch (ClassCastException e1) {
                    failedConstraint(22,quiet,stack,getQualifiedName());
                    throw new CompilerError("Method: exception " + theClass.getName() + " not a class type!");
                } catch (NullPointerException e2) {
                    failedConstraint(23,quiet,stack,getQualifiedName());
                    throw new CompilerError("Method: caught null pointer exception");
                }
            }
        } catch (ClassNotFound e) {
            classNotFound(quiet,stack,e);
            result = false;
        }
        if (!result) {
            throw new Exception();
        }
        int dupCount = 0;
        for (int i = 0; i < exceptions.length; i++) {
            for (int j = 0; j < exceptions.length; j++) {
                if (i != j && exceptions[i] != null && exceptions[i] == exceptions[j]) {
                    exceptions[j] = null;
                    dupCount++;
                }
            }
        }
        if (dupCount > 0) {
            int offset = 0;
            ValueType[] temp = new ValueType[exceptions.length - dupCount];
            for (int i = 0; i < exceptions.length; i++) {
                if (exceptions[i] != null) {
                    temp[offset++] = exceptions[i];
                }
            }
            exceptions = temp;
        }
        return exceptions;
    }
    protected static String getVisibilityString (MemberDefinition member) {
        String vis = "";
        String prefix = "";
        if (member.isPublic()) {
            vis += "public";
            prefix = " ";
        } else if (member.isProtected()) {
            vis += "protected";
            prefix = " ";
        } else if (member.isPrivate()) {
            vis += "private";
            prefix = " ";
        }
        if (member.isStatic()) {
            vis += prefix;
            vis += "static";
            prefix = " ";
        }
        if (member.isFinal()) {
            vis += prefix;
            vis += "final";
            prefix = " ";
        }
        return vis;
    }
    protected boolean assertNotImpl(Type type,
                                    boolean quiet,
                                    ContextStack stack,
                                    CompoundType enclosing,
                                    boolean dataMember) {
        if (type.isType(TYPE_IMPLEMENTATION)) {
            int constraint = dataMember ? 28 : 21;
            failedConstraint(constraint,quiet,stack,type,enclosing.getName());
            return false;
        }
        return true;
    }
    public class Method implements ContextElement, Cloneable {
        public boolean isInherited () {
            return declaredBy != enclosing.getIdentifier();
        }
        public boolean isAttribute () {
            return attributeKind != ATTRIBUTE_NONE;
        }
        public boolean isReadWriteAttribute () {
            return attributeKind == ATTRIBUTE_IS_RW ||
                attributeKind == ATTRIBUTE_GET_RW;
        }
        public int getAttributeKind() {
            return attributeKind;
        }
        public String getAttributeName() {
            return attributeName;
        }
        public int getAttributePairIndex() {
            return attributePairIndex;
        }
        public String getElementName() {
            return memberDef.toString();
        }
        public boolean equals(Object obj) {
            Method other = (Method) obj;
            if (getName().equals(other.getName()) &&
                arguments.length == other.arguments.length) {
                for (int i = 0; i < arguments.length; i++) {
                    if (! arguments[i].equals(other.arguments[i])) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        public Method mergeWith(Method other) {
            if (!equals(other)) {
                env.error(0, "attempt to merge method failed:", getName(),
                          enclosing.getClassDefinition().getName());
            }
            Vector legalExceptions = new Vector();
            try {
                collectCompatibleExceptions(
                      other.exceptions, exceptions, legalExceptions);
                collectCompatibleExceptions(
                      exceptions, other.exceptions, legalExceptions);
            } catch (ClassNotFound e) {
                env.error(0, "class.not.found", e.name,
                          enclosing.getClassDefinition().getName());
                return null;
            }
            Method merged = (Method) clone();
            merged.exceptions = new ValueType[legalExceptions.size()];
            legalExceptions.copyInto(merged.exceptions);
            merged.implExceptions = merged.exceptions;
            return merged;
        }
        private void collectCompatibleExceptions(
                ValueType[] from, ValueType[] with, Vector list)
                throws ClassNotFound {
            for (int i = 0; i < from.length; i++) {
                ClassDefinition exceptionDef = from[i].getClassDefinition();
                if (!list.contains(from[i])) {
                    for (int j = 0; j < with.length; j++) {
                        if (exceptionDef.subClassOf(
                                enclosing.getEnv(),
                                with[j].getClassDeclaration())) {
                            list.addElement(from[i]);
                            break;
                        }
                    }
                }
            }
        }
        public CompoundType getEnclosing() {
            return enclosing;
        }
        public Identifier getDeclaredBy() {
            return declaredBy;
        }
        public String getVisibility() {
            return vis;
        }
        public boolean isPublic() {
            return memberDef.isPublic();
        }
        public boolean isProtected() {
            return memberDef.isPrivate();
        }
        public boolean isPrivate() {
            return memberDef.isPrivate();
        }
        public boolean isStatic() {
            return memberDef.isStatic();
        }
        public String getName() {
            return name;
        }
        public String getIDLName() {
            return idlName;
        }
        public sun.tools.java.Type getType() {
            return memberDef.getType();
        }
        public boolean isConstructor () {
            return memberDef.isConstructor();
        }
        public boolean isNormalMethod () {
            return (!memberDef.isConstructor()) && attributeKind == ATTRIBUTE_NONE;
        }
        public Type getReturnType() {
            return returnType;
        }
        public Type[] getArguments() {
            return (Type[]) arguments.clone();
        }
        public String[] getArgumentNames() {
            return argumentNames;
        }
        public MemberDefinition getMemberDefinition() {
            return memberDef;
        }
        public ValueType[] getExceptions() {
            return (ValueType[]) exceptions.clone();
        }
        public ValueType[] getImplExceptions() {
            return (ValueType[]) implExceptions.clone();
        }
        public ValueType[] getUniqueCatchList(ValueType[] list) {
            ValueType[] result = list;
            int newSize = list.length;
            try {
                for (int i = 0; i < list.length; i++) {
                    ClassDeclaration decl = list[i].getClassDeclaration();
                    if (env.defRemoteException.superClassOf(env, decl) ||
                        env.defRuntimeException.superClassOf(env, decl) ||
                        env.defError.superClassOf(env, decl)) {
                        list[i] = null;
                        newSize--;
                    }
                }
                for (int i = 0; i < list.length; i++) {
                    if (list[i] != null) {
                        ClassDefinition current = list[i].getClassDefinition();
                        for (int j = 0; j < list.length; j++) {
                            if (j != i && list[i] != null && list[j] != null &&
                                current.superClassOf(env, list[j].getClassDeclaration())) {
                                list[j] = null;
                                newSize--;
                            }
                        }
                    }
                }
            } catch (ClassNotFound e) {
                classNotFound(stack,e); 
            }
            if (newSize < list.length) {
                ValueType[] temp = new ValueType[newSize];
                int offset = 0;
                for (int i = 0; i < list.length; i++) {
                    if (list[i] != null) {
                        temp[offset++] = list[i];
                    }
                }
                list = temp;
            }
            if (list.length == 0) {
                return null;
            } else {
                return list;
            }
        }
        public ValueType[] getFilteredStubExceptions(ValueType[] list) {
            ValueType[] result = list;
            int newSize = list.length;
            try {
                for (int i = 0; i < list.length; i++) {
                    ClassDeclaration decl = list[i].getClassDeclaration();
                    if ((env.defRemoteException.superClassOf(env, decl) &&
                         !env.defRemoteException.getClassDeclaration().equals(decl)) ||
                        env.defRuntimeException.superClassOf(env, decl) ||
                        env.defError.superClassOf(env, decl)) {
                        list[i] = null;
                        newSize--;
                    }
                }
            } catch (ClassNotFound e) {
                classNotFound(stack,e); 
            }
            if (newSize < list.length) {
                ValueType[] temp = new ValueType[newSize];
                int offset = 0;
                for (int i = 0; i < list.length; i++) {
                    if (list[i] != null) {
                        temp[offset++] = list[i];
                    }
                }
                list = temp;
            }
            return list;
        }
        public String toString() {
            if (stringRep == null) {
                StringBuffer result = new StringBuffer(returnType.toString());
                result.append(" ");
                result.append(getName());
                result.append(" (");
                for (int i = 0; i < arguments.length; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }
                    result.append(arguments[i]);
                    result.append(" ");
                    result.append(argumentNames[i]);
                }
                result.append(")");
                for (int i = 0; i < exceptions.length; i++) {
                    if (i == 0) {
                        result.append(" throws ");
                    } else {
                        result.append(", ");
                    }
                    result.append(exceptions[i]);
                }
                result.append(";");
                stringRep = result.toString();
            }
            return stringRep;
        }
        public void setAttributeKind(int kind) {
            attributeKind = kind;
        }
        public void setAttributePairIndex(int index) {
            attributePairIndex = index;
        }
        public void setAttributeName(String name) {
            attributeName = name;
        }
        public void setIDLName (String idlName) {
            this.idlName=idlName;
        }
        public void setImplExceptions (ValueType[] exceptions) {
            implExceptions = exceptions;
        }
        public void setDeclaredBy (Identifier by) {
            declaredBy = by;
        }
        protected void swapInvalidTypes () {
            if (returnType.getStatus() != STATUS_VALID) {
                returnType = getValidType(returnType);
            }
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i].getStatus() != STATUS_VALID) {
                    arguments[i] = getValidType(arguments[i]);
                }
            }
            for (int i = 0; i < exceptions.length; i++) {
                if (exceptions[i].getStatus() != STATUS_VALID) {
                    exceptions[i] = (ValueType)getValidType(exceptions[i]);
                }
            }
            for (int i = 0; i < implExceptions.length; i++) {
                if (implExceptions[i].getStatus() != STATUS_VALID) {
                    implExceptions[i] = (ValueType)getValidType(implExceptions[i]);
                }
            }
        }
        public void destroy () {
            if (memberDef != null) {
                memberDef = null;
                enclosing = null;
                if (exceptions != null) {
                    for (int i = 0; i < exceptions.length; i++) {
                        if (exceptions[i] != null) exceptions[i].destroy();
                        exceptions[i] = null;
                    }
                    exceptions = null;
                }
                if (implExceptions != null) {
                    for (int i = 0; i < implExceptions.length; i++) {
                        if (implExceptions[i] != null) implExceptions[i].destroy();
                        implExceptions[i] = null;
                    }
                    implExceptions = null;
                }
                if (returnType != null) returnType.destroy();
                returnType = null;
                if (arguments != null) {
                    for (int i = 0; i < arguments.length; i++) {
                        if (arguments[i] != null) arguments[i].destroy();
                        arguments[i] = null;
                    }
                    arguments = null;
                }
                if (argumentNames != null) {
                    for (int i = 0; i < argumentNames.length; i++) {
                        argumentNames[i] = null;
                    }
                    argumentNames = null;
                }
                vis = null;
                name = null;
                idlName = null;
                stringRep = null;
                attributeName = null;
                declaredBy = null;
            }
        }
        private MemberDefinition memberDef;
        private CompoundType enclosing;
        private ValueType[] exceptions;
        private ValueType[] implExceptions;
        private Type returnType;
        private Type[] arguments;
        private String[] argumentNames;
        private String vis;
        private String name;
        private String idlName;
        private String stringRep = null;
        private int attributeKind = ATTRIBUTE_NONE;
        private String attributeName = null;
        private int attributePairIndex = -1;
        private Identifier declaredBy = null;
        private String makeArgName (int argNum, Type type) {
            return "arg" + argNum;
        }
        public Method (CompoundType enclosing,
                       MemberDefinition memberDef,
                       boolean quiet,
                       ContextStack stack) throws Exception {
            this.enclosing = enclosing;
            this.memberDef = memberDef;
            vis = getVisibilityString(memberDef);
            idlName = null; 
            boolean valid = true;
            declaredBy = memberDef.getClassDeclaration().getName();
            name = memberDef.getName().toString();
            stack.setNewContextCode(ContextStack.METHOD);
            stack.push(this);
            stack.setNewContextCode(ContextStack.METHOD_RETURN);
            sun.tools.java.Type methodType = memberDef.getType();
            sun.tools.java.Type rtnType = methodType.getReturnType();
            if (rtnType == sun.tools.java.Type.tVoid) {
                returnType = PrimitiveType.forPrimitive(rtnType,stack);
            } else {
                returnType = makeType(rtnType,null,stack);
                if (returnType == null ||
                    !assertNotImpl(returnType,quiet,stack,enclosing,false)) {
                    valid = false;
                    failedConstraint(24,quiet,stack,enclosing.getName());
                }
            }
            stack.setNewContextCode(ContextStack.METHOD_ARGUMENT);
            sun.tools.java.Type[] args = memberDef.getType().getArgumentTypes();
            arguments = new Type[args.length];
            argumentNames = new String[args.length];
            Vector origArgNames = memberDef.getArguments();
            for (int i = 0; i < args.length; i++) {
                Type type = null;
                try {
                    type = makeType(args[i],null,stack);
                } catch (Exception e) {
                }
                if (type != null) {
                    if (!assertNotImpl(type,quiet,stack,enclosing,false)) {
                        valid = false;
                    } else {
                    arguments[i] = type;
                    if (origArgNames != null) {
                        LocalMember local = (LocalMember)origArgNames.elementAt(i+1);
                        argumentNames[i] = local.getName().toString();
                    } else {
                        argumentNames[i] = makeArgName(i,type);
                    }
                    }
                } else {
                    valid = false;
                    failedConstraint(25,false,stack,enclosing.getQualifiedName(),name);
                }
            }
            if (!valid) {
                stack.pop(false);
                throw new Exception();
            }
            try {
                exceptions = enclosing.getMethodExceptions(memberDef,quiet,stack);
                implExceptions = exceptions;
                stack.pop(true);
            } catch (Exception e) {
                stack.pop(false);
                throw new Exception();
            }
        }
        protected Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error("clone failed");
            }
        }
    }
    public class Member implements ContextElement, Cloneable {
        public String getElementName() {
            return "\"" + getName() + "\"";
        }
        public Type getType() {
            return type;
        }
        public String getName() {
            return name;
        }
        public String getIDLName() {
            return idlName;
        }
        public String getVisibility() {
            return vis;
        }
        public boolean isPublic() {
            return member.isPublic();
        }
        public boolean isPrivate() {
            return member.isPrivate();
        }
        public boolean isStatic() {
            return member.isStatic();
        }
        public boolean isFinal() {
            return member.isFinal();
        }
        public boolean isTransient() {
            if (forceTransient) return true;
            return member.isTransient();
        }
        public String getValue() {
            return value;
        }
        public boolean isInnerClassDeclaration() {
            return innerClassDecl;
        }
        public boolean isConstant () {
            return constant;
        }
        public String toString() {
            String result = type.toString();
            if (value != null) {
                result += (" = " + value);
            }
            return result;
        }
        protected void swapInvalidTypes () {
            if (type.getStatus() != STATUS_VALID) {
                type = getValidType(type);
            }
        }
        protected void setTransient() {
            if (! isTransient()) {
                forceTransient = true;
                if (vis.length() > 0) {
                    vis = vis + " transient";
                } else {
                    vis = "transient";
                }
            }
        }
        protected MemberDefinition getMemberDefinition() {
            return member;
        }
        public void destroy () {
            if (type != null) {
                type.destroy();
                type = null;
                vis = null;
                value = null;
                name = null;
                idlName = null;
                member = null;
            }
        }
        private Type type;
        private String vis;
        private String value;
        private String name;
        private String idlName;
        private boolean innerClassDecl;
        private boolean constant;
        private MemberDefinition member;
        private boolean forceTransient;
        public Member(MemberDefinition member,
                      String value,
                      ContextStack stack,
                      CompoundType enclosing) {
            this.member = member;
            this.value = value;
            forceTransient = false;
            innerClassDecl = member.getInnerClass() != null;
            if (!innerClassDecl) {
                init (stack,enclosing);
            }
        }
        public void init (ContextStack stack, CompoundType enclosing) {
            constant = false;
            name = member.getName().toString();
            vis = getVisibilityString(member);
            idlName = null;
            int contextCode = ContextStack.MEMBER;
            stack.setNewContextCode(contextCode);
            if (member.isVariable()) {
                if (value != null && member.isConstant()) {
                    contextCode = ContextStack.MEMBER_CONSTANT;
                    this.constant = true;
                } else if (member.isStatic()) {
                    contextCode = ContextStack.MEMBER_STATIC;
                } else if (member.isTransient()) {
                    contextCode = ContextStack.MEMBER_TRANSIENT;
                }
            }
            stack.setNewContextCode(contextCode);
            stack.push(this);
            type = makeType(member.getType(),null,stack);
            if (type == null ||
                (!innerClassDecl &&
                 !member.isStatic() &&
                 !member.isTransient() &&
                 !assertNotImpl(type,false,stack,enclosing,true))) {
                stack.pop(false);
                throw new CompilerError("");
            }
            if (constant && type.isPrimitive()) {
                if (type.isType(TYPE_LONG) || type.isType(TYPE_FLOAT) || type.isType(TYPE_DOUBLE)) {
                    int length = value.length();
                    char lastChar = value.charAt(length-1);
                    if (!Character.isDigit(lastChar)) {
                        this.value = value.substring(0,length-1);
                    }
                } else if (type.isType(TYPE_BOOLEAN)) {
                    value = value.toUpperCase();
                }
            }
            if (constant && type.isType(TYPE_STRING)) {
                value = "L" + value;
            }
            stack.pop(true);
        }
        public void setIDLName (String name) {
            this.idlName = name;
        }
        protected Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error("clone failed");
            }
        }
    }
}

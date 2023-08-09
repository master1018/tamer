public class RMIGenerator implements RMIConstants, Generator {
    private static final Hashtable versionOptions = new Hashtable();
    static {
        versionOptions.put("-v1.1", new Integer(STUB_VERSION_1_1));
        versionOptions.put("-vcompat", new Integer(STUB_VERSION_FAT));
        versionOptions.put("-v1.2", new Integer(STUB_VERSION_1_2));
    }
    public RMIGenerator() {
        version = STUB_VERSION_1_2;     
    }
    public boolean parseArgs(String argv[], Main main) {
        String explicitVersion = null;
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] != null) {
                String arg = argv[i].toLowerCase();
                if (versionOptions.containsKey(arg)) {
                    if (explicitVersion != null &&
                        !explicitVersion.equals(arg))
                    {
                        main.error("rmic.cannot.use.both",
                                   explicitVersion, arg);
                        return false;
                    }
                    explicitVersion = arg;
                    version = ((Integer) versionOptions.get(arg)).intValue();
                    argv[i] = null;
                }
            }
        }
        return true;
    }
    public void generate(BatchEnvironment env, ClassDefinition cdef, File destDir) {
        RemoteClass remoteClass = RemoteClass.forClass(env, cdef);
        if (remoteClass == null)        
            return;
        RMIGenerator gen;
        try {
            gen = new RMIGenerator(env, cdef, destDir, remoteClass, version);
        } catch (ClassNotFound e) {
            env.error(0, "rmic.class.not.found", e.name);
            return;
        }
        gen.generate();
    }
    private void generate() {
        env.addGeneratedFile(stubFile);
        try {
            IndentingWriter out = new IndentingWriter(
                new OutputStreamWriter(new FileOutputStream(stubFile)));
            writeStub(out);
            out.close();
            if (env.verbose()) {
                env.output(Main.getText("rmic.wrote", stubFile.getPath()));
            }
            env.parseFile(new ClassFile(stubFile));
        } catch (IOException e) {
            env.error(0, "cant.write", stubFile.toString());
            return;
        }
        if (version == STUB_VERSION_1_1 ||
            version == STUB_VERSION_FAT)
        {
            env.addGeneratedFile(skeletonFile);
            try {
                IndentingWriter out = new IndentingWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(skeletonFile)));
                writeSkeleton(out);
                out.close();
                if (env.verbose()) {
                    env.output(Main.getText("rmic.wrote",
                        skeletonFile.getPath()));
                }
                env.parseFile(new ClassFile(skeletonFile));
            } catch (IOException e) {
                env.error(0, "cant.write", stubFile.toString());
                return;
            }
        } else {
            File outputDir = Util.getOutputDirectoryFor(remoteClassName,destDir,env);
            File skeletonClassFile = new File(outputDir,skeletonClassName.getName().toString() + ".class");
            skeletonFile.delete();      
            skeletonClassFile.delete();
        }
    }
    protected static File sourceFileForClass(Identifier className,
                                             Identifier outputClassName,
                                             File destDir,
                                             BatchEnvironment env)
    {
        File packageDir = Util.getOutputDirectoryFor(className,destDir,env);
        String outputName = Names.mangleClass(outputClassName).getName().toString();
        if (outputName.endsWith("_Skel")) {
            String classNameStr = className.getName().toString();
            File temp = new File(packageDir, Utility.tieName(classNameStr) + ".class");
            if (temp.exists()) {
                if (!env.getMain().iiopGeneration) {
                    env.error(0,"warn.rmic.tie.found",
                              classNameStr,
                              temp.getAbsolutePath());
                }
            }
        }
        String outputFileName = outputName + ".java";
        return new File(packageDir, outputFileName);
    }
    private BatchEnvironment env;
    private RemoteClass remoteClass;
    private int version;
    private RemoteClass.Method[] remoteMethods;
    private Identifier remoteClassName;
    private Identifier stubClassName;
    private Identifier skeletonClassName;
    private ClassDefinition cdef;
    private File destDir;
    private File stubFile;
    private File skeletonFile;
    private String[] methodFieldNames;
    private ClassDefinition defException;
    private ClassDefinition defRemoteException;
    private ClassDefinition defRuntimeException;
    private RMIGenerator(BatchEnvironment env, ClassDefinition cdef,
                           File destDir, RemoteClass remoteClass, int version)
        throws ClassNotFound
    {
        this.destDir     = destDir;
        this.cdef        = cdef;
        this.env         = env;
        this.remoteClass = remoteClass;
        this.version     = version;
        remoteMethods = remoteClass.getRemoteMethods();
        remoteClassName = remoteClass.getName();
        stubClassName = Names.stubFor(remoteClassName);
        skeletonClassName = Names.skeletonFor(remoteClassName);
        methodFieldNames = nameMethodFields(remoteMethods);
        stubFile = sourceFileForClass(remoteClassName,stubClassName, destDir , env);
        skeletonFile = sourceFileForClass(remoteClassName,skeletonClassName, destDir, env);
        defException =
            env.getClassDeclaration(idJavaLangException).
                getClassDefinition(env);
        defRemoteException =
            env.getClassDeclaration(idRemoteException).
                getClassDefinition(env);
        defRuntimeException =
            env.getClassDeclaration(idJavaLangRuntimeException).
                getClassDefinition(env);
    }
    private void writeStub(IndentingWriter p) throws IOException {
        p.pln("
        p.pln("
        p.pln();
        if (remoteClassName.isQualified()) {
            p.pln("package " + remoteClassName.getQualifier() + ";");
            p.pln();
        }
        p.plnI("public final class " +
            Names.mangleClass(stubClassName.getName()));
        p.pln("extends " + idRemoteStub);
        ClassDefinition[] remoteInterfaces = remoteClass.getRemoteInterfaces();
        if (remoteInterfaces.length > 0) {
            p.p("implements ");
            for (int i = 0; i < remoteInterfaces.length; i++) {
                if (i > 0)
                    p.p(", ");
                p.p(remoteInterfaces[i].getName().toString());
            }
            p.pln();
        }
        p.pOlnI("{");
        if (version == STUB_VERSION_1_1 ||
            version == STUB_VERSION_FAT)
        {
            writeOperationsArray(p);
            p.pln();
            writeInterfaceHash(p);
            p.pln();
        }
        if (version == STUB_VERSION_FAT ||
            version == STUB_VERSION_1_2)
        {
            p.pln("private static final long serialVersionUID = " +
                STUB_SERIAL_VERSION_UID + ";");
            p.pln();
            if (methodFieldNames.length > 0) {
                if (version == STUB_VERSION_FAT) {
                    p.pln("private static boolean useNewInvoke;");
                }
                writeMethodFieldDeclarations(p);
                p.pln();
                p.plnI("static {");
                p.plnI("try {");
                if (version == STUB_VERSION_FAT) {
                    p.plnI(idRemoteRef + ".class.getMethod(\"invoke\",");
                    p.plnI("new java.lang.Class[] {");
                    p.pln(idRemote + ".class,");
                    p.pln("java.lang.reflect.Method.class,");
                    p.pln("java.lang.Object[].class,");
                    p.pln("long.class");
                    p.pOln("});");
                    p.pO();
                    p.pln("useNewInvoke = true;");
                }
                writeMethodFieldInitializers(p);
                p.pOlnI("} catch (java.lang.NoSuchMethodException e) {");
                if (version == STUB_VERSION_FAT) {
                    p.pln("useNewInvoke = false;");
                } else {
                    p.plnI("throw new java.lang.NoSuchMethodError(");
                    p.pln("\"stub class initialization failed\");");
                    p.pO();
                }
                p.pOln("}");            
                p.pOln("}");            
                p.pln();
            }
        }
        writeStubConstructors(p);
        p.pln();
        if (remoteMethods.length > 0) {
            p.pln("
            for (int i = 0; i < remoteMethods.length; ++i) {
                p.pln();
                writeStubMethod(p, i);
            }
        }
        p.pOln("}");                    
    }
    private void writeStubConstructors(IndentingWriter p)
        throws IOException
    {
        p.pln("
        if (version == STUB_VERSION_1_1 ||
            version == STUB_VERSION_FAT)
        {
            p.plnI("public " + Names.mangleClass(stubClassName.getName()) +
                "() {");
            p.pln("super();");
            p.pOln("}");
        }
        p.plnI("public " + Names.mangleClass(stubClassName.getName()) +
            "(" + idRemoteRef + " ref) {");
        p.pln("super(ref);");
        p.pOln("}");
    }
    private void writeStubMethod(IndentingWriter p, int opnum)
        throws IOException
    {
        RemoteClass.Method method = remoteMethods[opnum];
        Identifier methodName = method.getName();
        Type methodType = method.getType();
        Type paramTypes[] = methodType.getArgumentTypes();
        String paramNames[] = nameParameters(paramTypes);
        Type returnType = methodType.getReturnType();
        ClassDeclaration[] exceptions = method.getExceptions();
        p.pln("
            methodType.typeString(methodName.toString(), true, false));
        p.p("public " + returnType + " " + methodName + "(");
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0)
                p.p(", ");
            p.p(paramTypes[i] + " " + paramNames[i]);
        }
        p.plnI(")");
        if (exceptions.length > 0) {
            p.p("throws ");
            for (int i = 0; i < exceptions.length; i++) {
                if (i > 0)
                    p.p(", ");
                p.p(exceptions[i].getName().toString());
            }
            p.pln();
        }
        p.pOlnI("{");
        Vector catchList = computeUniqueCatchList(exceptions);
        if (catchList.size() > 0) {
            p.plnI("try {");
        }
        if (version == STUB_VERSION_FAT) {
            p.plnI("if (useNewInvoke) {");
        }
        if (version == STUB_VERSION_FAT ||
            version == STUB_VERSION_1_2)
        {
            if (!returnType.isType(TC_VOID)) {
                p.p("Object $result = ");               
            }
            p.p("ref.invoke(this, " + methodFieldNames[opnum] + ", ");
            if (paramTypes.length > 0) {
                p.p("new java.lang.Object[] {");
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0)
                        p.p(", ");
                    p.p(wrapArgumentCode(paramTypes[i], paramNames[i]));
                }
                p.p("}");
            } else {
                p.p("null");
            }
            p.pln(", " + method.getMethodHash() + "L);");
            if (!returnType.isType(TC_VOID)) {
                p.pln("return " +
                    unwrapArgumentCode(returnType, "$result") + ";");
            }
        }
        if (version == STUB_VERSION_FAT) {
            p.pOlnI("} else {");
        }
        if (version == STUB_VERSION_1_1 ||
            version == STUB_VERSION_FAT)
        {
            p.pln(idRemoteCall + " call = ref.newCall((" + idRemoteObject +
                ") this, operations, " + opnum + ", interfaceHash);");
            if (paramTypes.length > 0) {
                p.plnI("try {");
                p.pln("java.io.ObjectOutput out = call.getOutputStream();");
                writeMarshalArguments(p, "out", paramTypes, paramNames);
                p.pOlnI("} catch (java.io.IOException e) {");
                p.pln("throw new " + idMarshalException +
                    "(\"error marshalling arguments\", e);");
                p.pOln("}");
            }
            p.pln("ref.invoke(call);");
            if (returnType.isType(TC_VOID)) {
                p.pln("ref.done(call);");
            } else {
                p.pln(returnType + " $result;");        
                p.plnI("try {");
                p.pln("java.io.ObjectInput in = call.getInputStream();");
                boolean objectRead =
                    writeUnmarshalArgument(p, "in", returnType, "$result");
                p.pln(";");
                p.pOlnI("} catch (java.io.IOException e) {");
                p.pln("throw new " + idUnmarshalException +
                    "(\"error unmarshalling return\", e);");
                if (objectRead) {
                    p.pOlnI("} catch (java.lang.ClassNotFoundException e) {");
                    p.pln("throw new " + idUnmarshalException +
                        "(\"error unmarshalling return\", e);");
                }
                p.pOlnI("} finally {");
                p.pln("ref.done(call);");
                p.pOln("}");
                p.pln("return $result;");
            }
        }
        if (version == STUB_VERSION_FAT) {
            p.pOln("}");                
        }
        if (catchList.size() > 0) {
            for (Enumeration enumeration = catchList.elements();
                 enumeration.hasMoreElements();)
            {
                ClassDefinition def = (ClassDefinition) enumeration.nextElement();
                p.pOlnI("} catch (" + def.getName() + " e) {");
                p.pln("throw e;");
            }
            p.pOlnI("} catch (java.lang.Exception e) {");
            p.pln("throw new " + idUnexpectedException +
                "(\"undeclared checked exception\", e);");
            p.pOln("}");                
        }
        p.pOln("}");                    
    }
    private Vector computeUniqueCatchList(ClassDeclaration[] exceptions) {
        Vector uniqueList = new Vector();       
        uniqueList.addElement(defRuntimeException);
        uniqueList.addElement(defRemoteException);
    nextException:
        for (int i = 0; i < exceptions.length; i++) {
            ClassDeclaration decl = exceptions[i];
            try {
                if (defException.subClassOf(env, decl)) {
                    uniqueList.clear();
                    break;
                } else if (!defException.superClassOf(env, decl)) {
                    continue;
                }
                for (int j = 0; j < uniqueList.size();) {
                    ClassDefinition def =
                        (ClassDefinition) uniqueList.elementAt(j);
                    if (def.superClassOf(env, decl)) {
                        continue nextException;
                    } else if (def.subClassOf(env, decl)) {
                        uniqueList.removeElementAt(j);
                    } else {
                        j++;    
                    }
                }
                uniqueList.addElement(decl.getClassDefinition(env));
            } catch (ClassNotFound e) {
                env.error(0, "class.not.found", e.name, decl.getName());
            }
        }
        return uniqueList;
    }
    private void writeSkeleton(IndentingWriter p) throws IOException {
        if (version == STUB_VERSION_1_2) {
            throw new Error("should not generate skeleton for version");
        }
        p.pln("
        p.pln("
        p.pln();
        if (remoteClassName.isQualified()) {
            p.pln("package " + remoteClassName.getQualifier() + ";");
            p.pln();
        }
        p.plnI("public final class " +
            Names.mangleClass(skeletonClassName.getName()));
        p.pln("implements " + idSkeleton);
        p.pOlnI("{");
        writeOperationsArray(p);
        p.pln();
        writeInterfaceHash(p);
        p.pln();
        p.plnI("public " + idOperation + "[] getOperations() {");
        p.pln("return (" + idOperation + "[]) operations.clone();");
        p.pOln("}");
        p.pln();
        p.plnI("public void dispatch(" + idRemote + " obj, " +
            idRemoteCall + " call, int opnum, long hash)");
        p.pln("throws java.lang.Exception");
        p.pOlnI("{");
        if (version == STUB_VERSION_FAT) {
            p.plnI("if (opnum < 0) {");
            if (remoteMethods.length > 0) {
                for (int opnum = 0; opnum < remoteMethods.length; opnum++) {
                    if (opnum > 0)
                        p.pO("} else ");
                    p.plnI("if (hash == " +
                        remoteMethods[opnum].getMethodHash() + "L) {");
                    p.pln("opnum = " + opnum + ";");
                }
                p.pOlnI("} else {");
            }
            p.pln("throw new " +
                idUnmarshalException + "(\"invalid method hash\");");
            if (remoteMethods.length > 0) {
                p.pOln("}");
            }
            p.pOlnI("} else {");
        }
        p.plnI("if (hash != interfaceHash)");
        p.pln("throw new " +
            idSkeletonMismatchException + "(\"interface hash mismatch\");");
        p.pO();
        if (version == STUB_VERSION_FAT) {
            p.pOln("}");                
        }
        p.pln();
        p.pln(remoteClassName + " server = (" + remoteClassName + ") obj;");
        p.plnI("switch (opnum) {");
        for (int opnum = 0; opnum < remoteMethods.length; opnum++) {
            writeSkeletonDispatchCase(p, opnum);
        }
        p.pOlnI("default:");
        p.pln("throw new " + idUnmarshalException +
            "(\"invalid method number\");");
        p.pOln("}");                    
        p.pOln("}");                    
        p.pOln("}");                    
    }
    private void writeSkeletonDispatchCase(IndentingWriter p, int opnum)
        throws IOException
    {
        RemoteClass.Method method = remoteMethods[opnum];
        Identifier methodName = method.getName();
        Type methodType = method.getType();
        Type paramTypes[] = methodType.getArgumentTypes();
        String paramNames[] = nameParameters(paramTypes);
        Type returnType = methodType.getReturnType();
        p.pOlnI("case " + opnum + ": 
            methodType.typeString(methodName.toString(), true, false));
        p.pOlnI("{");
        if (paramTypes.length > 0) {
            for (int i = 0; i < paramTypes.length; i++) {
                p.pln(paramTypes[i] + " " + paramNames[i] + ";");
            }
            p.plnI("try {");
            p.pln("java.io.ObjectInput in = call.getInputStream();");
            boolean objectsRead = writeUnmarshalArguments(p, "in",
                paramTypes, paramNames);
            p.pOlnI("} catch (java.io.IOException e) {");
            p.pln("throw new " + idUnmarshalException +
                "(\"error unmarshalling arguments\", e);");
            if (objectsRead) {
                p.pOlnI("} catch (java.lang.ClassNotFoundException e) {");
                p.pln("throw new " + idUnmarshalException +
                    "(\"error unmarshalling arguments\", e);");
            }
            p.pOlnI("} finally {");
            p.pln("call.releaseInputStream();");
            p.pOln("}");
        } else {
            p.pln("call.releaseInputStream();");
        }
        if (!returnType.isType(TC_VOID)) {
            p.p(returnType + " $result = ");            
        }
        p.p("server." + methodName + "(");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0)
                p.p(", ");
            p.p(paramNames[i]);
        }
        p.pln(");");
        p.plnI("try {");
        if (!returnType.isType(TC_VOID)) {
            p.p("java.io.ObjectOutput out = ");
        }
        p.pln("call.getResultStream(true);");
        if (!returnType.isType(TC_VOID)) {
            writeMarshalArgument(p, "out", returnType, "$result");
            p.pln(";");
        }
        p.pOlnI("} catch (java.io.IOException e) {");
        p.pln("throw new " +
            idMarshalException + "(\"error marshalling return\", e);");
        p.pOln("}");
        p.pln("break;");                
        p.pOlnI("}");                   
        p.pln();
    }
    private void writeOperationsArray(IndentingWriter p)
        throws IOException
    {
        p.plnI("private static final " + idOperation + "[] operations = {");
        for (int i = 0; i < remoteMethods.length; i++) {
            if (i > 0)
                p.pln(",");
            p.p("new " + idOperation + "(\"" +
                remoteMethods[i].getOperationString() + "\")");
        }
        p.pln();
        p.pOln("};");
    }
    private void writeInterfaceHash(IndentingWriter p)
        throws IOException
    {
        p.pln("private static final long interfaceHash = " +
            remoteClass.getInterfaceHash() + "L;");
    }
    private void writeMethodFieldDeclarations(IndentingWriter p)
        throws IOException
    {
        for (int i = 0; i < methodFieldNames.length; i++) {
            p.pln("private static java.lang.reflect.Method " +
                methodFieldNames[i] + ";");
        }
    }
    private void writeMethodFieldInitializers(IndentingWriter p)
        throws IOException
    {
        for (int i = 0; i < methodFieldNames.length; i++) {
            p.p(methodFieldNames[i] + " = ");
            RemoteClass.Method method = remoteMethods[i];
            MemberDefinition def = method.getMemberDefinition();
            Identifier methodName = method.getName();
            Type methodType = method.getType();
            Type paramTypes[] = methodType.getArgumentTypes();
            p.p(def.getClassDefinition().getName() + ".class.getMethod(\"" +
                methodName + "\", new java.lang.Class[] {");
            for (int j = 0; j < paramTypes.length; j++) {
                if (j > 0)
                    p.p(", ");
                p.p(paramTypes[j] + ".class");
            }
            p.pln("});");
        }
    }
    private static String[] nameMethodFields(RemoteClass.Method[] methods) {
        String[] names = new String[methods.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = "$method_" + methods[i].getName() + "_" + i;
        }
        return names;
    }
    private static String[] nameParameters(Type[] types) {
        String[] names = new String[types.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = "$param_" +
                generateNameFromType(types[i]) + "_" + (i + 1);
        }
        return names;
    }
    private static String generateNameFromType(Type type) {
        int typeCode = type.getTypeCode();
        switch (typeCode) {
        case TC_BOOLEAN:
        case TC_BYTE:
        case TC_CHAR:
        case TC_SHORT:
        case TC_INT:
        case TC_LONG:
        case TC_FLOAT:
        case TC_DOUBLE:
            return type.toString();
        case TC_ARRAY:
            return "arrayOf_" + generateNameFromType(type.getElementType());
        case TC_CLASS:
            return Names.mangleClass(type.getClassName().getName()).toString();
        default:
            throw new Error("unexpected type code: " + typeCode);
        }
    }
    private static void writeMarshalArgument(IndentingWriter p,
                                             String streamName,
                                             Type type, String name)
        throws IOException
    {
        int typeCode = type.getTypeCode();
        switch (typeCode) {
        case TC_BOOLEAN:
            p.p(streamName + ".writeBoolean(" + name + ")");
            break;
        case TC_BYTE:
            p.p(streamName + ".writeByte(" + name + ")");
            break;
        case TC_CHAR:
            p.p(streamName + ".writeChar(" + name + ")");
            break;
        case TC_SHORT:
            p.p(streamName + ".writeShort(" + name + ")");
            break;
        case TC_INT:
            p.p(streamName + ".writeInt(" + name + ")");
            break;
        case TC_LONG:
            p.p(streamName + ".writeLong(" + name + ")");
            break;
        case TC_FLOAT:
            p.p(streamName + ".writeFloat(" + name + ")");
            break;
        case TC_DOUBLE:
            p.p(streamName + ".writeDouble(" + name + ")");
            break;
        case TC_ARRAY:
        case TC_CLASS:
            p.p(streamName + ".writeObject(" + name + ")");
            break;
        default:
            throw new Error("unexpected type code: " + typeCode);
        }
    }
    private static void writeMarshalArguments(IndentingWriter p,
                                              String streamName,
                                              Type[] types, String[] names)
        throws IOException
    {
        if (types.length != names.length) {
            throw new Error("paramter type and name arrays different sizes");
        }
        for (int i = 0; i < types.length; i++) {
            writeMarshalArgument(p, streamName, types[i], names[i]);
            p.pln(";");
        }
    }
    private static boolean writeUnmarshalArgument(IndentingWriter p,
                                                  String streamName,
                                                  Type type, String name)
        throws IOException
    {
        boolean readObject = false;
        if (name != null) {
            p.p(name + " = ");
        }
        int typeCode = type.getTypeCode();
        switch (type.getTypeCode()) {
        case TC_BOOLEAN:
            p.p(streamName + ".readBoolean()");
            break;
        case TC_BYTE:
            p.p(streamName + ".readByte()");
            break;
        case TC_CHAR:
            p.p(streamName + ".readChar()");
            break;
        case TC_SHORT:
            p.p(streamName + ".readShort()");
            break;
        case TC_INT:
            p.p(streamName + ".readInt()");
            break;
        case TC_LONG:
            p.p(streamName + ".readLong()");
            break;
        case TC_FLOAT:
            p.p(streamName + ".readFloat()");
            break;
        case TC_DOUBLE:
            p.p(streamName + ".readDouble()");
            break;
        case TC_ARRAY:
        case TC_CLASS:
            p.p("(" + type + ") " + streamName + ".readObject()");
            readObject = true;
            break;
        default:
            throw new Error("unexpected type code: " + typeCode);
        }
        return readObject;
    }
    private static boolean writeUnmarshalArguments(IndentingWriter p,
                                                   String streamName,
                                                   Type[] types,
                                                   String[] names)
        throws IOException
    {
        if (types.length != names.length) {
            throw new Error("paramter type and name arrays different sizes");
        }
        boolean readObject = false;
        for (int i = 0; i < types.length; i++) {
            if (writeUnmarshalArgument(p, streamName, types[i], names[i])) {
                readObject = true;
            }
            p.pln(";");
        }
        return readObject;
    }
    private static String wrapArgumentCode(Type type, String name) {
        int typeCode = type.getTypeCode();
        switch (typeCode) {
        case TC_BOOLEAN:
            return ("(" + name +
                    " ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE)");
        case TC_BYTE:
            return "new java.lang.Byte(" + name + ")";
        case TC_CHAR:
            return "new java.lang.Character(" + name + ")";
        case TC_SHORT:
            return "new java.lang.Short(" + name + ")";
        case TC_INT:
            return "new java.lang.Integer(" + name + ")";
        case TC_LONG:
            return "new java.lang.Long(" + name + ")";
        case TC_FLOAT:
            return "new java.lang.Float(" + name + ")";
        case TC_DOUBLE:
            return "new java.lang.Double(" + name + ")";
        case TC_ARRAY:
        case TC_CLASS:
            return name;
        default:
            throw new Error("unexpected type code: " + typeCode);
        }
    }
    private static String unwrapArgumentCode(Type type, String name) {
        int typeCode = type.getTypeCode();
        switch (typeCode) {
        case TC_BOOLEAN:
            return "((java.lang.Boolean) " + name + ").booleanValue()";
        case TC_BYTE:
            return "((java.lang.Byte) " + name + ").byteValue()";
        case TC_CHAR:
            return "((java.lang.Character) " + name + ").charValue()";
        case TC_SHORT:
            return "((java.lang.Short) " + name + ").shortValue()";
        case TC_INT:
            return "((java.lang.Integer) " + name + ").intValue()";
        case TC_LONG:
            return "((java.lang.Long) " + name + ").longValue()";
        case TC_FLOAT:
            return "((java.lang.Float) " + name + ").floatValue()";
        case TC_DOUBLE:
            return "((java.lang.Double) " + name + ").doubleValue()";
        case TC_ARRAY:
        case TC_CLASS:
            return "((" + type + ") " + name + ")";
        default:
            throw new Error("unexpected type code: " + typeCode);
        }
    }
}

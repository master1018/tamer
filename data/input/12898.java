class StubSkeletonWriter {
    private final BatchEnvironment env;
    private final RemoteClass remoteClass;
    private final StubVersion version;
    private final String stubClassName;
    private final String skeletonClassName;
    private final String packageName;
    private final String stubClassSimpleName;
    private final String skeletonClassSimpleName;
    private final RemoteClass.Method[] remoteMethods;
    private final String[] methodFieldNames;
    StubSkeletonWriter(BatchEnvironment env,
                       RemoteClass remoteClass,
                       StubVersion version)
    {
        this.env = env;
        this.remoteClass = remoteClass;
        this.version = version;
        stubClassName = Util.binaryNameOf(remoteClass.classDoc()) + "_Stub";
        skeletonClassName =
            Util.binaryNameOf(remoteClass.classDoc()) + "_Skel";
        int i = stubClassName.lastIndexOf('.');
        packageName = (i != -1 ? stubClassName.substring(0, i) : "");
        stubClassSimpleName = stubClassName.substring(i + 1);
        skeletonClassSimpleName = skeletonClassName.substring(i + 1);
        remoteMethods = remoteClass.remoteMethods();
        methodFieldNames = nameMethodFields(remoteMethods);
    }
    String stubClassName() {
        return stubClassName;
    }
    String skeletonClassName() {
        return skeletonClassName;
    }
    void writeStub(IndentingWriter p) throws IOException {
        p.pln("
        p.pln("
        p.pln();
        if (!packageName.equals("")) {
            p.pln("package " + packageName + ";");
            p.pln();
        }
        p.plnI("public final class " + stubClassSimpleName);
        p.pln("extends " + REMOTE_STUB);
        ClassDoc[] remoteInterfaces = remoteClass.remoteInterfaces();
        if (remoteInterfaces.length > 0) {
            p.p("implements ");
            for (int i = 0; i < remoteInterfaces.length; i++) {
                if (i > 0) {
                    p.p(", ");
                }
                p.p(remoteInterfaces[i].qualifiedName());
            }
            p.pln();
        }
        p.pOlnI("{");
        if (version == StubVersion.V1_1 ||
            version == StubVersion.VCOMPAT)
        {
            writeOperationsArray(p);
            p.pln();
            writeInterfaceHash(p);
            p.pln();
        }
        if (version == StubVersion.VCOMPAT ||
            version == StubVersion.V1_2)
        {
            p.pln("private static final long serialVersionUID = " +
                STUB_SERIAL_VERSION_UID + ";");
            p.pln();
            if (methodFieldNames.length > 0) {
                if (version == StubVersion.VCOMPAT) {
                    p.pln("private static boolean useNewInvoke;");
                }
                writeMethodFieldDeclarations(p);
                p.pln();
                p.plnI("static {");
                p.plnI("try {");
                if (version == StubVersion.VCOMPAT) {
                    p.plnI(REMOTE_REF + ".class.getMethod(\"invoke\",");
                    p.plnI("new java.lang.Class[] {");
                    p.pln(REMOTE + ".class,");
                    p.pln("java.lang.reflect.Method.class,");
                    p.pln("java.lang.Object[].class,");
                    p.pln("long.class");
                    p.pOln("});");
                    p.pO();
                    p.pln("useNewInvoke = true;");
                }
                writeMethodFieldInitializers(p);
                p.pOlnI("} catch (java.lang.NoSuchMethodException e) {");
                if (version == StubVersion.VCOMPAT) {
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
        if (version == StubVersion.V1_1 ||
            version == StubVersion.VCOMPAT)
        {
            p.plnI("public " + stubClassSimpleName + "() {");
            p.pln("super();");
            p.pOln("}");
        }
        p.plnI("public " + stubClassSimpleName + "(" + REMOTE_REF + " ref) {");
        p.pln("super(ref);");
        p.pOln("}");
    }
    private void writeStubMethod(IndentingWriter p, int opnum)
        throws IOException
    {
        RemoteClass.Method method = remoteMethods[opnum];
        MethodDoc methodDoc = method.methodDoc();
        String methodName = methodDoc.name();
        Type[] paramTypes = method.parameterTypes();
        String paramNames[] = nameParameters(paramTypes);
        Type returnType = methodDoc.returnType();
        ClassDoc[] exceptions = method.exceptionTypes();
        p.pln("
              Util.getFriendlyUnqualifiedSignature(methodDoc));
        p.p("public " + returnType.toString() + " " + methodName + "(");
        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0) {
                p.p(", ");
            }
            p.p(paramTypes[i].toString() + " " + paramNames[i]);
        }
        p.plnI(")");
        if (exceptions.length > 0) {
            p.p("throws ");
            for (int i = 0; i < exceptions.length; i++) {
                if (i > 0) {
                    p.p(", ");
                }
                p.p(exceptions[i].qualifiedName());
            }
            p.pln();
        }
        p.pOlnI("{");
        List<ClassDoc> catchList = computeUniqueCatchList(exceptions);
        if (catchList.size() > 0) {
            p.plnI("try {");
        }
        if (version == StubVersion.VCOMPAT) {
            p.plnI("if (useNewInvoke) {");
        }
        if (version == StubVersion.VCOMPAT ||
            version == StubVersion.V1_2)
        {
            if (!Util.isVoid(returnType)) {
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
            p.pln(", " + method.methodHash() + "L);");
            if (!Util.isVoid(returnType)) {
                p.pln("return " +
                    unwrapArgumentCode(returnType, "$result") + ";");
            }
        }
        if (version == StubVersion.VCOMPAT) {
            p.pOlnI("} else {");
        }
        if (version == StubVersion.V1_1 ||
            version == StubVersion.VCOMPAT)
        {
            p.pln(REMOTE_CALL + " call = ref.newCall((" + REMOTE_OBJECT +
                ") this, operations, " + opnum + ", interfaceHash);");
            if (paramTypes.length > 0) {
                p.plnI("try {");
                p.pln("java.io.ObjectOutput out = call.getOutputStream();");
                writeMarshalArguments(p, "out", paramTypes, paramNames);
                p.pOlnI("} catch (java.io.IOException e) {");
                p.pln("throw new " + MARSHAL_EXCEPTION +
                    "(\"error marshalling arguments\", e);");
                p.pOln("}");
            }
            p.pln("ref.invoke(call);");
            if (Util.isVoid(returnType)) {
                p.pln("ref.done(call);");
            } else {
                p.pln(returnType.toString() + " $result;");
                p.plnI("try {");
                p.pln("java.io.ObjectInput in = call.getInputStream();");
                boolean objectRead =
                    writeUnmarshalArgument(p, "in", returnType, "$result");
                p.pln(";");
                p.pOlnI("} catch (java.io.IOException e) {");
                p.pln("throw new " + UNMARSHAL_EXCEPTION +
                    "(\"error unmarshalling return\", e);");
                if (objectRead) {
                    p.pOlnI("} catch (java.lang.ClassNotFoundException e) {");
                    p.pln("throw new " + UNMARSHAL_EXCEPTION +
                        "(\"error unmarshalling return\", e);");
                }
                p.pOlnI("} finally {");
                p.pln("ref.done(call);");
                p.pOln("}");
                p.pln("return $result;");
            }
        }
        if (version == StubVersion.VCOMPAT) {
            p.pOln("}");                
        }
        if (catchList.size() > 0) {
            for (ClassDoc catchClass : catchList) {
                p.pOlnI("} catch (" + catchClass.qualifiedName() + " e) {");
                p.pln("throw e;");
            }
            p.pOlnI("} catch (java.lang.Exception e) {");
            p.pln("throw new " + UNEXPECTED_EXCEPTION +
                "(\"undeclared checked exception\", e);");
            p.pOln("}");                
        }
        p.pOln("}");                    
    }
    private List<ClassDoc> computeUniqueCatchList(ClassDoc[] exceptions) {
        List<ClassDoc> uniqueList = new ArrayList<ClassDoc>();
        uniqueList.add(env.docRuntimeException());
        uniqueList.add(env.docRemoteException()); 
    nextException:
        for (ClassDoc ex : exceptions) {
            if (env.docException().subclassOf(ex)) {
                uniqueList.clear();
                break;
            } else if (!ex.subclassOf(env.docException())) {
                continue;
            }
            for (Iterator<ClassDoc> i = uniqueList.iterator(); i.hasNext();) {
                ClassDoc ex2 = i.next();
                if (ex.subclassOf(ex2)) {
                    continue nextException;
                } else if (ex2.subclassOf(ex)) {
                    i.remove();
                }
            }
            uniqueList.add(ex);
        }
        return uniqueList;
    }
    void writeSkeleton(IndentingWriter p) throws IOException {
        if (version == StubVersion.V1_2) {
            throw new AssertionError(
                "should not generate skeleton for version " + version);
        }
        p.pln("
        p.pln("
        p.pln();
        if (!packageName.equals("")) {
            p.pln("package " + packageName + ";");
            p.pln();
        }
        p.plnI("public final class " + skeletonClassSimpleName);
        p.pln("implements " + SKELETON);
        p.pOlnI("{");
        writeOperationsArray(p);
        p.pln();
        writeInterfaceHash(p);
        p.pln();
        p.plnI("public " + OPERATION + "[] getOperations() {");
        p.pln("return (" + OPERATION + "[]) operations.clone();");
        p.pOln("}");
        p.pln();
        p.plnI("public void dispatch(" + REMOTE + " obj, " +
            REMOTE_CALL + " call, int opnum, long hash)");
        p.pln("throws java.lang.Exception");
        p.pOlnI("{");
        if (version == StubVersion.VCOMPAT) {
            p.plnI("if (opnum < 0) {");
            if (remoteMethods.length > 0) {
                for (int opnum = 0; opnum < remoteMethods.length; opnum++) {
                    if (opnum > 0)
                        p.pO("} else ");
                    p.plnI("if (hash == " +
                        remoteMethods[opnum].methodHash() + "L) {");
                    p.pln("opnum = " + opnum + ";");
                }
                p.pOlnI("} else {");
            }
            p.pln("throw new " +
                UNMARSHAL_EXCEPTION + "(\"invalid method hash\");");
            if (remoteMethods.length > 0) {
                p.pOln("}");
            }
            p.pOlnI("} else {");
        }
        p.plnI("if (hash != interfaceHash)");
        p.pln("throw new " +
            SKELETON_MISMATCH_EXCEPTION + "(\"interface hash mismatch\");");
        p.pO();
        if (version == StubVersion.VCOMPAT) {
            p.pOln("}");                
        }
        p.pln();
        if (!remoteClass.classDoc().isPrivate()) {
            p.pln(remoteClass.classDoc().qualifiedName() + " server = (" +
                  remoteClass.classDoc().qualifiedName() + ") obj;");
        }
        p.plnI("switch (opnum) {");
        for (int opnum = 0; opnum < remoteMethods.length; opnum++) {
            writeSkeletonDispatchCase(p, opnum);
        }
        p.pOlnI("default:");
        p.pln("throw new " + UNMARSHAL_EXCEPTION +
            "(\"invalid method number\");");
        p.pOln("}");                    
        p.pOln("}");                    
        p.pOln("}");                    
    }
    private void writeSkeletonDispatchCase(IndentingWriter p, int opnum)
        throws IOException
    {
        RemoteClass.Method method = remoteMethods[opnum];
        MethodDoc methodDoc = method.methodDoc();
        String methodName = methodDoc.name();
        Type paramTypes[] = method.parameterTypes();
        String paramNames[] = nameParameters(paramTypes);
        Type returnType = methodDoc.returnType();
        p.pOlnI("case " + opnum + ": 
            Util.getFriendlyUnqualifiedSignature(methodDoc));
        p.pOlnI("{");
        if (paramTypes.length > 0) {
            for (int i = 0; i < paramTypes.length; i++) {
                p.pln(paramTypes[i].toString() + " " + paramNames[i] + ";");
            }
            p.plnI("try {");
            p.pln("java.io.ObjectInput in = call.getInputStream();");
            boolean objectsRead = writeUnmarshalArguments(p, "in",
                paramTypes, paramNames);
            p.pOlnI("} catch (java.io.IOException e) {");
            p.pln("throw new " + UNMARSHAL_EXCEPTION +
                "(\"error unmarshalling arguments\", e);");
            if (objectsRead) {
                p.pOlnI("} catch (java.lang.ClassNotFoundException e) {");
                p.pln("throw new " + UNMARSHAL_EXCEPTION +
                    "(\"error unmarshalling arguments\", e);");
            }
            p.pOlnI("} finally {");
            p.pln("call.releaseInputStream();");
            p.pOln("}");
        } else {
            p.pln("call.releaseInputStream();");
        }
        if (!Util.isVoid(returnType)) {
            p.p(returnType.toString() + " $result = ");
        }
        String target = remoteClass.classDoc().isPrivate() ?
            "((" + methodDoc.containingClass().qualifiedName() + ") obj)" :
            "server";
        p.p(target + "." + methodName + "(");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0)
                p.p(", ");
            p.p(paramNames[i]);
        }
        p.pln(");");
        p.plnI("try {");
        if (!Util.isVoid(returnType)) {
            p.p("java.io.ObjectOutput out = ");
        }
        p.pln("call.getResultStream(true);");
        if (!Util.isVoid(returnType)) {
            writeMarshalArgument(p, "out", returnType, "$result");
            p.pln(";");
        }
        p.pOlnI("} catch (java.io.IOException e) {");
        p.pln("throw new " +
            MARSHAL_EXCEPTION + "(\"error marshalling return\", e);");
        p.pOln("}");
        p.pln("break;");                
        p.pOlnI("}");                   
        p.pln();
    }
    private void writeOperationsArray(IndentingWriter p)
        throws IOException
    {
        p.plnI("private static final " + OPERATION + "[] operations = {");
        for (int i = 0; i < remoteMethods.length; i++) {
            if (i > 0)
                p.pln(",");
            p.p("new " + OPERATION + "(\"" +
                remoteMethods[i].operationString() + "\")");
        }
        p.pln();
        p.pOln("};");
    }
    private void writeInterfaceHash(IndentingWriter p)
        throws IOException
    {
        p.pln("private static final long interfaceHash = " +
            remoteClass.interfaceHash() + "L;");
    }
    private void writeMethodFieldDeclarations(IndentingWriter p)
        throws IOException
    {
        for (String name : methodFieldNames) {
            p.pln("private static java.lang.reflect.Method " + name + ";");
        }
    }
    private void writeMethodFieldInitializers(IndentingWriter p)
        throws IOException
    {
        for (int i = 0; i < methodFieldNames.length; i++) {
            p.p(methodFieldNames[i] + " = ");
            RemoteClass.Method method = remoteMethods[i];
            MethodDoc methodDoc = method.methodDoc();
            String methodName = methodDoc.name();
            Type paramTypes[] = method.parameterTypes();
            p.p(methodDoc.containingClass().qualifiedName() + ".class.getMethod(\"" +
                methodName + "\", new java.lang.Class[] {");
            for (int j = 0; j < paramTypes.length; j++) {
                if (j > 0)
                    p.p(", ");
                p.p(paramTypes[j].toString() + ".class");
            }
            p.pln("});");
        }
    }
    private static String[] nameMethodFields(RemoteClass.Method[] methods) {
        String[] names = new String[methods.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = "$method_" + methods[i].methodDoc().name() + "_" + i;
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
        String name = type.typeName().replace('.', '$');
        int dimensions = type.dimension().length() / 2;
        for (int i = 0; i < dimensions; i++) {
            name = "arrayOf_" + name;
        }
        return name;
    }
    private static void writeMarshalArgument(IndentingWriter p,
                                             String streamName,
                                             Type type, String name)
        throws IOException
    {
        if (type.dimension().length() > 0 || type.asClassDoc() != null) {
            p.p(streamName + ".writeObject(" + name + ")");
        } else if (type.typeName().equals("boolean")) {
            p.p(streamName + ".writeBoolean(" + name + ")");
        } else if (type.typeName().equals("byte")) {
            p.p(streamName + ".writeByte(" + name + ")");
        } else if (type.typeName().equals("char")) {
            p.p(streamName + ".writeChar(" + name + ")");
        } else if (type.typeName().equals("short")) {
            p.p(streamName + ".writeShort(" + name + ")");
        } else if (type.typeName().equals("int")) {
            p.p(streamName + ".writeInt(" + name + ")");
        } else if (type.typeName().equals("long")) {
            p.p(streamName + ".writeLong(" + name + ")");
        } else if (type.typeName().equals("float")) {
            p.p(streamName + ".writeFloat(" + name + ")");
        } else if (type.typeName().equals("double")) {
            p.p(streamName + ".writeDouble(" + name + ")");
        } else {
            throw new AssertionError(type);
        }
    }
    private static void writeMarshalArguments(IndentingWriter p,
                                              String streamName,
                                              Type[] types, String[] names)
        throws IOException
    {
        assert types.length == names.length;
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
        if (type.dimension().length() > 0 || type.asClassDoc() != null) {
            p.p("(" + type.toString() + ") " + streamName + ".readObject()");
            readObject = true;
        } else if (type.typeName().equals("boolean")) {
            p.p(streamName + ".readBoolean()");
        } else if (type.typeName().equals("byte")) {
            p.p(streamName + ".readByte()");
        } else if (type.typeName().equals("char")) {
            p.p(streamName + ".readChar()");
        } else if (type.typeName().equals("short")) {
            p.p(streamName + ".readShort()");
        } else if (type.typeName().equals("int")) {
            p.p(streamName + ".readInt()");
        } else if (type.typeName().equals("long")) {
            p.p(streamName + ".readLong()");
        } else if (type.typeName().equals("float")) {
            p.p(streamName + ".readFloat()");
        } else if (type.typeName().equals("double")) {
            p.p(streamName + ".readDouble()");
        } else {
            throw new AssertionError(type);
        }
        return readObject;
    }
    private static boolean writeUnmarshalArguments(IndentingWriter p,
                                                   String streamName,
                                                   Type[] types,
                                                   String[] names)
        throws IOException
    {
        assert types.length == names.length;
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
        if (type.dimension().length() > 0 || type.asClassDoc() != null) {
            return name;
        } else if (type.typeName().equals("boolean")) {
            return ("(" + name +
                    " ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE)");
        } else if (type.typeName().equals("byte")) {
            return "new java.lang.Byte(" + name + ")";
        } else if (type.typeName().equals("char")) {
            return "new java.lang.Character(" + name + ")";
        } else if (type.typeName().equals("short")) {
            return "new java.lang.Short(" + name + ")";
        } else if (type.typeName().equals("int")) {
            return "new java.lang.Integer(" + name + ")";
        } else if (type.typeName().equals("long")) {
            return "new java.lang.Long(" + name + ")";
        } else if (type.typeName().equals("float")) {
            return "new java.lang.Float(" + name + ")";
        } else if (type.typeName().equals("double")) {
            return "new java.lang.Double(" + name + ")";
        } else {
            throw new AssertionError(type);
        }
    }
    private static String unwrapArgumentCode(Type type, String name) {
        if (type.dimension().length() > 0 || type.asClassDoc() != null) {
            return "((" + type.toString() + ") " + name + ")";
        } else if (type.typeName().equals("boolean")) {
            return "((java.lang.Boolean) " + name + ").booleanValue()";
        } else if (type.typeName().equals("byte")) {
            return "((java.lang.Byte) " + name + ").byteValue()";
        } else if (type.typeName().equals("char")) {
            return "((java.lang.Character) " + name + ").charValue()";
        } else if (type.typeName().equals("short")) {
            return "((java.lang.Short) " + name + ").shortValue()";
        } else if (type.typeName().equals("int")) {
            return "((java.lang.Integer) " + name + ").intValue()";
        } else if (type.typeName().equals("long")) {
            return "((java.lang.Long) " + name + ").longValue()";
        } else if (type.typeName().equals("float")) {
            return "((java.lang.Float) " + name + ").floatValue()";
        } else if (type.typeName().equals("double")) {
            return "((java.lang.Double) " + name + ").doubleValue()";
        } else {
            throw new AssertionError(type);
        }
    }
}

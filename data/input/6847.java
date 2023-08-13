class MethodAccessorGenerator extends AccessorGenerator {
    private static final short NUM_BASE_CPOOL_ENTRIES   = (short) 12;
    private static final short NUM_METHODS              = (short) 2;
    private static final short NUM_SERIALIZATION_CPOOL_ENTRIES = (short) 2;
    private static volatile int methodSymnum = 0;
    private static volatile int constructorSymnum = 0;
    private static volatile int serializationConstructorSymnum = 0;
    private Class   declaringClass;
    private Class[] parameterTypes;
    private Class   returnType;
    private boolean isConstructor;
    private boolean forSerialization;
    private short targetMethodRef;
    private short invokeIdx;
    private short invokeDescriptorIdx;
    private short nonPrimitiveParametersBaseIdx;
    MethodAccessorGenerator() {
    }
    public MethodAccessor generateMethod(Class declaringClass,
                                         String name,
                                         Class[] parameterTypes,
                                         Class   returnType,
                                         Class[] checkedExceptions,
                                         int modifiers)
    {
        return (MethodAccessor) generate(declaringClass,
                                         name,
                                         parameterTypes,
                                         returnType,
                                         checkedExceptions,
                                         modifiers,
                                         false,
                                         false,
                                         null);
    }
    public ConstructorAccessor generateConstructor(Class declaringClass,
                                                   Class[] parameterTypes,
                                                   Class[] checkedExceptions,
                                                   int modifiers)
    {
        return (ConstructorAccessor) generate(declaringClass,
                                              "<init>",
                                              parameterTypes,
                                              Void.TYPE,
                                              checkedExceptions,
                                              modifiers,
                                              true,
                                              false,
                                              null);
    }
    public SerializationConstructorAccessorImpl
    generateSerializationConstructor(Class declaringClass,
                                     Class[] parameterTypes,
                                     Class[] checkedExceptions,
                                     int modifiers,
                                     Class targetConstructorClass)
    {
        return (SerializationConstructorAccessorImpl)
            generate(declaringClass,
                     "<init>",
                     parameterTypes,
                     Void.TYPE,
                     checkedExceptions,
                     modifiers,
                     true,
                     true,
                     targetConstructorClass);
    }
    private MagicAccessorImpl generate(final Class declaringClass,
                                       String name,
                                       Class[] parameterTypes,
                                       Class   returnType,
                                       Class[] checkedExceptions,
                                       int modifiers,
                                       boolean isConstructor,
                                       boolean forSerialization,
                                       Class serializationTargetClass)
    {
        ByteVector vec = ByteVectorFactory.create();
        asm = new ClassFileAssembler(vec);
        this.declaringClass = declaringClass;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.modifiers = modifiers;
        this.isConstructor = isConstructor;
        this.forSerialization = forSerialization;
        asm.emitMagicAndVersion();
        short numCPEntries = NUM_BASE_CPOOL_ENTRIES + NUM_COMMON_CPOOL_ENTRIES;
        boolean usesPrimitives = usesPrimitiveTypes();
        if (usesPrimitives) {
            numCPEntries += NUM_BOXING_CPOOL_ENTRIES;
        }
        if (forSerialization) {
            numCPEntries += NUM_SERIALIZATION_CPOOL_ENTRIES;
        }
        numCPEntries += (short) (2 * numNonPrimitiveParameterTypes());
        asm.emitShort(add(numCPEntries, S1));
        final String generatedName = generateName(isConstructor, forSerialization);
        asm.emitConstantPoolUTF8(generatedName);
        asm.emitConstantPoolClass(asm.cpi());
        thisClass = asm.cpi();
        if (isConstructor) {
            if (forSerialization) {
                asm.emitConstantPoolUTF8
                    ("sun/reflect/SerializationConstructorAccessorImpl");
            } else {
                asm.emitConstantPoolUTF8("sun/reflect/ConstructorAccessorImpl");
            }
        } else {
            asm.emitConstantPoolUTF8("sun/reflect/MethodAccessorImpl");
        }
        asm.emitConstantPoolClass(asm.cpi());
        superClass = asm.cpi();
        asm.emitConstantPoolUTF8(getClassName(declaringClass, false));
        asm.emitConstantPoolClass(asm.cpi());
        targetClass = asm.cpi();
        short serializationTargetClassIdx = (short) 0;
        if (forSerialization) {
            asm.emitConstantPoolUTF8(getClassName(serializationTargetClass, false));
            asm.emitConstantPoolClass(asm.cpi());
            serializationTargetClassIdx = asm.cpi();
        }
        asm.emitConstantPoolUTF8(name);
        asm.emitConstantPoolUTF8(buildInternalSignature());
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        if (isInterface()) {
            asm.emitConstantPoolInterfaceMethodref(targetClass, asm.cpi());
        } else {
            if (forSerialization) {
                asm.emitConstantPoolMethodref(serializationTargetClassIdx, asm.cpi());
            } else {
                asm.emitConstantPoolMethodref(targetClass, asm.cpi());
            }
        }
        targetMethodRef = asm.cpi();
        if (isConstructor) {
            asm.emitConstantPoolUTF8("newInstance");
        } else {
            asm.emitConstantPoolUTF8("invoke");
        }
        invokeIdx = asm.cpi();
        if (isConstructor) {
            asm.emitConstantPoolUTF8("([Ljava/lang/Object;)Ljava/lang/Object;");
        } else {
            asm.emitConstantPoolUTF8
                ("(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        }
        invokeDescriptorIdx = asm.cpi();
        nonPrimitiveParametersBaseIdx = add(asm.cpi(), S2);
        for (int i = 0; i < parameterTypes.length; i++) {
            Class c = parameterTypes[i];
            if (!isPrimitive(c)) {
                asm.emitConstantPoolUTF8(getClassName(c, false));
                asm.emitConstantPoolClass(asm.cpi());
            }
        }
        emitCommonConstantPoolEntries();
        if (usesPrimitives) {
            emitBoxingContantPoolEntries();
        }
        if (asm.cpi() != numCPEntries) {
            throw new InternalError("Adjust this code (cpi = " + asm.cpi() +
                                    ", numCPEntries = " + numCPEntries + ")");
        }
        asm.emitShort(ACC_PUBLIC);
        asm.emitShort(thisClass);
        asm.emitShort(superClass);
        asm.emitShort(S0);
        asm.emitShort(S0);
        asm.emitShort(NUM_METHODS);
        emitConstructor();
        emitInvoke();
        asm.emitShort(S0);
        vec.trim();
        final byte[] bytes = vec.getData();
        return AccessController.doPrivileged(
            new PrivilegedAction<MagicAccessorImpl>() {
                public MagicAccessorImpl run() {
                        try {
                        return (MagicAccessorImpl)
                        ClassDefiner.defineClass
                                (generatedName,
                                 bytes,
                                 0,
                                 bytes.length,
                                 declaringClass.getClassLoader()).newInstance();
                        } catch (InstantiationException e) {
                            throw (InternalError)
                                new InternalError().initCause(e);
                        } catch (IllegalAccessException e) {
                            throw (InternalError)
                                new InternalError().initCause(e);
                        }
                    }
                });
    }
    private void emitInvoke() {
        if (parameterTypes.length > 65535) {
            throw new InternalError("Can't handle more than 65535 parameters");
        }
        ClassFileAssembler cb = new ClassFileAssembler();
        if (isConstructor) {
            cb.setMaxLocals(2);
        } else {
            cb.setMaxLocals(3);
        }
        short illegalArgStartPC = 0;
        if (isConstructor) {
            cb.opc_new(targetClass);
            cb.opc_dup();
        } else {
            if (isPrimitive(returnType)) {
                cb.opc_new(indexForPrimitiveType(returnType));
                cb.opc_dup();
            }
            if (!isStatic()) {
                cb.opc_aload_1();
                Label l = new Label();
                cb.opc_ifnonnull(l);
                cb.opc_new(nullPointerClass);
                cb.opc_dup();
                cb.opc_invokespecial(nullPointerCtorIdx, 0, 0);
                cb.opc_athrow();
                l.bind();
                illegalArgStartPC = cb.getLength();
                cb.opc_aload_1();
                cb.opc_checkcast(targetClass);
            }
        }
        Label successLabel = new Label();
        if (parameterTypes.length == 0) {
            if (isConstructor) {
                cb.opc_aload_1();
            } else {
                cb.opc_aload_2();
            }
            cb.opc_ifnull(successLabel);
        }
        if (isConstructor) {
            cb.opc_aload_1();
        } else {
            cb.opc_aload_2();
        }
        cb.opc_arraylength();
        cb.opc_sipush((short) parameterTypes.length);
        cb.opc_if_icmpeq(successLabel);
        cb.opc_new(illegalArgumentClass);
        cb.opc_dup();
        cb.opc_invokespecial(illegalArgumentCtorIdx, 0, 0);
        cb.opc_athrow();
        successLabel.bind();
        short paramTypeCPIdx = nonPrimitiveParametersBaseIdx;
        Label nextParamLabel = null;
        byte count = 1; 
        for (int i = 0; i < parameterTypes.length; i++) {
            Class paramType = parameterTypes[i];
            count += (byte) typeSizeInStackSlots(paramType);
            if (nextParamLabel != null) {
                nextParamLabel.bind();
                nextParamLabel = null;
            }
            if (isConstructor) {
                cb.opc_aload_1();
            } else {
                cb.opc_aload_2();
            }
            cb.opc_sipush((short) i);
            cb.opc_aaload();
            if (isPrimitive(paramType)) {
                if (isConstructor) {
                    cb.opc_astore_2();
                } else {
                    cb.opc_astore_3();
                }
                Label l = null; 
                nextParamLabel = new Label();
                for (int j = 0; j < primitiveTypes.length; j++) {
                    Class c = primitiveTypes[j];
                    if (canWidenTo(c, paramType)) {
                        if (l != null) {
                            l.bind();
                        }
                        if (isConstructor) {
                            cb.opc_aload_2();
                        } else {
                            cb.opc_aload_3();
                        }
                        cb.opc_instanceof(indexForPrimitiveType(c));
                        l = new Label();
                        cb.opc_ifeq(l);
                        if (isConstructor) {
                            cb.opc_aload_2();
                        } else {
                            cb.opc_aload_3();
                        }
                        cb.opc_checkcast(indexForPrimitiveType(c));
                        cb.opc_invokevirtual(unboxingMethodForPrimitiveType(c),
                                             0,
                                             typeSizeInStackSlots(c));
                        emitWideningBytecodeForPrimitiveConversion(cb,
                                                                   c,
                                                                   paramType);
                        cb.opc_goto(nextParamLabel);
                    }
                }
                if (l == null) {
                    throw new InternalError
                        ("Must have found at least identity conversion");
                }
                l.bind();
                cb.opc_new(illegalArgumentClass);
                cb.opc_dup();
                cb.opc_invokespecial(illegalArgumentCtorIdx, 0, 0);
                cb.opc_athrow();
            } else {
                cb.opc_checkcast(paramTypeCPIdx);
                paramTypeCPIdx = add(paramTypeCPIdx, S2);
            }
        }
        if (nextParamLabel != null) {
            nextParamLabel.bind();
        }
        short invokeStartPC = cb.getLength();
        if (isConstructor) {
            cb.opc_invokespecial(targetMethodRef, count, 0);
        } else {
            if (isStatic()) {
                cb.opc_invokestatic(targetMethodRef,
                                    count,
                                    typeSizeInStackSlots(returnType));
            } else {
                if (isInterface()) {
                    cb.opc_invokeinterface(targetMethodRef,
                                           count,
                                           count,
                                           typeSizeInStackSlots(returnType));
                } else {
                    cb.opc_invokevirtual(targetMethodRef,
                                         count,
                                         typeSizeInStackSlots(returnType));
                }
            }
        }
        short invokeEndPC = cb.getLength();
        if (!isConstructor) {
            if (isPrimitive(returnType)) {
                cb.opc_invokespecial(ctorIndexForPrimitiveType(returnType),
                                     typeSizeInStackSlots(returnType),
                                     0);
            } else if (returnType == Void.TYPE) {
                cb.opc_aconst_null();
            }
        }
        cb.opc_areturn();
        short classCastHandler = cb.getLength();
        cb.setStack(1);
        cb.opc_invokespecial(toStringIdx, 0, 1);
        cb.opc_new(illegalArgumentClass);
        cb.opc_dup_x1();
        cb.opc_swap();
        cb.opc_invokespecial(illegalArgumentStringCtorIdx, 1, 0);
        cb.opc_athrow();
        short invocationTargetHandler = cb.getLength();
        cb.setStack(1);
        cb.opc_new(invocationTargetClass);
        cb.opc_dup_x1();
        cb.opc_swap();
        cb.opc_invokespecial(invocationTargetCtorIdx, 1, 0);
        cb.opc_athrow();
        ClassFileAssembler exc = new ClassFileAssembler();
        exc.emitShort(illegalArgStartPC);       
        exc.emitShort(invokeStartPC);           
        exc.emitShort(classCastHandler);        
        exc.emitShort(classCastClass);          
        exc.emitShort(illegalArgStartPC);       
        exc.emitShort(invokeStartPC);           
        exc.emitShort(classCastHandler);        
        exc.emitShort(nullPointerClass);        
        exc.emitShort(invokeStartPC);           
        exc.emitShort(invokeEndPC);             
        exc.emitShort(invocationTargetHandler); 
        exc.emitShort(throwableClass);          
        emitMethod(invokeIdx, cb.getMaxLocals(), cb, exc,
                   new short[] { invocationTargetClass });
    }
    private boolean usesPrimitiveTypes() {
        if (returnType.isPrimitive()) {
            return true;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].isPrimitive()) {
                return true;
            }
        }
        return false;
    }
    private int numNonPrimitiveParameterTypes() {
        int num = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!parameterTypes[i].isPrimitive()) {
                ++num;
            }
        }
        return num;
    }
    private boolean isInterface() {
        return declaringClass.isInterface();
    }
    private String buildInternalSignature() {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            buf.append(getClassName(parameterTypes[i], true));
        }
        buf.append(")");
        buf.append(getClassName(returnType, true));
        return buf.toString();
    }
    private static synchronized String generateName(boolean isConstructor,
                                                    boolean forSerialization)
    {
        if (isConstructor) {
            if (forSerialization) {
                int num = ++serializationConstructorSymnum;
                return "sun/reflect/GeneratedSerializationConstructorAccessor" + num;
            } else {
                int num = ++constructorSymnum;
                return "sun/reflect/GeneratedConstructorAccessor" + num;
            }
        } else {
            int num = ++methodSymnum;
            return "sun/reflect/GeneratedMethodAccessor" + num;
        }
    }
}

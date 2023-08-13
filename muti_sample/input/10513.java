class AccessorGenerator implements ClassFileConstants {
    static final Unsafe unsafe = Unsafe.getUnsafe();
    protected static final short S0 = (short) 0;
    protected static final short S1 = (short) 1;
    protected static final short S2 = (short) 2;
    protected static final short S3 = (short) 3;
    protected static final short S4 = (short) 4;
    protected static final short S5 = (short) 5;
    protected static final short S6 = (short) 6;
    protected ClassFileAssembler asm;
    protected int   modifiers;
    protected short thisClass;
    protected short superClass;
    protected short targetClass;
    protected short throwableClass;
    protected short classCastClass;
    protected short nullPointerClass;
    protected short illegalArgumentClass;
    protected short invocationTargetClass;
    protected short initIdx;
    protected short initNameAndTypeIdx;
    protected short initStringNameAndTypeIdx;
    protected short nullPointerCtorIdx;
    protected short illegalArgumentCtorIdx;
    protected short illegalArgumentStringCtorIdx;
    protected short invocationTargetCtorIdx;
    protected short superCtorIdx;
    protected short objectClass;
    protected short toStringIdx;
    protected short codeIdx;
    protected short exceptionsIdx;
    protected short booleanIdx;
    protected short booleanCtorIdx;
    protected short booleanUnboxIdx;
    protected short byteIdx;
    protected short byteCtorIdx;
    protected short byteUnboxIdx;
    protected short characterIdx;
    protected short characterCtorIdx;
    protected short characterUnboxIdx;
    protected short doubleIdx;
    protected short doubleCtorIdx;
    protected short doubleUnboxIdx;
    protected short floatIdx;
    protected short floatCtorIdx;
    protected short floatUnboxIdx;
    protected short integerIdx;
    protected short integerCtorIdx;
    protected short integerUnboxIdx;
    protected short longIdx;
    protected short longCtorIdx;
    protected short longUnboxIdx;
    protected short shortIdx;
    protected short shortCtorIdx;
    protected short shortUnboxIdx;
    protected final short NUM_COMMON_CPOOL_ENTRIES = (short) 30;
    protected final short NUM_BOXING_CPOOL_ENTRIES = (short) 72;
    protected void emitCommonConstantPoolEntries() {
        asm.emitConstantPoolUTF8("java/lang/Throwable");
        asm.emitConstantPoolClass(asm.cpi());
        throwableClass = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/ClassCastException");
        asm.emitConstantPoolClass(asm.cpi());
        classCastClass = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/NullPointerException");
        asm.emitConstantPoolClass(asm.cpi());
        nullPointerClass = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/IllegalArgumentException");
        asm.emitConstantPoolClass(asm.cpi());
        illegalArgumentClass = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/reflect/InvocationTargetException");
        asm.emitConstantPoolClass(asm.cpi());
        invocationTargetClass = asm.cpi();
        asm.emitConstantPoolUTF8("<init>");
        initIdx = asm.cpi();
        asm.emitConstantPoolUTF8("()V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        initNameAndTypeIdx = asm.cpi();
        asm.emitConstantPoolMethodref(nullPointerClass, initNameAndTypeIdx);
        nullPointerCtorIdx = asm.cpi();
        asm.emitConstantPoolMethodref(illegalArgumentClass, initNameAndTypeIdx);
        illegalArgumentCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(Ljava/lang/String;)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        initStringNameAndTypeIdx = asm.cpi();
        asm.emitConstantPoolMethodref(illegalArgumentClass, initStringNameAndTypeIdx);
        illegalArgumentStringCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(Ljava/lang/Throwable;)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(invocationTargetClass, asm.cpi());
        invocationTargetCtorIdx = asm.cpi();
        asm.emitConstantPoolMethodref(superClass, initNameAndTypeIdx);
        superCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/Object");
        asm.emitConstantPoolClass(asm.cpi());
        objectClass = asm.cpi();
        asm.emitConstantPoolUTF8("toString");
        asm.emitConstantPoolUTF8("()Ljava/lang/String;");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(objectClass, asm.cpi());
        toStringIdx = asm.cpi();
        asm.emitConstantPoolUTF8("Code");
        codeIdx = asm.cpi();
        asm.emitConstantPoolUTF8("Exceptions");
        exceptionsIdx = asm.cpi();
    }
    protected void emitBoxingContantPoolEntries() {
        asm.emitConstantPoolUTF8("java/lang/Boolean");
        asm.emitConstantPoolClass(asm.cpi());
        booleanIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(Z)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S2), asm.cpi());
        booleanCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("booleanValue");
        asm.emitConstantPoolUTF8("()Z");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S6), asm.cpi());
        booleanUnboxIdx = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/Byte");
        asm.emitConstantPoolClass(asm.cpi());
        byteIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(B)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S2), asm.cpi());
        byteCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("byteValue");
        asm.emitConstantPoolUTF8("()B");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S6), asm.cpi());
        byteUnboxIdx = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/Character");
        asm.emitConstantPoolClass(asm.cpi());
        characterIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(C)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S2), asm.cpi());
        characterCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("charValue");
        asm.emitConstantPoolUTF8("()C");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S6), asm.cpi());
        characterUnboxIdx = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/Double");
        asm.emitConstantPoolClass(asm.cpi());
        doubleIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(D)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S2), asm.cpi());
        doubleCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("doubleValue");
        asm.emitConstantPoolUTF8("()D");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S6), asm.cpi());
        doubleUnboxIdx = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/Float");
        asm.emitConstantPoolClass(asm.cpi());
        floatIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(F)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S2), asm.cpi());
        floatCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("floatValue");
        asm.emitConstantPoolUTF8("()F");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S6), asm.cpi());
        floatUnboxIdx = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/Integer");
        asm.emitConstantPoolClass(asm.cpi());
        integerIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(I)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S2), asm.cpi());
        integerCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("intValue");
        asm.emitConstantPoolUTF8("()I");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S6), asm.cpi());
        integerUnboxIdx = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/Long");
        asm.emitConstantPoolClass(asm.cpi());
        longIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(J)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S2), asm.cpi());
        longCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("longValue");
        asm.emitConstantPoolUTF8("()J");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S6), asm.cpi());
        longUnboxIdx = asm.cpi();
        asm.emitConstantPoolUTF8("java/lang/Short");
        asm.emitConstantPoolClass(asm.cpi());
        shortIdx = asm.cpi();
        asm.emitConstantPoolUTF8("(S)V");
        asm.emitConstantPoolNameAndType(initIdx, asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S2), asm.cpi());
        shortCtorIdx = asm.cpi();
        asm.emitConstantPoolUTF8("shortValue");
        asm.emitConstantPoolUTF8("()S");
        asm.emitConstantPoolNameAndType(sub(asm.cpi(), S1), asm.cpi());
        asm.emitConstantPoolMethodref(sub(asm.cpi(), S6), asm.cpi());
        shortUnboxIdx = asm.cpi();
    }
    protected static short add(short s1, short s2) {
        return (short) (s1 + s2);
    }
    protected static short sub(short s1, short s2) {
        return (short) (s1 - s2);
    }
    protected boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
    protected static String getClassName
        (Class c, boolean addPrefixAndSuffixForNonPrimitiveTypes)
    {
        if (c.isPrimitive()) {
            if (c == Boolean.TYPE) {
                return "Z";
            } else if (c == Byte.TYPE) {
                return "B";
            } else if (c == Character.TYPE) {
                return "C";
            } else if (c == Double.TYPE) {
                return "D";
            } else if (c == Float.TYPE) {
                return "F";
            } else if (c == Integer.TYPE) {
                return "I";
            } else if (c == Long.TYPE) {
                return "J";
            } else if (c == Short.TYPE) {
                return "S";
            } else if (c == Void.TYPE) {
                return "V";
            }
            throw new InternalError("Should have found primitive type");
        } else if (c.isArray()) {
            return "[" + getClassName(c.getComponentType(), true);
        } else {
            if (addPrefixAndSuffixForNonPrimitiveTypes) {
                return internalize("L" + c.getName() + ";");
            } else {
                return internalize(c.getName());
            }
        }
    }
    private static String internalize(String className) {
        return className.replace('.', '/');
    }
    protected void emitConstructor() {
        ClassFileAssembler cb = new ClassFileAssembler();
        cb.setMaxLocals(1);
        cb.opc_aload_0();
        cb.opc_invokespecial(superCtorIdx, 0, 0);
        cb.opc_return();
        emitMethod(initIdx, cb.getMaxLocals(), cb, null, null);
    }
    protected void emitMethod(short nameIdx,
                              int numArgs,
                              ClassFileAssembler code,
                              ClassFileAssembler exceptionTable,
                              short[] checkedExceptionIndices)
    {
        int codeLen = code.getLength();
        int excLen  = 0;
        if (exceptionTable != null) {
            excLen = exceptionTable.getLength();
            if ((excLen % 8) != 0) {
                throw new IllegalArgumentException("Illegal exception table");
            }
        }
        int attrLen = 12 + codeLen + excLen;
        excLen = excLen / 8; 
        asm.emitShort(ACC_PUBLIC);
        asm.emitShort(nameIdx);
        asm.emitShort(add(nameIdx, S1));
        if (checkedExceptionIndices == null) {
            asm.emitShort(S1);
        } else {
            asm.emitShort(S2);
        }
        asm.emitShort(codeIdx);
        asm.emitInt(attrLen);
        asm.emitShort(code.getMaxStack());
        asm.emitShort((short) Math.max(numArgs, code.getMaxLocals()));
        asm.emitInt(codeLen);
        asm.append(code);
        asm.emitShort((short) excLen);
        if (exceptionTable != null) {
            asm.append(exceptionTable);
        }
        asm.emitShort(S0); 
        if (checkedExceptionIndices != null) {
            asm.emitShort(exceptionsIdx);
            asm.emitInt(2 + 2 * checkedExceptionIndices.length);
            asm.emitShort((short) checkedExceptionIndices.length);
            for (int i = 0; i < checkedExceptionIndices.length; i++) {
                asm.emitShort(checkedExceptionIndices[i]);
            }
        }
    }
    protected short indexForPrimitiveType(Class type) {
        if (type == Boolean.TYPE) {
            return booleanIdx;
        } else if (type == Byte.TYPE) {
            return byteIdx;
        } else if (type == Character.TYPE) {
            return characterIdx;
        } else if (type == Double.TYPE) {
            return doubleIdx;
        } else if (type == Float.TYPE) {
            return floatIdx;
        } else if (type == Integer.TYPE) {
            return integerIdx;
        } else if (type == Long.TYPE) {
            return longIdx;
        } else if (type == Short.TYPE) {
            return shortIdx;
        }
        throw new InternalError("Should have found primitive type");
    }
    protected short ctorIndexForPrimitiveType(Class type) {
        if (type == Boolean.TYPE) {
            return booleanCtorIdx;
        } else if (type == Byte.TYPE) {
            return byteCtorIdx;
        } else if (type == Character.TYPE) {
            return characterCtorIdx;
        } else if (type == Double.TYPE) {
            return doubleCtorIdx;
        } else if (type == Float.TYPE) {
            return floatCtorIdx;
        } else if (type == Integer.TYPE) {
            return integerCtorIdx;
        } else if (type == Long.TYPE) {
            return longCtorIdx;
        } else if (type == Short.TYPE) {
            return shortCtorIdx;
        }
        throw new InternalError("Should have found primitive type");
    }
    protected static boolean canWidenTo(Class type, Class otherType) {
        if (!type.isPrimitive()) {
            return false;
        }
        if (type == Boolean.TYPE) {
            if (otherType == Boolean.TYPE) {
                return true;
            }
        } else if (type == Byte.TYPE) {
            if (   otherType == Byte.TYPE
                   || otherType == Short.TYPE
                   || otherType == Integer.TYPE
                   || otherType == Long.TYPE
                   || otherType == Float.TYPE
                   || otherType == Double.TYPE) {
                return true;
            }
        } else if (type == Short.TYPE) {
            if (   otherType == Short.TYPE
                   || otherType == Integer.TYPE
                   || otherType == Long.TYPE
                   || otherType == Float.TYPE
                   || otherType == Double.TYPE) {
                return true;
            }
        } else if (type == Character.TYPE) {
            if (   otherType == Character.TYPE
                   || otherType == Integer.TYPE
                   || otherType == Long.TYPE
                   || otherType == Float.TYPE
                   || otherType == Double.TYPE) {
                return true;
            }
        } else if (type == Integer.TYPE) {
            if (   otherType == Integer.TYPE
                   || otherType == Long.TYPE
                   || otherType == Float.TYPE
                   || otherType == Double.TYPE) {
                return true;
            }
        } else if (type == Long.TYPE) {
            if (   otherType == Long.TYPE
                   || otherType == Float.TYPE
                   || otherType == Double.TYPE) {
                return true;
            }
        } else if (type == Float.TYPE) {
            if (   otherType == Float.TYPE
                   || otherType == Double.TYPE) {
                return true;
            }
        } else if (type == Double.TYPE) {
            if (otherType == Double.TYPE) {
                return true;
            }
        }
        return false;
    }
    protected static void emitWideningBytecodeForPrimitiveConversion
        (ClassFileAssembler cb,
         Class fromType,
         Class toType)
    {
        if (   fromType == Byte.TYPE
               || fromType == Short.TYPE
               || fromType == Character.TYPE
               || fromType == Integer.TYPE) {
            if (toType == Long.TYPE) {
                cb.opc_i2l();
            } else if (toType == Float.TYPE) {
                cb.opc_i2f();
            } else if (toType == Double.TYPE) {
                cb.opc_i2d();
            }
        } else if (fromType == Long.TYPE) {
            if (toType == Float.TYPE) {
                cb.opc_l2f();
            } else if (toType == Double.TYPE) {
                cb.opc_l2d();
            }
        } else if (fromType == Float.TYPE) {
            if (toType == Double.TYPE) {
                cb.opc_f2d();
            }
        }
    }
    protected short unboxingMethodForPrimitiveType(Class primType) {
        if (primType == Boolean.TYPE) {
            return booleanUnboxIdx;
        } else if (primType == Byte.TYPE) {
            return byteUnboxIdx;
        } else if (primType == Character.TYPE) {
            return characterUnboxIdx;
        } else if (primType == Short.TYPE) {
            return shortUnboxIdx;
        } else if (primType == Integer.TYPE) {
            return integerUnboxIdx;
        } else if (primType == Long.TYPE) {
            return longUnboxIdx;
        } else if (primType == Float.TYPE) {
            return floatUnboxIdx;
        } else if (primType == Double.TYPE) {
            return doubleUnboxIdx;
        }
        throw new InternalError("Illegal primitive type " + primType.getName());
    }
    protected static final Class[] primitiveTypes = new Class[] {
        Boolean.TYPE,
        Byte.TYPE,
        Character.TYPE,
        Short.TYPE,
        Integer.TYPE,
        Long.TYPE,
        Float.TYPE,
        Double.TYPE
    };
    protected static boolean isPrimitive(Class c) {
        return (c.isPrimitive() && c != Void.TYPE);
    }
    protected int typeSizeInStackSlots(Class c) {
        if (c == Void.TYPE) {
            return 0;
        }
        if (c == Long.TYPE || c == Double.TYPE) {
            return 2;
        }
        return 1;
    }
    private ClassFileAssembler illegalArgumentCodeBuffer;
    protected ClassFileAssembler illegalArgumentCodeBuffer() {
        if (illegalArgumentCodeBuffer == null) {
            illegalArgumentCodeBuffer = new ClassFileAssembler();
            illegalArgumentCodeBuffer.opc_new(illegalArgumentClass);
            illegalArgumentCodeBuffer.opc_dup();
            illegalArgumentCodeBuffer.opc_invokespecial(illegalArgumentCtorIdx, 0, 0);
            illegalArgumentCodeBuffer.opc_athrow();
        }
        return illegalArgumentCodeBuffer;
    }
}

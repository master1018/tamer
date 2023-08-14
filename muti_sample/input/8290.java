class AdapterMethodHandle extends BoundMethodHandle {
    private final int conversion;  
    private AdapterMethodHandle(MethodHandle target, MethodType newType,
                long conv, Object convArg) {
        super(newType, convArg, newType.parameterSlotDepth(1+convArgPos(conv)));
        this.conversion = convCode(conv);
        MethodHandleNatives.init(this, target, convArgPos(conv));
    }
    private AdapterMethodHandle(MethodHandle target, MethodType newType,
                long conv) {
        this(target, newType, conv, null);
    }
    int getConversion() { return conversion; }
    static boolean canPairwiseConvert(MethodType newType, MethodType oldType, int level) {
        int len = newType.parameterCount();
        if (len != oldType.parameterCount())
            return false;
        Class<?> exp = newType.returnType();
        Class<?> ret = oldType.returnType();
        if (!VerifyType.isNullConversion(ret, exp)) {
            if (!convOpSupported(OP_COLLECT_ARGS))
                return false;
            if (!canConvertArgument(ret, exp, level))
                return false;
        }
        for (int i = 0; i < len; i++) {
            Class<?> src = newType.parameterType(i); 
            Class<?> dst = oldType.parameterType(i); 
            if (!canConvertArgument(src, dst, level))
                return false;
        }
        return true;
    }
    static boolean canConvertArgument(Class<?> src, Class<?> dst, int level) {
        if (VerifyType.isNullConversion(src, dst)) {
            return true;
        } else if (convOpSupported(OP_COLLECT_ARGS)) {
            return true;
        } else if (src.isPrimitive()) {
            if (dst.isPrimitive())
                return canPrimCast(src, dst);
            else
                return canBoxArgument(src, dst);
        } else {
            if (dst.isPrimitive())
                return canUnboxArgument(src, dst, level);
            else
                return true;  
        }
    }
    static MethodHandle makePairwiseConvert(MethodType newType, MethodHandle target, int level) {
        MethodType oldType = target.type();
        if (newType == oldType)  return target;
        if (!canPairwiseConvert(newType, oldType, level))
            return null;
        int lastConv = newType.parameterCount()-1;
        while (lastConv >= 0) {
            Class<?> src = newType.parameterType(lastConv); 
            Class<?> dst = oldType.parameterType(lastConv); 
            if (isTrivialConversion(src, dst, level)) {
                --lastConv;
            } else {
                break;
            }
        }
        Class<?> needReturn = newType.returnType();
        Class<?> haveReturn = oldType.returnType();
        boolean retConv = !isTrivialConversion(haveReturn, needReturn, level);
        MethodHandle adapter = target, adapter2;
        MethodType midType = oldType;
        for (int i = 0; i <= lastConv; i++) {
            Class<?> src = newType.parameterType(i); 
            Class<?> dst = midType.parameterType(i); 
            if (isTrivialConversion(src, dst, level)) {
                continue;
            }
            midType = midType.changeParameterType(i, src);
            if (i == lastConv) {
                MethodType lastMidType = newType;
                if (retConv)  lastMidType = lastMidType.changeReturnType(haveReturn);
                assert(VerifyType.isNullConversion(lastMidType, midType));
                midType = lastMidType;
            }
            if (src.isPrimitive()) {
                if (dst.isPrimitive()) {
                    adapter2 = makePrimCast(midType, adapter, i, dst);
                } else {
                    adapter2 = makeBoxArgument(midType, adapter, i, src);
                }
            } else {
                if (dst.isPrimitive()) {
                    adapter2 = makeUnboxArgument(midType, adapter, i, dst, level);
                } else {
                    adapter2 = makeCheckCast(midType, adapter, i, dst);
                }
            }
            assert(adapter2 != null) : Arrays.asList(src, dst, midType, adapter, i, target, newType);
            assert(adapter2.type() == midType);
            adapter = adapter2;
        }
        if (retConv) {
            adapter2 = makeReturnConversion(adapter, haveReturn, needReturn);
            assert(adapter2 != null);
            adapter = adapter2;
        }
        if (adapter.type() != newType) {
            adapter2 = makeRetypeOnly(newType, adapter);
            assert(adapter2 != null);
            adapter = adapter2;
            assert(lastConv == -1 || retConv);
        }
        assert(adapter.type() == newType);
        return adapter;
    }
    private static boolean isTrivialConversion(Class<?> src, Class<?> dst, int level) {
        if (src == dst || dst == void.class)  return true;
        if (!VerifyType.isNullConversion(src, dst))  return false;
        if (level > 1)  return true;  
        boolean sp = src.isPrimitive();
        boolean dp = dst.isPrimitive();
        if (sp != dp)  return false;
        if (sp) {
            return Wrapper.forPrimitiveType(dst)
                    .isConvertibleFrom(Wrapper.forPrimitiveType(src));
        } else {
            return dst.isAssignableFrom(src);
        }
    }
    private static MethodHandle makeReturnConversion(MethodHandle target, Class<?> haveReturn, Class<?> needReturn) {
        MethodHandle adjustReturn;
        if (haveReturn == void.class) {
            Object zero = Wrapper.forBasicType(needReturn).zero();
            adjustReturn = MethodHandles.constant(needReturn, zero);
        } else {
            MethodType needConversion = MethodType.methodType(needReturn, haveReturn);
            adjustReturn = MethodHandles.identity(needReturn).asType(needConversion);
        }
        if (!canCollectArguments(adjustReturn.type(), target.type(), 0, false)) {
            assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
            throw new InternalError("NYI");
        }
        return makeCollectArguments(adjustReturn, target, 0, false);
    }
    static MethodHandle makePermutation(MethodType newType, MethodHandle target,
                int[] argumentMap) {
        MethodType oldType = target.type();
        boolean nullPermutation = true;
        for (int i = 0; i < argumentMap.length; i++) {
            int pos = argumentMap[i];
            if (pos != i)
                nullPermutation = false;
            if (pos < 0 || pos >= newType.parameterCount()) {
                argumentMap = new int[0]; break;
            }
        }
        if (argumentMap.length != oldType.parameterCount())
            throw newIllegalArgumentException("bad permutation: "+Arrays.toString(argumentMap));
        if (nullPermutation) {
            MethodHandle res = makePairwiseConvert(newType, target, 0);
            if (res == null)
                throw newIllegalArgumentException("cannot convert pairwise: "+newType);
            return res;
        }
        Class<?> exp = newType.returnType();
        Class<?> ret = oldType.returnType();
        if (!VerifyType.isNullConversion(ret, exp))
            throw newIllegalArgumentException("bad return conversion for "+newType);
        for (int i = 0; i < argumentMap.length; i++) {
            int j = argumentMap[i];
            Class<?> src = newType.parameterType(j);
            Class<?> dst = oldType.parameterType(i);
            if (!VerifyType.isNullConversion(src, dst))
                throw newIllegalArgumentException("bad argument #"+j+" conversion for "+newType);
        }
        throw new UnsupportedOperationException("NYI");
    }
    private static byte basicType(Class<?> type) {
        if (type == null)  return T_VOID;
        switch (Wrapper.forBasicType(type)) {
            case BOOLEAN:  return T_BOOLEAN;
            case CHAR:     return T_CHAR;
            case FLOAT:    return T_FLOAT;
            case DOUBLE:   return T_DOUBLE;
            case BYTE:     return T_BYTE;
            case SHORT:    return T_SHORT;
            case INT:      return T_INT;
            case LONG:     return T_LONG;
            case OBJECT:   return T_OBJECT;
            case VOID:     return T_VOID;
        }
        return 99; 
    }
    private static int type2size(int type) {
        assert(type >= T_BOOLEAN && type <= T_OBJECT);
        return (type == T_LONG || type == T_DOUBLE) ? 2 : 1;
    }
    private static int type2size(Class<?> type) {
        return type2size(basicType(type));
    }
    private static long insertStackMove(int stackMove) {
        long spChange = stackMove * MethodHandleNatives.JVM_STACK_MOVE_UNIT;
        return (spChange & CONV_STACK_MOVE_MASK) << CONV_STACK_MOVE_SHIFT;
    }
    static int extractStackMove(int convOp) {
        int spChange = convOp >> CONV_STACK_MOVE_SHIFT;
        return spChange / MethodHandleNatives.JVM_STACK_MOVE_UNIT;
    }
    static int extractStackMove(MethodHandle target) {
        if (target instanceof AdapterMethodHandle) {
            AdapterMethodHandle amh = (AdapterMethodHandle) target;
            return extractStackMove(amh.getConversion());
        } else {
            return 0;
        }
    }
    private static long makeConv(int convOp, int argnum, int src, int dest) {
        assert(src  == (src  & CONV_TYPE_MASK));
        assert(dest == (dest & CONV_TYPE_MASK));
        assert(convOp >= OP_CHECK_CAST && convOp <= OP_PRIM_TO_REF || convOp == OP_COLLECT_ARGS);
        int stackMove = type2size(dest) - type2size(src);
        return ((long) argnum << 32 |
                (long) convOp << CONV_OP_SHIFT |
                (int)  src    << CONV_SRC_TYPE_SHIFT |
                (int)  dest   << CONV_DEST_TYPE_SHIFT |
                insertStackMove(stackMove)
                );
    }
    private static long makeDupConv(int convOp, int argnum, int stackMove) {
        assert(convOp == OP_DUP_ARGS || convOp == OP_DROP_ARGS);
        byte src = 0, dest = 0;
        return ((long) argnum << 32 |
                (long) convOp << CONV_OP_SHIFT |
                (int)  src    << CONV_SRC_TYPE_SHIFT |
                (int)  dest   << CONV_DEST_TYPE_SHIFT |
                insertStackMove(stackMove)
                );
    }
    private static long makeSwapConv(int convOp, int srcArg, byte srcType, int destSlot, byte destType) {
        assert(convOp == OP_SWAP_ARGS || convOp == OP_ROT_ARGS);
        return ((long) srcArg << 32 |
                (long) convOp << CONV_OP_SHIFT |
                (int)  srcType << CONV_SRC_TYPE_SHIFT |
                (int)  destType << CONV_DEST_TYPE_SHIFT |
                (int)  destSlot << CONV_VMINFO_SHIFT
                );
    }
    private static long makeSpreadConv(int convOp, int argnum, int src, int dest, int stackMove) {
        assert(convOp == OP_SPREAD_ARGS || convOp == OP_COLLECT_ARGS || convOp == OP_FOLD_ARGS);
        return ((long) argnum << 32 |
                (long) convOp << CONV_OP_SHIFT |
                (int)  src    << CONV_SRC_TYPE_SHIFT |
                (int)  dest   << CONV_DEST_TYPE_SHIFT |
                insertStackMove(stackMove)
                );
    }
    private static long makeConv(int convOp) {
        assert(convOp == OP_RETYPE_ONLY || convOp == OP_RETYPE_RAW);
        return ((long)-1 << 32) | (convOp << CONV_OP_SHIFT);   
    }
    private static int convCode(long conv) {
        return (int)conv;
    }
    private static int convArgPos(long conv) {
        return (int)(conv >>> 32);
    }
    private static boolean convOpSupported(int convOp) {
        assert(convOp >= 0 && convOp <= CONV_OP_LIMIT);
        return ((1<<convOp) & MethodHandleNatives.CONV_OP_IMPLEMENTED_MASK) != 0;
    }
    int conversionOp() { return (conversion & CONV_OP_MASK) >> CONV_OP_SHIFT; }
    private static int diffTypes(MethodType adapterType,
                                 MethodType targetType,
                                 boolean raw) {
        int diff;
        diff = diffReturnTypes(adapterType, targetType, raw);
        if (diff != 0)  return diff;
        int nargs = adapterType.parameterCount();
        if (nargs != targetType.parameterCount())
            return -1;
        diff = diffParamTypes(adapterType, 0, targetType, 0, nargs, raw);
        return diff;
    }
    private static int diffReturnTypes(MethodType adapterType,
                                       MethodType targetType,
                                       boolean raw) {
        Class<?> src = targetType.returnType();
        Class<?> dst = adapterType.returnType();
        if ((!raw
             ? VerifyType.canPassUnchecked(src, dst)
             : VerifyType.canPassRaw(src, dst)
             ) > 0)
            return 0;  
        if (raw && !src.isPrimitive() && !dst.isPrimitive())
            return 0;  
        return -1;  
    }
    private static int diffParamTypes(MethodType adapterType, int astart,
                                      MethodType targetType, int tstart,
                                      int nargs, boolean raw) {
        assert(nargs >= 0);
        int res = 0;
        for (int i = 0; i < nargs; i++) {
            Class<?> src  = adapterType.parameterType(astart+i);
            Class<?> dest = targetType.parameterType(tstart+i);
            if ((!raw
                 ? VerifyType.canPassUnchecked(src, dest)
                 : VerifyType.canPassRaw(src, dest)
                ) <= 0) {
                if (res != 0)
                    return -1-res; 
                res = 1+i;
            }
        }
        return res;
    }
    static boolean canRetypeOnly(MethodType newType, MethodType targetType) {
        return canRetype(newType, targetType, false);
    }
    static boolean canRetypeRaw(MethodType newType, MethodType targetType) {
        return canRetype(newType, targetType, true);
    }
    static boolean canRetype(MethodType newType, MethodType targetType, boolean raw) {
        if (!convOpSupported(raw ? OP_RETYPE_RAW : OP_RETYPE_ONLY))  return false;
        int diff = diffTypes(newType, targetType, raw);
        assert(raw || (diff == 0) == VerifyType.isNullConversion(newType, targetType));
        return diff == 0;
    }
    static MethodHandle makeRetypeOnly(MethodType newType, MethodHandle target) {
        return makeRetype(newType, target, false);
    }
    static MethodHandle makeRetypeRaw(MethodType newType, MethodHandle target) {
        return makeRetype(newType, target, true);
    }
    static MethodHandle makeRetype(MethodType newType, MethodHandle target, boolean raw) {
        MethodType oldType = target.type();
        if (oldType == newType)  return target;
        if (!canRetype(newType, oldType, raw))
            return null;
        return new AdapterMethodHandle(target, newType, makeConv(raw ? OP_RETYPE_RAW : OP_RETYPE_ONLY));
    }
    static MethodHandle makeVarargsCollector(MethodHandle target, Class<?> arrayType) {
        MethodType type = target.type();
        int last = type.parameterCount() - 1;
        if (type.parameterType(last) != arrayType)
            target = target.asType(type.changeParameterType(last, arrayType));
        target = target.asFixedArity();  
        return new AsVarargsCollector(target, arrayType);
    }
    static class AsVarargsCollector extends AdapterMethodHandle {
        final MethodHandle target;
        final Class<?> arrayType;
        MethodHandle cache;
        AsVarargsCollector(MethodHandle target, Class<?> arrayType) {
            super(target, target.type(), makeConv(OP_RETYPE_ONLY));
            this.target = target;
            this.arrayType = arrayType;
            this.cache = target.asCollector(arrayType, 0);
        }
        @Override
        public boolean isVarargsCollector() {
            return true;
        }
        @Override
        public MethodHandle asFixedArity() {
            return target;
        }
        @Override
        public MethodHandle asType(MethodType newType) {
            MethodType type = this.type();
            int collectArg = type.parameterCount() - 1;
            int newArity = newType.parameterCount();
            if (newArity == collectArg+1 &&
                type.parameterType(collectArg).isAssignableFrom(newType.parameterType(collectArg))) {
                return super.asType(newType);
            }
            if (cache.type().parameterCount() == newArity)
                return cache.asType(newType);
            int arrayLength = newArity - collectArg;
            MethodHandle collector;
            try {
                collector = target.asCollector(arrayType, arrayLength);
            } catch (IllegalArgumentException ex) {
                throw new WrongMethodTypeException("cannot build collector");
            }
            cache = collector;
            return collector.asType(newType);
        }
    }
    static boolean canCheckCast(MethodType newType, MethodType targetType,
                int arg, Class<?> castType) {
        if (!convOpSupported(OP_CHECK_CAST))  return false;
        Class<?> src = newType.parameterType(arg);
        Class<?> dst = targetType.parameterType(arg);
        if (!canCheckCast(src, castType)
                || !VerifyType.isNullConversion(castType, dst))
            return false;
        int diff = diffTypes(newType, targetType, false);
        return (diff == arg+1) || (diff == 0);  
    }
    static boolean canCheckCast(Class<?> src, Class<?> dst) {
        return (!src.isPrimitive() && !dst.isPrimitive());
    }
    static MethodHandle makeCheckCast(MethodType newType, MethodHandle target,
                int arg, Class<?> castType) {
        if (!canCheckCast(newType, target.type(), arg, castType))
            return null;
        long conv = makeConv(OP_CHECK_CAST, arg, T_OBJECT, T_OBJECT);
        return new AdapterMethodHandle(target, newType, conv, castType);
    }
    static boolean canPrimCast(MethodType newType, MethodType targetType,
                int arg, Class<?> convType) {
        if (!convOpSupported(OP_PRIM_TO_PRIM))  return false;
        Class<?> src = newType.parameterType(arg);
        Class<?> dst = targetType.parameterType(arg);
        if (!canPrimCast(src, convType)
                || !VerifyType.isNullConversion(convType, dst))
            return false;
        int diff = diffTypes(newType, targetType, false);
        return (diff == arg+1);  
    }
    static boolean canPrimCast(Class<?> src, Class<?> dst) {
        if (src == dst || !src.isPrimitive() || !dst.isPrimitive()) {
            return false;
        } else {
            boolean sflt = Wrapper.forPrimitiveType(src).isFloating();
            boolean dflt = Wrapper.forPrimitiveType(dst).isFloating();
            return !(sflt | dflt);  
        }
    }
    static MethodHandle makePrimCast(MethodType newType, MethodHandle target,
                int arg, Class<?> convType) {
        Class<?> src = newType.parameterType(arg);
        if (canPrimCast(src, convType))
            return makePrimCastOnly(newType, target, arg, convType);
        Class<?> dst = convType;
        boolean sflt = Wrapper.forPrimitiveType(src).isFloating();
        boolean dflt = Wrapper.forPrimitiveType(dst).isFloating();
        if (sflt | dflt) {
            MethodHandle convMethod;
            if (sflt)
                convMethod = ((src == double.class)
                        ? ValueConversions.convertFromDouble(dst)
                        : ValueConversions.convertFromFloat(dst));
            else
                convMethod = ((dst == double.class)
                        ? ValueConversions.convertToDouble(src)
                        : ValueConversions.convertToFloat(src));
            long conv = makeConv(OP_COLLECT_ARGS, arg, basicType(src), basicType(dst));
            return new AdapterMethodHandle(target, newType, conv, convMethod);
        }
        throw new InternalError("makePrimCast");
    }
    static MethodHandle makePrimCastOnly(MethodType newType, MethodHandle target,
                int arg, Class<?> convType) {
        MethodType oldType = target.type();
        if (!canPrimCast(newType, oldType, arg, convType))
            return null;
        Class<?> src = newType.parameterType(arg);
        long conv = makeConv(OP_PRIM_TO_PRIM, arg, basicType(src), basicType(convType));
        return new AdapterMethodHandle(target, newType, conv);
    }
    static boolean canUnboxArgument(MethodType newType, MethodType targetType,
                int arg, Class<?> convType, int level) {
        if (!convOpSupported(OP_REF_TO_PRIM))  return false;
        Class<?> src = newType.parameterType(arg);
        Class<?> dst = targetType.parameterType(arg);
        Class<?> boxType = Wrapper.asWrapperType(convType);
        convType = Wrapper.asPrimitiveType(convType);
        if (!canCheckCast(src, boxType)
                || boxType == convType
                || !VerifyType.isNullConversion(convType, dst))
            return false;
        int diff = diffTypes(newType, targetType, false);
        return (diff == arg+1);  
    }
    static boolean canUnboxArgument(Class<?> src, Class<?> dst, int level) {
        assert(dst.isPrimitive());
        if (convOpSupported(OP_PRIM_TO_REF))  return true;
        Wrapper dw = Wrapper.forPrimitiveType(dst);
        if (level == 0)  return !src.isPrimitive();
        assert(level >= 0 && level <= 2);
        return dw.wrapperType() == src;
    }
    static MethodHandle makeUnboxArgument(MethodType newType, MethodHandle target,
                int arg, Class<?> convType, int level) {
        MethodType oldType = target.type();
        Class<?> src = newType.parameterType(arg);
        Class<?> dst = oldType.parameterType(arg);
        Class<?> boxType = Wrapper.asWrapperType(convType);
        Class<?> primType = Wrapper.asPrimitiveType(convType);
        if (!canUnboxArgument(newType, oldType, arg, convType, level))
            return null;
        MethodType castDone = newType;
        if (!VerifyType.isNullConversion(src, boxType)) {
            if (level != 0) {
                if (src == Object.class || !Wrapper.isWrapperType(src)) {
                    MethodHandle unboxMethod = (level == 1
                                                ? ValueConversions.unbox(dst)
                                                : ValueConversions.unboxCast(dst));
                    long conv = makeConv(OP_COLLECT_ARGS, arg, basicType(src), basicType(dst));
                    return new AdapterMethodHandle(target, newType, conv, unboxMethod);
                }
                Class<?> srcPrim = Wrapper.forWrapperType(src).primitiveType();
                MethodType midType = newType.changeParameterType(arg, srcPrim);
                MethodHandle fixPrim; 
                if (canPrimCast(midType, oldType, arg, dst))
                    fixPrim = makePrimCast(midType, target, arg, dst);
                else
                    fixPrim = target;
                return makeUnboxArgument(newType, fixPrim, arg, srcPrim, 0);
            }
            castDone = newType.changeParameterType(arg, boxType);
        }
        long conv = makeConv(OP_REF_TO_PRIM, arg, T_OBJECT, basicType(primType));
        MethodHandle adapter = new AdapterMethodHandle(target, castDone, conv, boxType);
        if (castDone == newType)
            return adapter;
        return makeCheckCast(newType, adapter, arg, boxType);
    }
    static boolean canBoxArgument(MethodType newType, MethodType targetType,
                int arg, Class<?> convType) {
        if (!convOpSupported(OP_PRIM_TO_REF))  return false;
        Class<?> src = newType.parameterType(arg);
        Class<?> dst = targetType.parameterType(arg);
        Class<?> boxType = Wrapper.asWrapperType(convType);
        convType = Wrapper.asPrimitiveType(convType);
        if (!canCheckCast(boxType, dst)
                || boxType == convType
                || !VerifyType.isNullConversion(src, convType))
            return false;
        int diff = diffTypes(newType, targetType, false);
        return (diff == arg+1);  
    }
    static boolean canBoxArgument(Class<?> src, Class<?> dst) {
        if (!convOpSupported(OP_PRIM_TO_REF))  return false;
        return (src.isPrimitive() && !dst.isPrimitive());
    }
    static MethodHandle makeBoxArgument(MethodType newType, MethodHandle target,
                int arg, Class<?> convType) {
        MethodType oldType = target.type();
        Class<?> src = newType.parameterType(arg);
        Class<?> dst = oldType.parameterType(arg);
        Class<?> boxType = Wrapper.asWrapperType(convType);
        Class<?> primType = Wrapper.asPrimitiveType(convType);
        if (!canBoxArgument(newType, oldType, arg, convType)) {
            return null;
        }
        if (!VerifyType.isNullConversion(boxType, dst))
            target = makeCheckCast(oldType.changeParameterType(arg, boxType), target, arg, dst);
        MethodHandle boxerMethod = ValueConversions.box(Wrapper.forPrimitiveType(primType));
        long conv = makeConv(OP_PRIM_TO_REF, arg, basicType(primType), T_OBJECT);
        return new AdapterMethodHandle(target, newType, conv, boxerMethod);
    }
    static boolean canDropArguments(MethodType newType, MethodType targetType,
                int dropArgPos, int dropArgCount) {
        if (dropArgCount == 0)
            return canRetypeOnly(newType, targetType);
        if (!convOpSupported(OP_DROP_ARGS))  return false;
        if (diffReturnTypes(newType, targetType, false) != 0)
            return false;
        int nptypes = newType.parameterCount();
        if (dropArgPos != 0 && diffParamTypes(newType, 0, targetType, 0, dropArgPos, false) != 0)
            return false;
        int afterPos = dropArgPos + dropArgCount;
        int afterCount = nptypes - afterPos;
        if (dropArgPos < 0 || dropArgPos >= nptypes ||
            dropArgCount < 1 || afterPos > nptypes ||
            targetType.parameterCount() != nptypes - dropArgCount)
            return false;
        if (afterCount != 0 && diffParamTypes(newType, afterPos, targetType, dropArgPos, afterCount, false) != 0)
            return false;
        return true;
    }
    static MethodHandle makeDropArguments(MethodType newType, MethodHandle target,
                int dropArgPos, int dropArgCount) {
        if (dropArgCount == 0)
            return makeRetypeOnly(newType, target);
        if (!canDropArguments(newType, target.type(), dropArgPos, dropArgCount))
            return null;
        int keep2InPos  = dropArgPos + dropArgCount;
        int dropSlot    = newType.parameterSlotDepth(keep2InPos);
        int keep1InSlot = newType.parameterSlotDepth(dropArgPos);
        int slotCount   = keep1InSlot - dropSlot;
        assert(slotCount >= dropArgCount);
        assert(target.type().parameterSlotCount() + slotCount == newType.parameterSlotCount());
        long conv = makeDupConv(OP_DROP_ARGS, dropArgPos + dropArgCount - 1, -slotCount);
        return new AdapterMethodHandle(target, newType, conv);
    }
    static boolean canDupArguments(MethodType newType, MethodType targetType,
                int dupArgPos, int dupArgCount) {
        if (!convOpSupported(OP_DUP_ARGS))  return false;
        if (diffReturnTypes(newType, targetType, false) != 0)
            return false;
        int nptypes = newType.parameterCount();
        if (dupArgCount < 0 || dupArgPos + dupArgCount > nptypes)
            return false;
        if (targetType.parameterCount() != nptypes + dupArgCount)
            return false;
        if (diffParamTypes(newType, 0, targetType, 0, nptypes, false) != 0)
            return false;
        if (diffParamTypes(newType, dupArgPos, targetType, nptypes, dupArgCount, false) != 0)
            return false;
        return true;
    }
    static MethodHandle makeDupArguments(MethodType newType, MethodHandle target,
                int dupArgPos, int dupArgCount) {
        if (!canDupArguments(newType, target.type(), dupArgPos, dupArgCount))
            return null;
        if (dupArgCount == 0)
            return target;
        int keep2InPos  = dupArgPos + dupArgCount;
        int dupSlot     = newType.parameterSlotDepth(keep2InPos);
        int keep1InSlot = newType.parameterSlotDepth(dupArgPos);
        int slotCount   = keep1InSlot - dupSlot;
        assert(target.type().parameterSlotCount() - slotCount == newType.parameterSlotCount());
        long conv = makeDupConv(OP_DUP_ARGS, dupArgPos + dupArgCount - 1, slotCount);
        return new AdapterMethodHandle(target, newType, conv);
    }
    static boolean canSwapArguments(MethodType newType, MethodType targetType,
                int swapArg1, int swapArg2) {
        if (!convOpSupported(OP_SWAP_ARGS))  return false;
        if (diffReturnTypes(newType, targetType, false) != 0)
            return false;
        if (swapArg1 >= swapArg2)  return false;  
        int nptypes = newType.parameterCount();
        if (targetType.parameterCount() != nptypes)
            return false;
        if (swapArg1 < 0 || swapArg2 >= nptypes)
            return false;
        if (diffParamTypes(newType, 0, targetType, 0, swapArg1, false) != 0)
            return false;
        if (diffParamTypes(newType, swapArg1, targetType, swapArg2, 1, false) != 0)
            return false;
        if (diffParamTypes(newType, swapArg1+1, targetType, swapArg1+1, swapArg2-swapArg1-1, false) != 0)
            return false;
        if (diffParamTypes(newType, swapArg2, targetType, swapArg1, 1, false) != 0)
            return false;
        if (diffParamTypes(newType, swapArg2+1, targetType, swapArg2+1, nptypes-swapArg2-1, false) != 0)
            return false;
        return true;
    }
    static MethodHandle makeSwapArguments(MethodType newType, MethodHandle target,
                int swapArg1, int swapArg2) {
        if (swapArg1 == swapArg2)
            return target;
        if (swapArg1 > swapArg2) { int t = swapArg1; swapArg1 = swapArg2; swapArg2 = t; }
        if (type2size(newType.parameterType(swapArg1)) !=
            type2size(newType.parameterType(swapArg2))) {
            int argc = swapArg2 - swapArg1 + 1;
            final int ROT = 1;
            ArrayList<Class<?>> rot1Params = new ArrayList<Class<?>>(target.type().parameterList());
            Collections.rotate(rot1Params.subList(swapArg1, swapArg1 + argc), -ROT);
            MethodType rot1Type = MethodType.methodType(target.type().returnType(), rot1Params);
            MethodHandle rot1 = makeRotateArguments(rot1Type, target, swapArg1, argc, +ROT);
            assert(rot1 != null);
            if (argc == 2)  return rot1;
            MethodHandle rot2 = makeRotateArguments(newType, rot1, swapArg1, argc-1, -ROT);
            assert(rot2 != null);
            return rot2;
        }
        if (!canSwapArguments(newType, target.type(), swapArg1, swapArg2))
            return null;
        Class<?> type1 = newType.parameterType(swapArg1);
        Class<?> type2 = newType.parameterType(swapArg2);
        int swapSlot2  = newType.parameterSlotDepth(swapArg2 + 1);
        long conv = makeSwapConv(OP_SWAP_ARGS, swapArg1, basicType(type1), swapSlot2, basicType(type2));
        return new AdapterMethodHandle(target, newType, conv);
    }
    static int positiveRotation(int argCount, int rotateBy) {
        assert(argCount > 0);
        if (rotateBy >= 0) {
            if (rotateBy < argCount)
                return rotateBy;
            return rotateBy % argCount;
        } else if (rotateBy >= -argCount) {
            return rotateBy + argCount;
        } else {
            return (-1-((-1-rotateBy) % argCount)) + argCount;
        }
    }
    final static int MAX_ARG_ROTATION = 1;
    static boolean canRotateArguments(MethodType newType, MethodType targetType,
                int firstArg, int argCount, int rotateBy) {
        if (!convOpSupported(OP_ROT_ARGS))  return false;
        rotateBy = positiveRotation(argCount, rotateBy);
        if (rotateBy == 0)  return false;  
        if (rotateBy > MAX_ARG_ROTATION && rotateBy < argCount - MAX_ARG_ROTATION)
            return false;  
        if (diffReturnTypes(newType, targetType, false) != 0)
            return false;
        int nptypes = newType.parameterCount();
        if (targetType.parameterCount() != nptypes)
            return false;
        if (firstArg < 0 || firstArg >= nptypes)  return false;
        int argLimit = firstArg + argCount;
        if (argLimit > nptypes)  return false;
        if (diffParamTypes(newType, 0, targetType, 0, firstArg, false) != 0)
            return false;
        int newChunk1 = argCount - rotateBy, newChunk2 = rotateBy;
        if (diffParamTypes(newType, firstArg, targetType, argLimit-newChunk1, newChunk1, false) != 0)
            return false;
        if (diffParamTypes(newType, firstArg+newChunk1, targetType, firstArg, newChunk2, false) != 0)
            return false;
        return true;
    }
    static MethodHandle makeRotateArguments(MethodType newType, MethodHandle target,
                int firstArg, int argCount, int rotateBy) {
        rotateBy = positiveRotation(argCount, rotateBy);
        if (!canRotateArguments(newType, target.type(), firstArg, argCount, rotateBy))
            return null;
        int limit = firstArg + argCount;
        int depth0 = newType.parameterSlotDepth(firstArg);
        int depth1 = newType.parameterSlotDepth(limit-rotateBy);
        int depth2 = newType.parameterSlotDepth(limit);
        int chunk1Slots = depth0 - depth1; assert(chunk1Slots > 0);
        int chunk2Slots = depth1 - depth2; assert(chunk2Slots > 0);
        assert(MAX_ARG_ROTATION == 1);
        int srcArg, dstArg;
        int dstSlot;
        int moveChunk;
        if (rotateBy == 1) {
            srcArg = limit-1;
            dstArg = firstArg;
            dstSlot = depth0 + MethodHandleNatives.OP_ROT_ARGS_DOWN_LIMIT_BIAS;
            moveChunk = chunk2Slots;
        } else {
            srcArg = firstArg;
            dstArg = limit-1;
            dstSlot = depth2;
            moveChunk = chunk1Slots;
        }
        byte srcType = basicType(newType.parameterType(srcArg));
        byte dstType = basicType(newType.parameterType(dstArg));
        assert(moveChunk == type2size(srcType));
        long conv = makeSwapConv(OP_ROT_ARGS, srcArg, srcType, dstSlot, dstType);
        return new AdapterMethodHandle(target, newType, conv);
    }
    static boolean canSpreadArguments(MethodType newType, MethodType targetType,
                Class<?> spreadArgType, int spreadArgPos, int spreadArgCount) {
        if (!convOpSupported(OP_SPREAD_ARGS))  return false;
        if (diffReturnTypes(newType, targetType, false) != 0)
            return false;
        int nptypes = newType.parameterCount();
        if (spreadArgPos != 0 && diffParamTypes(newType, 0, targetType, 0, spreadArgPos, false) != 0)
            return false;
        int afterPos = spreadArgPos + spreadArgCount;
        int afterCount = nptypes - (spreadArgPos + 1);
        if (spreadArgPos < 0 || spreadArgPos >= nptypes ||
            spreadArgCount < 0 ||
            targetType.parameterCount() != afterPos + afterCount)
            return false;
        if (afterCount != 0 && diffParamTypes(newType, spreadArgPos+1, targetType, afterPos, afterCount, false) != 0)
            return false;
        Class<?> rawSpreadArgType = newType.parameterType(spreadArgPos);
        if (rawSpreadArgType != spreadArgType && !canCheckCast(rawSpreadArgType, spreadArgType))
            return false;
        for (int i = 0; i < spreadArgCount; i++) {
            Class<?> src = VerifyType.spreadArgElementType(spreadArgType, i);
            Class<?> dst = targetType.parameterType(spreadArgPos + i);
            if (src == null || !canConvertArgument(src, dst, 1))
                return false;
        }
        return true;
    }
    static MethodHandle makeSpreadArguments(MethodType newType, MethodHandle target,
                Class<?> spreadArgType, int spreadArgPos, int spreadArgCount) {
        MethodType targetType = target.type();
        assert(canSpreadArguments(newType, targetType, spreadArgType, spreadArgPos, spreadArgCount))
            : "[newType, targetType, spreadArgType, spreadArgPos, spreadArgCount] = "
              + Arrays.asList(newType, targetType, spreadArgType, spreadArgPos, spreadArgCount);
        int dest = T_VOID;
        for (int i = 0; i < spreadArgCount; i++) {
            Class<?> arg = VerifyType.spreadArgElementType(spreadArgType, i);
            if (arg == null)  arg = Object.class;
            int dest2 = basicType(arg);
            if      (dest == T_VOID)  dest = dest2;
            else if (dest != dest2)   dest = T_VOID;
            if (dest == T_VOID)  break;
            targetType = targetType.changeParameterType(spreadArgPos + i, arg);
        }
        target = target.asType(targetType);
        int arrayArgSize = 1;  
        int keep2OutPos  = spreadArgPos + spreadArgCount;
        int keep1OutSlot = targetType.parameterSlotDepth(spreadArgPos);   
        int spreadSlot   = targetType.parameterSlotDepth(keep2OutPos);    
        assert(spreadSlot == newType.parameterSlotDepth(spreadArgPos+arrayArgSize));
        int slotCount    = keep1OutSlot - spreadSlot;                     
        assert(slotCount >= spreadArgCount);
        int stackMove = - arrayArgSize + slotCount;  
        long conv = makeSpreadConv(OP_SPREAD_ARGS, spreadArgPos, T_OBJECT, dest, stackMove);
        MethodHandle res = new AdapterMethodHandle(target, newType, conv, spreadArgType);
        assert(res.type().parameterType(spreadArgPos) == spreadArgType);
        return res;
    }
    static boolean canCollectArguments(MethodType targetType,
                MethodType collectorType, int collectArgPos, boolean retainOriginalArgs) {
        if (!convOpSupported(retainOriginalArgs ? OP_FOLD_ARGS : OP_COLLECT_ARGS))  return false;
        int collectArgCount = collectorType.parameterCount();
        Class<?> rtype = collectorType.returnType();
        assert(rtype == void.class || targetType.parameterType(collectArgPos) == rtype)
                : Arrays.asList(targetType, collectorType, collectArgPos, collectArgCount)
                ;
        return true;
    }
    static MethodHandle makeCollectArguments(MethodHandle target,
                MethodHandle collector, int collectArgPos, boolean retainOriginalArgs) {
        assert(canCollectArguments(target.type(), collector.type(), collectArgPos, retainOriginalArgs));
        MethodType targetType = target.type();
        MethodType collectorType = collector.type();
        int collectArgCount = collectorType.parameterCount();
        Class<?> collectValType = collectorType.returnType();
        int collectValCount = (collectValType == void.class ? 0 : 1);
        int collectValSlots = collectorType.returnSlotCount();
        MethodType newType = targetType
                .dropParameterTypes(collectArgPos, collectArgPos+collectValCount);
        if (!retainOriginalArgs) {
            newType = newType
                .insertParameterTypes(collectArgPos, collectorType.parameterList());
        } else {
            assert(diffParamTypes(newType, collectArgPos, targetType, collectValCount, collectArgCount, false) == 0)
                : Arrays.asList(target, collector, collectArgPos, retainOriginalArgs);
        }
        int keep2InPos   = collectArgPos + collectArgCount;
        int keep1InSlot  = newType.parameterSlotDepth(collectArgPos);  
        int collectSlot  = newType.parameterSlotDepth(keep2InPos);     
        int slotCount    = keep1InSlot - collectSlot;                  
        assert(slotCount >= collectArgCount);
        assert(collectSlot == targetType.parameterSlotDepth(
                collectArgPos + collectValCount + (retainOriginalArgs ? collectArgCount : 0) ));
        int dest = basicType(collectValType);
        int src = T_VOID;
        for (int i = 0; i < collectArgCount; i++) {
            int src2 = basicType(collectorType.parameterType(i));
            if      (src == T_VOID)  src = src2;
            else if (src != src2)    src = T_VOID;
            if (src == T_VOID)  break;
        }
        int stackMove = collectValSlots;  
        if (!retainOriginalArgs)  stackMove -= slotCount; 
        int lastCollectArg = keep2InPos-1;
        long conv = makeSpreadConv(retainOriginalArgs ? OP_FOLD_ARGS : OP_COLLECT_ARGS,
                                   lastCollectArg, src, dest, stackMove);
        MethodHandle res = new AdapterMethodHandle(target, newType, conv, collector);
        assert(res.type().parameterList().subList(collectArgPos, collectArgPos+collectArgCount)
                .equals(collector.type().parameterList()));
        return res;
    }
    @Override
    String debugString() {
        return getNameString(nonAdapter((MethodHandle)vmtarget), this);
    }
    private static MethodHandle nonAdapter(MethodHandle mh) {
        while (mh instanceof AdapterMethodHandle) {
            mh = (MethodHandle) mh.vmtarget;
        }
        return mh;
    }
}

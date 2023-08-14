public class CfTranslator {
    private static final boolean DEBUG = false;
    private CfTranslator() {
    }
    public static ClassDefItem translate(String filePath, byte[] bytes,
            CfOptions args) {
        try {
            return translate0(filePath, bytes, args);
        } catch (RuntimeException ex) {
            String msg = "...while processing " + filePath;
            throw ExceptionWithContext.withContext(ex, msg);
        }
    }
    private static ClassDefItem translate0(String filePath, byte[] bytes,
            CfOptions args) {
        DirectClassFile cf =
            new DirectClassFile(bytes, filePath, args.strictNameCheck);
        cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        cf.getMagic();
        OptimizerOptions.loadOptimizeLists(args.optimizeListFile,
                args.dontOptimizeListFile);
        CstType thisClass = cf.getThisClass();
        int classAccessFlags = cf.getAccessFlags() & ~AccessFlags.ACC_SUPER;
        CstUtf8 sourceFile = (args.positionInfo == PositionList.NONE) ? null :
            cf.getSourceFile();
        ClassDefItem out =
            new ClassDefItem(thisClass, classAccessFlags,
                    cf.getSuperclass(), cf.getInterfaces(), sourceFile);
        Annotations classAnnotations =
            AttributeTranslator.getClassAnnotations(cf, args);
        if (classAnnotations.size() != 0) {
            out.setClassAnnotations(classAnnotations);
        }
        processFields(cf, out);
        processMethods(cf, args, out);
        return out;
    }
    private static void processFields(DirectClassFile cf, ClassDefItem out) {
        CstType thisClass = cf.getThisClass();
        FieldList fields = cf.getFields();
        int sz = fields.size();
        for (int i = 0; i < sz; i++) {
            Field one = fields.get(i);
            try {
                CstFieldRef field = new CstFieldRef(thisClass, one.getNat());
                int accessFlags = one.getAccessFlags();
                if (AccessFlags.isStatic(accessFlags)) {
                    TypedConstant constVal = one.getConstantValue();
                    EncodedField fi = new EncodedField(field, accessFlags);
                    if (constVal != null) {
                        constVal = coerceConstant(constVal, field.getType());
                    }
                    out.addStaticField(fi, constVal);
                } else {
                    EncodedField fi = new EncodedField(field, accessFlags);
                    out.addInstanceField(fi);
                }
                Annotations annotations = 
                    AttributeTranslator.getAnnotations(one.getAttributes());
                if (annotations.size() != 0) {
                    out.addFieldAnnotations(field, annotations);
                }
            } catch (RuntimeException ex) {
                String msg = "...while processing " + one.getName().toHuman() +
                    " " + one.getDescriptor().toHuman();
                throw ExceptionWithContext.withContext(ex, msg);
            }
        }
    }
    private static TypedConstant coerceConstant(TypedConstant constant,
            Type type) {
        Type constantType = constant.getType();
        if (constantType.equals(type)) {
            return constant;
        }
        switch (type.getBasicType()) {
            case Type.BT_BOOLEAN: {
                return CstBoolean.make(((CstInteger) constant).getValue());
            }
            case Type.BT_BYTE: {
                return CstByte.make(((CstInteger) constant).getValue());
            }
            case Type.BT_CHAR: {
                return CstChar.make(((CstInteger) constant).getValue());
            }
            case Type.BT_SHORT: {
                return CstShort.make(((CstInteger) constant).getValue());
            }
            default: {
                throw new UnsupportedOperationException("can't coerce " +
                        constant + " to " + type);
            }
        }
    }
    private static void processMethods(DirectClassFile cf,
            CfOptions args, ClassDefItem out) {
        CstType thisClass = cf.getThisClass();
        MethodList methods = cf.getMethods();
        int sz = methods.size();
        for (int i = 0; i < sz; i++) {
            Method one = methods.get(i);
            try {
                CstMethodRef meth = new CstMethodRef(thisClass, one.getNat());
                int accessFlags = one.getAccessFlags();
                boolean isStatic = AccessFlags.isStatic(accessFlags);
                boolean isPrivate = AccessFlags.isPrivate(accessFlags);
                boolean isNative = AccessFlags.isNative(accessFlags);
                boolean isAbstract = AccessFlags.isAbstract(accessFlags);
                boolean isConstructor = meth.isInstanceInit() ||
                    meth.isClassInit();
                DalvCode code;
                if (isNative || isAbstract) {
                    code = null;
                } else {
                    ConcreteMethod concrete =
                        new ConcreteMethod(one, cf,
                                (args.positionInfo != PositionList.NONE),
                                args.localInfo);
                    TranslationAdvice advice;
                    advice = DexTranslationAdvice.THE_ONE;
                    RopMethod rmeth = Ropper.convert(concrete, advice);
                    RopMethod nonOptRmeth = null;
                    int paramSize;
                    paramSize = meth.getParameterWordCount(isStatic);
                    String canonicalName 
                            = thisClass.getClassType().getDescriptor()
                                + "." + one.getName().getString();
                    if (args.optimize &&
                            OptimizerOptions.shouldOptimize(canonicalName)) {
                        if (DEBUG) {
                            System.err.println("Optimizing " + canonicalName);
                        }
                        nonOptRmeth = rmeth;
                        rmeth = Optimizer.optimize(rmeth,
                                paramSize, isStatic, args.localInfo, advice);
                        if (DEBUG) {
                            OptimizerOptions.compareOptimizerStep(nonOptRmeth,
                                    paramSize, isStatic, args, advice, rmeth);
                        }
                        if (args.statistics) {
                            CodeStatistics.updateRopStatistics(
                                    nonOptRmeth, rmeth);
                        }
                    }
                    LocalVariableInfo locals = null;
                    if (args.localInfo) {
                        locals = LocalVariableExtractor.extract(rmeth);
                    }
                    code = RopTranslator.translate(rmeth, args.positionInfo,
                            locals, paramSize);
                    if (args.statistics && nonOptRmeth != null) {
                        updateDexStatistics(args, rmeth, nonOptRmeth, locals,
                                paramSize, concrete.getCode().size());
                    }
                }
                if (AccessFlags.isSynchronized(accessFlags)) {
                    accessFlags |= AccessFlags.ACC_DECLARED_SYNCHRONIZED;
                    if (!isNative) {
                        accessFlags &= ~AccessFlags.ACC_SYNCHRONIZED;
                    }
                }
                if (isConstructor) {
                    accessFlags |= AccessFlags.ACC_CONSTRUCTOR;
                }
                TypeList exceptions = AttributeTranslator.getExceptions(one);
                EncodedMethod mi =
                    new EncodedMethod(meth, accessFlags, code, exceptions);
                if (meth.isInstanceInit() || meth.isClassInit() ||
                    isStatic || isPrivate) {
                    out.addDirectMethod(mi);
                } else {
                    out.addVirtualMethod(mi);
                }
                Annotations annotations = 
                    AttributeTranslator.getMethodAnnotations(one);
                if (annotations.size() != 0) {
                    out.addMethodAnnotations(meth, annotations);
                }
                AnnotationsList list = 
                    AttributeTranslator.getParameterAnnotations(one);
                if (list.size() != 0) {
                    out.addParameterAnnotations(meth, list);
                }
            } catch (RuntimeException ex) {
                String msg = "...while processing " + one.getName().toHuman() +
                    " " + one.getDescriptor().toHuman();
                throw ExceptionWithContext.withContext(ex, msg);
            }
        }
    }
    private static void updateDexStatistics(CfOptions args,
            RopMethod optRmeth, RopMethod nonOptRmeth,
            LocalVariableInfo locals, int paramSize, int originalByteCount) {
        DalvCode optCode = RopTranslator.translate(optRmeth,
                args.positionInfo, locals, paramSize);
        DalvCode nonOptCode = RopTranslator.translate(nonOptRmeth,
                args.positionInfo, locals, paramSize);
        DalvCode.AssignIndicesCallback callback = 
            new DalvCode.AssignIndicesCallback() {
                public int getIndex(Constant cst) {
                    return 0;
                }
            };
        optCode.assignIndices(callback);
        nonOptCode.assignIndices(callback);
        CodeStatistics.updateDexStatistics(nonOptCode, optCode);
        CodeStatistics.updateOriginalByteCount(originalByteCount);
    }
}

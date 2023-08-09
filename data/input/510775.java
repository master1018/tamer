 final class RopperMachine extends ValueAwareMachine {
    private static final CstType ARRAY_REFLECT_TYPE =
        new CstType(Type.internClassName("java/lang/reflect/Array"));
    private static final CstMethodRef MULTIANEWARRAY_METHOD =
        new CstMethodRef(ARRAY_REFLECT_TYPE,
                         new CstNat(new CstUtf8("newInstance"),
                                    new CstUtf8("(Ljava/lang/Class;[I)" +
                                                "Ljava/lang/Object;")));
    private final Ropper ropper;
    private final ConcreteMethod method;
    private final TranslationAdvice advice;
    private final int maxLocals;
    private final ArrayList<Insn> insns;
    private TypeList catches;
    private boolean catchesUsed;
    private boolean returns;
    private int primarySuccessorIndex;
    private int extraBlockCount;
    private boolean hasJsr;
    private boolean blockCanThrow;
    private ReturnAddress returnAddress;
    private Rop returnOp;
    private SourcePosition returnPosition;
    public RopperMachine(Ropper ropper, ConcreteMethod method,
            TranslationAdvice advice) {
        super(method.getEffectiveDescriptor());
        if (ropper == null) {
            throw new NullPointerException("ropper == null");
        }
        if (advice == null) {
            throw new NullPointerException("advice == null");
        }
        this.ropper = ropper;
        this.method = method;
        this.advice = advice;
        this.maxLocals = method.getMaxLocals();
        this.insns = new ArrayList<Insn>(25);
        this.catches = null;
        this.catchesUsed = false;
        this.returns = false;
        this.primarySuccessorIndex = -1;
        this.extraBlockCount = 0;
        this.blockCanThrow = false;
        this.returnOp = null;
        this.returnPosition = null;
    }
    public ArrayList<Insn> getInsns() {
        return insns;
    }
    public Rop getReturnOp() {
        return returnOp;
    }
    public SourcePosition getReturnPosition() {
        return returnPosition;
    }
    public void startBlock(TypeList catches) {
        this.catches = catches;
        insns.clear();
        catchesUsed = false;
        returns = false;
        primarySuccessorIndex = 0;
        extraBlockCount = 0;
        blockCanThrow = false;
        hasJsr = false;
        returnAddress = null;
    }
    public boolean wereCatchesUsed() {
        return catchesUsed;
    }
    public boolean returns() {
        return returns;
    }
    public int getPrimarySuccessorIndex() {
        return primarySuccessorIndex;
    }
    public int getExtraBlockCount() {
        return extraBlockCount;
    }
    public boolean canThrow() {
        return blockCanThrow;
    }
    public boolean hasJsr() {
        return hasJsr;
    }
    public boolean hasRet() {
        return returnAddress != null;
    }
    public ReturnAddress getReturnAddress() {
        return returnAddress;
    }
    @Override
    public void run(Frame frame, int offset, int opcode) {
        int stackPointer = maxLocals + frame.getStack().size();
        RegisterSpecList sources = getSources(opcode, stackPointer);
        int sourceCount = sources.size();
        super.run(frame, offset, opcode);
        SourcePosition pos = method.makeSourcePosistion(offset);
        RegisterSpec localTarget = getLocalTarget();
        int destCount = resultCount();
        RegisterSpec dest;
        if (destCount == 0) {
            dest = null;
            switch (opcode) {
                case ByteOps.POP:
                case ByteOps.POP2: {
                    return;
                }
            }
        } else if (localTarget != null) {
            dest = localTarget;
        } else if (destCount == 1) {
            dest = RegisterSpec.make(stackPointer, result(0));
        } else {
            int scratchAt = ropper.getFirstTempStackReg();
            RegisterSpec[] scratchRegs = new RegisterSpec[sourceCount];
            for (int i = 0; i < sourceCount; i++) {
                RegisterSpec src = sources.get(i);
                TypeBearer type = src.getTypeBearer();
                RegisterSpec scratch = src.withReg(scratchAt);
                insns.add(new PlainInsn(Rops.opMove(type), pos, scratch, src));
                scratchRegs[i] = scratch;
                scratchAt += src.getCategory();
            }
            for (int pattern = getAuxInt(); pattern != 0; pattern >>= 4) {
                int which = (pattern & 0x0f) - 1;
                RegisterSpec scratch = scratchRegs[which];
                TypeBearer type = scratch.getTypeBearer();
                insns.add(new PlainInsn(Rops.opMove(type), pos,
                                        scratch.withReg(stackPointer),
                                        scratch));
                stackPointer += type.getType().getCategory();
            }
            return;
        }                
        TypeBearer destType = (dest != null) ? dest : Type.VOID;
        Constant cst = getAuxCst();
        int ropOpcode;
        Rop rop;
        Insn insn;
        if (opcode == ByteOps.MULTIANEWARRAY) {
            blockCanThrow = true;
            extraBlockCount = 6;
            RegisterSpec dimsReg = 
                RegisterSpec.make(dest.getNextReg(), Type.INT_ARRAY);
            rop = Rops.opFilledNewArray(Type.INT_ARRAY, sourceCount);
            insn = new ThrowingCstInsn(rop, pos, sources, catches,
                    CstType.INT_ARRAY);
            insns.add(insn);
            rop = Rops.opMoveResult(Type.INT_ARRAY);
            insn = new PlainInsn(rop, pos, dimsReg, RegisterSpecList.EMPTY);
            insns.add(insn);
            Type componentType = ((CstType) cst).getClassType();
            for (int i = 0; i < sourceCount; i++) {
                componentType = componentType.getComponentType();
            }
            RegisterSpec classReg =
                RegisterSpec.make(dest.getReg(), Type.CLASS);
            if (componentType.isPrimitive()) {
                CstFieldRef typeField =
                    CstFieldRef.forPrimitiveType(componentType);
                insn = new ThrowingCstInsn(Rops.GET_STATIC_OBJECT, pos,
                                           RegisterSpecList.EMPTY,
                                           catches, typeField);
            } else {
                insn = new ThrowingCstInsn(Rops.CONST_OBJECT, pos,
                                           RegisterSpecList.EMPTY, catches,
                                           new CstType(componentType));
            }
            insns.add(insn);
            rop = Rops.opMoveResultPseudo(classReg.getType());
            insn = new PlainInsn(rop, pos, classReg, RegisterSpecList.EMPTY);
            insns.add(insn);
            RegisterSpec objectReg =
                RegisterSpec.make(dest.getReg(), Type.OBJECT);
            insn = new ThrowingCstInsn(
                    Rops.opInvokeStatic(MULTIANEWARRAY_METHOD.getPrototype()),
                    pos, RegisterSpecList.make(classReg, dimsReg),
                    catches, MULTIANEWARRAY_METHOD);
            insns.add(insn);
            rop = Rops.opMoveResult(MULTIANEWARRAY_METHOD.getPrototype()
                    .getReturnType());
            insn = new PlainInsn(rop, pos, objectReg, RegisterSpecList.EMPTY);
            insns.add(insn);
            opcode = ByteOps.CHECKCAST;
            sources = RegisterSpecList.make(objectReg);
        } else if (opcode == ByteOps.JSR) {
            hasJsr = true;
            return;
        } else if (opcode == ByteOps.RET) {
            try {
                returnAddress = (ReturnAddress)arg(0);
            } catch (ClassCastException ex) {
                throw new RuntimeException(
                        "Argument to RET was not a ReturnAddress", ex);
            }
            return;
        }
        ropOpcode = jopToRopOpcode(opcode, cst);
        rop = Rops.ropFor(ropOpcode, destType, sources, cst);
        Insn moveResult = null;
        if (dest != null && rop.isCallLike()) {
            extraBlockCount++;
            moveResult = new PlainInsn(
                    Rops.opMoveResult(((CstMethodRef) cst).getPrototype()
                    .getReturnType()), pos, dest, RegisterSpecList.EMPTY);
            dest = null;
        } else if (dest != null && rop.canThrow()) {
            extraBlockCount++;
            moveResult = new PlainInsn(
                    Rops.opMoveResultPseudo(dest.getTypeBearer()),
                    pos, dest, RegisterSpecList.EMPTY);
            dest = null;
        }
        if (ropOpcode == RegOps.NEW_ARRAY) {
            cst = CstType.intern(rop.getResult());
        } else if ((cst == null) && (sourceCount == 2)) {
            TypeBearer lastType = sources.get(1).getTypeBearer();
            if (lastType.isConstant()
                    && advice.hasConstantOperation(rop,
                    sources.get(0), sources.get(1))) {
                cst = (Constant) lastType;
                sources = sources.withoutLast();
                rop = Rops.ropFor(ropOpcode, destType, sources, cst);
            }
        }
        SwitchList cases = getAuxCases();
        ArrayList<Constant> initValues = getInitValues();
        boolean canThrow = rop.canThrow();
        blockCanThrow |= canThrow;
        if (cases != null) {
            if (cases.size() == 0) {
                insn = new PlainInsn(Rops.GOTO, pos, null,
                                     RegisterSpecList.EMPTY);
                primarySuccessorIndex = 0;
            } else {
                IntList values = cases.getValues();
                insn = new SwitchInsn(rop, pos, dest, sources, values);
                primarySuccessorIndex = values.size();
            }
        } else if (ropOpcode == RegOps.RETURN) {
            if (sources.size() != 0) {
                RegisterSpec source = sources.get(0);
                TypeBearer type = source.getTypeBearer();
                if (source.getReg() != 0) {
                    insns.add(new PlainInsn(Rops.opMove(type), pos,
                                            RegisterSpec.make(0, type),
                                            source));
                }
            }
            insn = new PlainInsn(Rops.GOTO, pos, null, RegisterSpecList.EMPTY);
            primarySuccessorIndex = 0;
            updateReturnOp(rop, pos);
            returns = true;
        } else if (cst != null) {
            if (canThrow) {
                insn =
                    new ThrowingCstInsn(rop, pos, sources, catches, cst);
                catchesUsed = true;
                primarySuccessorIndex = catches.size();
            } else {
                insn = new PlainCstInsn(rop, pos, dest, sources, cst);
            }
        } else if (canThrow) {
            insn = new ThrowingInsn(rop, pos, sources, catches);
            catchesUsed = true;
            if (opcode == ByteOps.ATHROW) {
                primarySuccessorIndex = -1;
            } else {
                primarySuccessorIndex = catches.size();
            }
        } else {
            insn = new PlainInsn(rop, pos, dest, sources);
        }
        insns.add(insn);
        if (moveResult != null) {
            insns.add(moveResult);
        }
        if (initValues != null) {
            extraBlockCount++;
            insn = new FillArrayDataInsn(Rops.FILL_ARRAY_DATA, pos,
                    RegisterSpecList.make(moveResult.getResult()), initValues,
                    cst);
            insns.add(insn);
        }
    }
    private RegisterSpecList getSources(int opcode, int stackPointer) {
        int count = argCount();
        if (count == 0) {
            return RegisterSpecList.EMPTY;
        } 
        int localIndex = getLocalIndex();
        RegisterSpecList sources;
        if (localIndex >= 0) {
            sources = new RegisterSpecList(1);
            sources.set(0, RegisterSpec.make(localIndex, arg(0)));
        } else {
            sources = new RegisterSpecList(count);
            int regAt = stackPointer;
            for (int i = 0; i < count; i++) {
                RegisterSpec spec = RegisterSpec.make(regAt, arg(i));
                sources.set(i, spec);
                regAt += spec.getCategory();
            }
            switch (opcode) {
                case ByteOps.IASTORE: {
                    if (count != 3) {
                        throw new RuntimeException("shouldn't happen");
                    }
                    RegisterSpec array = sources.get(0);
                    RegisterSpec index = sources.get(1);
                    RegisterSpec value = sources.get(2);
                    sources.set(0, value);
                    sources.set(1, array);
                    sources.set(2, index);
                    break;
                }
                case ByteOps.PUTFIELD: {
                    if (count != 2) {
                        throw new RuntimeException("shouldn't happen");
                    }
                    RegisterSpec obj = sources.get(0);
                    RegisterSpec value = sources.get(1);
                    sources.set(0, value);
                    sources.set(1, obj);
                    break;
                }
            }
        }
        sources.setImmutable();
        return sources;
    }
    private void updateReturnOp(Rop op, SourcePosition pos) {
        if (op == null) {
            throw new NullPointerException("op == null");
        }
        if (pos == null) {
            throw new NullPointerException("pos == null");
        }
        if (returnOp == null) {
            returnOp = op;
            returnPosition = pos;
        } else {
            if (returnOp != op) {
                throw new SimException("return op mismatch: " + op + ", " +
                                       returnOp);
            }
            if (pos.getLine() > returnPosition.getLine()) {
                returnPosition = pos;
            }
        }
    }
    private int jopToRopOpcode(int jop, Constant cst) {
        switch (jop) {
            case ByteOps.POP:
            case ByteOps.POP2:
            case ByteOps.DUP:
            case ByteOps.DUP_X1:
            case ByteOps.DUP_X2:
            case ByteOps.DUP2:
            case ByteOps.DUP2_X1:
            case ByteOps.DUP2_X2:
            case ByteOps.SWAP:
            case ByteOps.JSR:
            case ByteOps.RET:
            case ByteOps.MULTIANEWARRAY: {
                break;
            }
            case ByteOps.NOP: {
                return RegOps.NOP;
            }
            case ByteOps.LDC:
            case ByteOps.LDC2_W: {
                return RegOps.CONST;
            }
            case ByteOps.ILOAD: 
            case ByteOps.ISTORE: {
                return RegOps.MOVE;
            }
            case ByteOps.IALOAD: {
                return RegOps.AGET;
            }
            case ByteOps.IASTORE: {
                return RegOps.APUT;
            }
            case ByteOps.IADD:
            case ByteOps.IINC: {
                return RegOps.ADD;
            }
            case ByteOps.ISUB: {
                return RegOps.SUB;
            }
            case ByteOps.IMUL: {
                return RegOps.MUL;
            }
            case ByteOps.IDIV: {
                return RegOps.DIV;
            }
            case ByteOps.IREM: {
                return RegOps.REM;
            }
            case ByteOps.INEG: {
                return RegOps.NEG;
            }
            case ByteOps.ISHL: {
                return RegOps.SHL;
            }
            case ByteOps.ISHR: {
                return RegOps.SHR;
            }
            case ByteOps.IUSHR: {
                return RegOps.USHR;
            }
            case ByteOps.IAND: {
                return RegOps.AND;
            }
            case ByteOps.IOR: {
                return RegOps.OR;
            }
            case ByteOps.IXOR: {
                return RegOps.XOR;
            }
            case ByteOps.I2L:
            case ByteOps.I2F:
            case ByteOps.I2D:
            case ByteOps.L2I:
            case ByteOps.L2F:
            case ByteOps.L2D:
            case ByteOps.F2I:
            case ByteOps.F2L:
            case ByteOps.F2D:
            case ByteOps.D2I:
            case ByteOps.D2L:
            case ByteOps.D2F: {
                return RegOps.CONV;
            }
            case ByteOps.I2B: {
                return RegOps.TO_BYTE;
            }
            case ByteOps.I2C: {
                return RegOps.TO_CHAR;
            }
            case ByteOps.I2S: {
                return RegOps.TO_SHORT;
            }
            case ByteOps.LCMP:
            case ByteOps.FCMPL:
            case ByteOps.DCMPL: {
                return RegOps.CMPL;
            }
            case ByteOps.FCMPG:
            case ByteOps.DCMPG: {
                return RegOps.CMPG;
            }
            case ByteOps.IFEQ:
            case ByteOps.IF_ICMPEQ:
            case ByteOps.IF_ACMPEQ:
            case ByteOps.IFNULL: {
                return RegOps.IF_EQ;
            }
            case ByteOps.IFNE:
            case ByteOps.IF_ICMPNE:
            case ByteOps.IF_ACMPNE:
            case ByteOps.IFNONNULL: {
                return RegOps.IF_NE;
            }
            case ByteOps.IFLT:
            case ByteOps.IF_ICMPLT: {
                return RegOps.IF_LT;
            }
            case ByteOps.IFGE:
            case ByteOps.IF_ICMPGE: {
                return RegOps.IF_GE;
            }
            case ByteOps.IFGT:
            case ByteOps.IF_ICMPGT: {
                return RegOps.IF_GT;
            }
            case ByteOps.IFLE:
            case ByteOps.IF_ICMPLE: {
                return RegOps.IF_LE;
            }
            case ByteOps.GOTO: {
                return RegOps.GOTO;
            }
            case ByteOps.LOOKUPSWITCH: {
                return RegOps.SWITCH;
            }
            case ByteOps.IRETURN:
            case ByteOps.RETURN: {
                return RegOps.RETURN;
            }
            case ByteOps.GETSTATIC: {
                return RegOps.GET_STATIC;
            }
            case ByteOps.PUTSTATIC: {
                return RegOps.PUT_STATIC;
            }
            case ByteOps.GETFIELD: {
                return RegOps.GET_FIELD;
            }
            case ByteOps.PUTFIELD: {
                return RegOps.PUT_FIELD;
            }
            case ByteOps.INVOKEVIRTUAL: {
                return RegOps.INVOKE_VIRTUAL;
            }
            case ByteOps.INVOKESPECIAL: {
                CstMethodRef ref = (CstMethodRef) cst;
                if (ref.isInstanceInit() ||
                    (ref.getDefiningClass() == method.getDefiningClass()) ||
                    !method.getAccSuper()) {
                    return RegOps.INVOKE_DIRECT;
                }
                return RegOps.INVOKE_SUPER;
            }
            case ByteOps.INVOKESTATIC: {
                return RegOps.INVOKE_STATIC;
            }
            case ByteOps.INVOKEINTERFACE: {
                return RegOps.INVOKE_INTERFACE;
            }
            case ByteOps.NEW: {
                return RegOps.NEW_INSTANCE;
            }
            case ByteOps.NEWARRAY:
            case ByteOps.ANEWARRAY: {
                return RegOps.NEW_ARRAY;
            }
            case ByteOps.ARRAYLENGTH: {
                return RegOps.ARRAY_LENGTH;
            }
            case ByteOps.ATHROW: {
                return RegOps.THROW;
            }
            case ByteOps.CHECKCAST: {
                return RegOps.CHECK_CAST;
            }
            case ByteOps.INSTANCEOF: {
                return RegOps.INSTANCE_OF;
            }
            case ByteOps.MONITORENTER: {
                return RegOps.MONITOR_ENTER;
            }
            case ByteOps.MONITOREXIT: {
                return RegOps.MONITOR_EXIT;
            }
        }
        throw new RuntimeException("shouldn't happen");
    }
}

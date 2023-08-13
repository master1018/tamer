public class Simulator {
    private static final String LOCAL_MISMATCH_ERROR =
        "This is symptomatic of .class transformation tools that ignore " +
        "local variable information.";
    private final Machine machine;
    private final BytecodeArray code;
    private final LocalVariableList localVariables;
    private final SimVisitor visitor;
    public Simulator(Machine machine, ConcreteMethod method) {
        if (machine == null) {
            throw new NullPointerException("machine == null");
        }
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        this.machine = machine;
        this.code = method.getCode();
        this.localVariables = method.getLocalVariables();
        this.visitor = new SimVisitor();
    }
    public void simulate(ByteBlock bb, Frame frame) {
        int end = bb.getEnd();
        visitor.setFrame(frame);
        try {
            for (int off = bb.getStart(); off < end; ) {
                int length = code.parseInstruction(off, visitor);
                visitor.setPreviousOffset(off);
                off += length;
            }
        } catch (SimException ex) {
            frame.annotate(ex);
            throw ex;
        }
    }
    public int simulate(int offset, Frame frame) {
        visitor.setFrame(frame);
        return code.parseInstruction(offset, visitor);
    }
    private static SimException illegalTos() {
        return new SimException("stack mismatch: illegal " +
                "top-of-stack for opcode");
    }
    private class SimVisitor implements BytecodeArray.Visitor {
        private final Machine machine;
        private Frame frame;
        private int previousOffset;
        public SimVisitor() {
            this.machine = Simulator.this.machine;
            this.frame = null;
        }
        public void setFrame(Frame frame) {
            if (frame == null) {
                throw new NullPointerException("frame == null");
            }
            this.frame = frame;
        }
        public void visitInvalid(int opcode, int offset, int length) {
            throw new SimException("invalid opcode " + Hex.u1(opcode));
        }
        public void visitNoArgs(int opcode, int offset, int length,
                Type type) {
            switch (opcode) {
                case ByteOps.NOP: {
                    machine.clearArgs();
                    break;
                }
                case ByteOps.INEG: {
                    machine.popArgs(frame, type);
                    break;
                }
                case ByteOps.I2L:
                case ByteOps.I2F:
                case ByteOps.I2D:
                case ByteOps.I2B:
                case ByteOps.I2C:
                case ByteOps.I2S: {
                    machine.popArgs(frame, Type.INT);
                    break;
                }
                case ByteOps.L2I:
                case ByteOps.L2F:
                case ByteOps.L2D: {
                    machine.popArgs(frame, Type.LONG);
                    break;
                }
                case ByteOps.F2I:
                case ByteOps.F2L:
                case ByteOps.F2D: {
                    machine.popArgs(frame, Type.FLOAT);
                    break;
                }
                case ByteOps.D2I:
                case ByteOps.D2L:
                case ByteOps.D2F: {
                    machine.popArgs(frame, Type.DOUBLE);
                    break;
                }
                case ByteOps.RETURN: {
                    machine.clearArgs();
                    checkReturnType(Type.VOID);
                    break;
                }
                case ByteOps.IRETURN: {
                    Type checkType = type;
                    if (type == Type.OBJECT) {
                        checkType = frame.getStack().peekType(0);
                    }
                    machine.popArgs(frame, type);
                    checkReturnType(checkType);
                    break;
                }
                case ByteOps.POP: {
                    Type peekType = frame.getStack().peekType(0);
                    if (peekType.isCategory2()) {
                        throw illegalTos();
                    }
                    machine.popArgs(frame, 1);
                    break;
                }
                case ByteOps.ARRAYLENGTH: {
                    Type arrayType = frame.getStack().peekType(0);
                    if (!arrayType.isArrayOrKnownNull()) {
                        throw new SimException("type mismatch: expected " +
                                "array type but encountered " +
                                arrayType.toHuman());
                    }
                    machine.popArgs(frame, Type.OBJECT);
                    break;
                }
                case ByteOps.ATHROW:
                case ByteOps.MONITORENTER:
                case ByteOps.MONITOREXIT: {
                    machine.popArgs(frame, Type.OBJECT);
                    break;
                }
                case ByteOps.IALOAD: {
                    Type foundArrayType = frame.getStack().peekType(1);
                    Type requireArrayType;
                    if (foundArrayType != Type.KNOWN_NULL) {
                        requireArrayType = foundArrayType;
                        type = foundArrayType.getComponentType();
                    } else {
                        requireArrayType = type.getArrayType();
                    }
                    machine.popArgs(frame, requireArrayType, Type.INT);
                    break;
                }
                case ByteOps.IADD:
                case ByteOps.ISUB:
                case ByteOps.IMUL:
                case ByteOps.IDIV:
                case ByteOps.IREM:
                case ByteOps.IAND:
                case ByteOps.IOR:
                case ByteOps.IXOR: {
                    machine.popArgs(frame, type, type);
                    break;
                }
                case ByteOps.ISHL:
                case ByteOps.ISHR:
                case ByteOps.IUSHR: {
                    machine.popArgs(frame, type, Type.INT);
                    break;
                }
                case ByteOps.LCMP: {
                    machine.popArgs(frame, Type.LONG, Type.LONG);
                    break;
                }
                case ByteOps.FCMPL:
                case ByteOps.FCMPG: {
                    machine.popArgs(frame, Type.FLOAT, Type.FLOAT);
                    break;
                }
                case ByteOps.DCMPL:
                case ByteOps.DCMPG: {
                    machine.popArgs(frame, Type.DOUBLE, Type.DOUBLE);
                    break;
                }
                case ByteOps.IASTORE: {
                    Type foundArrayType =
                        frame.getStack().peekType(type.isCategory1() ? 2 : 3);
                    Type requireArrayType;
                    if (foundArrayType != Type.KNOWN_NULL) {
                        requireArrayType = foundArrayType;
                        type = foundArrayType.getComponentType();
                    } else {
                        requireArrayType = type.getArrayType();
                    }
                    machine.popArgs(frame, requireArrayType, Type.INT, type);
                    break;
                }
                case ByteOps.POP2:
                case ByteOps.DUP2: {
                    ExecutionStack stack = frame.getStack();
                    int pattern;
                    if (stack.peekType(0).isCategory2()) {
                        machine.popArgs(frame, 1);
                        pattern = 0x11;
                    } else if (stack.peekType(1).isCategory1()) {
                        machine.popArgs(frame, 2);
                        pattern = 0x2121;
                    } else {
                        throw illegalTos();
                    }
                    if (opcode == ByteOps.DUP2) {
                        machine.auxIntArg(pattern);
                    }
                    break;
                }
                case ByteOps.DUP: {
                    Type peekType = frame.getStack().peekType(0);
                    if (peekType.isCategory2()) {
                        throw illegalTos();
                    }
                    machine.popArgs(frame, 1);
                    machine.auxIntArg(0x11);
                    break;
                }
                case ByteOps.DUP_X1: {
                    ExecutionStack stack = frame.getStack();
                    if (! (stack.peekType(0).isCategory1() &&
                           stack.peekType(1).isCategory1())) {
                        throw illegalTos();
                    }
                    machine.popArgs(frame, 2);
                    machine.auxIntArg(0x212);
                    break;
                }
                case ByteOps.DUP_X2: {
                    ExecutionStack stack = frame.getStack();
                    if (stack.peekType(0).isCategory2()) {
                        throw illegalTos();
                    }
                    if (stack.peekType(1).isCategory2()) {
                        machine.popArgs(frame, 2);
                        machine.auxIntArg(0x212);
                    } else if (stack.peekType(2).isCategory1()) {
                        machine.popArgs(frame, 3);
                        machine.auxIntArg(0x3213);
                    } else {
                        throw illegalTos();
                    }
                    break;
                }
                case ByteOps.DUP2_X1: {
                    ExecutionStack stack = frame.getStack();
                    if (stack.peekType(0).isCategory2()) {
                        if (stack.peekType(2).isCategory2()) {
                            throw illegalTos();
                        }
                        machine.popArgs(frame, 2);
                        machine.auxIntArg(0x212);
                    } else {
                        if (stack.peekType(1).isCategory2() ||
                            stack.peekType(2).isCategory2()) {
                            throw illegalTos();
                        }
                        machine.popArgs(frame, 3);
                        machine.auxIntArg(0x32132);
                    }
                    break;
                }
                case ByteOps.DUP2_X2: {
                    ExecutionStack stack = frame.getStack();
                    if (stack.peekType(0).isCategory2()) {
                        if (stack.peekType(2).isCategory2()) {
                            machine.popArgs(frame, 2);
                            machine.auxIntArg(0x212);
                        } else if (stack.peekType(3).isCategory1()) {
                            machine.popArgs(frame, 3);
                            machine.auxIntArg(0x3213);
                        } else {
                            throw illegalTos();
                        }
                    } else if (stack.peekType(1).isCategory1()) {
                        if (stack.peekType(2).isCategory2()) {
                            machine.popArgs(frame, 3);
                            machine.auxIntArg(0x32132);
                        } else if (stack.peekType(3).isCategory1()) {
                            machine.popArgs(frame, 4);
                            machine.auxIntArg(0x432143);
                        } else {
                            throw illegalTos();
                        }
                    } else {
                        throw illegalTos();
                    }
                    break;
                }
                case ByteOps.SWAP: {
                    ExecutionStack stack = frame.getStack();
                    if (! (stack.peekType(0).isCategory1() &&
                           stack.peekType(1).isCategory1())) {
                        throw illegalTos();
                    }
                    machine.popArgs(frame, 2);
                    machine.auxIntArg(0x12);
                    break;
                }
                default: {
                    visitInvalid(opcode, offset, length);
                    return;
                }
            }
            machine.auxType(type);
            machine.run(frame, offset, opcode);
        }
        private void checkReturnType(Type encountered) {
            Type returnType = machine.getPrototype().getReturnType();
            if (! Merger.isPossiblyAssignableFrom(returnType, encountered)) {
                throw new SimException("return type mismatch: prototype " +
                        "indicates " + returnType.toHuman() +
                        ", but encountered type " + encountered.toHuman());
            }
        }
        public void visitLocal(int opcode, int offset, int length,
                int idx, Type type, int value) {
            int localOffset =
                (opcode == ByteOps.ISTORE) ? (offset + length) : offset;
            LocalVariableList.Item local =
                localVariables.pcAndIndexToLocal(localOffset, idx);
            Type localType;
            if (local != null) {
                localType = local.getType();
                if (localType.getBasicFrameType() !=
                        type.getBasicFrameType()) {
                    BaseMachine.throwLocalMismatch(type, localType);
                    return;
                }
            } else {
                localType = type;
            }
            switch (opcode) {
                case ByteOps.ILOAD:
                case ByteOps.RET: {
                    machine.localArg(frame, idx);
                    machine.auxType(type);
                    break;
                }
                case ByteOps.ISTORE: {
                    LocalItem item
                            = (local == null) ? null : local.getLocalItem();
                    machine.popArgs(frame, type);
                    machine.auxType(type);
                    machine.localTarget(idx, localType, item);
                    break;
                }
                case ByteOps.IINC: {
                    LocalItem item
                            = (local == null) ? null : local.getLocalItem();
                    machine.localArg(frame, idx);
                    machine.localTarget(idx, localType, item);
                    machine.auxType(type);
                    machine.auxIntArg(value);
                    machine.auxCstArg(CstInteger.make(value));
                    break;
                }
                default: {
                    visitInvalid(opcode, offset, length);
                    return;
                }
            }
            machine.run(frame, offset, opcode);
        }
        public void visitConstant(int opcode, int offset, int length,
                Constant cst, int value) {
            switch (opcode) {
                case ByteOps.ANEWARRAY: {
                    machine.popArgs(frame, Type.INT);
                    break;
                }
                case ByteOps.PUTSTATIC: {
                    Type fieldType = ((CstFieldRef) cst).getType();
                    machine.popArgs(frame, fieldType);
                    break;
                }
                case ByteOps.GETFIELD:
                case ByteOps.CHECKCAST:
                case ByteOps.INSTANCEOF: {
                    machine.popArgs(frame, Type.OBJECT);
                    break;
                }
                case ByteOps.PUTFIELD: {
                    Type fieldType = ((CstFieldRef) cst).getType();
                    machine.popArgs(frame, Type.OBJECT, fieldType);
                    break;
                }
                case ByteOps.INVOKEINTERFACE: {
                    cst = ((CstInterfaceMethodRef) cst).toMethodRef();
                }
                case ByteOps.INVOKEVIRTUAL:
                case ByteOps.INVOKESPECIAL: {
                    Prototype prototype = 
                        ((CstMethodRef) cst).getPrototype(false);
                    machine.popArgs(frame, prototype);
                    break;
                }
                case ByteOps.INVOKESTATIC: {
                    Prototype prototype = 
                        ((CstMethodRef) cst).getPrototype(true);
                    machine.popArgs(frame, prototype);
                    break;
                }
                case ByteOps.MULTIANEWARRAY: {
                    Prototype prototype =
                        Prototype.internInts(Type.VOID, value);
                    machine.popArgs(frame, prototype);
                    break;
                }
                default: {
                    machine.clearArgs();
                    break;
                }
            }
            machine.auxIntArg(value);
            machine.auxCstArg(cst);
            machine.run(frame, offset, opcode);
        }
        public void visitBranch(int opcode, int offset, int length,
                int target) {
            switch (opcode) {
                case ByteOps.IFEQ:
                case ByteOps.IFNE:
                case ByteOps.IFLT:
                case ByteOps.IFGE:
                case ByteOps.IFGT:
                case ByteOps.IFLE: {
                    machine.popArgs(frame, Type.INT);
                    break;
                }
                case ByteOps.IFNULL:
                case ByteOps.IFNONNULL: {
                    machine.popArgs(frame, Type.OBJECT);
                    break;
                }
                case ByteOps.IF_ICMPEQ:
                case ByteOps.IF_ICMPNE:
                case ByteOps.IF_ICMPLT:
                case ByteOps.IF_ICMPGE:
                case ByteOps.IF_ICMPGT:
                case ByteOps.IF_ICMPLE: {
                    machine.popArgs(frame, Type.INT, Type.INT);
                    break;
                }
                case ByteOps.IF_ACMPEQ:
                case ByteOps.IF_ACMPNE: {
                    machine.popArgs(frame, Type.OBJECT, Type.OBJECT);
                    break;
                }
                case ByteOps.GOTO:
                case ByteOps.JSR:
                case ByteOps.GOTO_W:
                case ByteOps.JSR_W: {
                    machine.clearArgs();
                    break;
                }
                default: {
                    visitInvalid(opcode, offset, length);
                    return;
                }
            }
            machine.auxTargetArg(target);
            machine.run(frame, offset, opcode);
        }
        public void visitSwitch(int opcode, int offset, int length,
                SwitchList cases, int padding) {
            machine.popArgs(frame, Type.INT);
            machine.auxIntArg(padding);
            machine.auxSwitchArg(cases);
            machine.run(frame, offset, opcode);
        }
        public void visitNewarray(int offset, int length, CstType type,
                ArrayList<Constant> initValues) {
            machine.popArgs(frame, Type.INT);
            machine.auxInitValues(initValues);
            machine.auxCstArg(type);
            machine.run(frame, offset, ByteOps.NEWARRAY);
        }
        public void setPreviousOffset(int offset) {
            previousOffset = offset;
        }
        public int getPreviousOffset() {
            return previousOffset;
        }
    }
}

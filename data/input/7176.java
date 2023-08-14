public class Items {
    Pool pool;
    Code code;
    Symtab syms;
    Types types;
    private final Item voidItem;
    private final Item thisItem;
    private final Item superItem;
    private final Item[] stackItem = new Item[TypeCodeCount];
    public Items(Pool pool, Code code, Symtab syms, Types types) {
        this.code = code;
        this.pool = pool;
        this.types = types;
        voidItem = new Item(VOIDcode) {
                public String toString() { return "void"; }
            };
        thisItem = new SelfItem(false);
        superItem = new SelfItem(true);
        for (int i = 0; i < VOIDcode; i++) stackItem[i] = new StackItem(i);
        stackItem[VOIDcode] = voidItem;
        this.syms = syms;
    }
    Item makeVoidItem() {
        return voidItem;
    }
    Item makeThisItem() {
        return thisItem;
    }
    Item makeSuperItem() {
        return superItem;
    }
    Item makeStackItem(Type type) {
        return stackItem[Code.typecode(type)];
    }
    Item makeIndexedItem(Type type) {
        return new IndexedItem(type);
    }
    LocalItem makeLocalItem(VarSymbol v) {
        return new LocalItem(v.erasure(types), v.adr);
    }
    private LocalItem makeLocalItem(Type type, int reg) {
        return new LocalItem(type, reg);
    }
    Item makeStaticItem(Symbol member) {
        return new StaticItem(member);
    }
    Item makeMemberItem(Symbol member, boolean nonvirtual) {
        return new MemberItem(member, nonvirtual);
    }
    Item makeImmediateItem(Type type, Object value) {
        return new ImmediateItem(type, value);
    }
    Item makeAssignItem(Item lhs) {
        return new AssignItem(lhs);
    }
    CondItem makeCondItem(int opcode, Chain trueJumps, Chain falseJumps) {
        return new CondItem(opcode, trueJumps, falseJumps);
    }
    CondItem makeCondItem(int opcode) {
        return makeCondItem(opcode, null, null);
    }
    abstract class Item {
        int typecode;
        Item(int typecode) {
            this.typecode = typecode;
        }
        Item load() {
            throw new AssertionError();
        }
        void store() {
            throw new AssertionError("store unsupported: " + this);
        }
        Item invoke() {
            throw new AssertionError(this);
        }
        void duplicate() {}
        void drop() {}
        void stash(int toscode) {
            stackItem[toscode].duplicate();
        }
        CondItem mkCond() {
            load();
            return makeCondItem(ifne);
        }
        Item coerce(int targetcode) {
            if (typecode == targetcode)
                return this;
            else {
                load();
                int typecode1 = Code.truncate(typecode);
                int targetcode1 = Code.truncate(targetcode);
                if (typecode1 != targetcode1) {
                    int offset = targetcode1 > typecode1 ? targetcode1 - 1
                        : targetcode1;
                    code.emitop0(i2l + typecode1 * 3 + offset);
                }
                if (targetcode != targetcode1) {
                    code.emitop0(int2byte + targetcode - BYTEcode);
                }
                return stackItem[targetcode];
            }
        }
        Item coerce(Type targettype) {
            return coerce(Code.typecode(targettype));
        }
        int width() {
            return 0;
        }
        public abstract String toString();
    }
    class StackItem extends Item {
        StackItem(int typecode) {
            super(typecode);
        }
        Item load() {
            return this;
        }
        void duplicate() {
            code.emitop0(width() == 2 ? dup2 : dup);
        }
        void drop() {
            code.emitop0(width() == 2 ? pop2 : pop);
        }
        void stash(int toscode) {
            code.emitop0(
                (width() == 2 ? dup_x2 : dup_x1) + 3 * (Code.width(toscode) - 1));
        }
        int width() {
            return Code.width(typecode);
        }
        public String toString() {
            return "stack(" + typecodeNames[typecode] + ")";
        }
    }
    class IndexedItem extends Item {
        IndexedItem(Type type) {
            super(Code.typecode(type));
        }
        Item load() {
            code.emitop0(iaload + typecode);
            return stackItem[typecode];
        }
        void store() {
            code.emitop0(iastore + typecode);
        }
        void duplicate() {
            code.emitop0(dup2);
        }
        void drop() {
            code.emitop0(pop2);
        }
        void stash(int toscode) {
            code.emitop0(dup_x2 + 3 * (Code.width(toscode) - 1));
        }
        int width() {
            return 2;
        }
        public String toString() {
            return "indexed(" + ByteCodes.typecodeNames[typecode] + ")";
        }
    }
    class SelfItem extends Item {
        boolean isSuper;
        SelfItem(boolean isSuper) {
            super(OBJECTcode);
            this.isSuper = isSuper;
        }
        Item load() {
            code.emitop0(aload_0);
            return stackItem[typecode];
        }
        public String toString() {
            return isSuper ? "super" : "this";
        }
    }
    class LocalItem extends Item {
        int reg;
        Type type;
        LocalItem(Type type, int reg) {
            super(Code.typecode(type));
            Assert.check(reg >= 0);
            this.type = type;
            this.reg = reg;
        }
        Item load() {
            if (reg <= 3)
                code.emitop0(iload_0 + Code.truncate(typecode) * 4 + reg);
            else
                code.emitop1w(iload + Code.truncate(typecode), reg);
            return stackItem[typecode];
        }
        void store() {
            if (reg <= 3)
                code.emitop0(istore_0 + Code.truncate(typecode) * 4 + reg);
            else
                code.emitop1w(istore + Code.truncate(typecode), reg);
            code.setDefined(reg);
        }
        void incr(int x) {
            if (typecode == INTcode && x >= -32768 && x <= 32767) {
                code.emitop1w(iinc, reg, x);
            } else {
                load();
                if (x >= 0) {
                    makeImmediateItem(syms.intType, x).load();
                    code.emitop0(iadd);
                } else {
                    makeImmediateItem(syms.intType, -x).load();
                    code.emitop0(isub);
                }
                makeStackItem(syms.intType).coerce(typecode);
                store();
            }
        }
        public String toString() {
            return "localItem(type=" + type + "; reg=" + reg + ")";
        }
    }
    class StaticItem extends Item {
        Symbol member;
        StaticItem(Symbol member) {
            super(Code.typecode(member.erasure(types)));
            this.member = member;
        }
        Item load() {
            code.emitop2(getstatic, pool.put(member));
            return stackItem[typecode];
        }
        void store() {
            code.emitop2(putstatic, pool.put(member));
        }
        Item invoke() {
            MethodType mtype = (MethodType)member.erasure(types);
            int rescode = Code.typecode(mtype.restype);
            code.emitInvokestatic(pool.put(member), mtype);
            return stackItem[rescode];
        }
        public String toString() {
            return "static(" + member + ")";
        }
    }
    class MemberItem extends Item {
        Symbol member;
        boolean nonvirtual;
        MemberItem(Symbol member, boolean nonvirtual) {
            super(Code.typecode(member.erasure(types)));
            this.member = member;
            this.nonvirtual = nonvirtual;
        }
        Item load() {
            code.emitop2(getfield, pool.put(member));
            return stackItem[typecode];
        }
        void store() {
            code.emitop2(putfield, pool.put(member));
        }
        Item invoke() {
            MethodType mtype = (MethodType)member.externalType(types);
            int rescode = Code.typecode(mtype.restype);
            if ((member.owner.flags() & Flags.INTERFACE) != 0) {
                code.emitInvokeinterface(pool.put(member), mtype);
            } else if (nonvirtual) {
                code.emitInvokespecial(pool.put(member), mtype);
            } else {
                code.emitInvokevirtual(pool.put(member), mtype);
            }
            return stackItem[rescode];
        }
        void duplicate() {
            stackItem[OBJECTcode].duplicate();
        }
        void drop() {
            stackItem[OBJECTcode].drop();
        }
        void stash(int toscode) {
            stackItem[OBJECTcode].stash(toscode);
        }
        int width() {
            return 1;
        }
        public String toString() {
            return "member(" + member + (nonvirtual ? " nonvirtual)" : ")");
        }
    }
    class ImmediateItem extends Item {
        Object value;
        ImmediateItem(Type type, Object value) {
            super(Code.typecode(type));
            this.value = value;
        }
        private void ldc() {
            int idx = pool.put(value);
            if (typecode == LONGcode || typecode == DOUBLEcode) {
                code.emitop2(ldc2w, idx);
            } else if (idx <= 255) {
                code.emitop1(ldc1, idx);
            } else {
                code.emitop2(ldc2, idx);
            }
        }
        Item load() {
            switch (typecode) {
            case INTcode: case BYTEcode: case SHORTcode: case CHARcode:
                int ival = ((Number)value).intValue();
                if (-1 <= ival && ival <= 5)
                    code.emitop0(iconst_0 + ival);
                else if (Byte.MIN_VALUE <= ival && ival <= Byte.MAX_VALUE)
                    code.emitop1(bipush, ival);
                else if (Short.MIN_VALUE <= ival && ival <= Short.MAX_VALUE)
                    code.emitop2(sipush, ival);
                else
                    ldc();
                break;
            case LONGcode:
                long lval = ((Number)value).longValue();
                if (lval == 0 || lval == 1)
                    code.emitop0(lconst_0 + (int)lval);
                else
                    ldc();
                break;
            case FLOATcode:
                float fval = ((Number)value).floatValue();
                if (isPosZero(fval) || fval == 1.0 || fval == 2.0)
                    code.emitop0(fconst_0 + (int)fval);
                else {
                    ldc();
                }
                break;
            case DOUBLEcode:
                double dval = ((Number)value).doubleValue();
                if (isPosZero(dval) || dval == 1.0)
                    code.emitop0(dconst_0 + (int)dval);
                else
                    ldc();
                break;
            case OBJECTcode:
                ldc();
                break;
            default:
                Assert.error();
            }
            return stackItem[typecode];
        }
            private boolean isPosZero(float x) {
                return x == 0.0f && 1.0f / x > 0.0f;
            }
            private boolean isPosZero(double x) {
                return x == 0.0d && 1.0d / x > 0.0d;
            }
        CondItem mkCond() {
            int ival = ((Number)value).intValue();
            return makeCondItem(ival != 0 ? goto_ : dontgoto);
        }
        Item coerce(int targetcode) {
            if (typecode == targetcode) {
                return this;
            } else {
                switch (targetcode) {
                case INTcode:
                    if (Code.truncate(typecode) == INTcode)
                        return this;
                    else
                        return new ImmediateItem(
                            syms.intType,
                            ((Number)value).intValue());
                case LONGcode:
                    return new ImmediateItem(
                        syms.longType,
                        ((Number)value).longValue());
                case FLOATcode:
                    return new ImmediateItem(
                        syms.floatType,
                        ((Number)value).floatValue());
                case DOUBLEcode:
                    return new ImmediateItem(
                        syms.doubleType,
                        ((Number)value).doubleValue());
                case BYTEcode:
                    return new ImmediateItem(
                        syms.byteType,
                        (int)(byte)((Number)value).intValue());
                case CHARcode:
                    return new ImmediateItem(
                        syms.charType,
                        (int)(char)((Number)value).intValue());
                case SHORTcode:
                    return new ImmediateItem(
                        syms.shortType,
                        (int)(short)((Number)value).intValue());
                default:
                    return super.coerce(targetcode);
                }
            }
        }
        public String toString() {
            return "immediate(" + value + ")";
        }
    }
    class AssignItem extends Item {
        Item lhs;
        AssignItem(Item lhs) {
            super(lhs.typecode);
            this.lhs = lhs;
        }
        Item load() {
            lhs.stash(typecode);
            lhs.store();
            return stackItem[typecode];
        }
        void duplicate() {
            load().duplicate();
        }
        void drop() {
            lhs.store();
        }
        void stash(int toscode) {
            Assert.error();
        }
        int width() {
            return lhs.width() + Code.width(typecode);
        }
        public String toString() {
            return "assign(lhs = " + lhs + ")";
        }
    }
    class CondItem extends Item {
        Chain trueJumps;
        Chain falseJumps;
        int opcode;
        JCTree tree;
        CondItem(int opcode, Chain truejumps, Chain falsejumps) {
            super(BYTEcode);
            this.opcode = opcode;
            this.trueJumps = truejumps;
            this.falseJumps = falsejumps;
        }
        Item load() {
            Chain trueChain = null;
            Chain falseChain = jumpFalse();
            if (!isFalse()) {
                code.resolve(trueJumps);
                code.emitop0(iconst_1);
                trueChain = code.branch(goto_);
            }
            if (falseChain != null) {
                code.resolve(falseChain);
                code.emitop0(iconst_0);
            }
            code.resolve(trueChain);
            return stackItem[typecode];
        }
        void duplicate() {
            load().duplicate();
        }
        void drop() {
            load().drop();
        }
        void stash(int toscode) {
            Assert.error();
        }
        CondItem mkCond() {
            return this;
        }
        Chain jumpTrue() {
            if (tree == null) return Code.mergeChains(trueJumps, code.branch(opcode));
            int startpc = code.curPc();
            Chain c = Code.mergeChains(trueJumps, code.branch(opcode));
            code.crt.put(tree, CRTable.CRT_BRANCH_TRUE, startpc, code.curPc());
            return c;
        }
        Chain jumpFalse() {
            if (tree == null) return Code.mergeChains(falseJumps, code.branch(Code.negate(opcode)));
            int startpc = code.curPc();
            Chain c = Code.mergeChains(falseJumps, code.branch(Code.negate(opcode)));
            code.crt.put(tree, CRTable.CRT_BRANCH_FALSE, startpc, code.curPc());
            return c;
        }
        CondItem negate() {
            CondItem c = new CondItem(Code.negate(opcode), falseJumps, trueJumps);
            c.tree = tree;
            return c;
        }
        int width() {
            throw new AssertionError();
        }
        boolean isTrue() {
            return falseJumps == null && opcode == goto_;
        }
        boolean isFalse() {
            return trueJumps == null && opcode == dontgoto;
        }
        public String toString() {
            return "cond(" + Code.mnem(opcode) + ")";
        }
    }
}

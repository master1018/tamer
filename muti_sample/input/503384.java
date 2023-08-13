public abstract class Insn implements ToHuman {
    private final Rop opcode;
    private final SourcePosition position;
    private final RegisterSpec result;
    private final RegisterSpecList sources;
    public Insn(Rop opcode, SourcePosition position, RegisterSpec result,
                RegisterSpecList sources) {
        if (opcode == null) {
            throw new NullPointerException("opcode == null");
        }
        if (position == null) {
            throw new NullPointerException("position == null");
        }
        if (sources == null) {
            throw new NullPointerException("sources == null");
        }
        this.opcode = opcode;
        this.position = position;
        this.result = result;
        this.sources = sources;
    }
    @Override
    public final boolean equals(Object other) {
        return (this == other);
    }
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }
    @Override
    public String toString() {
        return toStringWithInline(getInlineString());
    }
    public String toHuman() {
        return toHumanWithInline(getInlineString());
    }
    public String getInlineString() {
        return null;
    }
    public final Rop getOpcode() {
        return opcode;
    }
    public final SourcePosition getPosition() {
        return position;
    }
    public final RegisterSpec getResult() {
        return result;
    }
    public final RegisterSpec getLocalAssignment() {
        RegisterSpec assignment;
        if (opcode.getOpcode() == RegOps.MARK_LOCAL) {
            assignment = sources.get(0);
        } else {
            assignment = result;
        }
        if (assignment == null) {
            return null;
        }
        LocalItem localItem = assignment.getLocalItem();
        if (localItem == null) {
            return null;
        }
        return assignment;
    }
    public final RegisterSpecList getSources() {
        return sources;
    }
    public final boolean canThrow() {
        return opcode.canThrow();
    }
    public abstract TypeList getCatches();
    public abstract void accept(Visitor visitor);
    public abstract Insn withAddedCatch(Type type);
    public abstract Insn withRegisterOffset(int delta);
    public Insn withLastSourceLiteral() {
        return this;
    }
    public Insn copy() {
        return withRegisterOffset(0);
    }
    private static boolean equalsHandleNulls (Object a, Object b) {
        return (a == b) || ((a != null) && a.equals(b));
    }
    public boolean contentEquals(Insn b) {
        return opcode == b.getOpcode()
                && position.equals(b.getPosition())
                && (getClass() == b.getClass())
                && equalsHandleNulls(result, b.getResult())
                && equalsHandleNulls(sources, b.getSources())
                && StdTypeList.equalContents(getCatches(), b.getCatches());
    }
    public abstract Insn withNewRegisters(RegisterSpec result,
            RegisterSpecList sources);
    protected final String toStringWithInline(String extra) {
        StringBuffer sb = new StringBuffer(80);
        sb.append("Insn{");
        sb.append(position);
        sb.append(' ');
        sb.append(opcode);
        if (extra != null) {
            sb.append(' ');
            sb.append(extra);
        }
        sb.append(" :: ");
        if (result != null) {
            sb.append(result);
            sb.append(" <- ");
        }
        sb.append(sources);
        sb.append('}');
        return sb.toString();
    }
    protected final String toHumanWithInline(String extra) {
        StringBuffer sb = new StringBuffer(80);
        sb.append(position);
        sb.append(": ");
        sb.append(opcode.getNickname());
        if (extra != null) {
            sb.append("(");
            sb.append(extra);
            sb.append(")");
        }
        if (result == null) {
            sb.append(" .");
        } else {
            sb.append(" ");
            sb.append(result.toHuman());
        }
        sb.append(" <-");
        int sz = sources.size();
        if (sz == 0) {
            sb.append(" .");
        } else {
            for (int i = 0; i < sz; i++) {
                sb.append(" ");
                sb.append(sources.get(i).toHuman());
            }
        }
        return sb.toString();
    }
    public static interface Visitor {
        public void visitPlainInsn(PlainInsn insn);
        public void visitPlainCstInsn(PlainCstInsn insn);
        public void visitSwitchInsn(SwitchInsn insn);
        public void visitThrowingCstInsn(ThrowingCstInsn insn);
        public void visitThrowingInsn(ThrowingInsn insn);
        public void visitFillArrayDataInsn(FillArrayDataInsn insn);
    }
    public static class BaseVisitor implements Visitor {
        public void visitPlainInsn(PlainInsn insn) {
        }
        public void visitPlainCstInsn(PlainCstInsn insn) {
        }
        public void visitSwitchInsn(SwitchInsn insn) {
        }
        public void visitThrowingCstInsn(ThrowingCstInsn insn) {
        }
        public void visitThrowingInsn(ThrowingInsn insn) {
        }
        public void visitFillArrayDataInsn(FillArrayDataInsn insn) {
        }
    }
}

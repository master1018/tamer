public final class EncodedMethod extends EncodedMember 
        implements Comparable<EncodedMethod> {
    private final CstMethodRef method;
    private final CodeItem code;
    public EncodedMethod(CstMethodRef method, int accessFlags,
            DalvCode code, TypeList throwsList) {
        super(accessFlags);
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        this.method = method;
        if (code == null) {
            this.code = null;
        } else {
            boolean isStatic = (accessFlags & AccessFlags.ACC_STATIC) != 0;
            this.code = new CodeItem(method, code, isStatic, throwsList);
        }
    }
    public boolean equals(Object other) {
        if (! (other instanceof EncodedMethod)) {
            return false;
        }
        return compareTo((EncodedMethod) other) == 0;
    }
    public int compareTo(EncodedMethod other) {
        return method.compareTo(other.method);
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append(getClass().getName());
        sb.append('{');
        sb.append(Hex.u2(getAccessFlags()));
        sb.append(' ');
        sb.append(method);
        if (code != null) {
            sb.append(' ');
            sb.append(code);
        }
        sb.append('}');
        return sb.toString();
    }
    @Override
    public void addContents(DexFile file) {
        MethodIdsSection methodIds = file.getMethodIds();
        MixedItemSection wordData = file.getWordData();
        methodIds.intern(method);
        if (code != null) {
            wordData.add(code);
        }
    }
    public final String toHuman() {
        return method.toHuman();
    }
    @Override
    public final CstUtf8 getName() {
        return method.getNat().getName();
    }
    @Override
    public void debugPrint(PrintWriter out, boolean verbose) {
        if (code == null) {
            out.println(getRef().toHuman() + ": abstract or native");
        } else {
            code.debugPrint(out, "  ", verbose);
        }
    }
    public final CstMethodRef getRef() {
        return method;
    }
    @Override
    public int encode(DexFile file, AnnotatedOutput out, 
            int lastIndex, int dumpSeq) {
        int methodIdx = file.getMethodIds().indexOf(method);
        int diff = methodIdx - lastIndex;
        int accessFlags = getAccessFlags();
        int codeOff = OffsettedItem.getAbsoluteOffsetOr0(code);
        boolean hasCode = (codeOff != 0);
        boolean shouldHaveCode = (accessFlags & 
                (AccessFlags.ACC_ABSTRACT | AccessFlags.ACC_NATIVE)) == 0;
        if (hasCode != shouldHaveCode) {
            throw new UnsupportedOperationException(
                    "code vs. access_flags mismatch");
        }
        if (out.annotates()) {
            out.annotate(0, String.format("  [%x] %s", dumpSeq,
                            method.toHuman()));
            out.annotate(Leb128Utils.unsignedLeb128Size(diff),
                    "    method_idx:   " + Hex.u4(methodIdx));
            out.annotate(Leb128Utils.unsignedLeb128Size(accessFlags),
                    "    access_flags: " +
                    AccessFlags.methodString(accessFlags));
            out.annotate(Leb128Utils.unsignedLeb128Size(codeOff),
                    "    code_off:     " + Hex.u4(codeOff));
        }
        out.writeUnsignedLeb128(diff);
        out.writeUnsignedLeb128(accessFlags);
        out.writeUnsignedLeb128(codeOff);
        return methodIdx;
    }
}

public final class ConcreteMethod implements Method {
    private final Method method;
    private final CstUtf8 sourceFile;
    private final boolean accSuper;
    private final AttCode attCode;
    private final LineNumberList lineNumbers;
    private final LocalVariableList localVariables;
    public ConcreteMethod(Method method, ClassFile cf, boolean keepLines,
                          boolean keepLocals) {
        this.method = method;
        this.accSuper = (cf.getAccessFlags() & AccessFlags.ACC_SUPER) != 0;
        this.sourceFile = cf.getSourceFile();
        AttributeList attribs = method.getAttributes();
        this.attCode = (AttCode) attribs.findFirst(AttCode.ATTRIBUTE_NAME);
        AttributeList codeAttribs = attCode.getAttributes();
        LineNumberList lineNumbers = LineNumberList.EMPTY;
        if (keepLines) {
            for (AttLineNumberTable lnt = (AttLineNumberTable)
                     codeAttribs.findFirst(AttLineNumberTable.ATTRIBUTE_NAME);
                 lnt != null;
                 lnt = (AttLineNumberTable) codeAttribs.findNext(lnt)) {
                lineNumbers = LineNumberList.concat(lineNumbers,
                        lnt.getLineNumbers());
            }
        }
        this.lineNumbers = lineNumbers;
        LocalVariableList localVariables = LocalVariableList.EMPTY;
        if (keepLocals) {
            for (AttLocalVariableTable lvt = (AttLocalVariableTable)
                     codeAttribs.findFirst(
                             AttLocalVariableTable.ATTRIBUTE_NAME);
                 lvt != null;
                 lvt = (AttLocalVariableTable) codeAttribs.findNext(lvt)) {
                localVariables =
                    LocalVariableList.concat(localVariables,
                            lvt.getLocalVariables());
            }
            LocalVariableList typeList = LocalVariableList.EMPTY;
            for (AttLocalVariableTypeTable lvtt = (AttLocalVariableTypeTable)
                     codeAttribs.findFirst(
                             AttLocalVariableTypeTable.ATTRIBUTE_NAME);
                 lvtt != null;
                 lvtt =
                     (AttLocalVariableTypeTable) codeAttribs.findNext(lvtt)) {
                typeList =
                    LocalVariableList.concat(typeList,
                            lvtt.getLocalVariables());
            }
            if (typeList.size() != 0) {
                localVariables =
                    LocalVariableList.mergeDescriptorsAndSignatures(
                            localVariables, typeList);
            }
        }
        this.localVariables = localVariables;
    }
    public CstNat getNat() {
        return method.getNat();
    }
    public CstUtf8 getName() {
        return method.getName();
    }
    public CstUtf8 getDescriptor() {
        return method.getDescriptor();
    }
    public int getAccessFlags() {
        return method.getAccessFlags();
    }
    public AttributeList getAttributes() {
        return method.getAttributes();
    }
    public CstType getDefiningClass() {
        return method.getDefiningClass();
    }
    public Prototype getEffectiveDescriptor() {
        return method.getEffectiveDescriptor();
    }
    public boolean getAccSuper() {
        return accSuper;
    }
    public int getMaxStack() {
        return attCode.getMaxStack();
    }
    public int getMaxLocals() {
        return attCode.getMaxLocals();
    }
    public BytecodeArray getCode() {
        return attCode.getCode();
    }
    public ByteCatchList getCatches() {
        return attCode.getCatches();
    }
    public LineNumberList getLineNumbers() {
        return lineNumbers;
    }
    public LocalVariableList getLocalVariables() {
        return localVariables;
    }
    public SourcePosition makeSourcePosistion(int offset) {
        return new SourcePosition(sourceFile, offset,
                                  lineNumbers.pcToLine(offset));
    }
}

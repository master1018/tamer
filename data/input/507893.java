public class BlockDumper
        extends BaseDumper {
    private boolean rop;
    protected DirectClassFile classFile;
    protected boolean suppressDump;
    private boolean first;
    private boolean optimize;
    public static void dump(byte[] bytes, PrintStream out,
                            String filePath, boolean rop, Args args) {
        BlockDumper bd =
            new BlockDumper(bytes, out, filePath,
                            rop, args);
        bd.dump();
    }
    BlockDumper(byte[] bytes, PrintStream out,
                        String filePath,
                        boolean rop, Args args) {
        super(bytes, out, filePath, args);
        this.rop = rop;
        this.classFile = null;
        this.suppressDump = true;
        this.first = true;
        this.optimize = args.optimize;
    }
    public void dump() {
        byte[] bytes = getBytes();
        ByteArray ba = new ByteArray(bytes);
        classFile = new DirectClassFile(ba, getFilePath(), getStrictParse());
        classFile.setAttributeFactory(StdAttributeFactory.THE_ONE);
        classFile.getMagic(); 
        DirectClassFile liveCf =
            new DirectClassFile(ba, getFilePath(), getStrictParse());
        liveCf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        liveCf.setObserver(this);
        liveCf.getMagic(); 
    }
    @Override
    public void changeIndent(int indentDelta) {
        if (!suppressDump) {
            super.changeIndent(indentDelta);
        }
    }
    @Override
    public void parsed(ByteArray bytes, int offset, int len, String human) {
        if (!suppressDump) {
            super.parsed(bytes, offset, len, human);
        }
    }
    protected boolean shouldDumpMethod(String name) {
        return args.method == null || args.method.equals(name);
    }
    @Override
    public void startParsingMember(ByteArray bytes, int offset, String name,
                                   String descriptor) {
        if (descriptor.indexOf('(') < 0) {
            return;
        }
        if (!shouldDumpMethod(name)) {
            return;
        }
        setAt(bytes, offset);
        suppressDump = false;
        if (first) {
            first = false;
        } else {
            parsed(bytes, offset, 0, "\n");
        }
        parsed(bytes, offset, 0, "method " + name + " " + descriptor);
        suppressDump = true;
    }
    @Override
    public void endParsingMember(ByteArray bytes, int offset, String name,
                                 String descriptor, Member member) {
        if (!(member instanceof Method)) {
            return;
        }
        if (!shouldDumpMethod(name)) {
            return;
        }
        ConcreteMethod meth = new ConcreteMethod((Method) member, classFile,
                                                 true, true);
        if (rop) {
            ropDump(meth);
        } else {
            regularDump(meth);
        }
    }
    private void regularDump(ConcreteMethod meth) {
        BytecodeArray code = meth.getCode();
        ByteArray bytes = code.getBytes();
        ByteBlockList list = BasicBlocker.identifyBlocks(meth);
        int sz = list.size();
        CodeObserver codeObserver = new CodeObserver(bytes, BlockDumper.this);
        setAt(bytes, 0);
        suppressDump = false;
        int byteAt = 0;
        for (int i = 0; i < sz; i++) {
            ByteBlock bb = list.get(i);
            int start = bb.getStart();
            int end = bb.getEnd();
            if (byteAt < start) {
                parsed(bytes, byteAt, start - byteAt,
                       "dead code " + Hex.u2(byteAt) + ".." + Hex.u2(start));
            }
            parsed(bytes, start, 0,
                   "block " + Hex.u2(bb.getLabel()) + ": " +
                   Hex.u2(start) + ".." + Hex.u2(end));
            changeIndent(1);
            int len;
            for (int j = start; j < end; j += len) {
                len = code.parseInstruction(j, codeObserver);
                codeObserver.setPreviousOffset(j);
            }
            IntList successors = bb.getSuccessors();
            int ssz = successors.size();
            if (ssz == 0) {
                parsed(bytes, end, 0, "returns");
            } else {
                for (int j = 0; j < ssz; j++) {
                    int succ = successors.get(j);
                    parsed(bytes, end, 0, "next " + Hex.u2(succ));
                }
            }
            ByteCatchList catches = bb.getCatches();
            int csz = catches.size();
            for (int j = 0; j < csz; j++) {
                ByteCatchList.Item one = catches.get(j);
                CstType exceptionClass = one.getExceptionClass();
                parsed(bytes, end, 0,
                       "catch " +
                       ((exceptionClass == CstType.OBJECT) ? "<any>" : 
                        exceptionClass.toHuman()) + " -> " +
                       Hex.u2(one.getHandlerPc()));
            }
            changeIndent(-1);
            byteAt = end;
        }
        int end = bytes.size();
        if (byteAt < end) {
            parsed(bytes, byteAt, end - byteAt,
                   "dead code " + Hex.u2(byteAt) + ".." + Hex.u2(end));
        }
        suppressDump = true;
    }
    private void ropDump(ConcreteMethod meth) {
        BytecodeArray code = meth.getCode();
        ByteArray bytes = code.getBytes();
        TranslationAdvice advice = DexTranslationAdvice.THE_ONE;
        RopMethod rmeth =
            Ropper.convert(meth, advice);
        StringBuffer sb = new StringBuffer(2000);
        if (optimize) {
            boolean isStatic = AccessFlags.isStatic(meth.getAccessFlags());
            int paramWidth = computeParamWidth(meth, isStatic);
            rmeth = Optimizer.optimize(rmeth, paramWidth, isStatic, true,
                    advice);
        }
        BasicBlockList blocks = rmeth.getBlocks();
        sb.append("first " + Hex.u2(rmeth.getFirstLabel()) + "\n");
        int sz = blocks.size();
        for (int i = 0; i < sz; i++) {
            BasicBlock bb = blocks.get(i);
            int label = bb.getLabel();
            sb.append("block ");
            sb.append(Hex.u2(label));
            sb.append("\n");
            IntList preds = rmeth.labelToPredecessors(label);
            int psz = preds.size();
            for (int j = 0; j < psz; j++) {
                sb.append("  pred ");
                sb.append(Hex.u2(preds.get(j)));
                sb.append("\n");
            }
            InsnList il = bb.getInsns();
            int ilsz = il.size();
            for (int j = 0; j < ilsz; j++) {
                Insn one = il.get(j);
                sb.append("  ");
                sb.append(il.get(j).toHuman());
                sb.append("\n");
            }
            IntList successors = bb.getSuccessors();
            int ssz = successors.size();
            if (ssz == 0) {
                sb.append("  returns\n");
            } else {
                int primary = bb.getPrimarySuccessor();
                for (int j = 0; j < ssz; j++) {
                    int succ = successors.get(j);
                    sb.append("  next ");
                    sb.append(Hex.u2(succ));
                    if ((ssz != 1) && (succ == primary)) {
                        sb.append(" *");
                    }
                    sb.append("\n");
                }
            }
        }
        suppressDump = false;
        setAt(bytes, 0);
        parsed(bytes, 0, bytes.size(), sb.toString());
        suppressDump = true;
    }
}

public final class DebugInfoEncoder {
    private static final boolean DEBUG = false;
    private final PositionList positions;
    private final LocalList locals;
    private final ByteArrayAnnotatedOutput output;
    private final DexFile file;
    private final int codeSize;
    private final int regSize;
    private final Prototype desc;
    private final boolean isStatic;
    private int address = 0;
    private int line = 1;
    private AnnotatedOutput annotateTo;
    private PrintWriter debugPrint;
    private String prefix;
    private boolean shouldConsume;
    private final LocalList.Entry[] lastEntryForReg;
    public DebugInfoEncoder(PositionList positions, LocalList locals,
            DexFile file, int codeSize, int regSize,
            boolean isStatic, CstMethodRef ref) {
        this.positions = positions;
        this.locals = locals;
        this.file = file;
        this.desc = ref.getPrototype();
        this.isStatic = isStatic;
        this.codeSize = codeSize;
        this.regSize = regSize;
        output = new ByteArrayAnnotatedOutput();
        lastEntryForReg = new LocalList.Entry[regSize];
    }
    private void annotate(int length, String message) {
        if (prefix != null) {
            message = prefix + message;
        }
        if (annotateTo != null) {
            annotateTo.annotate(shouldConsume ? length : 0, message);
        }
        if (debugPrint != null) {
            debugPrint.println(message);
        }
    }
    public byte[] convert() {
        try {
            byte[] ret;
            ret = convert0();
            if (DEBUG) {
                for (int i = 0 ; i < ret.length; i++) {
                    System.err.printf("byte %02x\n", (0xff & ret[i]));
                }
            }
            return ret;
        } catch (IOException ex) {
            throw ExceptionWithContext
                    .withContext(ex, "...while encoding debug info");
        }
    }
    public byte[] convertAndAnnotate(String prefix, PrintWriter debugPrint,
            AnnotatedOutput out, boolean consume) {
        this.prefix = prefix;
        this.debugPrint = debugPrint;
        annotateTo = out;
        shouldConsume = consume;
        byte[] result = convert();
        return result;
    }
    private byte[] convert0() throws IOException {
        ArrayList<PositionList.Entry> sortedPositions = buildSortedPositions();
        ArrayList<LocalList.Entry> methodArgs = extractMethodArguments();
        emitHeader(sortedPositions, methodArgs);
        output.writeByte(DBG_SET_PROLOGUE_END);
        if (annotateTo != null || debugPrint != null) {
            annotate(1, String.format("%04x: prologue end",address));
        }
        int positionsSz = sortedPositions.size();
        int localsSz = locals.size();
        int curPositionIdx = 0;
        int curLocalIdx = 0;
        for (;;) {
            curLocalIdx = emitLocalsAtAddress(curLocalIdx);
            curPositionIdx =
                emitPositionsAtAddress(curPositionIdx, sortedPositions);
            int nextAddrL = Integer.MAX_VALUE; 
            int nextAddrP = Integer.MAX_VALUE; 
            if (curLocalIdx < localsSz) {
                nextAddrL = locals.get(curLocalIdx).getAddress();
            }
            if (curPositionIdx < positionsSz) {
                nextAddrP = sortedPositions.get(curPositionIdx).getAddress();
            }
            int next = Math.min(nextAddrP, nextAddrL);
            if (next == Integer.MAX_VALUE) {
                break;
            }
            if (next == codeSize
                    && nextAddrL == Integer.MAX_VALUE
                    && nextAddrP == Integer.MAX_VALUE) {
                break;
            }
            if (next == nextAddrP) {
                emitPosition(sortedPositions.get(curPositionIdx++));
            } else {
                emitAdvancePc(next - address);
            }
        }
        emitEndSequence();
        return output.toByteArray();
    }
    private int emitLocalsAtAddress(int curLocalIdx)
            throws IOException {
        int sz = locals.size();
        while ((curLocalIdx < sz)
                && (locals.get(curLocalIdx).getAddress() == address)) {
            LocalList.Entry entry = locals.get(curLocalIdx++);
            int reg = entry.getRegister();
            LocalList.Entry prevEntry = lastEntryForReg[reg];
            if (entry == prevEntry) {
                continue;
            }
            lastEntryForReg[reg] = entry;
            if (entry.isStart()) {
                if ((prevEntry != null) && entry.matches(prevEntry)) {
                    if (prevEntry.isStart()) {
                        throw new RuntimeException("shouldn't happen");
                    }
                    emitLocalRestart(entry);
                } else {
                    emitLocalStart(entry);
                }
            } else {
                if (entry.getDisposition()
                        != LocalList.Disposition.END_REPLACED) {
                    emitLocalEnd(entry);
                }
            }
        }
        return curLocalIdx;
    }
    private int emitPositionsAtAddress(int curPositionIdx,
            ArrayList<PositionList.Entry> sortedPositions)
            throws IOException {
        int positionsSz = sortedPositions.size();
        while ((curPositionIdx < positionsSz)
                && (sortedPositions.get(curPositionIdx).getAddress()
                        == address)) {
            emitPosition(sortedPositions.get(curPositionIdx++));
        }
        return curPositionIdx;
    }
    private void emitHeader(ArrayList<PositionList.Entry> sortedPositions,
            ArrayList<LocalList.Entry> methodArgs) throws IOException {
        boolean annotate = (annotateTo != null) || (debugPrint != null);
        int mark = output.getCursor();
        if (sortedPositions.size() > 0) {
            PositionList.Entry entry = sortedPositions.get(0);
            line = entry.getPosition().getLine();
        }
        output.writeUnsignedLeb128(line);
        if (annotate) {
            annotate(output.getCursor() - mark, "line_start: " + line);
        }
        int curParam = getParamBase();
        StdTypeList paramTypes = desc.getParameterTypes();
        int szParamTypes = paramTypes.size();
        if (!isStatic) {
            for (LocalList.Entry arg : methodArgs) {
                if (curParam == arg.getRegister()) {
                    lastEntryForReg[curParam] = arg;
                    break;
                }
            }
            curParam++;
        }
        mark = output.getCursor();
        output.writeUnsignedLeb128(szParamTypes);
        if (annotate) {
            annotate(output.getCursor() - mark,
                    String.format("parameters_size: %04x", szParamTypes));
        }
        for (int i = 0; i < szParamTypes; i++) {
            Type pt = paramTypes.get(i);
            LocalList.Entry found = null;
            mark = output.getCursor();
            for (LocalList.Entry arg : methodArgs) {
                if (curParam == arg.getRegister()) {
                    found = arg;
                    if (arg.getSignature() != null) {
                        emitStringIndex(null);
                    } else {
                        emitStringIndex(arg.getName());
                    }
                    lastEntryForReg[curParam] = arg;
                    break;
                }
            }
            if (found == null) {
                emitStringIndex(null);
            }
            if (annotate) {
                String parameterName
                        = (found == null || found.getSignature() != null)
                                ? "<unnamed>" : found.getName().toHuman();
                annotate(output.getCursor() - mark,
                        "parameter " + parameterName + " "
                                + RegisterSpec.PREFIX + curParam);
            }
            curParam += pt.getCategory();
        }
        for (LocalList.Entry arg : lastEntryForReg) {
            if (arg == null) {
                continue;
            }
            CstUtf8 signature = arg.getSignature();
            if (signature != null) {
                emitLocalStartExtended(arg);
            }
        }
    }
    private ArrayList<PositionList.Entry> buildSortedPositions() {
        int sz = (positions == null) ? 0 : positions.size();
        ArrayList<PositionList.Entry> result = new ArrayList(sz);
        for (int i = 0; i < sz; i++) {
            result.add(positions.get(i));
        }
        Collections.sort (result, new Comparator<PositionList.Entry>() {
            public int compare (PositionList.Entry a, PositionList.Entry b) {
                return a.getAddress() - b.getAddress();
            }
            public boolean equals (Object obj) {
               return obj == this;
            }
        });
        return result;
    }
    private int getParamBase() {
        return regSize
                - desc.getParameterTypes().getWordCount() - (isStatic? 0 : 1);
    }
    private ArrayList<LocalList.Entry> extractMethodArguments() {
        ArrayList<LocalList.Entry> result
                = new ArrayList(desc.getParameterTypes().size());
        int argBase = getParamBase();
        BitSet seen = new BitSet(regSize - argBase);
        int sz = locals.size();
        for (int i = 0; i < sz; i++) {
            LocalList.Entry e = locals.get(i);
            int reg = e.getRegister();
            if (reg < argBase) {
                continue;
            }
            if (seen.get(reg - argBase)) {
                continue;
            }
            seen.set(reg - argBase);
            result.add(e);
        }
        Collections.sort(result, new Comparator<LocalList.Entry>() {
            public int compare(LocalList.Entry a, LocalList.Entry b) {
                return a.getRegister() - b.getRegister();
            }
            public boolean equals(Object obj) {
               return obj == this;
            }
        });
        return result;
    }
    private String entryAnnotationString(LocalList.Entry e) {
        StringBuilder sb = new StringBuilder();
        sb.append(RegisterSpec.PREFIX);
        sb.append(e.getRegister());
        sb.append(' ');
        CstUtf8 name = e.getName();
        if (name == null) {
            sb.append("null");
        } else {
            sb.append(name.toHuman());
        }
        sb.append(' ');
        CstType type = e.getType();
        if (type == null) {
            sb.append("null");
        } else {
            sb.append(type.toHuman());
        }
        CstUtf8 signature = e.getSignature();
        if (signature != null) {
            sb.append(' ');
            sb.append(signature.toHuman());
        }
        return sb.toString();
    }
    private void emitLocalRestart(LocalList.Entry entry)
            throws IOException {
        int mark = output.getCursor();
        output.writeByte(DBG_RESTART_LOCAL);
        emitUnsignedLeb128(entry.getRegister());
        if (annotateTo != null || debugPrint != null) {
            annotate(output.getCursor() - mark,
                    String.format("%04x: +local restart %s",
                            address, entryAnnotationString(entry)));
        }
        if (DEBUG) {
            System.err.println("emit local restart");
        }
    }
    private void emitStringIndex(CstUtf8 string) throws IOException {
        if ((string == null) || (file == null)) {
            output.writeUnsignedLeb128(0);
        } else {
            output.writeUnsignedLeb128(
                1 + file.getStringIds().indexOf(string));
        }
        if (DEBUG) {
            System.err.printf("Emit string %s\n",
                    string == null ? "<null>" : string.toQuoted());
        }
    }
    private void emitTypeIndex(CstType type) throws IOException {
        if ((type == null) || (file == null)) {
            output.writeUnsignedLeb128(0);
        } else {
            output.writeUnsignedLeb128(
                1 + file.getTypeIds().indexOf(type));
        }
        if (DEBUG) {
            System.err.printf("Emit type %s\n",
                    type == null ? "<null>" : type.toHuman());
        }
    }
    private void emitLocalStart(LocalList.Entry entry)
        throws IOException {
        if (entry.getSignature() != null) {
            emitLocalStartExtended(entry);
            return;
        }
        int mark = output.getCursor();
        output.writeByte(DBG_START_LOCAL);
        emitUnsignedLeb128(entry.getRegister());
        emitStringIndex(entry.getName());
        emitTypeIndex(entry.getType());
        if (annotateTo != null || debugPrint != null) {
            annotate(output.getCursor() - mark,
                    String.format("%04x: +local %s", address,
                            entryAnnotationString(entry)));
        }
        if (DEBUG) {
            System.err.println("emit local start");
        }
    }
    private void emitLocalStartExtended(LocalList.Entry entry)
        throws IOException {
        int mark = output.getCursor();
        output.writeByte(DBG_START_LOCAL_EXTENDED);
        emitUnsignedLeb128(entry.getRegister());
        emitStringIndex(entry.getName());
        emitTypeIndex(entry.getType());
        emitStringIndex(entry.getSignature());
        if (annotateTo != null || debugPrint != null) {
            annotate(output.getCursor() - mark,
                    String.format("%04x: +localx %s", address,
                            entryAnnotationString(entry)));
        }
        if (DEBUG) {
            System.err.println("emit local start");
        }
    }
    private void emitLocalEnd(LocalList.Entry entry)
            throws IOException {
        int mark = output.getCursor();
        output.writeByte(DBG_END_LOCAL);
        output.writeUnsignedLeb128(entry.getRegister());
        if (annotateTo != null || debugPrint != null) {
            annotate(output.getCursor() - mark,
                    String.format("%04x: -local %s", address,
                            entryAnnotationString(entry)));
        }
        if (DEBUG) {
            System.err.println("emit local end");
        }
    }
    private void emitPosition(PositionList.Entry entry)
            throws IOException {
        SourcePosition pos = entry.getPosition();
        int newLine = pos.getLine();
        int newAddress = entry.getAddress();
        int opcode;
        int deltaLines = newLine - line;
        int deltaAddress = newAddress - address;
        if (deltaAddress < 0) {
            throw new RuntimeException(
                    "Position entries must be in ascending address order");
        }
        if ((deltaLines < DBG_LINE_BASE)
                || (deltaLines > (DBG_LINE_BASE + DBG_LINE_RANGE -1))) {
            emitAdvanceLine(deltaLines);
            deltaLines = 0;
        }
        opcode = computeOpcode (deltaLines, deltaAddress);
        if ((opcode & ~0xff) > 0) {
            emitAdvancePc(deltaAddress);
            deltaAddress = 0;
            opcode = computeOpcode (deltaLines, deltaAddress);
            if ((opcode & ~0xff) > 0) {
                emitAdvanceLine(deltaLines);
                deltaLines = 0;
                opcode = computeOpcode (deltaLines, deltaAddress);
            }
        }
        output.writeByte(opcode);
        line += deltaLines;
        address += deltaAddress;
        if (annotateTo != null || debugPrint != null) {
            annotate(1,
                    String.format("%04x: line %d", address, line));
        }
    }
    private static int computeOpcode(int deltaLines, int deltaAddress) {
        if (deltaLines < DBG_LINE_BASE
                || deltaLines > (DBG_LINE_BASE + DBG_LINE_RANGE -1)) {
            throw new RuntimeException("Parameter out of range");
        }
        return (deltaLines - DBG_LINE_BASE)
            + (DBG_LINE_RANGE * deltaAddress) + DBG_FIRST_SPECIAL;
    }
    private void emitAdvanceLine(int deltaLines) throws IOException {
        int mark = output.getCursor();
        output.writeByte(DBG_ADVANCE_LINE);
        output.writeSignedLeb128(deltaLines);
        line += deltaLines;
        if (annotateTo != null || debugPrint != null) {
            annotate(output.getCursor() - mark,
                    String.format("line = %d", line));
        }
        if (DEBUG) {
            System.err.printf("Emitting advance_line for %d\n", deltaLines);
        }
    }
    private void emitAdvancePc(int deltaAddress) throws IOException {
        int mark = output.getCursor();
        output.writeByte(DBG_ADVANCE_PC);
        output.writeUnsignedLeb128(deltaAddress);
        address += deltaAddress;
        if (annotateTo != null || debugPrint != null) {
            annotate(output.getCursor() - mark,
                    String.format("%04x: advance pc", address));
        }
        if (DEBUG) {
            System.err.printf("Emitting advance_pc for %d\n", deltaAddress);
        }
    }
    private void emitUnsignedLeb128(int n) throws IOException {
        if (n < 0) {
            throw new RuntimeException(
                    "Signed value where unsigned required: " + n);
        }
        output.writeUnsignedLeb128(n);
    }
    private void emitEndSequence() {
        output.writeByte(DBG_END_SEQUENCE);
        if (annotateTo != null || debugPrint != null) {
            annotate(1, "end sequence");
        }
    }
}

public class DebugInfoDecoder {
    private final byte[] encoded;
    private final ArrayList<PositionEntry> positions;
    private final ArrayList<LocalEntry> locals;
    private final int codesize;
    private final LocalEntry[] lastEntryForReg;
    private final Prototype desc;
    private final boolean isStatic;
    private final DexFile file;
    private final int regSize;
    private int line = 1;
    private int address = 0;
    private final int thisStringIdx;
    DebugInfoDecoder(byte[] encoded, int codesize, int regSize,
            boolean isStatic, CstMethodRef ref, DexFile file) {
        if (encoded == null) {
            throw new NullPointerException("encoded == null");
        }
        this.encoded = encoded;
        this.isStatic = isStatic;
        this.desc = ref.getPrototype();
        this.file = file;
        this.regSize = regSize;
        positions = new ArrayList<PositionEntry>();
        locals = new ArrayList<LocalEntry>();
        this.codesize = codesize;
        lastEntryForReg = new LocalEntry[regSize];
        int idx = -1;
        try {
            idx = file.getStringIds().indexOf(new CstUtf8("this"));
        } catch (IllegalArgumentException ex) {
        }
        thisStringIdx = idx;
    }
    static private class PositionEntry {
        public int address;
        public int line;
        public PositionEntry(int address, int line) {
            this.address = address;
            this.line = line;
        }
    }
    static private class LocalEntry {
        public int address;
        public boolean isStart;
        public int reg;
        public int nameIndex;
        public int typeIndex;
        public int signatureIndex;
        public LocalEntry(int address, boolean isStart, int reg, int nameIndex,
                int typeIndex, int signatureIndex) {
            this.address        = address;
            this.isStart        = isStart;
            this.reg            = reg;
            this.nameIndex      = nameIndex;
            this.typeIndex      = typeIndex;
            this.signatureIndex = signatureIndex;
        }
        public String toString() {
            return String.format("[%x %s v%d %04x %04x %04x]",
                    address, isStart ? "start" : "end", reg,
                    nameIndex, typeIndex, signatureIndex);
        }
    }
    public List<PositionEntry> getPositionList() {
        return positions;
    }
    public List<LocalEntry> getLocals() {
        return locals;
    }
    public void decode() {
        try {
            decode0();
        } catch (Exception ex) {
            throw ExceptionWithContext.withContext(ex,
                    "...while decoding debug info");
        }
    }
    private int readStringIndex(InputStream bs) throws IOException {
        int offsetIndex = readUnsignedLeb128(bs);
        return offsetIndex - 1;
    }
    private int getParamBase() {
        return regSize
                - desc.getParameterTypes().getWordCount() - (isStatic? 0 : 1);
    }
    private void decode0() throws IOException {
        ByteArrayInputStream bs = new ByteArrayInputStream(encoded);
        line = readUnsignedLeb128(bs);
        int szParams = readUnsignedLeb128(bs);
        StdTypeList params = desc.getParameterTypes();
        int curReg = getParamBase();
        if (szParams != params.size()) {
            throw new RuntimeException(
                    "Mismatch between parameters_size and prototype");
        }
        if (!isStatic) {
            LocalEntry thisEntry =
                new LocalEntry(0, true, curReg, thisStringIdx, 0, 0);
            locals.add(thisEntry);
            lastEntryForReg[curReg] = thisEntry;
            curReg++;
        }
        for (int i = 0; i < szParams; i++) {
            Type paramType = params.getType(i);
            LocalEntry le;
            int nameIdx = readStringIndex(bs);
            if (nameIdx == -1) {
                le = new LocalEntry(0, true, curReg, -1, 0, 0);
            } else {
                le = new LocalEntry(0, true, curReg, nameIdx, 0, 0);
            }
            locals.add(le);
            lastEntryForReg[curReg] = le;
            curReg += paramType.getCategory();
        }
        for (;;) {
            int opcode = bs.read();
            if (opcode < 0) {
                throw new RuntimeException
                        ("Reached end of debug stream without "
                                + "encountering end marker");
            }
            switch (opcode) {
                case DBG_START_LOCAL: {
                    int reg = readUnsignedLeb128(bs);
                    int nameIdx = readStringIndex(bs);
                    int typeIdx = readStringIndex(bs);
                    LocalEntry le = new LocalEntry(
                            address, true, reg, nameIdx, typeIdx, 0);
                    locals.add(le);
                    lastEntryForReg[reg] = le;
                }
                break;
                case DBG_START_LOCAL_EXTENDED: {
                    int reg = readUnsignedLeb128(bs);
                    int nameIdx = readStringIndex(bs);
                    int typeIdx = readStringIndex(bs);
                    int sigIdx = readStringIndex(bs);
                    LocalEntry le = new LocalEntry(
                            address, true, reg, nameIdx, typeIdx, sigIdx);
                    locals.add(le);
                    lastEntryForReg[reg] = le;
                }
                break;
                case DBG_RESTART_LOCAL: {
                    int reg = readUnsignedLeb128(bs);
                    LocalEntry prevle;
                    LocalEntry le;
                    try {
                        prevle = lastEntryForReg[reg];
                        if (prevle.isStart) {
                            throw new RuntimeException("nonsensical "
                                    + "RESTART_LOCAL on live register v"
                                    + reg);
                        }
                        le = new LocalEntry(address, true, reg,
                                prevle.nameIndex, prevle.typeIndex, 0);
                    } catch (NullPointerException ex) {
                        throw new RuntimeException(
                                "Encountered RESTART_LOCAL on new v" + reg);
                    }
                    locals.add(le);
                    lastEntryForReg[reg] = le;
                }
                break;
                case DBG_END_LOCAL: {
                    int reg = readUnsignedLeb128(bs);
                    LocalEntry prevle;
                    LocalEntry le;
                    try {
                        prevle = lastEntryForReg[reg];
                        if (!prevle.isStart) {
                            throw new RuntimeException("nonsensical "
                                    + "END_LOCAL on dead register v" + reg);
                        }
                        le = new LocalEntry(address, false, reg,
                                prevle.nameIndex, prevle.typeIndex,
                                prevle.signatureIndex);
                    } catch (NullPointerException ex) {
                        throw new RuntimeException(
                                "Encountered END_LOCAL on new v" + reg);
                    }
                    locals.add(le);
                    lastEntryForReg[reg] = le;
                }
                break;
                case DBG_END_SEQUENCE:
                return;
                case DBG_ADVANCE_PC:
                    address += readUnsignedLeb128(bs);
                break;
                case DBG_ADVANCE_LINE:
                    line += readSignedLeb128(bs);
                break;
                case DBG_SET_PROLOGUE_END:
                break;
                case DBG_SET_EPILOGUE_BEGIN:
                break;
                case DBG_SET_FILE:
                break;
                default:
                    if (opcode < DBG_FIRST_SPECIAL) {
                        throw new RuntimeException(
                                "Invalid extended opcode encountered "
                                        + opcode);
                    }
                    int adjopcode = opcode - DBG_FIRST_SPECIAL;
                    address += adjopcode / DBG_LINE_RANGE;
                    line += DBG_LINE_BASE + (adjopcode % DBG_LINE_RANGE);
                    positions.add(new PositionEntry(address, line));
                break;
            }
        }
    }
    public static void validateEncode(byte[] info, DexFile file,
            CstMethodRef ref, DalvCode code, boolean isStatic) {
        PositionList pl = code.getPositions();
        LocalList ll = code.getLocals();
        DalvInsnList insns = code.getInsns();
        int codeSize = insns.codeSize();
        int countRegisters = insns.getRegistersSize();
        try {
            validateEncode0(info, codeSize, countRegisters,
                    isStatic, ref, file, pl, ll);
        } catch (RuntimeException ex) {
            System.err.println("instructions:");
            insns.debugPrint(System.err, "  ", true);
            System.err.println("local list:");
            ll.debugPrint(System.err, "  ");
            throw ExceptionWithContext.withContext(ex,
                    "while processing " + ref.toHuman());
        }
    }
    private static void validateEncode0(byte[] info, int codeSize,
            int countRegisters, boolean isStatic, CstMethodRef ref,
            DexFile file, PositionList pl, LocalList ll) {
        DebugInfoDecoder decoder
                = new DebugInfoDecoder(info, codeSize, countRegisters,
                    isStatic, ref, file);
        decoder.decode();
        List<PositionEntry> decodedEntries = decoder.getPositionList();
        if (decodedEntries.size() != pl.size()) {
            throw new RuntimeException(
                    "Decoded positions table not same size was "
                    + decodedEntries.size() + " expected " + pl.size());
        }
        for (PositionEntry entry : decodedEntries) {
            boolean found = false;
            for (int i = pl.size() - 1; i >= 0; i--) {
                PositionList.Entry ple = pl.get(i);
                if (entry.line == ple.getPosition().getLine()
                        && entry.address == ple.getAddress()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException ("Could not match position entry: "
                        + entry.address + ", " + entry.line);
            }
        }
        List<LocalEntry> decodedLocals = decoder.getLocals();
        int thisStringIdx = decoder.thisStringIdx;
        int decodedSz = decodedLocals.size();
        int paramBase = decoder.getParamBase();
        for (int i = 0; i < decodedSz; i++) {
            LocalEntry entry = decodedLocals.get(i);
            int idx = entry.nameIndex;
            if ((idx < 0) || (idx == thisStringIdx)) {
                for (int j = i + 1; j < decodedSz; j++) {
                    LocalEntry e2 = decodedLocals.get(j);
                    if (e2.address != 0) {
                        break;
                    }
                    if ((entry.reg == e2.reg) && e2.isStart) {
                        decodedLocals.set(i, e2);
                        decodedLocals.remove(j);
                        decodedSz--;
                        break;
                    }
                }
            }
        }
        int origSz = ll.size();
        int decodeAt = 0;
        boolean problem = false;
        for (int i = 0; i < origSz; i++) {
            LocalList.Entry origEntry = ll.get(i);
            if (origEntry.getDisposition()
                    == LocalList.Disposition.END_REPLACED) {
                continue;
            }
            LocalEntry decodedEntry;
            do {
                decodedEntry = decodedLocals.get(decodeAt);
                if (decodedEntry.nameIndex >= 0) {
                    break;
                }
                decodeAt++;
            } while (decodeAt < decodedSz);
            int decodedAddress = decodedEntry.address;
            if (decodedEntry.reg != origEntry.getRegister()) {
                System.err.println("local register mismatch at orig " + i +
                        " / decoded " + decodeAt);
                problem = true;
                break;
            }
            if (decodedEntry.isStart != origEntry.isStart()) {
                System.err.println("local start/end mismatch at orig " + i +
                        " / decoded " + decodeAt);
                problem = true;
                break;
            }
            if ((decodedAddress != origEntry.getAddress()) 
                    && !((decodedAddress == 0)
                            && (decodedEntry.reg >= paramBase))) {
                System.err.println("local address mismatch at orig " + i +
                        " / decoded " + decodeAt);
                problem = true;
                break;
            }
            decodeAt++;
        }
        if (problem) {
            System.err.println("decoded locals:");
            for (LocalEntry e : decodedLocals) {
                System.err.println("  " + e);
            }
            throw new RuntimeException("local table problem");
        }
    }
    public static int readSignedLeb128(InputStream bs) throws IOException {
        int result = 0;
        int cur;
        int count = 0;
        int signBits = -1;
        do {
            cur = bs.read();
            result |= (cur & 0x7f) << (count * 7);
            signBits <<= 7;
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IOException ("invalid LEB128 sequence");
        }
        if (((signBits >> 1) & result) != 0 ) {
            result |= signBits;
        }
        return result;
    }
    public static int readUnsignedLeb128(InputStream bs) throws IOException {
        int result = 0;
        int cur;
        int count = 0;
        do {
            cur = bs.read();
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IOException ("invalid LEB128 sequence");
        }
        return result;
    }
}

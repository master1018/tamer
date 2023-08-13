public class DebugInfoItem extends OffsettedItem {
    private static final int ALIGNMENT = 1;
    private static final boolean ENABLE_ENCODER_SELF_CHECK = false;
    private final DalvCode code;
    private byte[] encoded;
    private final boolean isStatic;
    private final CstMethodRef ref;
    public DebugInfoItem(DalvCode code, boolean isStatic, CstMethodRef ref) {
        super (ALIGNMENT, -1);
        if (code == null) {
            throw new NullPointerException("code == null");
        }
        this.code = code;
        this.isStatic = isStatic;
        this.ref = ref;
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_DEBUG_INFO_ITEM;
    }
    @Override
    public void addContents(DexFile file) {
    }
    @Override
    protected void place0(Section addedTo, int offset) {
        try {
            encoded = encode(addedTo.getFile(), null, null, null, false);
            setWriteSize(encoded.length);
        } catch (RuntimeException ex) {
            throw ExceptionWithContext.withContext(ex,
                    "...while placing debug info for " + ref.toHuman());
        }
    }
    @Override
    public String toHuman() {
        throw new RuntimeException("unsupported");
    }
    public void annotateTo(DexFile file, AnnotatedOutput out, String prefix) {
        encode(file, prefix, null, out, false);
    }    
    public void debugPrint(PrintWriter out, String prefix) {
        encode(null, prefix, out, null, false);
    }
    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        if (out.annotates()) {
            out.annotate(offsetString() + " debug info");
            encode(file, null, null, out, true);
        }
        out.write(encoded);
    }
    private byte[] encode(DexFile file, String prefix, PrintWriter debugPrint,
            AnnotatedOutput out, boolean consume) {
        byte[] result = encode0(file, prefix, debugPrint, out, consume);
        if (ENABLE_ENCODER_SELF_CHECK && (file != null)) {
            try {
                DebugInfoDecoder.validateEncode(result, file, ref, code,
                        isStatic);
            } catch (RuntimeException ex) {
                encode0(file, "", new PrintWriter(System.err, true), null,
                        false);
                throw ex;
            }
        }
        return result;
    }
    private byte[] encode0(DexFile file, String prefix, PrintWriter debugPrint,
            AnnotatedOutput out, boolean consume) {
        PositionList positions = code.getPositions();
        LocalList locals = code.getLocals();
        DalvInsnList insns = code.getInsns();
        int codeSize = insns.codeSize();
        int regSize = insns.getRegistersSize();
        DebugInfoEncoder encoder =
            new DebugInfoEncoder(positions, locals,
                    file, codeSize, regSize, isStatic, ref);
        byte[] result;
        if ((debugPrint == null) && (out == null)) {
            result = encoder.convert();
        } else {
            result = encoder.convertAndAnnotate(prefix, debugPrint, out,
                    consume);
        }
        return result;
    }
}

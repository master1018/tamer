public abstract class EncodedMember implements ToHuman {
    private final int accessFlags;
    public EncodedMember(int accessFlags) {
        this.accessFlags = accessFlags;
    }
    public final int getAccessFlags() {
        return accessFlags;
    }
    public abstract CstUtf8 getName();
    public abstract void debugPrint(PrintWriter out, boolean verbose);
    public abstract void addContents(DexFile file);
    public abstract int encode(DexFile file, AnnotatedOutput out, 
            int lastIndex, int dumpSeq);
}

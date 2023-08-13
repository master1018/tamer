public final class AnnotationSetItem extends OffsettedItem {
    private static final int ALIGNMENT = 4;
    private static final int ENTRY_WRITE_SIZE = 4;
    private final Annotations annotations;
    private final AnnotationItem[] items;
    public AnnotationSetItem(Annotations annotations) {
        super(ALIGNMENT, writeSize(annotations));
        this.annotations = annotations;
        this.items = new AnnotationItem[annotations.size()];
        int at = 0;
        for (Annotation a : annotations.getAnnotations()) {
            items[at] = new AnnotationItem(a);
            at++;
        }
    }
    private static int writeSize(Annotations annotations) {
        try {
            return (annotations.size() * ENTRY_WRITE_SIZE) + 4;
        } catch (NullPointerException ex) {
            throw new NullPointerException("list == null");
        }
    }
    public Annotations getAnnotations() {
        return annotations;
    }
    @Override
    public int hashCode() {
        return annotations.hashCode();
    }
    @Override
    protected int compareTo0(OffsettedItem other) {
        AnnotationSetItem otherSet = (AnnotationSetItem) other;
        return annotations.compareTo(otherSet.annotations);
    }
    @Override
    public ItemType itemType() {
        return ItemType.TYPE_ANNOTATION_SET_ITEM;
    }
    @Override
    public String toHuman() {
        return annotations.toString();
    }
    public void addContents(DexFile file) {
        MixedItemSection byteData = file.getByteData();
        int size = items.length;
        for (int i = 0; i < size; i++) {
            items[i] = byteData.intern(items[i]);
        }
    }
    @Override
    protected void place0(Section addedTo, int offset) {
        AnnotationItem.sortByTypeIdIndex(items);
    }
    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        int size = items.length;
        if (annotates) {
            out.annotate(0, offsetString() + " annotation set");
            out.annotate(4, "  size: " + Hex.u4(size));
        }
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            AnnotationItem item = items[i];
            int offset = item.getAbsoluteOffset();
            if (annotates) {
                out.annotate(4, "  entries[" + Integer.toHexString(i) + "]: " +
                        Hex.u4(offset));
                items[i].annotateTo(out, "    ");
            }
            out.writeInt(offset);
        }
    }
}

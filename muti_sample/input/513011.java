public final class InnerClassList extends FixedSizeList {
    public InnerClassList(int count) {
        super(count);
    }
    public Item get(int n) {
        return (Item) get0(n);
    }
    public void set(int n, CstType innerClass, CstType outerClass,
                    CstUtf8 innerName, int accessFlags) {
        set0(n, new Item(innerClass, outerClass, innerName, accessFlags));
    }
    public static class Item {
        private final CstType innerClass;
        private final CstType outerClass;
        private final CstUtf8 innerName;
        private final int accessFlags;
        public Item(CstType innerClass, CstType outerClass,
                    CstUtf8 innerName, int accessFlags) {
            if (innerClass == null) {
                throw new NullPointerException("innerClass == null");
            }
            this.innerClass = innerClass;
            this.outerClass = outerClass;
            this.innerName = innerName;
            this.accessFlags = accessFlags;
        }
        public CstType getInnerClass() {
            return innerClass;
        }
        public CstType getOuterClass() {
            return outerClass;
        }
        public CstUtf8 getInnerName() {
            return innerName;
        }
        public int getAccessFlags() {
            return accessFlags;
        }
    }
}

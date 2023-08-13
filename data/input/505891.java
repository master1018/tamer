public final class LocalVariableList extends FixedSizeList {
    public static final LocalVariableList EMPTY = new LocalVariableList(0);
    public static LocalVariableList concat(LocalVariableList list1,
                                           LocalVariableList list2) {
        if (list1 == EMPTY) {
            return list2;
        }
        int sz1 = list1.size();
        int sz2 = list2.size();
        LocalVariableList result = new LocalVariableList(sz1 + sz2);
        for (int i = 0; i < sz1; i++) {
            result.set(i, list1.get(i));
        }
        for (int i = 0; i < sz2; i++) {
            result.set(sz1 + i, list2.get(i));
        }
        result.setImmutable();
        return result;
    }
    public static LocalVariableList mergeDescriptorsAndSignatures(
            LocalVariableList descriptorList,
            LocalVariableList signatureList) {
        int descriptorSize = descriptorList.size();
        LocalVariableList result = new LocalVariableList(descriptorSize);
        for (int i = 0; i < descriptorSize; i++) {
            Item item = descriptorList.get(i);
            Item signatureItem = signatureList.itemToLocal(item);
            if (signatureItem != null) {
                CstUtf8 signature = signatureItem.getSignature();
                item = item.withSignature(signature);
            }
            result.set(i, item);
        }        
        result.setImmutable();
        return result;
    }
    public LocalVariableList(int count) {
        super(count);
    }
    public Item get(int n) {
        return (Item) get0(n);
    }
    public void set(int n, Item item) {
        if (item == null) {
            throw new NullPointerException("item == null");
        }
        set0(n, item);
    }
    public void set(int n, int startPc, int length, CstUtf8 name,
            CstUtf8 descriptor, CstUtf8 signature, int index) {
        set0(n, new Item(startPc, length, name, descriptor, signature, index));
    }
    public Item itemToLocal(Item item) {
        int sz = size();
        for (int i = 0; i < sz; i++) {
            Item one = (Item) get0(i);
            if ((one != null) && one.matchesAllButType(item)) {
                return one;
            }
        }
        return null;
    }
    public Item pcAndIndexToLocal(int pc, int index) {
        int sz = size();
        for (int i = 0; i < sz; i++) {
            Item one = (Item) get0(i);
            if ((one != null) && one.matchesPcAndIndex(pc, index)) {
                return one;
            }
        }
        return null;
    }
    public static class Item {
        private final int startPc;
        private final int length;
        private final CstUtf8 name;
        private final CstUtf8 descriptor;
        private final CstUtf8 signature;
        private final int index;
        public Item(int startPc, int length, CstUtf8 name,
                CstUtf8 descriptor, CstUtf8 signature, int index) {
            if (startPc < 0) {
                throw new IllegalArgumentException("startPc < 0");
            }
            if (length < 0) {
                throw new IllegalArgumentException("length < 0");
            }
            if (name == null) {
                throw new NullPointerException("name == null");
            }
            if ((descriptor == null) && (signature == null)) {
                throw new NullPointerException(
                        "(descriptor == null) && (signature == null)");
            }
            if (index < 0) {
                throw new IllegalArgumentException("index < 0");
            }
            this.startPc = startPc;
            this.length = length;
            this.name = name;
            this.descriptor = descriptor;
            this.signature = signature;
            this.index = index;
        }
        public int getStartPc() {
            return startPc;
        }
        public int getLength() {
            return length;
        }
        public CstUtf8 getDescriptor() {
            return descriptor;
        }
        public LocalItem getLocalItem() {
            return LocalItem.make(name, signature);
        }
        private CstUtf8 getSignature() {
            return signature;
        }
        public int getIndex() {
            return index;
        }
        public Type getType() {
            return Type.intern(descriptor.getString());
        }
        public Item withSignature(CstUtf8 newSignature) {
            return new Item(startPc, length, name, descriptor, newSignature,
                    index);
        }
        public boolean matchesPcAndIndex(int pc, int index) {
            return (index == this.index) &&
                (pc >= startPc) &&
                (pc < (startPc + length));
        }
        public boolean matchesAllButType(Item other) {
            return (startPc == other.startPc)
                && (length == other.length)
                && (index == other.index)
                && name.equals(other.name);
        }
    }
}

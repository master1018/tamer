public final class HeapSegment implements Comparable<HeapSegment> {
    public static class HeapSegmentElement implements Comparable<HeapSegmentElement> {
        public static int SOLIDITY_FREE = 0;
        public static int SOLIDITY_HARD = 1;
        public static int SOLIDITY_SOFT = 2;
        public static int SOLIDITY_WEAK = 3;
        public static int SOLIDITY_PHANTOM = 4;
        public static int SOLIDITY_FINALIZABLE = 5;
        public static int SOLIDITY_SWEEP = 6;
        public static int SOLIDITY_INVALID = -1;
        public static int KIND_OBJECT = 0;
        public static int KIND_CLASS_OBJECT = 1;
        public static int KIND_ARRAY_1 = 2;
        public static int KIND_ARRAY_2 = 3;
        public static int KIND_ARRAY_4 = 4;
        public static int KIND_ARRAY_8 = 5;
        public static int KIND_UNKNOWN = 6;
        public static int KIND_NATIVE = 7;
        public static int KIND_INVALID = -1;
        private static int PARTIAL_MASK = 1 << 7;
        private int mSolidity;
        private int mKind;
        private int mLength;
        public HeapSegmentElement() {
            setSolidity(SOLIDITY_INVALID);
            setKind(KIND_INVALID);
            setLength(-1);
        }
        public HeapSegmentElement(HeapSegment hs)
                throws BufferUnderflowException, ParseException {
            set(hs);
        }
        public HeapSegmentElement set(HeapSegment hs)
                throws BufferUnderflowException, ParseException {
            ByteBuffer data = hs.mUsageData;
            int eState = (int)data.get() & 0x000000ff;
            int eLen = ((int)data.get() & 0x000000ff) + 1;
            while ((eState & PARTIAL_MASK) != 0) {
                int nextState = (int)data.get() & 0x000000ff;
                if ((nextState & ~PARTIAL_MASK) != (eState & ~PARTIAL_MASK)) {
                    throw new ParseException("State mismatch", data.position());
                }
                eState = nextState;
                eLen += ((int)data.get() & 0x000000ff) + 1;
            }
            setSolidity(eState & 0x7);
            setKind((eState >> 3) & 0x7);
            setLength(eLen * hs.mAllocationUnitSize);
            return this;
        }
        public int getSolidity() {
            return mSolidity;
        }
        public void setSolidity(int solidity) {
            this.mSolidity = solidity;
        }
        public int getKind() {
            return mKind;
        }
        public void setKind(int kind) {
            this.mKind = kind;
        }
        public int getLength() {
            return mLength;
        }
        public void setLength(int length) {
            this.mLength = length;
        }
        public int compareTo(HeapSegmentElement other) {
            if (mLength != other.mLength) {
                return mLength < other.mLength ? -1 : 1;
            }
            return 0;
        }
    }
    protected int mHeapId;
    protected int mAllocationUnitSize;
    protected long mStartAddress;
    protected int mOffset;
    protected int mAllocationUnitCount;
    protected ByteBuffer mUsageData;
    private final static long INVALID_START_ADDRESS = -1;
    public HeapSegment(ByteBuffer hpsgData) throws BufferUnderflowException {
        hpsgData.order(ByteOrder.BIG_ENDIAN);
        mHeapId = hpsgData.getInt();
        mAllocationUnitSize = (int) hpsgData.get();
        mStartAddress = (long) hpsgData.getInt() & 0x00000000ffffffffL;
        mOffset = hpsgData.getInt();
        mAllocationUnitCount = hpsgData.getInt();
        mUsageData = hpsgData.slice();
        mUsageData.order(ByteOrder.BIG_ENDIAN);   
    }
    public boolean isValid() {
        return mStartAddress != INVALID_START_ADDRESS;
    }
    public boolean canAppend(HeapSegment other) {
        return isValid() && other.isValid() && mHeapId == other.mHeapId &&
                mAllocationUnitSize == other.mAllocationUnitSize &&
                getEndAddress() == other.getStartAddress();
    }
    public boolean append(HeapSegment other) {
        if (canAppend(other)) {
            int pos = mUsageData.position();
            if (mUsageData.capacity() - mUsageData.limit() <
                    other.mUsageData.limit()) {
                int newSize = mUsageData.limit() + other.mUsageData.limit();
                ByteBuffer newData = ByteBuffer.allocate(newSize * 2);
                mUsageData.rewind();
                newData.put(mUsageData);
                mUsageData = newData;
            }
            other.mUsageData.rewind();
            mUsageData.put(other.mUsageData);
            mUsageData.position(pos);
            mAllocationUnitCount += other.mAllocationUnitCount;
            other.mStartAddress = INVALID_START_ADDRESS;
            other.mUsageData = null;
            return true;
        } else {
            return false;
        }
    }
    public long getStartAddress() {
        return mStartAddress + mOffset;
    }
    public int getLength() {
        return mAllocationUnitSize * mAllocationUnitCount;
    }
    public long getEndAddress() {
        return getStartAddress() + getLength();
    }
    public void rewindElements() {
        if (mUsageData != null) {
            mUsageData.rewind();
        }
    }
    public HeapSegmentElement getNextElement(HeapSegmentElement reuse) {
        try {
            if (reuse != null) {
                return reuse.set(this);
            } else {
                return new HeapSegmentElement(this);
            }
        } catch (BufferUnderflowException ex) {
        } catch (ParseException ex) {
        }
        return null;
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof HeapSegment) {
            return compareTo((HeapSegment) o) == 0;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mHeapId * 31 +
                mAllocationUnitSize * 31 +
                (int) mStartAddress * 31 +
                mOffset * 31 +
                mAllocationUnitCount * 31 +
                mUsageData.hashCode();
    }
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("HeapSegment { heap ").append(mHeapId)
                .append(", start 0x")
                .append(Integer.toHexString((int) getStartAddress()))
                .append(", length ").append(getLength())
                .append(" }");
        return str.toString();
    }
    public int compareTo(HeapSegment other) {
        if (mHeapId != other.mHeapId) {
            return mHeapId < other.mHeapId ? -1 : 1;
        }
        if (getStartAddress() != other.getStartAddress()) {
            return getStartAddress() < other.getStartAddress() ? -1 : 1;
        }
        if (mAllocationUnitSize != other.mAllocationUnitSize) {
            return mAllocationUnitSize < other.mAllocationUnitSize ? -1 : 1;
        }
        if (mStartAddress != other.mStartAddress) {
            return mStartAddress < other.mStartAddress ? -1 : 1;
        }
        if (mOffset != other.mOffset) {
            return mOffset < other.mOffset ? -1 : 1;
        }
        if (mAllocationUnitCount != other.mAllocationUnitCount) {
            return mAllocationUnitCount < other.mAllocationUnitCount ? -1 : 1;
        }
        if (mUsageData != other.mUsageData) {
            return mUsageData.compareTo(other.mUsageData);
        }
        return 0;
    }
}

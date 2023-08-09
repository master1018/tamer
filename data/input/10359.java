public abstract class RenderQueue {
    private static final int BUFFER_SIZE = 32000;
    protected RenderBuffer buf;
    protected Set refSet;
    protected RenderQueue() {
        refSet = new HashSet();
        buf = RenderBuffer.allocate(BUFFER_SIZE);
    }
    public final void lock() {
        SunToolkit.awtLock();
    }
    public final boolean tryLock() {
        return SunToolkit.awtTryLock();
    }
    public final void unlock() {
        SunToolkit.awtUnlock();
    }
    public final void addReference(Object ref) {
        refSet.add(ref);
    }
    public final RenderBuffer getBuffer() {
        return buf;
    }
    public final void ensureCapacity(int opsize) {
        if (buf.remaining() < opsize) {
            flushNow();
        }
    }
    public final void ensureCapacityAndAlignment(int opsize,
                                                 int first8ByteValueOffset)
    {
        ensureCapacity(opsize + 4);
        ensureAlignment(first8ByteValueOffset);
    }
    public final void ensureAlignment(int first8ByteValueOffset) {
        int first8ByteValuePosition = buf.position() + first8ByteValueOffset;
        if ((first8ByteValuePosition & 7) != 0) {
            buf.putInt(BufferedOpCodes.NOOP);
        }
    }
    public abstract void flushNow();
    public abstract void flushAndInvokeNow(Runnable task);
    public void flushNow(int position) {
        buf.position(position);
        flushNow();
    }
}

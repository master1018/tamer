class NIOAccess {
    static long getBasePointer(Buffer b) {
        if (b instanceof DirectBuffer) {
            PlatformAddress address = ((DirectBuffer) b).getEffectiveAddress();
            if (address == null) {
                return 0L;
            }
            return address.toInt() + (b.position() << b._elementSizeShift);
        }
        return 0L;
    }
    static int getRemainingBytes(Buffer b) {
        return (b.limit - b.position) << b._elementSizeShift;
    }
    static Object getBaseArray(Buffer b) {
        return b._array();
    }
    static int getBaseArrayOffset(Buffer b) {
        return b._arrayOffset() << b._elementSizeShift;
    }
}

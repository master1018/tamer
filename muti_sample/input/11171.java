public class XIDGenerator {
    private final static int XID_BUFFER_SIZE = 512;
    int[] xidBuffer = new int[XID_BUFFER_SIZE];
    int currentIndex = XID_BUFFER_SIZE;
    public int getNextXID() {
        if (currentIndex >= XID_BUFFER_SIZE) {
            bufferXIDs(xidBuffer, xidBuffer.length);
            currentIndex = 0;
        }
        return xidBuffer[currentIndex++];
    }
    private static native void bufferXIDs(int[] buffer, int arraySize);
}

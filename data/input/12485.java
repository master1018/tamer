public class ExpandingMap {
    public static void main(String[] args) throws Exception {
        int initialSize = 20480*1024;
        int maximumMapSize = 16*1024*1024;
        int maximumFileSize = 300000000;
        File file = File.createTempFile("exp", "tmp");
        file.deleteOnExit();
        RandomAccessFile f = new RandomAccessFile(file, "rw");
        f.setLength(initialSize);
        FileChannel fc = f.getChannel();
        ByteBuffer[] buffers = new ByteBuffer[128];
        System.out.format("map %d -> %d\n", 0, initialSize);
        buffers[0] = fc.map(FileChannel.MapMode.READ_WRITE, 0, initialSize);
        int currentBuffer = 0;
        int currentSize = initialSize;
        int currentPosition = 0;
        ArrayList<String> junk = new ArrayList<String>();
        while (currentPosition+currentSize < maximumFileSize) {
            int inc = Math.max(1000*1024, (currentPosition+currentSize)/8);
            int size = currentPosition+currentSize+inc;
            f.setLength(size);
            while (currentSize+inc > maximumMapSize) {
                if (currentSize < maximumMapSize) {
                    System.out.format("map %d -> %d\n", currentPosition,
                        (currentPosition + maximumMapSize));
                    buffers[currentBuffer] = fc.map(FileChannel.MapMode.READ_WRITE,
                        currentPosition, maximumMapSize);
                    fillBuffer(buffers[currentBuffer], currentSize);
                }
                currentPosition += maximumMapSize;
                inc = currentSize+inc-maximumMapSize;
                currentSize = 0;
                currentBuffer++;
                if (currentBuffer == buffers.length) {
                    ByteBuffer[] old = buffers;
                    buffers = new ByteBuffer[currentBuffer+currentBuffer/2];
                    System.arraycopy(old, 0, buffers, 0, currentBuffer);                                        }
            }
            currentSize += inc;
            if (currentSize > 0) {
                System.out.format("map %d -> %d\n", currentPosition,
                    (currentPosition + currentSize));
                buffers[currentBuffer] = fc.map(FileChannel.MapMode.READ_WRITE,
                     currentPosition, currentSize);
                fillBuffer(buffers[currentBuffer], currentSize-inc);
            }
            long t = System.currentTimeMillis();
            while (System.currentTimeMillis() < t+500) {
                junk.add(String.valueOf(t));
                if (junk.size() > 100000) junk.clear();
            }
        }
        fc.close();
        for (int i = 0; i < buffers.length; i++)
            buffers[i] = null;
        System.gc();
        Thread.sleep(1000);
        System.out.println("TEST PASSED");
    }
    static void fillBuffer(ByteBuffer buf, int from) {
        int limit = buf.limit();
        for (int i=from; i<limit; i++) {
            buf.put(i, (byte)i);
        }
    }
}

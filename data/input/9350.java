public class AtomicAppend {
    static final Random rand = new Random();
    static FileChannel newFileChannel(File file) throws IOException {
        if (rand.nextBoolean()) {
            return new FileOutputStream(file, true).getChannel();
        } else {
            return FileChannel.open(file.toPath(), APPEND);
        }
    }
    static OutputStream newOutputStream(File file) throws IOException {
        if (rand.nextBoolean()) {
            return new FileOutputStream(file, true);
        } else {
            return Files.newOutputStream(file.toPath(), APPEND);
        }
    }
    static void write(FileChannel fc, int b) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.put((byte)b);
        buf.flip();
        if (rand.nextBoolean()) {
            ByteBuffer[] bufs = new ByteBuffer[1];
            bufs[0] = buf;
            fc.write(bufs);
        } else {
            fc.write(buf);
        }
    }
    public static void main(String[] args) throws Throwable {
        final int nThreads = 16;
        final int writes = 1000;
        final File file = File.createTempFile("foo", null);
        try {
            ExecutorService pool = Executors.newFixedThreadPool(nThreads);
            for (int i = 0; i < nThreads; i++)
                pool.execute(new Runnable() { public void run() {
                    try {
                        if (rand.nextBoolean()) {
                            try (FileChannel fc = newFileChannel(file)) {
                                for (int j=0; j<writes; j++) write(fc, 'x');
                            }
                        } else {
                            try (OutputStream out = newOutputStream(file)) {
                                for (int j = 0; j<writes; j++) out.write('x');
                            }
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }});
            pool.shutdown();
            pool.awaitTermination(1L, TimeUnit.MINUTES);
            if (file.length() != (long) (nThreads * writes))
                throw new RuntimeException("File not expected length");
        } finally {
            file.delete();
        }
    }
}

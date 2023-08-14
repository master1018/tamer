public class ClosedByInterrupt {
    static final int K = 1024;
    static final Random rand = new Random();
    static volatile boolean failed;
    public static void main(String[] args) throws Exception {
        File f = File.createTempFile("blah", null);
        f.deleteOnExit();
        byte[] b = new byte[K*K];
        rand.nextBytes(b);
        ByteBuffer bb = ByteBuffer.wrap(b);
        try (FileChannel fc = new FileOutputStream(f).getChannel()) {
            while (bb.hasRemaining())
                fc.write(bb);
        }
        for (int i=1; i<=16; i++) {
            System.out.format("%d thread(s)%n", i);
            test(f, i);
            if (failed)
                break;
        }
        if (failed)
            throw new RuntimeException("Test failed");
    }
    static void test(File f, int nThreads) throws Exception {
        try (FileChannel fc = new RandomAccessFile(f, "rwd").getChannel()) {
            Thread[] threads = new Thread[nThreads];
            for (int i=0; i<nThreads; i++) {
                boolean interruptible = (i==0);
                ReaderWriter task = new ReaderWriter(fc, interruptible);
                Thread t = new Thread(task);
                t.start();
                threads[i] = t;
            }
            Thread.sleep(500 + rand.nextInt(1000));
            while (fc.isOpen()) {
                threads[0].interrupt();
                Thread.sleep(rand.nextInt(50));
            }
            for (int i=0; i<nThreads; i++) {
                threads[i].join();
            }
        }
    }
    static class ReaderWriter implements Runnable {
        final FileChannel fc;
        final boolean interruptible;
        final boolean writer;
        ReaderWriter(FileChannel fc, boolean interruptible) {
            this.fc = fc;
            this.interruptible = interruptible;
            this.writer = rand.nextBoolean();
        }
        public void run() {
            ByteBuffer bb = ByteBuffer.allocate(K);
            if (writer)
                rand.nextBytes(bb.array());
            try {
                for (;;) {
                    long position = rand.nextInt(K*K - bb.capacity());
                    if (writer) {
                        bb.position(0).limit(bb.capacity());
                        fc.write(bb, position);
                    } else {
                        bb.clear();
                        fc.read(bb, position);
                    }
                    if (!interruptible) {
                        try {
                            Thread.sleep(rand.nextInt(50));
                        } catch (InterruptedException e) {
                            unexpected(e);
                        }
                    }
                }
            } catch (ClosedByInterruptException e) {
                if (interruptible) {
                    if (Thread.interrupted()) {
                        expected(e + " thrown and interrupt status set");
                    } else {
                        unexpected(e + " thrown but interrupt status not set");
                    }
                } else {
                    unexpected(e);
                }
            } catch (ClosedChannelException e) {
                if (interruptible) {
                    unexpected(e);
                } else {
                    expected(e);
                }
            } catch (Exception e) {
                unexpected(e);
            }
        }
    }
    static void expected(Exception e) {
        System.out.format("%s (expected)%n", e);
    }
    static void expected(String msg) {
        System.out.format("%s (expected)%n", msg);
    }
    static void unexpected(Exception e) {
        System.err.format("%s (not expected)%n", e);
        failed = true;
    }
    static void unexpected(String msg) {
        System.err.println(msg);
        failed = true;
    }
}

public class SerializationDeadlock {
    public static void main(final String[] args) throws Exception {
        final Vector<Object> v1 = new Vector<>();
        final Vector<Object> v2 = new Vector<>();
        final TestBarrier testStart = new TestBarrier(3);
        v1.add(testStart);
        v1.add(v2);
        v2.add(testStart);
        v2.add(v1);
        final CyclicBarrier testEnd = new CyclicBarrier(3);
        final TestThread t1 = new TestThread(v1, testEnd);
        final TestThread t2 = new TestThread(v2, testEnd);
        t1.start();
        t2.start();
        testStart.await();
        System.out.println("Waiting for Vector serialization to complete ...");
        System.out.println("(This test will hang if serialization deadlocks)");
        testEnd.await();
        System.out.println("Test PASSED: serialization completed successfully");
        TestThread.handleExceptions();
    }
    static final class TestBarrier extends CyclicBarrier
            implements Serializable {
        public TestBarrier(final int count) {
            super(count);
        }
        private void writeObject(final ObjectOutputStream oos)
                throws IOException {
            oos.defaultWriteObject();
            try {
                await();
            } catch (final Exception e) {
                throw new IOException("Test ERROR: Unexpected exception caught", e);
            }
        }
    }
    static final class TestThread extends Thread {
        private static final List<Exception> exceptions = new ArrayList<>();
        private final Vector vector;
        private final CyclicBarrier testEnd;
        public TestThread(final Vector vector, final CyclicBarrier testEnd) {
            this.vector = vector;
            this.testEnd = testEnd;
            setDaemon(true);
        }
        public void run() {
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(vector);
                oos.close();
            } catch (final IOException ioe) {
                addException(ioe);
            } finally {
                try {
                    testEnd.await();
                } catch (Exception e) {
                    addException(e);
                }
            }
        }
        private static synchronized void addException(final Exception exception) {
            exceptions.add(exception);
        }
        public static synchronized void handleExceptions() {
            if (false == exceptions.isEmpty()) {
                throw new RuntimeException(getErrorText(exceptions));
            }
        }
        private static String getErrorText(final List<Exception> exceptions) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            pw.println("Test ERROR: Unexpected exceptions thrown on test threads:");
            for (Exception exception : exceptions) {
                pw.print("\t");
                pw.println(exception);
                for (StackTraceElement element : exception.getStackTrace()) {
                    pw.print("\t\tat ");
                    pw.println(element);
                }
            }
            pw.close();
            return sw.toString();
        }
    }
}

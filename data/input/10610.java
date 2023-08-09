public class ChunkedEncodingWithProgressMonitorTest {
    public static void main (String[] args) throws Exception {
        ProgressMonitor.setMeteringPolicy(new MyProgressMeteringPolicy());
        ProgressListener listener = new MyProgressListener();
        ProgressMonitor.getDefault().addProgressListener(listener);
        ChunkedEncodingTest.test();
        ProgressMonitor.getDefault().removeProgressListener(listener);
        if (flag.cardinality() != 3) {
            throw new RuntimeException("All three methods in ProgressListener"+
                                       " should be called. Yet the number of"+
                                       " methods actually called are "+
                                       flag.cardinality());
        }
    }
    static class MyProgressMeteringPolicy implements ProgressMeteringPolicy {
        public boolean shouldMeterInput(URL url, String method) {
            return true;
        }
        public int getProgressUpdateThreshold() {
            return 8192;
        }
    }
    static BitSet flag = new BitSet(3);
    static class MyProgressListener implements ProgressListener {
        public void progressStart(ProgressEvent evt) {
            System.out.println("start: received progressevent "+evt);
            if (flag.nextSetBit(0) == -1)
                flag.set(0);
        }
        public void progressUpdate(ProgressEvent evt) {
            System.out.println("update: received progressevent "+evt);
            if (flag.nextSetBit(1) == -1)
                flag.set(1);
        }
        public void progressFinish(ProgressEvent evt) {
            System.out.println("finish: received progressevent "+evt);
            if (flag.nextSetBit(2) == -1)
                flag.set(2);
        }
    }
}

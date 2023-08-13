public class RandomIDs {
    private static final int COUNT = 10000;
    public static void main(String[] args) throws Exception {
        boolean shouldBeRandom = false;
        boolean shouldBeSequential = false;
        String usage = "Usage: java RandomIDs [random|sequential]";
        if (args.length != 1) {
            System.err.println(usage);
            throw new Error("wrong number of arguments");
        } else if (args[0].equals("random")) {
            shouldBeRandom = true;
        } else if (args[0].equals("sequential")) {
            shouldBeSequential = true;
        } else {
            System.err.println(usage);
            throw new Error("invalid argument");
        }
        System.err.println("\nRegression test for bug 6364692\n");
        String propertyValue = System.getProperty("java.rmi.server.randomIDs");
        System.err.println(
            "Value of java.rmi.server.randomIDs system property: " +
            (propertyValue != null ? "\"" + propertyValue + "\"" : null));
        System.err.println(
            "Expecting object numbers of unique ObjIDs to be: " + args[0]);
        final long[] objnums = new long[COUNT];
        for (int i = 0; i < COUNT; i++) {
            final int j = i;
            class Escape extends RuntimeException { }
            try {
                new ObjID().write(new ObjectOutputStream(new OutputStream() {
                    public void write(int b) { }
                }) {
                    public void writeLong(long val) throws IOException {
                        objnums[j] = val;
                        throw new Escape();
                    }
                });
                throw new Error("writeLong not invoked");
            } catch (Escape e) {
            }
        }
        if (shouldBeRandom) {
            int bitCount = 0;
            int piHitCount = 0;
            for (int i = 0; i < COUNT; i++) {
                bitCount += Long.bitCount(objnums[i]);
                double x = ((double) (objnums[i] >>> 32)) / (1L << 32);
                double y = ((double) (objnums[i] & 0xFFFFFFFFL)) / (1L << 32);
                if (((x * x) + (y * y)) <= 1.0) {
                    piHitCount++;
                }
            }
            int bitCountTarget = COUNT * 32;
            double bitCountError =
                ((double) (bitCount - bitCountTarget)) / bitCountTarget;
            if (Math.abs(bitCountError) > 0.05) { 
                throw new Error("TEST FAILED: " +
                                "bitCount == " + bitCount);
            }
            double piEstimate = ((double) piHitCount / COUNT) * 4.0;
            double piEstimateError = (piEstimate - Math.PI) / Math.PI;
            if (Math.abs(piEstimateError) > 0.05) { 
                throw new Error("TEST FAILED: " +
                                "piEstimate == " + piEstimate);
            }
        }
        if (shouldBeSequential) {
            long first = objnums[0];
            if (first != 0) {
                throw new Error("TEST FAILED: " +
                                "first object number == " + first +
                                " (not zero)");
            }
            for (int i = 1; i < COUNT; i++) {
                if (objnums[i] != first + i) {
                    throw new Error("TEST FAILED: first == " + first + ", " +
                                    "objnums[" + i + "] == " + objnums[i]);
                }
            }
        }
        System.err.println("TEST PASSED");
    }
}

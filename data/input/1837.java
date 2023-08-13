public class MergeDemo {
    private final Random random = new Random(759123751834L);
    private static final int ITERATIONS = 10;
    private static class Range {
        private final int start;
        private final int step;
        private final int iterations;
        private Range(int start, int step, int iterations) {
            this.start = start;
            this.step = step;
            this.iterations = iterations;
        }
        public static Range parse(String[] args, int start) {
            if (args.length < start + 3) {
                throw new IllegalArgumentException("Too few elements in array");
            }
            return new Range(parseInt(args[start]), parseInt(args[start + 1]), parseInt(args[start + 2]));
        }
        public int get(int iteration) {
            return start + (step * iteration);
        }
        public int getIterations() {
            return iterations;
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(start).append(" ").append(step).append(" ").append(iterations);
            return builder.toString();
        }
    }
    private static class Configuration {
        private final Range sizes;
        private final Range parallelism;
        private final static Configuration defaultConfig = new Configuration(new Range(20000, 20000, 10),
                new Range(2, 2, 10));
        private Configuration(Range sizes, Range parallelism) {
            this.sizes = sizes;
            this.parallelism = parallelism;
        }
        public static Configuration parse(String[] args) {
            if (args.length == 0) {
                return defaultConfig;
            } else {
                try {
                    if (args.length == 6) {
                        return new Configuration(Range.parse(args, 0), Range.parse(args, 3));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("MergeExample: error: Argument was not a number.");
                }
                System.err.println("MergeExample <size start> <size step> <size steps> <parallel start> <parallel step>" +
                        " <parallel steps>");
                System.err.println("example: MergeExample 20000 10000 3 1 1 4");
                System.err.println("example: will run with arrays of sizes 20000, 30000, 40000" +
                        " and parallelism: 1, 2, 3, 4");
                return null;
            }
        }
        private long[][] createTimesArray() {
            return new long[sizes.getIterations()][parallelism.getIterations()];
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("");
            if (this == defaultConfig) {
                builder.append("Default configuration. ");
            }
            builder.append("Running with parameters: ");
            builder.append(sizes);
            builder.append(" ");
            builder.append(parallelism);
            return builder.toString();
        }
    }
    private int[] generateArray(int elements) {
        int[] array = new int[elements];
        for (int i = 0; i < elements; ++i) {
            array[i] = random.nextInt();
        }
        return array;
    }
    private void run(Configuration config) {
        Range sizes = config.sizes;
        Range parallelism = config.parallelism;
        warmup();
        long[][] times = config.createTimesArray();
        for (int size = 0; size < sizes.getIterations(); size++) {
            runForSize(parallelism, sizes.get(size), times, size);
        }
        printResults(sizes, parallelism, times);
    }
    private void printResults(Range sizes, Range parallelism, long[][] times) {
        System.out.println("Time in milliseconds. Y-axis: number of elements. X-axis parallelism used.");
        long[] sums = new long[times[0].length];
        System.out.format("%8s  ", "");
        for (int i = 0; i < times[0].length; i++) {
            System.out.format("%4d ", parallelism.get(i));
        }
        System.out.println("");
        for (int size = 0; size < sizes.getIterations(); size++) {
            System.out.format("%8d: ", sizes.get(size));
            for (int i = 0; i < times[size].length; i++) {
                sums[i] += times[size][i];
                System.out.format("%4d ", times[size][i]);
            }
            System.out.println("");
        }
        System.out.format("%8s: ", "Total");
        for (long sum : sums) {
            System.out.format("%4d ", sum);
        }
        System.out.println("");
    }
    private void runForSize(Range parallelism, int elements, long[][] times, int size) {
        for (int step = 0; step < parallelism.getIterations(); step++) {
            long time = runForParallelism(ITERATIONS, elements, parallelism.get(step));
            times[size][step] = time;
        }
    }
    private long runForParallelism(int iterations, int elements, int parallelism) {
        MergeSort mergeSort = new MergeSort(parallelism);
        long[] times = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            System.gc();
            long start = System.currentTimeMillis();
            mergeSort.sort(generateArray(elements));
            times[i] = System.currentTimeMillis() - start;
        }
        return medianValue(times);
    }
    private long medianValue(long[] times) {
        if (times.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }
        Arrays.sort(times.clone());
        long median = times[times.length / 2];
        if (times.length > 1 && times.length % 2 != 0) {
            median = (median + times[times.length / 2 + 1]) / 2;
        }
        return median;
    }
    private void warmup() {
        MergeSort mergeSort = new MergeSort(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < 1000; i++) {
            mergeSort.sort(generateArray(1000));
        }
    }
    public static void main(String[] args) {
        Configuration configuration = Configuration.parse(args);
        if (configuration == null) {
            System.exit(1);
        }
        System.out.println(configuration);
        new MergeDemo().run(configuration);
    }
}

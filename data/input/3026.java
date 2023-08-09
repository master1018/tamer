public class MergeSortTest {
    private Random random;
    private MergeSort target;
    public MergeSortTest(Random random, MergeSort target) {
        this.random = random;
        this.target = target;
    }
    public static void main(String[] args) {
        MergeSortTest test = new MergeSortTest(new Random(), new MergeSort(Runtime.getRuntime().availableProcessors() * 4));
        test.run();
    }
    private int[] generateArray(int elements) {
        int[] array = new int[elements];
        for (int i = 0; i < array.length; ++i) {
            array[i] = random.nextInt(10);
        }
        return array;
    }
    private void run() {
        testSort();
        testSortSingle();
        testSortEmpty();
        testLong();
    }
    public void testLong() {
        for (int i = 0; i < 1000; ++i) {
            int elements = 1 + i * 100;
            int[] array = generateArray(elements);
            int[] copy = Arrays.copyOf(array, array.length);
            Arrays.sort(copy);
            target.sort(array);
            assertEqual(copy, array);
        }
   }
    private void testSortEmpty() {
        int[] array = { };
        target.sort(array);
        assertEqual(new int[] { }, array);
    }
    private void testSortSingle() {
        int[] array = { 1 };
        target.sort(array);
        assertEqual(new int[] { 1 }, array);
    }
    private void testSort() {
        int[] array = { 7, 3, 9, 0, -6, 12, 54, 3, -6, 88, 1412};
        target.sort(array);
        assertEqual(new int[] { -6, -6, 0, 3, 3, 7, 9, 12, 54, 88, 1412 }, array);
    }
    private void assertEqual(int[] expected, int[] array) {
        if (!Arrays.equals(expected, array)) {
            throw new RuntimeException("Invalid sorted array!");
        }
    }
}

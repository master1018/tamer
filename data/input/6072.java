public class Correct {
    static Random rnd = new Random();
    static final int ITERATIONS = 1000;
    static final int TEST_SIZE = 1000;
    public static void main(String[] args) throws Exception {
        Object[] array1 = null;
        Object[] array2 = null;
        for (int i=0; i<ITERATIONS; i++) {
            int size = rnd.nextInt(TEST_SIZE) + 1;
            array1 = (Object[])getIntegerArray(size);
            array2 = (Object[])array1.clone();
            Arrays.sort(array1, array1.length/3, array1.length/2);
            stupidSort(array2, array2.length/3, array2.length/2);
            if(!Arrays.equals(array1, array2))
                throw new RuntimeException("failed!");
        }
        for (int i=0; i<ITERATIONS; i++) {
            int size = rnd.nextInt(TEST_SIZE) + 1;
            array1 = (Object[])getIntegerArray(size);
            array2 = (Object[])array1.clone();
            Arrays.sort(array1, array1.length/3, array1.length/2, TEST_ORDER);
            stupidSort(array2, array2.length/3, array2.length/2);
            if(!Arrays.equals(array1, array2))
                throw new RuntimeException("failed!");
        }
    }
    static Integer[] getIntegerArray(int size) throws Exception {
        Integer[] blah = new Integer[size];
        for (int x=0; x<size; x++) {
            blah[x] = new Integer(rnd.nextInt());
        }
        return blah;
    }
    static void stupidSort(Object[] a1, int from, int to) throws Exception {
        for (int x=from; x<to; x++) {
            Object lowest = new Integer(Integer.MAX_VALUE);
            int lowestIndex = 0;
            for (int y=x; y<to; y++) {
                if (((Comparable)a1[y]).compareTo((Comparable)lowest) < 0) {
                    lowest = a1[y];
                    lowestIndex = y;
                }
            }
            if (lowestIndex != x) {
                swap(a1, x, lowestIndex);
            }
        }
    }
    static void swap(Object x[], int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
    private static final Comparator TEST_ORDER = new IntegerComparator();
    private static class IntegerComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Comparable c1 = (Comparable)o1;
            Comparable c2 = (Comparable)o2;
            return  c1.compareTo(c2);
        }
    }
}

public class test {
    public static final int binarySearch(Object[] a, Object key, Comparator cp, int begin, int end) {
        int x1 = begin;
        int x2 = end;
        int i = x1 + (x2 - x1) / 2, c;
        while (x1 < x2) {
            c = cp.compare(a[i], key);
            if (c == 0) {
                return i;
            } else if (c < 0) {
                x1 = i + 1;
            } else {
                x2 = i;
            }
            i = x1 + (x2 - x1) / 2;
        }
        return -1 * (i + 1);
    }
}

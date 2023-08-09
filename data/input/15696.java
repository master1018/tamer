public class VectorSorter {
    static public void sort(Vector<Object> v, Comparer c)  {
        quickSort(v, c, 0, v.size()-1);
    }
    static public void sortVectorOfStrings(Vector<Object> v) {
        sort(v, new Comparer() {
            public int compare(Object lhs, Object rhs) {
                return ((String) lhs).compareTo((String) rhs);
            }
        });
    }
    static private void swap(Vector<Object> v, int a, int b) {
        Object tmp = v.elementAt(a);
        v.setElementAt(v.elementAt(b), a);
        v.setElementAt(tmp, b);
    }
    static private void quickSort(Vector<Object> v, Comparer c, int from, int to) {
        if (to <= from)
            return;
        int mid = (from + to) / 2;
        if (mid != from)
            swap(v, mid, from);
        Object pivot = v.elementAt(from);
        int highestBelowPivot = from - 1;
        int low = from+1;
        int high = to;
        while (low <= high) {
            int cmp = c.compare(v.elementAt(low), pivot);
            if (cmp <= 0) {    
                if (cmp < 0) {
                    highestBelowPivot = low;
                }
                low++;
            } else {
                int c2;
                for (;;) {
                    c2 = c.compare(v.elementAt(high), pivot);
                    if (c2 > 0) {
                        high--;
                        if (low > high) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (low <= high) {
                    swap(v, low, high);
                    if (c2 < 0) {
                        highestBelowPivot = low;
                    }
                    low++;
                    high--;
                }
            }
        }
        if (highestBelowPivot > from) {
            swap(v, from, highestBelowPivot);
            quickSort(v, c, from, highestBelowPivot-1);
        }
        quickSort(v, c, high+1, to);
    }
}

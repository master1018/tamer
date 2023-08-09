final class DualPivotQuicksort {
    private DualPivotQuicksort() {}
    private static final int MAX_RUN_COUNT = 67;
    private static final int MAX_RUN_LENGTH = 33;
    private static final int QUICKSORT_THRESHOLD = 286;
    private static final int INSERTION_SORT_THRESHOLD = 47;
    private static final int COUNTING_SORT_THRESHOLD_FOR_BYTE = 29;
    private static final int COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR = 3200;
    public static void sort(int[] a) {
        sort(a, 0, a.length - 1);
    }
    public static void sort(int[] a, int left, int right) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { 
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { 
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    int t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { 
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }
        if (run[count] == right++) { 
            run[++count] = right;
        } else if (count == 1) { 
            return;
        }
        int[] b; byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);
        if (odd == 0) {
            b = a; a = new int[b.length];
            for (int i = left - 1; ++i < right; a[i] = b[i]);
        } else {
            b = new int[a.length];
        }
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p] <= a[q]) {
                        b[i] = a[p++];
                    } else {
                        b[i] = a[q++];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i] = a[i]
                );
                run[++last] = right;
            }
            int[] t = a; a = b; b = t;
        }
    }
    private static void sort(int[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                for (int i = left, j = i; i < right; j = ++i) {
                    int ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);
                for (int k = left; ++left <= right; k = ++left) {
                    int a1 = a[k], a2 = a[left];
                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;
                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                int last = a[right];
                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (left + right) >>> 1; 
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) { int t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
        if (a[e3] < a[e2]) { int t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { int t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { int t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }
        int less  = left;  
        int great = right; 
        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            int pivot1 = a[e2];
            int pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);
            outer:
            for (int k = less - 1; ++k <= great; ) {
                int ak = a[k];
                if (ak < pivot1) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    ++less;
                }
                while (a[great] == pivot2) {
                    --great;
                }
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    int ak = a[k];
                    if (ak == pivot1) { 
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { 
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { 
                            a[k] = a[less];
                            a[less] = pivot1;
                            ++less;
                        } else { 
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }
            sort(a, less, great, false);
        } else { 
            int pivot = a[e3];
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                int ak = a[k];
                if (ak < pivot) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { 
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
    public static void sort(long[] a) {
        sort(a, 0, a.length - 1);
    }
    public static void sort(long[] a, int left, int right) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { 
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { 
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    long t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { 
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }
        if (run[count] == right++) { 
            run[++count] = right;
        } else if (count == 1) { 
            return;
        }
        long[] b; byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);
        if (odd == 0) {
            b = a; a = new long[b.length];
            for (int i = left - 1; ++i < right; a[i] = b[i]);
        } else {
            b = new long[a.length];
        }
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p] <= a[q]) {
                        b[i] = a[p++];
                    } else {
                        b[i] = a[q++];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i] = a[i]
                );
                run[++last] = right;
            }
            long[] t = a; a = b; b = t;
        }
    }
    private static void sort(long[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                for (int i = left, j = i; i < right; j = ++i) {
                    long ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);
                for (int k = left; ++left <= right; k = ++left) {
                    long a1 = a[k], a2 = a[left];
                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;
                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                long last = a[right];
                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (left + right) >>> 1; 
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) { long t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
        if (a[e3] < a[e2]) { long t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { long t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { long t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }
        int less  = left;  
        int great = right; 
        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            long pivot1 = a[e2];
            long pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);
            outer:
            for (int k = less - 1; ++k <= great; ) {
                long ak = a[k];
                if (ak < pivot1) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    ++less;
                }
                while (a[great] == pivot2) {
                    --great;
                }
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    long ak = a[k];
                    if (ak == pivot1) { 
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { 
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { 
                            a[k] = a[less];
                            a[less] = pivot1;
                            ++less;
                        } else { 
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }
            sort(a, less, great, false);
        } else { 
            long pivot = a[e3];
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                long ak = a[k];
                if (ak < pivot) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { 
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
    public static void sort(short[] a) {
        sort(a, 0, a.length - 1);
    }
    public static void sort(short[] a, int left, int right) {
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
            int[] count = new int[NUM_SHORT_VALUES];
            for (int i = left - 1; ++i <= right;
                count[a[i] - Short.MIN_VALUE]++
            );
            for (int i = NUM_SHORT_VALUES, k = right + 1; k > left; ) {
                while (count[--i] == 0);
                short value = (short) (i + Short.MIN_VALUE);
                int s = count[i];
                do {
                    a[--k] = value;
                } while (--s > 0);
            }
        } else { 
            doSort(a, left, right);
        }
    }
    private static final int NUM_SHORT_VALUES = 1 << 16;
    private static void doSort(short[] a, int left, int right) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { 
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { 
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    short t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { 
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }
        if (run[count] == right++) { 
            run[++count] = right;
        } else if (count == 1) { 
            return;
        }
        short[] b; byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);
        if (odd == 0) {
            b = a; a = new short[b.length];
            for (int i = left - 1; ++i < right; a[i] = b[i]);
        } else {
            b = new short[a.length];
        }
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p] <= a[q]) {
                        b[i] = a[p++];
                    } else {
                        b[i] = a[q++];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i] = a[i]
                );
                run[++last] = right;
            }
            short[] t = a; a = b; b = t;
        }
    }
    private static void sort(short[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                for (int i = left, j = i; i < right; j = ++i) {
                    short ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);
                for (int k = left; ++left <= right; k = ++left) {
                    short a1 = a[k], a2 = a[left];
                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;
                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                short last = a[right];
                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (left + right) >>> 1; 
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) { short t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
        if (a[e3] < a[e2]) { short t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { short t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { short t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }
        int less  = left;  
        int great = right; 
        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            short pivot1 = a[e2];
            short pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);
            outer:
            for (int k = less - 1; ++k <= great; ) {
                short ak = a[k];
                if (ak < pivot1) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    ++less;
                }
                while (a[great] == pivot2) {
                    --great;
                }
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    short ak = a[k];
                    if (ak == pivot1) { 
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { 
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { 
                            a[k] = a[less];
                            a[less] = pivot1;
                            ++less;
                        } else { 
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }
            sort(a, less, great, false);
        } else { 
            short pivot = a[e3];
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                short ak = a[k];
                if (ak < pivot) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { 
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
    public static void sort(char[] a) {
        sort(a, 0, a.length - 1);
    }
    public static void sort(char[] a, int left, int right) {
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
            int[] count = new int[NUM_CHAR_VALUES];
            for (int i = left - 1; ++i <= right;
                count[a[i]]++
            );
            for (int i = NUM_CHAR_VALUES, k = right + 1; k > left; ) {
                while (count[--i] == 0);
                char value = (char) i;
                int s = count[i];
                do {
                    a[--k] = value;
                } while (--s > 0);
            }
        } else { 
            doSort(a, left, right);
        }
    }
    private static final int NUM_CHAR_VALUES = 1 << 16;
    private static void doSort(char[] a, int left, int right) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { 
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { 
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    char t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { 
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }
        if (run[count] == right++) { 
            run[++count] = right;
        } else if (count == 1) { 
            return;
        }
        char[] b; byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);
        if (odd == 0) {
            b = a; a = new char[b.length];
            for (int i = left - 1; ++i < right; a[i] = b[i]);
        } else {
            b = new char[a.length];
        }
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p] <= a[q]) {
                        b[i] = a[p++];
                    } else {
                        b[i] = a[q++];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i] = a[i]
                );
                run[++last] = right;
            }
            char[] t = a; a = b; b = t;
        }
    }
    private static void sort(char[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                for (int i = left, j = i; i < right; j = ++i) {
                    char ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);
                for (int k = left; ++left <= right; k = ++left) {
                    char a1 = a[k], a2 = a[left];
                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;
                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                char last = a[right];
                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (left + right) >>> 1; 
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) { char t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
        if (a[e3] < a[e2]) { char t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { char t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { char t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }
        int less  = left;  
        int great = right; 
        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            char pivot1 = a[e2];
            char pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);
            outer:
            for (int k = less - 1; ++k <= great; ) {
                char ak = a[k];
                if (ak < pivot1) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    ++less;
                }
                while (a[great] == pivot2) {
                    --great;
                }
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    char ak = a[k];
                    if (ak == pivot1) { 
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { 
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { 
                            a[k] = a[less];
                            a[less] = pivot1;
                            ++less;
                        } else { 
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }
            sort(a, less, great, false);
        } else { 
            char pivot = a[e3];
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                char ak = a[k];
                if (ak < pivot) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { 
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = pivot;
                    }
                    a[great] = ak;
                    --great;
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
    private static final int NUM_BYTE_VALUES = 1 << 8;
    public static void sort(byte[] a) {
        sort(a, 0, a.length - 1);
    }
    public static void sort(byte[] a, int left, int right) {
        if (right - left > COUNTING_SORT_THRESHOLD_FOR_BYTE) {
            int[] count = new int[NUM_BYTE_VALUES];
            for (int i = left - 1; ++i <= right;
                count[a[i] - Byte.MIN_VALUE]++
            );
            for (int i = NUM_BYTE_VALUES, k = right + 1; k > left; ) {
                while (count[--i] == 0);
                byte value = (byte) (i + Byte.MIN_VALUE);
                int s = count[i];
                do {
                    a[--k] = value;
                } while (--s > 0);
            }
        } else { 
            for (int i = left, j = i; i < right; j = ++i) {
                byte ai = a[i + 1];
                while (ai < a[j]) {
                    a[j + 1] = a[j];
                    if (j-- == left) {
                        break;
                    }
                }
                a[j + 1] = ai;
            }
        }
    }
    public static void sort(float[] a) {
        sort(a, 0, a.length - 1);
    }
    public static void sort(float[] a, int left, int right) {
        while (left <= right && Float.isNaN(a[right])) {
            --right;
        }
        for (int k = right; --k >= left; ) {
            float ak = a[k];
            if (ak != ak) { 
                a[k] = a[right];
                a[right] = ak;
                --right;
            }
        }
        doSort(a, left, right);
        int hi = right;
        while (left < hi) {
            int middle = (left + hi) >>> 1;
            float middleValue = a[middle];
            if (middleValue < 0.0f) {
                left = middle + 1;
            } else {
                hi = middle;
            }
        }
        while (left <= right && Float.floatToRawIntBits(a[left]) < 0) {
            ++left;
        }
        for (int k = left, p = left - 1; ++k <= right; ) {
            float ak = a[k];
            if (ak != 0.0f) {
                break;
            }
            if (Float.floatToRawIntBits(ak) < 0) { 
                a[k] = 0.0f;
                a[++p] = -0.0f;
            }
        }
    }
    private static void doSort(float[] a, int left, int right) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { 
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { 
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    float t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { 
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }
        if (run[count] == right++) { 
            run[++count] = right;
        } else if (count == 1) { 
            return;
        }
        float[] b; byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);
        if (odd == 0) {
            b = a; a = new float[b.length];
            for (int i = left - 1; ++i < right; a[i] = b[i]);
        } else {
            b = new float[a.length];
        }
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p] <= a[q]) {
                        b[i] = a[p++];
                    } else {
                        b[i] = a[q++];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i] = a[i]
                );
                run[++last] = right;
            }
            float[] t = a; a = b; b = t;
        }
    }
    private static void sort(float[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                for (int i = left, j = i; i < right; j = ++i) {
                    float ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);
                for (int k = left; ++left <= right; k = ++left) {
                    float a1 = a[k], a2 = a[left];
                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;
                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                float last = a[right];
                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (left + right) >>> 1; 
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) { float t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
        if (a[e3] < a[e2]) { float t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { float t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { float t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }
        int less  = left;  
        int great = right; 
        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            float pivot1 = a[e2];
            float pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);
            outer:
            for (int k = less - 1; ++k <= great; ) {
                float ak = a[k];
                if (ak < pivot1) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    ++less;
                }
                while (a[great] == pivot2) {
                    --great;
                }
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    float ak = a[k];
                    if (ak == pivot1) { 
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { 
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { 
                            a[k] = a[less];
                            a[less] = a[great];
                            ++less;
                        } else { 
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }
            sort(a, less, great, false);
        } else { 
            float pivot = a[e3];
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                float ak = a[k];
                if (ak < pivot) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { 
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
    public static void sort(double[] a) {
        sort(a, 0, a.length - 1);
    }
    public static void sort(double[] a, int left, int right) {
        while (left <= right && Double.isNaN(a[right])) {
            --right;
        }
        for (int k = right; --k >= left; ) {
            double ak = a[k];
            if (ak != ak) { 
                a[k] = a[right];
                a[right] = ak;
                --right;
            }
        }
        doSort(a, left, right);
        int hi = right;
        while (left < hi) {
            int middle = (left + hi) >>> 1;
            double middleValue = a[middle];
            if (middleValue < 0.0d) {
                left = middle + 1;
            } else {
                hi = middle;
            }
        }
        while (left <= right && Double.doubleToRawLongBits(a[left]) < 0) {
            ++left;
        }
        for (int k = left, p = left - 1; ++k <= right; ) {
            double ak = a[k];
            if (ak != 0.0d) {
                break;
            }
            if (Double.doubleToRawLongBits(ak) < 0) { 
                a[k] = 0.0d;
                a[++p] = -0.0d;
            }
        }
    }
    private static void doSort(double[] a, int left, int right) {
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;
        for (int k = left; k < right; run[count] = k) {
            if (a[k] < a[k + 1]) { 
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { 
                while (++k <= right && a[k - 1] >= a[k]);
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    double t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            } else { 
                for (int m = MAX_RUN_LENGTH; ++k <= right && a[k - 1] == a[k]; ) {
                    if (--m == 0) {
                        sort(a, left, right, true);
                        return;
                    }
                }
            }
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }
        if (run[count] == right++) { 
            run[++count] = right;
        } else if (count == 1) { 
            return;
        }
        double[] b; byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);
        if (odd == 0) {
            b = a; a = new double[b.length];
            for (int i = left - 1; ++i < right; a[i] = b[i]);
        } else {
            b = new double[a.length];
        }
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p] <= a[q]) {
                        b[i] = a[p++];
                    } else {
                        b[i] = a[q++];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i] = a[i]
                );
                run[++last] = right;
            }
            double[] t = a; a = b; b = t;
        }
    }
    private static void sort(double[] a, int left, int right, boolean leftmost) {
        int length = right - left + 1;
        if (length < INSERTION_SORT_THRESHOLD) {
            if (leftmost) {
                for (int i = left, j = i; i < right; j = ++i) {
                    double ai = a[i + 1];
                    while (ai < a[j]) {
                        a[j + 1] = a[j];
                        if (j-- == left) {
                            break;
                        }
                    }
                    a[j + 1] = ai;
                }
            } else {
                do {
                    if (left >= right) {
                        return;
                    }
                } while (a[++left] >= a[left - 1]);
                for (int k = left; ++left <= right; k = ++left) {
                    double a1 = a[k], a2 = a[left];
                    if (a1 < a2) {
                        a2 = a1; a1 = a[left];
                    }
                    while (a1 < a[--k]) {
                        a[k + 2] = a[k];
                    }
                    a[++k + 1] = a1;
                    while (a2 < a[--k]) {
                        a[k + 1] = a[k];
                    }
                    a[k + 1] = a2;
                }
                double last = a[right];
                while (last < a[--right]) {
                    a[right + 1] = a[right];
                }
                a[right + 1] = last;
            }
            return;
        }
        int seventh = (length >> 3) + (length >> 6) + 1;
        int e3 = (left + right) >>> 1; 
        int e2 = e3 - seventh;
        int e1 = e2 - seventh;
        int e4 = e3 + seventh;
        int e5 = e4 + seventh;
        if (a[e2] < a[e1]) { double t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
        if (a[e3] < a[e2]) { double t = a[e3]; a[e3] = a[e2]; a[e2] = t;
            if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
        }
        if (a[e4] < a[e3]) { double t = a[e4]; a[e4] = a[e3]; a[e3] = t;
            if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
            }
        }
        if (a[e5] < a[e4]) { double t = a[e5]; a[e5] = a[e4]; a[e4] = t;
            if (t < a[e3]) { a[e4] = a[e3]; a[e3] = t;
                if (t < a[e2]) { a[e3] = a[e2]; a[e2] = t;
                    if (t < a[e1]) { a[e2] = a[e1]; a[e1] = t; }
                }
            }
        }
        int less  = left;  
        int great = right; 
        if (a[e1] != a[e2] && a[e2] != a[e3] && a[e3] != a[e4] && a[e4] != a[e5]) {
            double pivot1 = a[e2];
            double pivot2 = a[e4];
            a[e2] = a[left];
            a[e4] = a[right];
            while (a[++less] < pivot1);
            while (a[--great] > pivot2);
            outer:
            for (int k = less - 1; ++k <= great; ) {
                double ak = a[k];
                if (ak < pivot1) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
            a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
            a[right] = a[great + 1]; a[great + 1] = pivot2;
            sort(a, left, less - 2, leftmost);
            sort(a, great + 2, right, false);
            if (less < e1 && e5 < great) {
                while (a[less] == pivot1) {
                    ++less;
                }
                while (a[great] == pivot2) {
                    --great;
                }
                outer:
                for (int k = less - 1; ++k <= great; ) {
                    double ak = a[k];
                    if (ak == pivot1) { 
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else if (ak == pivot2) { 
                        while (a[great] == pivot2) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (a[great] == pivot1) { 
                            a[k] = a[less];
                            a[less] = a[great];
                            ++less;
                        } else { 
                            a[k] = a[great];
                        }
                        a[great] = ak;
                        --great;
                    }
                }
            }
            sort(a, less, great, false);
        } else { 
            double pivot = a[e3];
            for (int k = less; k <= great; ++k) {
                if (a[k] == pivot) {
                    continue;
                }
                double ak = a[k];
                if (ak < pivot) { 
                    a[k] = a[less];
                    a[less] = ak;
                    ++less;
                } else { 
                    while (a[great] > pivot) {
                        --great;
                    }
                    if (a[great] < pivot) { 
                        a[k] = a[less];
                        a[less] = a[great];
                        ++less;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great] = ak;
                    --great;
                }
            }
            sort(a, left, less - 1, leftmost);
            sort(a, great + 1, right, false);
        }
    }
}

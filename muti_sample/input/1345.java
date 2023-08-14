class BidirBubbleSortAlgorithm extends SortAlgorithm {
    @Override
    void sort(int a[]) throws Exception {
        int j;
        int limit = a.length;
        int st = -1;
        while (st < limit) {
            st++;
            limit--;
            boolean swapped = false;
            for (j = st; j < limit; j++) {
                if (stopRequested) {
                    return;
                }
                if (a[j] > a[j + 1]) {
                    int T = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = T;
                    swapped = true;
                }
                pause(st, limit);
            }
            if (!swapped) {
                return;
            } else {
                swapped = false;
            }
            for (j = limit; --j >= st;) {
                if (stopRequested) {
                    return;
                }
                if (a[j] > a[j + 1]) {
                    int T = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = T;
                    swapped = true;
                }
                pause(st, limit);
            }
            if (!swapped) {
                return;
            }
        }
    }
}

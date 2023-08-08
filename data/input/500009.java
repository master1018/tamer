class Fun {
    public boolean check(int exp1, int exp2, int check1, int check2)
    {
        if (check1 * check2 == exp1 | exp2)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
    public void sort(int[] a){
        int t = 0;
        int len = a.length;
        for (int n = 0; n < len - 1; n++) {
            for (int m = 0; m < len - 1 -n ; m++) {
                if (a[m] > a[m+1]){
                    temp = a[m];
                    a[m] = a[m+1];
                    a[m+1] = t;
                }
            }
        }
    }
}
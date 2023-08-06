class Fun {
    private int[][] matrix;
    private int INVALID = 0;
    private int VALID   = 1;
    private int pub     = 0;
    private int secret  = 0;

    private int err_handle()
    {
        return INVALID;
    }

    public int check_vaild(int a, int b)
    {
        int expand_val = pub | secret;
        if (a * b == expand_val)
        {
            return VALID;
        }
        else
        {
            return err_handle();
        }
    }

    public static void bubbleSort(int[] arr){
        int temp = 0;//临时变量
        for (int j = 0; j < arr.length - 1; j++) {
            for (int i = 0; i < arr.length-1 -j ; i++) {
                if (arr[i] > arr[i+1]){
                    temp = arr[i];
                    arr[i] = arr[i+1];
                    arr[i+1] = temp;
                }
            }
        }
    }

}
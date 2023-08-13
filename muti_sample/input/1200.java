public class Test1 {
    public static void init(int src[], int [] dst, int[] ref) {
        for (int i =0; i<src.length; i++) {
            src[i] =  i;
            dst[i] = 2;      
            ref[i] = src[i]; 
        }
    }
    public static void verify(int src[], int[] ref) {
        for (int i = 0; i < src.length; i++) {
            if (src[i] != ref[i]) {
                System.out.println("Error: src and ref don't match at " + i);
                System.exit(97);
            }
        }
    }
    public static void test() {
        int[] src = new int[34];
        int[] dst = new int[34];
        int[] ref = new int[34];
        init(src, dst, ref);
        verify(src, ref);
    }
    public static void main(String[] args) {
        for (int i=0; i< 2000; i++) {
            test();
        }
    }
}

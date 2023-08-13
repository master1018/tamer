public class Test2 {
    public static void init(int src[]) {
        for (int i = 0; i < src.length; i++)
            src[i] = i;
    }
   public static void shift(int src[]) {
       for (int i = src.length-1; i > 0; i--){
           int tmp  = src[i];
           src[i]   = src[i-1];
           src[i-1] = tmp;
       }
    }
    public static void verify(int src[]) {
        for (int i = 0; i < src.length; i++){
            int value = (i-1 + src.length)%src.length; 
                if (src[i] != value) {
                    System.out.println("Error: src["+i+"] should be "+ value + " instead of " + src[i]);
                    System.exit(97);
                }
        }
    }
    public static void test() {
        int[] src = new int[10];
        init(src);
        shift(src);
        verify(src);
    }
    public static void main(String[] args) {
        for (int i=0; i< 2000; i++)
            test();
    }
}

public class Test6758234 {
    static int x = 0;
    static int y = 1;
    public static void main(String[] args) {
        if (1 != ((x < y) ? 1L : 0)) {
            throw new InternalError();
        }
   }
}

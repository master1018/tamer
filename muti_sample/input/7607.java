public class Test6579789 {
    public static void main(String[] args) {
        bug(4);
    }
    public static void bug(int n) {
        float f = 1;
        int i = 1;
        try {
            int x = 1 / n; 
            f = 2;
            i = 2;
            int y = 2 / n; 
        } catch (Exception ex) {
            f++;
            i++;
        }
    }
}

public class Test6196102 {
    static public void main(String[] args) {
        int i1 = 0;
        int i2 = Integer.MAX_VALUE;
        while (i1 >= 0) {
            i1++;
            if (i1 > i2) {
                System.out.println("E R R O R: " + i1);
                System.exit(97);
            }
        }
    }
}

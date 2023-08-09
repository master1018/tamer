public class JFrameCreateTime {
    public static void main(String[] args) {
        JFrame f;
        long start, end;
        start = System.currentTimeMillis();
        f = new JFrame("JFrame");
        end = System.currentTimeMillis();
        System.out.println("JFrame first creation took " + (end - start) + " ms");
        start = System.currentTimeMillis();
        f = new JFrame("JFrame");
        end = System.currentTimeMillis();
        System.out.println("JFrame second creation took " + (end - start) + " ms");
        System.exit(0);
    }
}

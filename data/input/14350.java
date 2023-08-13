public class IntGenerator {
    Random r;
    public IntGenerator() {
        r = new Random(0);
    }
    public int next(int max) {
        if (max <= 0)
            return 0;
        int x = r.nextInt();
        if (x < 0)
            x = 0 - x;
        return x % (max + 1);
    }
}

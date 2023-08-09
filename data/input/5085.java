public class Swap {
    static final int SIZE = 100;
    public static void main(String[] args) throws Exception {
        List l = new ArrayList(Collections.nCopies(100, Boolean.FALSE));
        l.set(0, Boolean.TRUE);
        for (int i=0; i < SIZE-1; i++)
            Collections.swap(l, i, i+1);
        List l2 = new ArrayList(Collections.nCopies(100, Boolean.FALSE));
        l2.set(SIZE-1, Boolean.TRUE);
        if (!l.equals(l2))
            throw new RuntimeException("Wrong result");
    }
}

public class CheckForComodification {
    private static final int LENGTH = 10;
    public static void main(String[] args) throws Exception {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < LENGTH; i++)
            list.add(i);
        try {
            for (int i : list)
                if (i == LENGTH - 2)
                    list.remove(i);
        } catch(ConcurrentModificationException e) {
            return;
        }
        throw new RuntimeException("No ConcurrentModificationException");
    }
}

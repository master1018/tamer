public class Disjoint {
    final static int N = 20;
    public static void main(String args[]) {
        Random rnd = new Random();
        List[] lists = new List[N];
        int x = 0;
        for (int i = 0; i < N; i++) {
            int size = rnd.nextInt(10) + 2;
            List<Integer> list = new ArrayList<Integer>(size);
            for (int j = 1; j < size; j++)
                list.add(x++);
            list.add(x);
            Collections.shuffle(list);
            lists[i] = list;
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                boolean disjoint = (Math.abs(i - j) > 1);
                List<Integer> a = (List<Integer>) lists[i];
                List<Integer> b = (List<Integer>) lists[j];
                if (Collections.disjoint(a, b) != disjoint)
                    throw new RuntimeException("A: " + i + ", " + j);
                if (Collections.disjoint(new HashSet<Integer>(a), b)
                    != disjoint)
                    throw new RuntimeException("B: " + i + ", " + j);
                if (Collections.disjoint(new HashSet<Integer>(a),
                                         new HashSet<Integer>(b)) != disjoint)
                    throw new RuntimeException("C: " + i + ", " + j);
            }
        }
    }
}

public class NQueensCS extends RecursiveAction {
    static long lastStealCount;
    static int boardSize;
    static final int[] expectedSolutions = new int[] {
        0, 1, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200,
        73712, 365596, 2279184, 14772512, 95815104, 666090624
    }; 
    static String keywordValue(String[] args, String keyword) {
        for (String arg : args)
            if (arg.startsWith(keyword))
                return arg.substring(keyword.length() + 1);
        return null;
    }
    static int intArg(String[] args, String keyword, int defaultValue) {
        String val = keywordValue(args, keyword);
        return (val == null) ? defaultValue : Integer.parseInt(val);
    }
    static final long NPS = (1000L * 1000L * 1000L);
    public static void main(String[] args) throws Exception {
        final int minBoardSize = intArg(args, "minBoardSize",  8);
        final int maxBoardSize = intArg(args, "maxBoardSize", 15);
        final int procs = intArg(args, "procs", 0);
        for (int reps = intArg(args, "reps", 10); reps > 0; reps--) {
            ForkJoinPool g = (procs == 0) ?
                new ForkJoinPool() :
                new ForkJoinPool(procs);
            lastStealCount = g.getStealCount();
            for (int i = minBoardSize; i <= maxBoardSize; i++)
                test(g, i);
            System.out.println(g);
            g.shutdown();
        }
    }
    static void test(ForkJoinPool g, int i) throws Exception {
        boardSize = i;
        int ps = g.getParallelism();
        long start = System.nanoTime();
        NQueensCS task = new NQueensCS(new int[0]);
        g.invoke(task);
        int solutions = task.solutions;
        long time = System.nanoTime() - start;
        double secs = (double) time / NPS;
        if (solutions != expectedSolutions[i])
            throw new Error();
        System.out.printf("NQueensCS %3d", i);
        System.out.printf(" Time: %7.3f", secs);
        long sc = g.getStealCount();
        long ns = sc - lastStealCount;
        lastStealCount = sc;
        System.out.printf(" Steals/t: %5d", ns/ps);
        System.out.println();
    }
    final int[] sofar;
    NQueensCS nextSubtask; 
    int solutions;
    NQueensCS(int[] a) {
        this.sofar = a;
    }
    public final void compute() {
        NQueensCS subtasks;
        int bs = boardSize;
        if (sofar.length >= bs)
            solutions = 1;
        else if ((subtasks = explore(sofar, bs)) != null)
            solutions = processSubtasks(subtasks);
    }
    private static NQueensCS explore(int[] array, int bs) {
        int row = array.length;
        NQueensCS s = null; 
        outer:
        for (int q = 0; q < bs; ++q) {
            for (int i = 0; i < row; i++) {
                int p = array[i];
                if (q == p || q == p - (row - i) || q == p + (row - i))
                    continue outer; 
            }
            NQueensCS first = s; 
            if (first != null)
                first.fork();
            int[] next = Arrays.copyOf(array, row+1);
            next[row] = q;
            NQueensCS subtask = new NQueensCS(next);
            subtask.nextSubtask = first;
            s = subtask;
        }
        return s;
    }
    private static int processSubtasks(NQueensCS s) {
        s.compute();
        int ns = s.solutions;
        s = s.nextSubtask;
        while (s != null && s.tryUnfork()) {
            s.compute();
            ns += s.solutions;
            s = s.nextSubtask;
        }
        while (s != null) {
            s.join();
            ns += s.solutions;
            s = s.nextSubtask;
        }
        return ns;
    }
}

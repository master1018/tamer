public final class Integrate {
    static final double errorTolerance = 1.0e-11;
    static final long NPS = (1000L * 1000 * 1000);
    static final int SERIAL = -1;
    static final int DYNAMIC = 0;
    static final int FORK = 1;
    static double computeFunction(double x)  {
        return (x * x + 1.0) * x;
    }
    static final double start = 0.0;
    static final double end = 1536.0;
    static final int calls = 263479047;
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
    static int policyArg(String[] args, String keyword, int defaultPolicy) {
        String val = keywordValue(args, keyword);
        if (val == null) return defaultPolicy;
        if (val.equals("dynamic")) return DYNAMIC;
        if (val.equals("serial")) return SERIAL;
        if (val.equals("fork")) return FORK;
        throw new Error();
    }
    public static void main(String[] args) throws Exception {
        final int procs = intArg(args, "procs",
                                 Runtime.getRuntime().availableProcessors());
        final int forkPolicy = policyArg(args, "forkPolicy", DYNAMIC);
        ForkJoinPool g = new ForkJoinPool(procs);
        System.out.println("Integrating from " + start + " to " + end +
                           " forkPolicy = " + forkPolicy);
        long lastTime = System.nanoTime();
        for (int reps = intArg(args, "reps", 10); reps > 0; reps--) {
            double a;
            if (forkPolicy == SERIAL)
                a = SQuad.computeArea(g, start, end);
            else if (forkPolicy == FORK)
                a = FQuad.computeArea(g, start, end);
            else
                a = DQuad.computeArea(g, start, end);
            long now = System.nanoTime();
            double s = (double) (now - lastTime) / NPS;
            lastTime = now;
            System.out.printf("Calls/sec: %12d", (long) (calls / s));
            System.out.printf(" Time: %7.3f", s);
            System.out.printf(" Area: %12.1f", a);
            System.out.println();
        }
        System.out.println(g);
        g.shutdown();
    }
    static final class SQuad extends RecursiveAction {
        static double computeArea(ForkJoinPool pool, double l, double r) {
            SQuad q = new SQuad(l, r, 0);
            pool.invoke(q);
            return q.area;
        }
        final double left;       
        final double right;      
        double area;
        SQuad(double l, double r, double a) {
            this.left = l; this.right = r; this.area = a;
        }
        public final void compute() {
            double l = left;
            double r = right;
            area = recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r, area);
        }
        static final double recEval(double l, double r, double fl,
                                    double fr, double a) {
            double h = (r - l) * 0.5;
            double c = l + h;
            double fc = (c * c + 1.0) * c;
            double hh = h * 0.5;
            double al = (fl + fc) * hh;
            double ar = (fr + fc) * hh;
            double alr = al + ar;
            if (Math.abs(alr - a) <= errorTolerance)
                return alr;
            else
                return recEval(c, r, fc, fr, ar) + recEval(l, c, fl, fc, al);
        }
    }
    static final class FQuad extends RecursiveAction {
        static double computeArea(ForkJoinPool pool, double l, double r) {
            FQuad q = new FQuad(l, r, 0);
            pool.invoke(q);
            return q.area;
        }
        final double left;       
        final double right;      
        double area;
        FQuad(double l, double r, double a) {
            this.left = l; this.right = r; this.area = a;
        }
        public final void compute() {
            double l = left;
            double r = right;
            area = recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r, area);
        }
        static final double recEval(double l, double r, double fl,
                                    double fr, double a) {
            double h = (r - l) * 0.5;
            double c = l + h;
            double fc = (c * c + 1.0) * c;
            double hh = h * 0.5;
            double al = (fl + fc) * hh;
            double ar = (fr + fc) * hh;
            double alr = al + ar;
            if (Math.abs(alr - a) <= errorTolerance)
                return alr;
            FQuad q = new FQuad(l, c, al);
            q.fork();
            ar = recEval(c, r, fc, fr, ar);
            if (!q.tryUnfork()) {
                q.quietlyJoin();
                return ar + q.area;
            }
            return ar + recEval(l, c, fl, fc, al);
        }
    }
    static final class DQuad extends RecursiveAction {
        static double computeArea(ForkJoinPool pool, double l, double r) {
            DQuad q = new DQuad(l, r, 0);
            pool.invoke(q);
            return q.area;
        }
        final double left;       
        final double right;      
        double area;
        DQuad(double l, double r, double a) {
            this.left = l; this.right = r; this.area = a;
        }
        public final void compute() {
            double l = left;
            double r = right;
            area = recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r, area);
        }
        static final double recEval(double l, double r, double fl,
                                    double fr, double a) {
            double h = (r - l) * 0.5;
            double c = l + h;
            double fc = (c * c + 1.0) * c;
            double hh = h * 0.5;
            double al = (fl + fc) * hh;
            double ar = (fr + fc) * hh;
            double alr = al + ar;
            if (Math.abs(alr - a) <= errorTolerance)
                return alr;
            DQuad q = null;
            if (getSurplusQueuedTaskCount() <= 3)
                (q = new DQuad(l, c, al)).fork();
            ar = recEval(c, r, fc, fr, ar);
            if (q != null && !q.tryUnfork()) {
                q.quietlyJoin();
                return ar + q.area;
            }
            return ar + recEval(l, c, fl, fc, al);
        }
    }
}

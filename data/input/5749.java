public class NameConstructors {
    private static final String NAME1 = "Norm D. Plume";
    private static final String NAME2 = "Ann Onymous";
    public static void main (String[] args) throws Exception  {
        Random rnd = new Random();
        test(new Timer(NAME1), NAME1);
        test(new Timer(NAME2, true), NAME2);
    }
    private static boolean done, passed;
    public static void test(Timer timer, final String name) throws Exception {
        done = passed = false;
        TimerTask task = new TimerTask() {
            public void run() {
                passed = Thread.currentThread().getName().equals(name);
                done = true;
            }
        };
        timer.schedule(task, 0); 
        Thread.sleep(500);
        if (!(done && passed))
            throw new RuntimeException(done + " : " + passed);
        timer.cancel();
    }
}

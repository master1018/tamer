public class Purge {
    public static void main (String[] args) {
        Random rnd = new Random();
        Timer timer = new Timer();
        int cancelled = 0;
        for (long i = 1; i <= 1000; i++) {
            TimerTask task = new TimerTask() {
                public void run() {
                    System.out.println("How come no one ever calls me?");
                }
            };
            timer.schedule(task, i * 60*60*1000); 
            if (i != 1 && rnd.nextBoolean()) {
                task.cancel();
                cancelled++;
            }
        }
        int purged = timer.purge();
        if (purged != cancelled)
            throw new RuntimeException(purged + " != " + cancelled);
        purged = timer.purge();
        if (purged != 0)
            throw new RuntimeException(purged + " nonzero");
        timer.cancel();
    }
}

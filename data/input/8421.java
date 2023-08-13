public class KillThread {
    public static void main (String[] args) throws Exception  {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                throw new ThreadDeath();
            }
        }, 0);
        try {
            Thread.sleep(100);
        } catch(InterruptedException e) {
        }
        try {
            t.schedule(new TimerTask() {
                public void run() {
                }
            }, 0);
            throw new Exception("We failed silently");
        } catch(IllegalStateException e) {
        }
    }
}

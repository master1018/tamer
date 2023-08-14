public class Daemon {
    public static void main(String args[]) throws Exception {
        ThreadGroup tg = new ThreadGroup("madbot-threads");
        Thread myThread = new MadThread(tg,"mad");
        ThreadGroup aGroup = new ThreadGroup(tg, "ness");
        tg.setDaemon(true);
        if (tg.activeCount() != 0)
            throw new RuntimeException("activeCount");
        aGroup.destroy();
        if (tg.isDestroyed())
            throw new RuntimeException("destroy");
        try {
            Thread anotherThread = new MadThread(aGroup, "bot");
            throw new RuntimeException("illegal");
        } catch (IllegalThreadStateException itse) {
        }
    }
}
class MadThread extends Thread {
    String name;
    MadThread(ThreadGroup tg, String name) {
        super(tg, name);
        this.name = name;
    }
    public void run() {
        System.out.println("me run "+name);
    }
}

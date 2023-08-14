public class Priority_Inversion {
    public static void main(String args[]) {
        int deltaPriority = 1;
        if (args.length == 1) {
            try {
                deltaPriority = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException nfe) {
                System.err.println
                        ("Sorry, \"" + args[0] + "\" is not a number");
                System.exit(1);
            }
        }
        RandomTest rand = new RandomTest();
        InvertTest invert = new InvertTest(deltaPriority, rand);
        rand.start();
        invert.start();
    }
}
class RandomTest extends Thread {
    public synchronized void run() {
        System.out.println("Start priority " + getPriority());
        SecureRandom rand = new SecureRandom();
        rand.nextBytes(new byte[5]);
    }
    void invertPriority() {
        System.out.println("Waiting ..., priority " +
                Thread.currentThread().getPriority());
        synchronized(this) {
        }
        System.out.println("Released Lock");
    }
}
class InvertTest extends Thread {
    private int delta;
    private RandomTest rand;
    InvertTest(int delta, RandomTest rand) {
        this.delta = delta;
        this.rand = rand;
    }
    public void run() {
        setPriority(getPriority() + delta);
        try {
            sleep(500);
        }
        catch (InterruptedException ie) { }
        rand.invertPriority();
    }
}

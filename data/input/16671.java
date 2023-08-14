public class SyncLastIndexOf {
    static Vector v = new Vector();
    static class RemovingThread extends Thread {
        public void run() {
           synchronized(v) {
                try {
                sleep(200);
                } catch (InterruptedException e) {
                }
                v.removeElementAt(0);
           }
        }
    }
    public static void main(String args[]) {
        Integer x = new Integer(1);
        v.addElement(x);
        new RemovingThread().start();
        try {
            v.lastIndexOf(x);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException(
                  "Vector.lastIndexOf() synchronization failed.");
        }
    }
}

public class Bug4518797 {
    static volatile boolean runrun = true;
    static volatile String message = null;
    public static void main(String[] args) {
        int duration = 180;
        if (args.length == 1) {
            duration = Math.max(5, Integer.parseInt(args[0]));
        }
        final Locale loc = new Locale("ja", "US");
        final int hashcode = loc.hashCode();
        System.out.println("correct hash code: " + hashcode);
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                while (runrun) {
                    int hc = loc.hashCode();
                    if (hc != hashcode) {
                        runrun = false;
                        message = "t1: wrong hashcode: " + hc;
                    }
                }
            }
          });
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                while (runrun) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        oos.writeObject(loc);
                        byte[] b = baos.toByteArray();
                        oos.close();
                        ByteArrayInputStream bais = new ByteArrayInputStream(b);
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        Locale loc2 = (Locale) ois.readObject();
                        int hc = loc2.hashCode();
                        if (hc != hashcode) {
                            runrun = false;
                            message = "t2: wrong hashcode: " + hc;
                        }
                    } catch (IOException ioe) {
                        runrun = false;
                        throw new RuntimeException("t2: can't perform test", ioe);
                    } catch (ClassNotFoundException cnfe) {
                        runrun = false;
                        throw new RuntimeException("t2: can't perform test", cnfe);
                    }
                }
            }
          });
        t1.start();
        t2.start();
        try {
            for (int i = 0; runrun && i < duration; i++) {
                Thread.sleep(1000);
            }
            runrun = false;
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
        }
        if (message != null) {
            throw new RuntimeException(message);
        }
    }
}

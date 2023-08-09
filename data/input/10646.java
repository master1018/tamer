public class SynchronizedAccess {
    public static void main(String[] args) {
        AccessorThread[] acc = new AccessorThread[200];
        for (int i=0; i < acc.length; i++)
            acc[i] = new AccessorThread("thread"+i);
        for (int i=0; i < acc.length; i++)
            acc[i].start();
    }
}
class AccessorThread extends Thread {
    public AccessorThread(String str) {
        super(str);
    }
    public void run() {
        Provider[] provs = new Provider[10];
        for (int i=0; i < provs.length; i++)
            provs[i] = new MyProvider("name"+i, 1, "test");
        int rounds = 20;
        while (rounds-- > 0) {
            try {
                for (int i=0; i<provs.length; i++) {
                    Security.addProvider(provs[i]);
                }
                Signature sig = Signature.getInstance("sigalg");
                for (int i=0; i<provs.length; i++) {
                    Security.removeProvider("name"+i);
                }
                provs = Security.getProviders();
            } catch (NoSuchAlgorithmException nsae) {
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
            }
        } 
    }
}
class MyProvider extends Provider {
    public MyProvider(String name, double version, String info) {
        super(name, version, info);
        put("Signature.sigalg", "sigimpl");
    }
}

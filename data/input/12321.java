public class Test_ia32 {
    public static int NUM_THREADS = 100;
    public static int CLONE_LENGTH = 1000;
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
        Reflector[] threads = new Reflector[NUM_THREADS];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Reflector();
            threads[i].start();
        }
        System.out.println("Give Reflector.run() some time to compile...");
        Thread.sleep(5000);
        System.out.println("Load RMISecurityException causing run() deoptimization");
        ClassLoader.getSystemClassLoader().loadClass("java.rmi.RMISecurityException");
        for (Reflector thread : threads)
            thread.requestStop();
        for (Reflector thread : threads)
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
    }
}
class Reflector extends Thread {
    volatile boolean _doSpin = true;
    Test_ia32[] _tests;
    Reflector() {
        _tests = new Test_ia32[Test_ia32.CLONE_LENGTH];
        for (int i = 0; i < _tests.length; i++) {
            _tests[i] = new Test_ia32();
        }
    }
    static int g(int i1, int i2, Test_ia32[] arr, int i3, int i4) {
        if (!(i1==1 && i2==2 && i3==3 && i4==4)) {
            System.out.println("Bug!");
        }
        return arr.length;
    }
    static int f(Test_ia32[] arr) {
        return g(1, 2, arr.clone(), 3, 4);
    }
    @Override
    public void run() {
        Constructor[] ctrs = null;
        Class<Test_ia32> klass = Test_ia32.class;
        try {
            ctrs = klass.getConstructors();
        } catch (SecurityException e) {
            System.out.println(e);
        }
        try {
            while (_doSpin) {
                if (f(_tests) < 0)
                    System.out.println("return value usage");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        System.out.println(this + " - stopped.");
    }
    public void requestStop() {
        System.out.println(this + " - stop requested.");
        _doSpin = false;
    }
}

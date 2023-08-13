public class TestDesignTime implements Runnable {
    public static void main(String[] args) throws InterruptedException {
        if (Beans.isDesignTime()) {
            throw new Error("unexpected DesignTime property");
        }
        Beans.setDesignTime(!Beans.isDesignTime());
        ThreadGroup group = new ThreadGroup("$$$");
        Thread thread = new Thread(group, new TestDesignTime());
        thread.start();
        thread.join();
    }
    public void run() {
        SunToolkit.createNewAppContext();
        if (Beans.isDesignTime()) {
            throw new Error("shared DesignTime property");
        }
    }
}

public class FontThread extends Thread {
    String fontName = "Dialog";
    static FontThread thread1;
    static FontThread thread2;
    static FontThread thread3;
    public static void main(String args[]) throws Exception {
        thread1 = new FontThread("SansSerif");
        thread2 = new FontThread("Serif");
        thread3 = new FontThread("Monospaced");
        thread1.dometrics(60); 
        thread1.start();
        thread2.start();
        thread3.start();
        InterruptThread ithread = new InterruptThread();
        ithread.setDaemon(true);
        ithread.start();
        thread1.join();
        thread2.join();
        thread3.join();
    }
    FontThread(String font) {
        super();
        this.fontName = font;
    }
    public void run() {
        System.out.println("started "+fontName); System.out.flush();
        dometrics(4000);
        System.out.println("done "+fontName); System.out.flush();
    }
    private void dometrics(int max) {
        Font f = new Font(fontName, Font.PLAIN, 12);
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(f);
        for (char i=0;i<max;i++) {
            if (f.canDisplay(i)) fm.charWidth(i);
        }
    }
    static class InterruptThread extends Thread {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
                thread1.interrupt();
                thread2.interrupt();
                thread3.interrupt();
            }
        }
    }
}

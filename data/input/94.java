public class CrashXCheckJni {
    public static void main(String []s)
    {
        final Dialog fd = new Dialog(new Frame(), true);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                System.out.println("RUNNING TASK");
                fd.setVisible(false);
                fd.dispose();
                System.out.println("FINISHING TASK");
            }
        }, 3000L);
        fd.setVisible(true);
        t.cancel();
        Util.waitForIdle(null);
        AbstractTest.pass();
    }
}

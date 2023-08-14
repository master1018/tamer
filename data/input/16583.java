public class NpeOnCloseTest {
    public static void main(String[] args)
    {
        Frame frame1 = new Frame("frame 1");
        frame1.setBounds(0, 0, 100, 100);
        frame1.setVisible(true);
        Util.waitForIdle(null);
        Frame frame2 = new Frame("frame 2");
        frame2.setBounds(150, 0, 100, 100);
        frame2.setVisible(true);
        Util.waitForIdle(null);
        Frame frame3 = new Frame("frame 3");
        final Dialog dialog = new Dialog(frame3, "dialog", true);
        dialog.setBounds(300, 0, 100, 100);
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            });
        try {
            EventQueue.invokeAndWait(new Runnable() { public void run() {} });
            Util.waitForIdle(null);
            EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        dialog.dispose();
                    }
                });
        }
        catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
        catch (InvocationTargetException ite) {
            throw new RuntimeException(ite);
        }
        frame1.dispose();
        frame2.dispose();
        frame3.dispose();
    }
}

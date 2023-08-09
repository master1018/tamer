public class RequestFocusAndHideTest {
    public static void main(String[] args) throws InterruptedException, java.lang.reflect.InvocationTargetException
    {
        final Frame frame = new Frame("the test");
        frame.setLayout(new FlowLayout());
        final Button btn1 = new Button("button 1");
        frame.add(btn1);
        frame.add(new Button("button 2"));
        frame.add(new Button("button 3"));
        frame.pack();
        frame.setVisible(true);
        Robot r = Util.createRobot();
        Util.waitForIdle(r);
        Util.clickOnComp(btn1, r);
        Util.waitForIdle(r);
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (kfm.getFocusOwner() != btn1) {
            throw new RuntimeException("test error: can not set focus on " + btn1 + ".");
        }
        EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    final int n_comps = frame.getComponentCount();
                    for (int i = 0; i < n_comps; ++i) {
                        frame.getComponent(i).setVisible(false);
                    }
                }
            });
        Util.waitForIdle(r);
        final Component focus_owner = kfm.getFocusOwner();
        if (focus_owner != null && !focus_owner.isVisible()) {
            throw new RuntimeException("we have invisible focus owner");
        }
        System.out.println("test passed");
        frame.dispose();
    }
}

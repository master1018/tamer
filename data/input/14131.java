public class ActionAfterRemove
{
    private static volatile boolean passed = true;
    public void handle(Throwable e) {
        e.printStackTrace();
        passed = false;
    }
    public static final void main(String args[])
    {
        final Frame frame = new Frame();
        final List list = new List();
        Robot robot = null;
        System.setProperty("sun.awt.exception.handler", "ActionAfterRemove");
        list.add("will be removed");
        frame.add(list);
        frame.setLayout(new FlowLayout());
        frame.setBounds(100,100,300,300);
        frame.setVisible(true);
        list.select(0);
        list.remove(0);
        try{
            robot = new Robot();
        }catch(AWTException e){
            throw new RuntimeException(e);
        }
        Util.clickOnComp(list, robot);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        Util.clickOnComp(list, robot);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        if (!passed){
            throw new RuntimeException("Test failed: exception was thrown on EDT.");
        }
    }
}

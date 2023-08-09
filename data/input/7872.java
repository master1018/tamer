public class UnfocusableToplevel {
    final static Robot robot = Util.createRobot();
    final static int REASONABLE_PATH_TIME = 5000;
    public static void main(String []s)
    {
        Frame f = new Frame();
        Window w = new Window(f);
        final Choice ch = new Choice();
        ch.add("item 1");
        ch.add("item 2");
        ch.add("item 3");
        ch.add("item 4");
        ch.add("item 5");
        w.add(ch);
        w.setLayout(new FlowLayout());
        w.setSize(200, 200);
        ch.addKeyListener(new KeyAdapter(){
                public void keyTyped(KeyEvent e){
                    traceEvent("keytyped", e);
                }
                public void keyPressed(KeyEvent e){
                    traceEvent("keypress", e);
                }
                public void keyReleased(KeyEvent e){
                    traceEvent("keyrelease", e);
                }
        });
        ch.addItemListener(new ItemListener(){
                public void itemStateChanged(ItemEvent ie){
                    traceEvent("stateChanged", ie);
                }
            });
        w.setVisible(true);
        Util.waitForIdle(robot);
        Util.clickOnComp(ch, robot);
        Util.waitForIdle(robot);
        testKeys();
        Util.waitForIdle(robot);
    }
    private static void testKeys(){
        typeKey(KeyEvent.VK_UP);
        typeKey(KeyEvent.VK_DOWN);
        typeKey(KeyEvent.VK_K);
        typeKey(KeyEvent.VK_PAGE_UP);
        typeKey(KeyEvent.VK_PAGE_DOWN);
    }
    private static void typeKey(int keyChar){
        try {
            robot.keyPress(keyChar);
            robot.delay(5);
        } finally {
            robot.keyRelease(keyChar);
        }
        robot.delay(100);
    }
    private static void traceEvent(String message, AWTEvent e){
        AbstractTest.fail(message + " " + e.toString());
    }
}

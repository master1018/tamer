public class CorrectTime extends Frame implements KeyListener {
    final static int REASONABLE_PATH_TIME = 5000;
    static TextField tf = new TextField("press keys", 10);
    static TextArea ta = new TextArea("press keys", 10, 10);
    static Button bt = new Button("press button");
    static List list = new List();
    final static Robot robot = Util.createRobot();
    public CorrectTime(){
        super("Check time of KeyEvents");
        setPreferredSize(new Dimension(400,400));
        setLayout(new FlowLayout());
        list.add("item1");
        list.add("item2");
        list.add("item3");
        add(bt);
        add(tf);
        add(ta);
        add(list);
        bt.addKeyListener(this);
        tf.addKeyListener(this);
        ta.addKeyListener(this);
        list.addKeyListener(this);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    public static void main(String []s) {
        Frame frame = new CorrectTime();
        Robot robot = Util.createRobot();
        Util.waitForIdle(robot);
        testComponent(tf);
        testComponent(ta);
        testComponent(bt);
        testComponent(list);
    }
    private static void testComponent(final Component comp){
        Runnable action = new Runnable(){
                public void run(){
                    Util.clickOnComp(comp, robot);
                    Util.waitForIdle(robot);
                }
            };
        if (! Util.trackFocusGained(comp, action, REASONABLE_PATH_TIME, true)){
            AbstractTest.fail("Focus didn't come to " + comp);
        }
        testKeys();
        Util.waitForIdle(robot);
    }
    private static void testKeys(){
        typeKey(KeyEvent.VK_A);
        typeKey(KeyEvent.VK_B);
        typeKey(KeyEvent.VK_SPACE);
        typeKey(KeyEvent.VK_Z);
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
    private void traceKey(String k, KeyEvent e){
        long eventTime = e.getWhen();
        long currTime = System.currentTimeMillis();
        long diff = currTime - eventTime;
        Sysout.println(k + " diff is " + diff + ", event is "+ e);
        if (diff < 0 ||
            diff > REASONABLE_PATH_TIME)
        {
            AbstractTest.fail(k + " diff is " + diff + ", event = "+e);
        }
    }
    public void keyTyped(KeyEvent e){
        traceKey("keytyped",e);
    }
    public void keyPressed(KeyEvent e){
        traceKey("keypress",e);
    }
    public void keyReleased(KeyEvent e){
        traceKey("keyrelease",e);
    }
}

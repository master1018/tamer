public class ChoiceKeyEventReaction extends Applet
{
    Robot robot;
    Choice choice1 = new Choice();
    Point pt;
    TextField tf = new TextField("Hi");
    boolean keyTypedOnTextField = false;
    boolean itemChanged = false;
    String toolkit;
    public void init()
    {
        toolkit = Toolkit.getDefaultToolkit().getClass().getName();
        System.out.println("Current toolkit is :" +toolkit);
        for (int i = 1; i<20; i++){
            choice1.add("item-0"+i);
        }
        tf.addKeyListener(new KeyAdapter(){
                public void keyPressed(KeyEvent ke) {
                    keyTypedOnTextField = true;
                    System.out.println(ke);
                }
            });
        choice1.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    itemChanged = true;
                    System.out.println(e);
                }
            });
        choice1.setFocusable(false);
        add(tf);
        add(choice1);
        setLayout (new FlowLayout());
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
        try{
            robot = new Robot();
            Util.waitForIdle(robot);
            moveFocusToTextField();
            testKeyOnChoice(InputEvent.BUTTON1_MASK, KeyEvent.VK_UP);
        } catch (Throwable e) {
            throw new RuntimeException("Test failed. Exception thrown: "+e);
        }
    }
    public void testKeyOnChoice(int button, int key){
        pt = choice1.getLocationOnScreen();
        robot.mouseMove(pt.x + choice1.getWidth()/2, pt.y + choice1.getHeight()/2);
        Util.waitForIdle(robot);
        robot.mousePress(button);
        robot.delay(10);
        robot.mouseRelease(button);
        Util.waitForIdle(robot);
        robot.keyPress(key);
        robot.keyRelease(key);
        Util.waitForIdle(robot);
        System.out.println("keyTypedOnTextField = "+keyTypedOnTextField +": itemChanged = " + itemChanged);
        if (itemChanged){
                throw new RuntimeException("Test failed. ItemChanged event occur on Choice.");
        }
        if (toolkit.equals("sun.awt.windows.WToolkit") &&
            !keyTypedOnTextField)
        {
            throw new RuntimeException("Test failed. (Win32) KeyEvent wasn't addressed to TextField. ");
        }
        if (!toolkit.equals("sun.awt.windows.WToolkit") &&
            keyTypedOnTextField)
        {
            throw new RuntimeException("Test failed. (XToolkit/MToolkit). KeyEvent was addressed to TextField.");
        }
        System.out.println("Test passed. Unfocusable Choice doesn't react on keys.");
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.keyRelease(KeyEvent.VK_ESCAPE);
        Util.waitForIdle(robot);
    }
    public void moveFocusToTextField(){
        pt = tf.getLocationOnScreen();
        robot.mouseMove(pt.x + tf.getWidth()/2, pt.y + tf.getHeight()/2);
        Util.waitForIdle(robot);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(10);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        Util.waitForIdle(robot);
    }
}
